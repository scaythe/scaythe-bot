package com.scaythe.bot.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.scaythe.bot.config.encounter.EncounterConfig;

@Component
@ConfigurationProperties(ConfigPrefixes.GW)
public class Gw2Config {
    private final List<EncounterConfig> encounters = new ArrayList<>();

    public List<EncounterConfig> getEncounters() {
        return encounters;
    }
}
