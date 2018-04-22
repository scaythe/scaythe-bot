package com.scaythe.bot.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(ConfigPrefixes.TTS)
public class TtsConfig {
    private final Map<String, String> ibm = new HashMap<>();

    public Map<String, String> getIbm() {
        return ibm;
    }

    @Override
    public String toString() {
        return "TtsConfig [ibm=" + ibm + "]";
    }
}
