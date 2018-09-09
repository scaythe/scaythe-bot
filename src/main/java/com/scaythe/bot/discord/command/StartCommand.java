package com.scaythe.bot.discord.command;

import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.scaythe.bot.discord.guild.GuildObjects;
import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.EncounterRepository;
import com.scaythe.bot.execution.EncounterMessagePublisherBuilder;
import com.scaythe.bot.i18n.MessageResolver;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@CommandInfo(name = { "start" }, description = "Starts an encounter")
@Author("Scaythe")
@Component
public class StartCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.start.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EncounterRepository encounterRepository;
    private final EncounterMessagePublisherBuilder messagePublisherBuilder;
    private final EventWaiter eventWaiter;

    public StartCommand(
            EncounterRepository encounterRepository,
            EncounterMessagePublisherBuilder messagePublisherBuilder,
            EventWaiter eventWaiter) {
        super(I18N_PREFIX);

        this.encounterRepository = encounterRepository;
        this.messagePublisherBuilder = messagePublisherBuilder;
        this.eventWaiter = eventWaiter;

        this.name = "start";
        this.help = "starts an encounter";
    }

    @Override
    protected void execute(CommandEvent event) {
        log().debug("received command with args : {}", event.getArgs());

        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        Disposable player = guildObjects.currentPlayer();

        if (player != null) {
            player.dispose();
            guildObjects.currentPlayer(null);
        }

        encounterMenu(event.getMember(), guildObjects, event).display(event.getChannel());
    }

    private OrderedMenu.Builder ordered(Member member) {
        return ordered(member, eventWaiter);
    }

    private Menu encounterMenu(Member member, GuildObjects guildObjects, CommandEvent event) {
        return ordered(member).setDescription(message("choose.encounter", guildObjects))
                .setChoices(encountersNames(guildObjects))
                .setSelection(encounterSelection(member, guildObjects, event))
                .build();
    }

    private BiConsumer<Message, Integer> encounterSelection(
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return (m, n) -> startTypeMenu(encounter(n - 1), member, guildObjects, event)
                .display(m.getChannel());
    }

    private Menu startTypeMenu(
            Encounter encounter,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return ordered(member).setDescription(message("choose.start-type", guildObjects))
                .setChoices("immediate", "delayed")
                .setSelection(startTypeSelection(encounter, member, guildObjects, event))
                .build();
    }

    private BiConsumer<Message, Integer> startTypeSelection(
            Encounter encounter,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return (m, n) -> start(n, encounter, guildObjects, event);
    }

    private void start(
            int startType,
            Encounter encounter,
            GuildObjects guildObjects,
            CommandEvent event) {
        if (startType == 1) {
            startEncounter(encounter, guildObjects, event, messagePublisherBuilder::build);
        } else {
            startEncounter(encounter, guildObjects, event, messagePublisherBuilder::buildWithDelay);
        }
    }

    private void startEncounter(
            Encounter encounter,
            GuildObjects guildObjects,
            CommandEvent event,
            BiFunction<Encounter, MessageResolver, Flux<String>> messageFluxBuilder) {
        Member member = event.getMember();
        VoiceChannel vc = member.getVoiceState().getChannel();
        if (vc == null) {
            return;
        }
        AudioManager am = vc.getGuild().getAudioManager();

        am.openAudioConnection(vc);
        
        Disposable player = messageFluxBuilder.apply(encounter, messageResolver(guildObjects))
                .publishOn(Schedulers.elastic())
                .doOnError(t -> log.error("{} : {}", t.getClass(), t.getMessage()))
                .subscribe(m -> play(m, guildObjects));

        guildObjects.currentPlayer(player);

        event.reply(message("started", Collections.singletonList(encounter.id()), guildObjects));
    }

    private void play(String message, GuildObjects guildObjects) {
        Locale locale = guildObjects.settings().locale();

        guildObjects.player().play(message, locale, guildObjects.settings().voice(locale));
    }

    private Encounter encounter(int n) {
        return encounters().skip(n).findFirst().get();
    }

    private String[] encountersNames(GuildObjects guildObjects) {
        return encounters().map(e -> encounterName(e, guildObjects)).toArray(String[]::new);
    }

    private String encounterName(Encounter encounter, GuildObjects guildObjects) {
        return MessageResolver.message(
                encounter.id() + ".name",
                guildObjects.settings().locale(),
                guildObjects.messageSource());
    }

    private Stream<Encounter> encounters() {
        return encounterRepository.list().stream().sorted(Comparator.comparing(Encounter::id));
    }
}
