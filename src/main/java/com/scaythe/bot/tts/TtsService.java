package com.scaythe.bot.tts;

import java.util.Collection;
import java.util.Locale;

public interface TtsService {
    public byte[] read(String text, Locale locale) throws TtsException;
    public byte[] read(String text, Locale locale, String voice) throws TtsException;
    public Collection<VoiceDescriptor> voices(Locale locale) throws TtsException;
}
