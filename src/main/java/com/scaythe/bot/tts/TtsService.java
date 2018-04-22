package com.scaythe.bot.tts;

import java.io.IOException;
import java.util.Locale;

public interface TtsService {
    public byte[] read(String text, Locale locale) throws IOException;
}
