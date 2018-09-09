package com.scaythe.bot.tts;

public class TtsException extends Exception {

    public TtsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TtsException(String message) {
        super(message);
    }

    public TtsException(Throwable cause) {
        super(cause);
    }
}
