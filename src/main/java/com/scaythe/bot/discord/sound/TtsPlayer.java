package com.scaythe.bot.discord.sound;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scaythe.bot.file.TempFileService;
import com.scaythe.bot.tts.TtsService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class TtsPlayer {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final AudioPlayerManager playerManager;
    private final TtsService ttsService;
    private final AudioLoadResultHandler player;
    private final TempFileService fileService;
    
    public TtsPlayer(
            AudioPlayerManager playerManager,
            TtsService ttsService,
            AudioLoadResultHandler player,
            TempFileService fileService) {
        this.playerManager = playerManager;
        this.ttsService = ttsService;
        this.player = player;
        this.fileService = fileService;
    }

    public void play(String text, Locale locale) {
        log.info("playing : {}", text);
        
        try {
            byte[] bytes = ttsService.read(text, locale);

            Path path = fileService.newFile(bytes);

            log.debug("loadItem {}", path);
            playerManager.loadItem(path.toString(), player);
        } catch (IOException e) {
            log.warn("erreur de lecture de stream : {}", e.getMessage());
            log.trace("", e);
        }
    }
}
