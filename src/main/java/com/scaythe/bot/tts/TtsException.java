package com.scaythe.bot.tts;

public class TtsException extends Exception {
    private static final long serialVersionUID = -2224635548007138735L;

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
