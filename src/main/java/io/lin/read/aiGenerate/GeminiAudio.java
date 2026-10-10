package io.lin.read.aiGenerate;

import com.google.genai.Client;
import com.google.genai.types.*;
import io.lin.read.config.gemini.GeminiProperties;
import io.lin.read.aiGenerate.dto.AudioResult;
import io.lin.read.exception.CustomException;
import io.lin.read.utils.file.AudioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class GeminiAudio {

    /** Male dialogue voice (Gemini prebuilt; used in Google multi-speaker examples for male roles). */
    public static final String VOICE_DEFAULT = "Kore";
    public static final String VOICE_FEMALE = "Kore";
    public static final String VOICE_MALE = "Puck";

    private final Client client;
    private final String model;

    public GeminiAudio(Client client, GeminiProperties props) {
        this.client = client;
        this.model = props.models().audio();
    }

    /**
     * Generates exam listening audio. Two-speaker labelled scripts use one TTS call per line with
     * distinct male/female voices, then concatenates the audio.
     */
    public AudioResult generateExamAudio(String audioScript, String instructionPrompt) {
        if (audioScript == null || audioScript.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.content.no_audio_script");
        }
        String script = audioScript.trim();

        Optional<ListeningDialogueParser.TwoSpeakerDialogue> dialogue =
                ListeningDialogueParser.parseTwoSpeakerDialogue(script);

        if (dialogue.isPresent()) {
            return generateDialogueTurnByTurn(dialogue.get());
        }

        return generateSingleSpeaker(instructionPrompt, VOICE_DEFAULT);
    }

    /** @deprecated Prefer {@link #generateExamAudio(String, String)} for exam scripts. */
    @Deprecated
    public AudioResult basicAudio(String text) {
        return generateSingleSpeaker(text, VOICE_DEFAULT);
    }

    private AudioResult generateDialogueTurnByTurn(ListeningDialogueParser.TwoSpeakerDialogue dialogue) {
        List<byte[]> wavParts = new ArrayList<>();

        for (ListeningDialogueParser.DialogueTurn turn : dialogue.turns()) {
            String fallback = turn.speakerLabel().equalsIgnoreCase(dialogue.speakerOne())
                    ? VOICE_MALE
                    : VOICE_FEMALE;
            String voice = ListeningDialogueParser.voiceNameForSpeakerLabel(
                    turn.speakerLabel(),
                    fallback
            );

            String speakText = turn.lineText().trim();
            log.info(
                    "Dialogue TTS turn: speaker={} voice={} chars={}",
                    turn.speakerLabel(),
                    voice,
                    speakText.length()
            );

            AudioUtil.PlayableAudio wav = synthesizeToWav(speakText, voice);
            wavParts.add(wav.bytes());
        }

        byte[] mergedWav = AudioUtil.concatWavMono16(wavParts);
        AudioUtil.PlayableAudio mp3 = AudioUtil.toStorageMp3(mergedWav, "audio/wav");
        return new AudioResult(mp3.bytes(), mp3.mimeType());
    }

    private AudioResult generateSingleSpeaker(String text, String voiceName) {
        AudioUtil.PlayableAudio wav = synthesizeToWav(text, voiceName);
        AudioUtil.PlayableAudio mp3 = AudioUtil.toStorageMp3(wav.bytes(), wav.mimeType());
        return new AudioResult(mp3.bytes(), mp3.mimeType());
    }

    private AudioUtil.PlayableAudio synthesizeToWav(String text, String voiceName) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(List.of("AUDIO"))
                .speechConfig(
                        SpeechConfig.builder()
                                .voiceConfig(
                                        VoiceConfig.builder()
                                                .prebuiltVoiceConfig(
                                                        PrebuiltVoiceConfig.builder()
                                                                .voiceName(voiceName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        return invokeTtsRaw(text, config);
    }

    private AudioUtil.PlayableAudio invokeTtsRaw(String text, GenerateContentConfig config) {
        List<Content> contents = List.of(
                Content.builder()
                        .role("user")
                        .parts(List.of(Part.fromText(text)))
                        .build()
        );

        try {
            GenerateContentResponse response = client.models.generateContent(model, contents, config);

            if (response.candidates().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "error.ai.no_audio");
            }

            var candidate = response.candidates().get().get(0);
            if (candidate.content().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "error.ai.no_audio");
            }

            for (Part part : candidate.content().get().parts().orElse(List.of())) {
                if (part.inlineData().isEmpty()) {
                    continue;
                }

                Blob blob = part.inlineData().get();
                byte[] audioBytes = blob.data().orElseThrow(
                        () -> new CustomException(HttpStatus.BAD_REQUEST, "error.ai.no_audio")
                );

                String mimeType = blob.mimeType().orElse("audio/L16;codec=pcm;rate=24000");
                return AudioUtil.toPlayableAudio(audioBytes, mimeType);
            }

            throw new CustomException(HttpStatus.BAD_REQUEST, "error.ai.no_audio");
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Gemini TTS generation failed", e);
            throw new CustomException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "error.ai.audio_generation_failed"
            );
        }
    }
}
