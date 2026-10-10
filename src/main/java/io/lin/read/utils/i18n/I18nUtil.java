package io.lin.read.utils.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class I18nUtil {

    private final MessageSource messageSource;

    public String m(String key, Object... args) {
        return messageSource.getMessage(
                key,
                args,
                LocaleContextHolder.getLocale()
        );
    }

}
