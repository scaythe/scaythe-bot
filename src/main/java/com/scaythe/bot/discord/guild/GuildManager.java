package com.scaythe.bot.discord.guild;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.scaythe.bot.config.DefaultsConfig;
import com.scaythe.bot.db.GuildPersistence;
import com.scaythe.bot.db.PropertyPersistence;
import com.scaythe.bot.execution.EncounterPlayer;

import net.dv8tion.jda.core.entities.Guild;

@Service
public class GuildManager implements GuildSettingsManager<GuildObjects> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final GuildPlayerBuilder playerBuilder;
    private final GuildMessageSourceBuilder messageSourceBuilder;
    private final GuildPersistence guildPersistence;
    private final PropertyPersistence propertyPersistence;
    private final DefaultsConfig defaultsConfig;

    private final Map<Long, GuildObjects> map = new ConcurrentHashMap<>();

    public GuildManager(
            GuildPlayerBuilder playerBuilder,
            GuildMessageSourceBuilder messageSourceBuilder,
            GuildPersistence guildPersistence,
            PropertyPersistence propertyPersistence,
            DefaultsConfig defaultsConfig) {
        this.playerBuilder = playerBuilder;
        this.messageSourceBuilder = messageSourceBuilder;
        this.guildPersistence = guildPersistence;
        this.propertyPersistence = propertyPersistence;
        this.defaultsConfig = defaultsConfig;
    }

    @Override
    public GuildObjects getSettings(Guild guild) {
        if (!guildPersistence.exists(guild)) {
            setGuildDefaults(guild);
        }
        
        return this.map.computeIfAbsent(guild.getIdLong(), l -> create(guild));
    }
    
    private void setGuildDefaults(Guild guild) {
        propertyPersistence.language(guild, defaultsConfig.getLanguage());
        defaultsConfig.getVoices().forEach((l, v) -> propertyPersistence.voice(guild, l, v));
        
        guildPersistence.save(guild);
    }

    private GuildObjects create(Guild guild) {
        log.info("creating settings for guild {}", guild.getName());

        return GuildObjectsImmutable.builder()
                .player(playerBuilder.build(guild))
                .messageSource(messageSourceBuilder.build(guild))
                .settings(new GuildSettings(propertyPersistence, guild))
                .build();
    }

    @PreDestroy
    private void clean() {
        map.values()
                .stream()
                .map(GuildObjects::currentPlayer)
                .filter(Objects::nonNull)
                .forEach(EncounterPlayer::stop);
    }
}
