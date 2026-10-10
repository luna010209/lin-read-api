package io.lin.read.aiGenerate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects labeled dialogue lines in listening audio scripts (e.g. Man:/Woman:, A:/B:).
 */
public final class ListeningDialogueParser {

    private static final Pattern SPEAKER_TURN = Pattern.compile(
            "(?:^|\\G|\\s)([A-Za-z][A-Za-z0-9 _-]{0,30}):\\s*(.*?)(?=(?:\\s[A-Za-z][A-Za-z0-9 _-]{0,30}:)|\\s*$)",
            Pattern.DOTALL
    );

    private ListeningDialogueParser() {}

    public record DialogueTurn(String speakerLabel, String lineText) {}

    public record TwoSpeakerDialogue(String speakerOne, String speakerTwo, List<DialogueTurn> turns) {
        public String scriptWithLabels() {
            StringBuilder sb = new StringBuilder();
            for (DialogueTurn turn : turns) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(turn.speakerLabel()).append(": ").append(turn.lineText().trim());
            }
            return sb.toString();
        }
    }

    public static Optional<TwoSpeakerDialogue> parseTwoSpeakerDialogue(String script) {
        if (script == null || script.isBlank()) {
            return Optional.empty();
        }

        List<DialogueTurn> turns = parseTurns(script);
        if (turns.size() < 2) {
            return Optional.empty();
        }

        Set<String> speakers = new LinkedHashSet<>();
        for (DialogueTurn turn : turns) {
            speakers.add(turn.speakerLabel());
        }

        if (speakers.size() != 2) {
            return Optional.empty();
        }

        var iterator = speakers.iterator();
        String first = iterator.next();
        String second = iterator.next();
        return Optional.of(new TwoSpeakerDialogue(first, second, List.copyOf(turns)));
    }

    static List<DialogueTurn> parseTurns(String script) {
        List<DialogueTurn> turns = new ArrayList<>();
        String flat = script.trim().replaceAll("\\s+", " ");

        Matcher matcher = SPEAKER_TURN.matcher(flat);
        while (matcher.find()) {
            String speaker = matcher.group(1).trim();
            String text = matcher.group(2).trim();
            if (speaker.isEmpty() || text.isEmpty()) {
                continue;
            }
            if (looksLikeOptionKey(speaker)) {
                continue;
            }
            turns.add(new DialogueTurn(speaker, text));
        }
        return turns;
    }

    /** Skip bare A/B/C/D option keys mistaken as speakers. */
    private static boolean looksLikeOptionKey(String speaker) {
        return speaker.length() == 1 && "ABCDTF".contains(speaker.toUpperCase(Locale.ROOT));
    }

    public static String voiceNameForSpeakerLabel(String label, String fallbackVoice) {
        if (label == null || label.isBlank()) {
            return fallbackVoice;
        }
        String n = label.trim().toLowerCase(Locale.ROOT);

        if (isLikelyMaleLabel(n)) {
            return GeminiAudio.VOICE_MALE;
        }
        if (isLikelyFemaleLabel(n)) {
            return GeminiAudio.VOICE_FEMALE;
        }
        if (n.equals("a") || n.equals("speaker 1") || n.equals("speaker1")) {
            return GeminiAudio.VOICE_MALE;
        }
        if (n.equals("b") || n.equals("speaker 2") || n.equals("speaker2")) {
            return GeminiAudio.VOICE_FEMALE;
        }
        return fallbackVoice;
    }

    private static boolean isLikelyMaleLabel(String n) {
        return n.equals("man")
                || n.equals("male")
                || n.equals("m")
                || n.contains(" boy")
                || n.startsWith("boy ")
                || n.equals("boy")
                || n.equals("mr")
                || n.equals("sir")
                || n.contains("husband")
                || n.contains("father");
    }

    private static boolean isLikelyFemaleLabel(String n) {
        return n.equals("woman")
                || n.equals("female")
                || n.equals("f")
                || n.equals("w")
                || n.contains(" girl")
                || n.startsWith("girl ")
                || n.equals("girl")
                || n.equals("ms")
                || n.equals("mrs")
                || n.equals("miss")
                || n.contains("sarah")
                || n.contains("wife")
                || n.contains("mother");
    }
}
