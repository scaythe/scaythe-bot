package com.scaythe.bot;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import com.scaythe.bot.db.DB;
import com.scaythe.bot.discord.Discord;
import com.scaythe.bot.encounter.Encounters;
import com.scaythe.bot.execution.Execution;
import com.scaythe.bot.i18n.I18n;
import com.scaythe.bot.tts.TTS;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableCaching(proxyTargetClass = true)
@EnableConfigurationProperties
@Import({DB.class, Discord.class, Encounters.class, Execution.class, I18n.class, TTS.class})
public class Bot {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Bot.class).web(WebApplicationType.NONE).run(args);
    }
}
