package com.workspace.codeforgeai.common.i18n;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CareerLocaleResolver {

    public SupportedLocale resolve(String explicitLocale) {
        if (explicitLocale != null && !explicitLocale.isBlank()) {
            return SupportedLocale.from(explicitLocale);
        }

        return SupportedLocale.from(LocaleContextHolder.getLocale());
    }

    public String resolveLanguageTag(String explicitLocale) {
        return resolve(explicitLocale).languageTag();
    }
}
