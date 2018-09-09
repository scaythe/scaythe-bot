package com.scaythe.bot.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.scaythe.bot.config.ConfigPrefixes;

@Component
@ConfigurationProperties(ConfigPrefixes.DISCORD)
public class DiscordConfig {
    private String token;
    private String owner;
    private String prefix;
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "DiscordConfig [token=" + token + ", owner=" + owner + ", prefix=" + prefix + "]";
    }
}
