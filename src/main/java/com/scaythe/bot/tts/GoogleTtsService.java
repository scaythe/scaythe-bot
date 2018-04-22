package com.scaythe.bot.tts;

import java.io.IOException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.cloud.texttospeech.v1beta1.AudioConfig;
import com.google.cloud.texttospeech.v1beta1.AudioEncoding;
import com.google.cloud.texttospeech.v1beta1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1beta1.SynthesisInput;
import com.google.cloud.texttospeech.v1beta1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1beta1.VoiceSelectionParams;

@Service
public class GoogleTtsService implements TtsService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final VoiceSelectionParams voiceFr = VoiceSelectionParams.newBuilder()
            .setName("fr-FR-Standard-C")
            .setSsmlGender(SsmlVoiceGender.FEMALE)
            .setLanguageCode("fr-FR")
            .build();
    private static final VoiceSelectionParams voiceEn = VoiceSelectionParams.newBuilder()
            .setSsmlGender(SsmlVoiceGender.FEMALE)
            .setLanguageCode("en-US")
            .build();
    private static final AudioConfig audioConfig
            = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.OGG_OPUS).build();

    @Override
    @Cacheable("tts")
    public byte[] read(String text, Locale locale) throws IOException {
        log.info("synthetising {}", text);

        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        try (TextToSpeechClient tts = TextToSpeechClient.create()) {
            SynthesizeSpeechResponse response = tts.synthesizeSpeech(input, voiceFromLocale(locale), audioConfig);
            return response.getAudioContent().toByteArray();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private VoiceSelectionParams voiceFromLocale(Locale locale) {
        if (Locale.FRENCH.getLanguage().equals(locale.getLanguage())) {
            return voiceFr;
        } else {
            return voiceEn;
        }
    }
}
