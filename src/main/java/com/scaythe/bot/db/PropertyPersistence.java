package com.scaythe.bot.db;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.core.entities.Guild;

@Component
public class PropertyPersistence {

    private static final String PROPERTY_KIND = "Property";

    private static final String LANGUAGE = "bot.language";
    private static final String VOICE = "bot.voice";

    private final ValueDao dao;

    public PropertyPersistence(ValueDao dao) {
        this.dao = dao;
    }

    public Optional<Locale> language(Guild guild) {
        return dao.get(guild, PROPERTY_KIND, LANGUAGE, Optional.empty())
                .map(Locale::forLanguageTag);
    }

    public void language(Guild guild, Locale locale) {
        dao.save(guild, PROPERTY_KIND, LANGUAGE, Optional.empty(), locale.toLanguageTag());
    }
    
    public Optional<String> voice(Guild guild, Locale locale) {
        return dao.get(guild, PROPERTY_KIND, VOICE, Optional.ofNullable(locale));
    }
    
    public void voice(Guild guild, Locale locale, String voice) {
        dao.save(guild, PROPERTY_KIND, VOICE, Optional.ofNullable(locale), voice);
    }
    
    public void voiceUnset(Guild guild, Locale locale) {
        dao.delete(guild, PROPERTY_KIND, VOICE, Optional.ofNullable(locale));
    }
}
