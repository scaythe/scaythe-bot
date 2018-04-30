package com.scaythe.bot.tts;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.cloud.texttospeech.v1beta1.AudioConfig;
import com.google.cloud.texttospeech.v1beta1.AudioEncoding;
import com.google.cloud.texttospeech.v1beta1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1beta1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1beta1.SynthesisInput;
import com.google.cloud.texttospeech.v1beta1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1beta1.Voice;
import com.google.cloud.texttospeech.v1beta1.VoiceSelectionParams;

@Service
@Primary
public class GoogleTtsService implements TtsService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final AudioConfig AUDIO_CONFIG
            = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.OGG_OPUS).build();

    @Override
    @Cacheable("tts")
    public byte[] read(String text, Locale locale, String voice) throws TtsException {
        return read(
                text,
                VoiceSelectionParams.newBuilder()
                        .setName(voice)
                        .setLanguageCode(locale.toLanguageTag())
                        .build());
    }

    @Override
    @Cacheable("tts")
    public byte[] read(String text, Locale locale) throws TtsException {
        return read(text, voiceFromLocale(locale));
    }

    private byte[] read(String text, VoiceSelectionParams voice) throws TtsException {
        log.info("synthetising {} : {} : {}", text, voice.getName(), voice.getLanguageCode());

        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        try (TextToSpeechClient tts = TextToSpeechClient.create()) {
            SynthesizeSpeechResponse response = tts.synthesizeSpeech(input, voice, AUDIO_CONFIG);

            return response.getAudioContent().toByteArray();
        } catch (Exception e) {
            throw new TtsException(e);
        }
    }

    private VoiceSelectionParams voiceFromLocale(Locale locale) {
        return VoiceSelectionParams.newBuilder().setLanguageCode(locale.toLanguageTag()).build();
    }

    @Override
    @Cacheable("voices")
    public Collection<VoiceDescriptor> voices(Locale locale) throws TtsException {
        try (TextToSpeechClient tts = TextToSpeechClient.create()) {
            ListVoicesRequest request = ListVoicesRequest.newBuilder()
                    .setLanguageCode(locale.toLanguageTag())
                    .build();

            ListVoicesResponse response = tts.listVoices(request);
            List<Voice> voices = response.getVoicesList();

            return voices.stream()
                    .map(v -> VoiceDescriptor.of(v.getName(), v.getSsmlGender().name()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new TtsException(e);
        }
    }
}
