package com.scaythe.bot.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;

public abstract class MessageResolver {

    public static String message(String code, List<String> args, Locale locale, MessageSource source) {
        return source.getMessage(
                code,
                args.stream().toArray(String[]::new),
                locale);
    }

    public static String message(String code, Locale locale, MessageSource source) {
        return message(code, Collections.emptyList(), locale, source);
    }
}
