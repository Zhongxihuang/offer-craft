package com.workspace.codeforgeai.common.i18n;

import java.util.Locale;

public enum SupportedLocale {
    EN("en", Locale.ENGLISH),
    ZH_CN("zh-CN", Locale.forLanguageTag("zh-CN"));

    private final String languageTag;
    private final Locale locale;

    SupportedLocale(String languageTag, Locale locale) {
        this.languageTag = languageTag;
        this.locale = locale;
    }

    public String languageTag() {
        return languageTag;
    }

    public Locale locale() {
        return locale;
    }

    public boolean isChinese() {
        return this == ZH_CN;
    }

    public static SupportedLocale from(String value) {
        if (value == null || value.isBlank()) {
            return EN;
        }

        String normalized = value.trim().replace('_', '-').toLowerCase(Locale.ROOT);
        return normalized.startsWith("zh") ? ZH_CN : EN;
    }

    public static SupportedLocale from(Locale locale) {
        if (locale == null) {
            return EN;
        }

        return from(locale.toLanguageTag());
    }
}
