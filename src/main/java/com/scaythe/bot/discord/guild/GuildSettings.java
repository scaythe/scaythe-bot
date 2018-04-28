package com.scaythe.bot.discord.guild;

import java.util.Locale;
import java.util.Optional;

import com.scaythe.bot.db.PropertyPersistence;

import net.dv8tion.jda.core.entities.Guild;

public class GuildSettings {

    private final PropertyPersistence persistence;
    private final Guild guild;

    public GuildSettings(PropertyPersistence persistence, Guild guild) {
        this.persistence = persistence;
        this.guild = guild;
    }

    public Locale locale() {
        return persistence.language(guild).orElseThrow(() -> new RuntimeException());
    }

    public void locale(Locale locale) {
        persistence.language(guild, locale);
    }

    public Optional<String> voice(Locale locale) {
        return persistence.voice(guild, locale);
    }

    public void voice(Locale locale, String voice) {
        persistence.voice(guild, locale, voice);
    }

    public void voiceUnset(Locale locale) {
        persistence.voiceUnset(guild, locale);
    }
}
