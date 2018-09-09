package com.scaythe.bot.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.scaythe.bot.config.ConfigPrefixes;

@Component
@ConfigurationProperties(ConfigPrefixes.LANGUAGE)
public class LanguageConfig {
    private final List<Locale> locales = new ArrayList<>();
    
    public List<Locale> getLocales() {
        return locales;
    }

    @Override
    public String toString() {
        return "LanguageConfig [locales=" + locales + "]";
    }
}
