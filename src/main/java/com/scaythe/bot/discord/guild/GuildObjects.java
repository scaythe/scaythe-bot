package com.scaythe.bot.discord.guild;

import org.immutables.value.Value.Immutable;

import com.scaythe.bot.discord.sound.TtsPlayer;
import com.scaythe.bot.i18n.ConfigurableMessageSource;

import reactor.core.Disposable;

@Immutable
public abstract class GuildObjects {
    private Disposable player = null;

    public abstract TtsPlayer player();
    public abstract ConfigurableMessageSource messageSource();
    public abstract GuildSettings settings();
    
    public Disposable currentPlayer() {
        return player;
    }
    
    public void currentPlayer(Disposable player) {
        this.player = player;
    }
}
