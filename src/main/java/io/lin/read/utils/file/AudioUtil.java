package io.lin.read.utils.file;

import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AudioUtil {

    private static final int MP3_BIT_RATE = 128_000;
    private static final int MP3_SAMPLE_RATE = 24_000;

    private AudioUtil() {}

    /**
     * Normalizes input audio and stores as MP3 for smaller file size.
     */
    public static PlayableAudio toStorageMp3(byte[] rawBytes, String mimeType) {
        PlayableAudio normalized = toPlayableAudio(rawBytes, mimeType);
        if ("audio/mpeg".equals(normalized.mimeType())) {
            return normalized;
        }
        byte[] mp3Bytes = transcodeToMp3(normalized.bytes(), normalized.mimeType());
        return new PlayableAudio(mp3Bytes, "audio/mpeg", "mp3");
    }

    public static PlayableAudio toPlayableAudio(byte[] rawBytes, String mimeType) {
        if (rawBytes == null || rawBytes.length == 0) {
            throw new IllegalArgumentException("Audio bytes are empty");
        }

        if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("audio/l16")) {
            int sampleRate = parseSampleRate(mimeType, MP3_SAMPLE_RATE);
            return new PlayableAudio(pcm16ToWav(rawBytes, sampleRate, 1), "audio/wav", "wav");
        }

        if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("audio/wav")) {
            return new PlayableAudio(rawBytes, "audio/wav", "wav");
        }

        if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("audio/mpeg")) {
            return new PlayableAudio(rawBytes, "audio/mpeg", "mp3");
        }

        return new PlayableAudio(
                rawBytes,
                mimeType != null ? mimeType : "application/octet-stream",
                extensionForMime(mimeType != null ? mimeType : "application/octet-stream")
        );
    }

    private static byte[] transcodeToMp3(byte[] inputBytes, String inputMimeType) {
        String inputExt = extensionForMime(inputMimeType);
        java.io.File source = null;
        java.io.File target = null;
        try {
            source = Files.createTempFile("audio-in-", "." + inputExt).toFile();
            target = Files.createTempFile("audio-out-", ".mp3").toFile();
            Files.write(source.toPath(), inputBytes);

            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(MP3_BIT_RATE);
            audio.setChannels(1);
            audio.setSamplingRate(MP3_SAMPLE_RATE);

            EncodingAttributes encoding = new EncodingAttributes();
            encoding.setOutputFormat("mp3");
            encoding.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, encoding);

            return Files.readAllBytes(target.toPath());
        } catch (EncoderException | IOException e) {
            throw new IllegalStateException("Failed to transcode audio to MP3", e);
        } finally {
            if (source != null) {
                source.delete();
            }
            if (target != null) {
                target.delete();
            }
        }
    }

    private static String extensionForMime(String mimeType) {
        if (mimeType == null) {
            return "wav";
        }
        String normalized = mimeType.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("audio/mpeg")) {
            return "mp3";
        }
        if (normalized.startsWith("audio/wav")) {
            return "wav";
        }
        if (normalized.startsWith("audio/ogg")) {
            return "ogg";
        }
        if (normalized.startsWith("audio/webm")) {
            return "webm";
        }
        if (normalized.startsWith("audio/l16")) {
            return "wav";
        }
        return "wav";
    }

    private static int parseSampleRate(String mimeType, int defaultRate) {
        for (String part : mimeType.split(";")) {
            String trimmed = part.trim().toLowerCase(Locale.ROOT);
            if (trimmed.startsWith("rate=")) {
                return Integer.parseInt(trimmed.substring("rate=".length()));
            }
        }
        return defaultRate;
    }

    private static byte[] pcm16ToWav(byte[] pcmData, int sampleRate, int channels) {
        int bitsPerSample = 16;
        int blockAlign = channels * bitsPerSample / 8;
        int byteRate = sampleRate * blockAlign;
        int dataSize = pcmData.length;
        int fileSize = 36 + dataSize;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataSize)) {
            out.write("RIFF".getBytes());
            out.write(intLE(fileSize));
            out.write("WAVE".getBytes());
            out.write("fmt ".getBytes());
            out.write(intLE(16));
            out.write(shortLE((short) 1));
            out.write(shortLE((short) channels));
            out.write(intLE(sampleRate));
            out.write(intLE(byteRate));
            out.write(shortLE((short) blockAlign));
            out.write(shortLE((short) bitsPerSample));
            out.write("data".getBytes());
            out.write(intLE(dataSize));
            out.write(pcmData);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build WAV audio", e);
        }
    }

    private static byte[] intLE(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    private static byte[] shortLE(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    public record PlayableAudio(byte[] bytes, String mimeType, String extension) {}

    private static final int WAV_HEADER_SIZE = 44;
    private static final int DEFAULT_PAUSE_MS = 180;

    /**
     * Concatenates mono 16-bit WAV payloads (same sample rate). Inserts a short pause between parts.
     */
    public static byte[] concatWavMono16(List<byte[]> wavFiles) {
        return concatWavMono16(wavFiles, DEFAULT_PAUSE_MS);
    }

    public static byte[] concatWavMono16(List<byte[]> wavFiles, int pauseMs) {
        if (wavFiles == null || wavFiles.isEmpty()) {
            throw new IllegalArgumentException("No WAV parts to concat");
        }

        int sampleRate = MP3_SAMPLE_RATE;
        int channels = 1;
        int bitsPerSample = 16;

        List<byte[]> pcmParts = new ArrayList<>();
        for (byte[] wav : wavFiles) {
            PlayableAudio playable = toPlayableAudio(wav, "audio/wav");
            byte[] data = playable.bytes();
            if (data.length <= WAV_HEADER_SIZE) {
                continue;
            }
            if (sampleRate == MP3_SAMPLE_RATE && data.length >= 28) {
                sampleRate = ByteBuffer.wrap(data, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            }
            pcmParts.add(java.util.Arrays.copyOfRange(data, WAV_HEADER_SIZE, data.length));
        }

        if (pcmParts.isEmpty()) {
            throw new IllegalArgumentException("WAV parts contained no PCM data");
        }

        int pauseBytes = (sampleRate * channels * (bitsPerSample / 8) * pauseMs) / 1000;
        byte[] silence = new byte[pauseBytes];

        int totalPcm = 0;
        for (int i = 0; i < pcmParts.size(); i++) {
            totalPcm += pcmParts.get(i).length;
            if (i < pcmParts.size() - 1) {
                totalPcm += silence.length;
            }
        }

        byte[] mergedPcm = new byte[totalPcm];
        int offset = 0;
        for (int i = 0; i < pcmParts.size(); i++) {
            byte[] part = pcmParts.get(i);
            System.arraycopy(part, 0, mergedPcm, offset, part.length);
            offset += part.length;
            if (i < pcmParts.size() - 1) {
                System.arraycopy(silence, 0, mergedPcm, offset, silence.length);
                offset += silence.length;
            }
        }

        return pcm16ToWav(mergedPcm, sampleRate, channels);
    }
}
