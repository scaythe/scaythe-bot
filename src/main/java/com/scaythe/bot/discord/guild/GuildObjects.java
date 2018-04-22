package com.scaythe.bot.discord.guild;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import com.scaythe.bot.discord.sound.TtsPlayer;
import com.scaythe.bot.i18n.ConfigurableMessageSource;

@Immutable
public interface GuildObjects {

    public TtsPlayer player();
    public ConfigurableMessageSource messageSource();
    
    @Default
    default MutableConfig config() {
        return new MutableConfig();
    }
}
