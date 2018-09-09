package com.scaythe.bot.discord;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.scaythe.bot.config.ConfigPrefixes;

@Component
@ConfigurationProperties(ConfigPrefixes.DEFAULTS)
public class DefaultsConfig {

    private Locale language = Locale.ENGLISH;
    private final Map<Locale, String> voices = new HashMap<>();

    public Locale getLanguage() {
        return language;
    }

    public void setLanguage(Locale language) {
        this.language = language;
    }

    public Map<Locale, String> getVoices() {
        return voices;
    }
}
