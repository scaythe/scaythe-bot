package com.scaythe.bot.discord.guild;

import java.util.Locale;

import com.scaythe.bot.execution.EncounterPlayer;

public class MutableConfig {
    private Locale locale = Locale.ENGLISH;
    private EncounterPlayer player = null;
    
    public Locale locale() {
        return locale;
    }

    public void locale(Locale locale) {
        this.locale = locale;
    }

    public EncounterPlayer player() {
        return player;
    }
    
    public void player(EncounterPlayer player) {
        this.player = player;
    }
}
