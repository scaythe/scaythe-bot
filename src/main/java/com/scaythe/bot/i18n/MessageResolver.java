package com.scaythe.bot.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;

@FunctionalInterface
public interface MessageResolver {

    public String resolve(String code, List<String> args);

    public default String resolve(String code) {
        return resolve(code, Collections.emptyList());
    }

    public static String message(
            String code,
            List<String> args,
            Locale locale,
            MessageSource source) {
        return source.getMessage(code, args.stream().toArray(String[]::new), locale);
    }

    public static String message(String code, Locale locale, MessageSource source) {
        return message(code, Collections.emptyList(), locale, source);
    }
}
