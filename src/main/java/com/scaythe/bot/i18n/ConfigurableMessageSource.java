package com.scaythe.bot.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.util.Assert;

import com.scaythe.bot.db.SpeechPersistence;

import net.dv8tion.jda.core.entities.Guild;

public class ConfigurableMessageSource extends AbstractMessageSource {

    private final SpeechPersistence persistence;
    private final Guild guild;

    public ConfigurableMessageSource(
            MessageSource parent,
            SpeechPersistence persistence,
            Guild guild) {
        this.setParentMessageSource(parent);

        this.persistence = persistence;
        this.guild = guild;
    }

    public Optional<String> set(String code, String value) {
        Assert.notNull(value, "can't set null value");

        return persistence.set(guild, code, value);
    }

    public Optional<String> set(String code, Locale locale, String value) {
        Assert.notNull(value, "can't set null value");

        return persistence.set(guild, code, locale, value);
    }

    public Optional<String> unset(String code) {
        return persistence.unset(guild, code);
    }

    public Optional<String> unset(String code, Locale locale) {
        return persistence.unset(guild, code, locale);
    }

    public boolean isSet(String code, Locale locale) {
        return persistence.get(guild, code, locale).isPresent()
                || persistence.get(guild, code).isPresent();
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        Optional<String> message = persistence.get(guild, code, locale);

        if (!message.isPresent()) {
            message = persistence.get(guild, code);
        }

        return message.map(v -> new MessageFormat(v, locale)).orElse(null);
    }
}
