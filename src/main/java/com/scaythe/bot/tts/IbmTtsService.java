package com.scaythe.bot.tts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteStreams;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;

@Service
public class IbmTtsService implements TtsService {

    private final TextToSpeech tts;

    private final Map<Locale, String> voices = new HashMap<>();

    public IbmTtsService(TtsConfig config) {
        this.tts = new TextToSpeech(config.getIbm().get("login"), config.getIbm().get("password"));
    }

    @Override
    @Cacheable("tts")
    public byte[] read(String text, Locale locale) throws TtsException {
        return read(text, locale, voiceFromLocale(locale));
    }

    @Override
    @Cacheable("tts")
    public byte[] read(String text, Locale locale, String voice) throws TtsException {
        SynthesizeOptions options = new SynthesizeOptions.Builder(text)
                .accept(SynthesizeOptions.Accept.AUDIO_OGG_CODECS_OPUS)
                .voice(voice)
                .build();
        try (InputStream in = tts.synthesize(options).execute()) {
            return ByteStreams.toByteArray(in);
        } catch (IOException e) {
            throw new TtsException(e);
        }
    }

    @Override
    @Cacheable("voices")
    public Collection<VoiceDescriptor> voices(Locale locale) {
        return tts.listVoices()
                .execute()
                .getVoices()
                .stream()
                .filter(v -> v.getLanguage().equals(locale.getLanguage()))
                .map(v -> VoiceDescriptor.of(v.getName(), v.getDescription()))
                .collect(Collectors.toList());
    }

    private String voiceFromLocale(Locale locale) {
        return voices.computeIfAbsent(
                locale,
                l -> voices(l).stream().map(VoiceDescriptor::name).findAny().orElseGet(
                        () -> voiceFromLocale(Locale.ENGLISH)));
    }
}
