package com.scaythe.bot.execution;

import java.util.Collections;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scaythe.bot.discord.guild.GuildObjects;
import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.i18n.SpeechMessageResolver;

public class EncounterPlayer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final GuildObjects guildObjects;
    
    private final SpeechMessageResolver messageResolver;

    private final EncounterExecutor executor;

    public EncounterPlayer(Encounter encounter, GuildObjects guildObjects, SpeechMessageResolver messageResolver) {
        log.debug("creating for {}", encounter.id());

        this.guildObjects = guildObjects;
        this.messageResolver = messageResolver;
        this.executor = new EncounterExecutor(encounter, Collections.singleton(this::read));
    }

    private void read(EncounterEvent event) {
        log.debug(
                "listener received {} {} {} {}",
                event.mechanicCount(),
                event.encounter().id(),
                event.mechanic().id(),
                event.warning().id());

        Locale locale = guildObjects.config().locale();
        guildObjects.player().play(messageResolver.message(event, locale, guildObjects.messageSource()), locale);
    }

    public void stop() {
        executor.stop();
    }
}
