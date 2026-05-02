package com.workspace.codeforgeai.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LocalizedMessages {

    private final MessageSource messageSource;

    public LocalizedMessages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String get(SupportedLocale locale, String key, Object... args) {
        return messageSource.getMessage(key, args, locale.locale());
    }
}
