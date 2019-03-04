package com.scaythe.bot.discord.guild;

import com.scaythe.bot.discord.sound.*;
import org.springframework.stereotype.Component;

import com.scaythe.bot.tts.TtsService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;

@Component
public class GuildPlayerBuilder {
    private final ByteArrayAudioSourceManager audioSourceManager;
    private final AudioPlayerManager playerManager;
    private final TtsService ttsService;

    public GuildPlayerBuilder(ByteArrayAudioSourceManager audioSourceManager,
            AudioPlayerManager playerManager,
            TtsService ttsService) {
        this.audioSourceManager = audioSourceManager;
        this.playerManager = playerManager;
        this.ttsService = ttsService;
    }

    public TtsPlayer build(Guild guild) {
        AudioPlayer audioPlayer = playerManager.createPlayer();

        AudioSendHandler ash = new AudioPlayerSendHandler(audioPlayer);

        guild.getAudioManager().setSendingHandler(ash);

        QueuedFilePlayer filePlayer = new QueuedFilePlayer(audioPlayer);

        return new TtsPlayer(audioSourceManager, playerManager, ttsService, filePlayer);
    }
}
