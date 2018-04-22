package com.scaythe.bot.tts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteStreams;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;
import com.scaythe.bot.config.TtsConfig;

@Service
public class IbmTtsService implements TtsService {
    private final String login;
    private final String password;
    
    @Autowired
    public IbmTtsService(TtsConfig config) {
        this.login = config.getIbm().get("login");
        this.password = config.getIbm().get("password");
    }

    @Override
    @Cacheable("tts")
    public byte[] read(String text, Locale locale) throws IOException {
        TextToSpeech tts = new TextToSpeech(login, password);
        SynthesizeOptions options = new SynthesizeOptions.Builder(text)
                .accept(SynthesizeOptions.Accept.AUDIO_OGG_CODECS_OPUS)
                .voice(voiceFromLocale(locale))
                .build();
        try (InputStream in = tts.synthesize(options).execute()) {
            return ByteStreams.toByteArray(in);
        }
    }

    private String voiceFromLocale(Locale locale) {
        if (Locale.FRENCH.getLanguage().equals(locale.getLanguage())) {
            return SynthesizeOptions.Voice.FR_FR_RENEEVOICE;
        } else {
            return SynthesizeOptions.Voice.EN_US_ALLISONVOICE;
        }
    }
}
