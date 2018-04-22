package com.scaythe.bot.discord.guild;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.scaythe.bot.discord.sound.AudioPlayerSendHandler;
import com.scaythe.bot.discord.sound.QueuedFilePlayer;
import com.scaythe.bot.discord.sound.TtsPlayer;
import com.scaythe.bot.execution.EncounterPlayer;
import com.scaythe.bot.file.TempFileService;
import com.scaythe.bot.i18n.ConfigurableMessageSource;
import com.scaythe.bot.tts.GoogleTtsService;
import com.scaythe.bot.tts.TtsService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;

@Service
public class GuildManager implements GuildSettingsManager<GuildObjects> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AudioPlayerManager playerManager;
    private final TempFileService fileService;
    private final TtsService ttsService;
    private final MessageSource messageSource;

    private final Map<Long, GuildObjects> map = new ConcurrentHashMap<>();

    @Autowired
    public GuildManager(
            AudioPlayerManager playerManager,
            TempFileService fileService,
            GoogleTtsService ttsService,
            MessageSource messageSource) {
        this.playerManager = playerManager;
        this.fileService = fileService;
        this.ttsService = ttsService;
        this.messageSource = messageSource;
    }

    @Override
    public GuildObjects getSettings(Guild guild) {
        GuildObjects objects = this.map.get(guild.getIdLong());

        if (objects == null) {
            objects = create(guild);
            this.map.put(guild.getIdLong(), objects);
        }

        return objects;
    }

    private GuildObjects create(Guild guild) {
        log.info("creating settings for guild {}", guild.getName());

        AudioPlayer audioPlayer = playerManager.createPlayer();

        AudioSendHandler ash = new AudioPlayerSendHandler(audioPlayer);

        guild.getAudioManager().setSendingHandler(ash);

        QueuedFilePlayer filePlayer = new QueuedFilePlayer(audioPlayer, fileService);

        TtsPlayer player = new TtsPlayer(playerManager, ttsService, filePlayer, fileService);

        return GuildObjectsImmutable.builder()
                .player(player)
                .messageSource(new ConfigurableMessageSource(messageSource))
                .build();
    }

    @PreDestroy
    private void clean() {
        map.values()
                .stream()
                .map(GuildObjects::config)
                .map(MutableConfig::player)
                .filter(Objects::nonNull)
                .forEach(EncounterPlayer::stop);
    }
}
