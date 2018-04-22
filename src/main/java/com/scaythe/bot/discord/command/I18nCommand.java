package com.scaythe.bot.discord.command;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.jagrosh.jdautilities.command.Command;
import com.scaythe.bot.i18n.MessageResolver;

public abstract class I18nCommand extends Command {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String i18nPrefix;

    public I18nCommand(String i18nPrefix) {
        this.i18nPrefix = i18nPrefix;
    }

    public Logger log() {
        return log;
    }

    public String message(String code, List<String> args, Locale locale, MessageSource source) {
        return MessageResolver.message(i18nPrefix + code, args, locale, source);
    }

    public String message(String code, Locale locale, MessageSource source) {
        return MessageResolver.message(i18nPrefix + code, locale, source);
    }
}
