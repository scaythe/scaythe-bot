package com.scaythe.bot.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.scaythe.bot.config.encounter.EncounterConfig;

@Component
@ConfigurationProperties(ConfigPrefixes.ENCOUNTERS)
public class EncountersConfig {
    private final List<EncounterConfig> list = new ArrayList<>();

    public List<EncounterConfig> getList() {
        return list;
    }
}
