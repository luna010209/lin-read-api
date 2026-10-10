package io.lin.read.utils;

import io.lin.read.common.enums.Language;

public final class LanguageUtil {

    private LanguageUtil() {
    }

    /**
     * English display name for prompts and AI instructions (aligned with lin-langa-api).
     */
    public static String lang(Language language) {
        return switch (language) {
            case VI -> "Vietnamese";
            case TH -> "Thai";
            case ID -> "Indonesian";
            case MS -> "Malay";
            case TL -> "Tagalog";
            case KO -> "Korean";
            case JA -> "Japanese";
            case ZH -> "Chinese";
            case HI -> "Hindi";
            case EN -> "English";
            case FR -> "French";
            case DE -> "German";
            case ES -> "Spanish";
            case IT -> "Italian";
            case RU -> "Russian";
            case NL -> "Dutch";
            case PL -> "Polish";
            case PT -> "Portuguese";
            case AR -> "Arabic";
            case TR -> "Turkish";
        };
    }
}
