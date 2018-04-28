package com.scaythe.bot.discord.guild;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.scaythe.bot.db.SpeechPersistence;
import com.scaythe.bot.i18n.ConfigurableMessageSource;

import net.dv8tion.jda.core.entities.Guild;

@Component
public class GuildMessageSourceBuilder {
    private final MessageSource messageSource;
    private final SpeechPersistence speechPersistence;
    
    public GuildMessageSourceBuilder(
            MessageSource messageSource,
            SpeechPersistence speechPersistence) {
        this.messageSource = messageSource;
        this.speechPersistence = speechPersistence;
    }
    
    public ConfigurableMessageSource build(Guild guild) {
        return new ConfigurableMessageSource(messageSource, speechPersistence, guild);
    }
}
