package com.scaythe.bot.discord.guild;

import org.immutables.value.Value.Immutable;

import com.scaythe.bot.discord.sound.TtsPlayer;
import com.scaythe.bot.execution.EncounterPlayer;
import com.scaythe.bot.i18n.ConfigurableMessageSource;

@Immutable
public abstract class GuildObjects {
    private EncounterPlayer player = null;

    public abstract TtsPlayer player();
    public abstract ConfigurableMessageSource messageSource();
    public abstract GuildSettings settings();
    
    public EncounterPlayer currentPlayer() {
        return player;
    }
    
    public void currentPlayer(EncounterPlayer player) {
        this.player = player;
    }
}
