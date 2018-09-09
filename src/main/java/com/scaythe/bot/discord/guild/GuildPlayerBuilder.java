package com.scaythe.bot.discord.guild;

import org.springframework.stereotype.Component;

import com.scaythe.bot.discord.sound.AudioPlayerSendHandler;
import com.scaythe.bot.discord.sound.QueuedFilePlayer;
import com.scaythe.bot.discord.sound.TempFileService;
import com.scaythe.bot.discord.sound.TtsPlayer;
import com.scaythe.bot.tts.TtsService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;

@Component
public class GuildPlayerBuilder {
    private final AudioPlayerManager playerManager;
    private final TempFileService fileService;
    private final TtsService ttsService;
    
    public GuildPlayerBuilder(
            AudioPlayerManager playerManager,
            TempFileService fileService,
            TtsService ttsService) {
        this.playerManager = playerManager;
        this.fileService = fileService;
        this.ttsService = ttsService;
    }

    public TtsPlayer build(Guild guild) {
        AudioPlayer audioPlayer = playerManager.createPlayer();

        AudioSendHandler ash = new AudioPlayerSendHandler(audioPlayer);

        guild.getAudioManager().setSendingHandler(ash);

        QueuedFilePlayer filePlayer = new QueuedFilePlayer(audioPlayer, fileService);

        return new TtsPlayer(playerManager, ttsService, filePlayer, fileService);
    }
}
