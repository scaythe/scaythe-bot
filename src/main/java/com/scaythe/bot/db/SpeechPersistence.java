package com.scaythe.bot.db;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import net.dv8tion.jda.core.entities.Guild;

@Component
public class SpeechPersistence {
    private static final String SPEECH_KIND = "Speech";
    
    private final ValueDao dao;

    public SpeechPersistence(ValueDao dao) {
        this.dao = dao;
    }

    public Optional<String> get(Guild guild, String code) {
        return get(guild, code, Optional.empty());
    }

    public Optional<String> get(Guild guild, String code, Locale locale) {
        return get(guild, code, Optional.ofNullable(locale));
    }

    public Optional<String> set(Guild guild, String code, String value) {
        return set(guild, code, Optional.empty(), value);
    }

    public Optional<String> set(Guild guild, String code, Locale locale, String value) {
        return set(guild, code, Optional.ofNullable(locale), value);
    }

    public Optional<String> unset(Guild guild, String code) {
        return unset(guild, code, Optional.empty());
    }

    public Optional<String> unset(Guild guild, String code, Locale locale) {
        return unset(guild, code, Optional.ofNullable(locale));
    }

    private Optional<String> get(Guild guild, String code, Optional<Locale> locale) {
        return dao.get(guild, SPEECH_KIND, code, locale);
    }

    private Optional<String> set(Guild guild, String code, Optional<Locale> locale, String value) {
        Assert.notNull(value, "can't set null value");
        
        Optional<String> old = get(guild, code, locale);

        dao.save(guild, SPEECH_KIND, code, locale, value);

        return old;
    }

    private Optional<String> unset(Guild guild, String code, Optional<Locale> locale) {
        Optional<String> old = get(guild, code, locale);
        
        dao.delete(guild, SPEECH_KIND, code, locale);
        
        return old;
    }
}
