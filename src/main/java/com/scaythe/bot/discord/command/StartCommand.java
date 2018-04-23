package com.scaythe.bot.discord.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

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
import com.scaythe.bot.execution.EncounterPlayer;
import com.scaythe.bot.i18n.MessageResolver;
import com.scaythe.bot.i18n.SpeechMessageResolver;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

@CommandInfo(name = { "start" }, description = "Starts an encounter")
@Author("Scaythe")
@Component
public class StartCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.start.";

    private final EncounterRepository encounterRepository;
    private final SpeechMessageResolver speechMessageResolver;
    private final EventWaiter eventWaiter;

    public StartCommand(
            EncounterRepository encounterRepository,
            SpeechMessageResolver speechMessageResolver,
            EventWaiter eventWaiter) {
        super(I18N_PREFIX);
        
        this.encounterRepository = encounterRepository;
        this.speechMessageResolver = speechMessageResolver;
        this.eventWaiter = eventWaiter;

        this.name = "start";
        this.help = "starts an encounter";
    }

    @Override
    protected void execute(CommandEvent event) {
        log().debug("received command with args : {}", event.getArgs());

        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        EncounterPlayer player = guildObjects.config().player();

        if (player != null) {
            player.stop();
            guildObjects.config().player(null);
        }
        
        encounterMenu(event.getMember(), guildObjects, event).display(event.getChannel());
    }

    private OrderedMenu.Builder common(Member member) {
        return new OrderedMenu.Builder().setUsers(member.getUser())
                .setText(member.getEffectiveName())
                .setEventWaiter(eventWaiter)
                .allowTextInput(false)
                .useCancelButton(true)
                .setTimeout(30, TimeUnit.SECONDS);
    }

    private Menu encounterMenu(Member member, GuildObjects guildObjects, CommandEvent event) {
        return common(member)
                .setDescription(
                        message(
                                "choose.encounter",
                                guildObjects.config().locale(),
                                guildObjects.messageSource()))
                .setChoices(encountersIds())
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
        return common(member)
                .setDescription(
                        message(
                                "choose.encounter",
                                guildObjects.config().locale(),
                                guildObjects.messageSource()))
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
            startEncounter(encounter, guildObjects, event);
        } else {
            startEncounterWithDelay(encounter, guildObjects, event);
        }
    }

    private void startEncounterWithDelay(
            Encounter encounter,
            GuildObjects guildObjects,
            CommandEvent event) {
        Member member = event.getMember();
        VoiceChannel vc = member.getVoiceState().getChannel();
        if (vc == null) {
            return;
        }
        AudioManager am = vc.getGuild().getAudioManager();

        am.openAudioConnection(vc);
        
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.execute(() -> play("delayed-start.launched", Arrays.asList(encounter.id()), guildObjects));
        executor.schedule(() -> play("delayed-start.3", guildObjects), 3, TimeUnit.SECONDS);
        executor.schedule(() -> play("delayed-start.2", guildObjects), 4, TimeUnit.SECONDS);
        executor.schedule(() -> play("delayed-start.1", guildObjects), 5, TimeUnit.SECONDS);
        executor.schedule(() -> play("delayed-start.go", guildObjects), 6, TimeUnit.SECONDS);
        executor.schedule(
                () -> startEncounter(encounter, guildObjects, event),
                6,
                TimeUnit.SECONDS);

        executor.schedule(executor::shutdownNow, 10, TimeUnit.SECONDS);
    }

    private void play(String code, GuildObjects guildObjects) {
        play(code, Collections.emptyList(), guildObjects);
    }

    private void play(String code, List<String> args, GuildObjects guildObjects) {
        guildObjects.player().play(
                MessageResolver.message(code, args, guildObjects.config().locale(), guildObjects.messageSource()),
                guildObjects.config().locale());
    }

    private void startEncounter(
            Encounter encounter,
            GuildObjects guildObjects,
            CommandEvent event) {
        Member member = event.getMember();
        VoiceChannel vc = member.getVoiceState().getChannel();
        if (vc == null) {
            return;
        }
        AudioManager am = vc.getGuild().getAudioManager();

        am.openAudioConnection(vc);
        
        guildObjects.config()
                .player(new EncounterPlayer(encounter, guildObjects, speechMessageResolver));

        event.reply(
                message(
                        "started",
                        Collections.singletonList(encounter.id()),
                        guildObjects.config().locale(),
                        guildObjects.messageSource()));
    }

    private Encounter encounter(int n) {
        return encounters().skip(n).findFirst().get();
    }

    private String[] encountersIds() {
        return encounters().map(Encounter::id).toArray(String[]::new);
    }

    private Stream<Encounter> encounters() {
        return encounterRepository.list().stream().sorted(Comparator.comparing(Encounter::id));
    }
}
