package com.scaythe.bot.discord.sound;

import com.scaythe.bot.tts.TtsException;
import com.scaythe.bot.tts.TtsService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

public class TtsPlayer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ByteArrayAudioSourceManager audioSourceManager;
    private final AudioPlayerManager playerManager;
    private final TtsService ttsService;
    private final AudioLoadResultHandler player;

    public TtsPlayer(ByteArrayAudioSourceManager audioSourceManager,
            AudioPlayerManager playerManager,
            TtsService ttsService,
            AudioLoadResultHandler player) {
        this.audioSourceManager = audioSourceManager;
        this.playerManager = playerManager;
        this.ttsService = ttsService;
        this.player = player;
    }

    public void play(String text, Locale locale, Optional<String> voice) {
        log.info("playing : {}", text);

        try {
            byte[] bytes;
            if (voice.isPresent()) {
                bytes = ttsService.read(text, locale, voice.get());
            } else {
                bytes = ttsService.read(text, locale);
            }

            String identifier = audioSourceManager.register(bytes);

            log.debug("loadItem {}", identifier);
            playerManager.loadItem(identifier, player);
        } catch (TtsException e) {
            log.warn("erreur de lecture de stream : {}", e.getMessage());
            log.trace("", e);
        }
    }
}
