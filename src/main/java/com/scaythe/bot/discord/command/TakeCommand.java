package com.scaythe.bot.discord.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
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
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.i18n.MessageResolver;
import com.scaythe.bot.i18n.SpeechCodeBuilder;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

@CommandInfo(name = { "Take" }, description = "claim a role")
@Author("Scaythe")
@Component
public class TakeCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.take.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EncounterRepository encounterRepository;
    private final SpeechCodeBuilder codeBuilder;
    private final EventWaiter eventWaiter;

    public TakeCommand(
            EncounterRepository encounterRepository,
            SpeechCodeBuilder codeBuilder,
            EventWaiter eventWaiter) {
        super(I18N_PREFIX);

        this.encounterRepository = encounterRepository;
        this.codeBuilder = codeBuilder;
        this.eventWaiter = eventWaiter;

        this.name = "take";
        this.help = "claim a role";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        encounterMenu(event.getMember(), guildObjects, event).display(event.getChannel());
    }

    private OrderedMenu.Builder ordered(Member member) {
        return ordered(member, eventWaiter);
    }

    private Menu encounterMenu(Member member, GuildObjects guildObjects, CommandEvent event) {
        return ordered(member)
                .setDescription(
                        message(
                                "choose.encounter",
                                guildObjects.settings().locale(),
                                guildObjects.messageSource()))
                .setChoices(encountersNames(guildObjects))
                .setSelection(encounterSelection(member, guildObjects, event))
                .build();
    }

    private BiConsumer<Message, Integer> encounterSelection(
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return (m, n) -> mechanicMenu(encounter(n - 1), member, guildObjects, event)
                .ifPresent(menu -> menu.display(m.getChannel()));
    }

    private Optional<Menu> mechanicMenu(
            Encounter encounter,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        String[] mechanicsIds = mechanicsNames(encounter, guildObjects);

        if (mechanicsIds.length == 1) {
            return roleMenu(mechanic(0, encounter), encounter, member, guildObjects, event);
        }

        return Optional
                .of(
                        ordered(member)
                                .setDescription(
                                        message(
                                                "choose.mechanic",
                                                Arrays.asList(
                                                        encounterName(encounter, guildObjects)),
                                                guildObjects.settings().locale(),
                                                guildObjects.messageSource()))
                                .setChoices(mechanicsIds)
                                .setSelection(
                                        mechanicSelection(encounter, member, guildObjects, event))
                                .build());
    }

    private BiConsumer<Message, Integer> mechanicSelection(
            Encounter encounter,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return (
                m,
                n) -> roleMenu(mechanic(n - 1, encounter), encounter, member, guildObjects, event)
                        .ifPresent(menu -> menu.display(m.getChannel()));
    }

    private Optional<Menu> roleMenu(
            Mechanic mechanic,
            Encounter encounter,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        if (mechanic.roles() == 1) {
            setRole(0, mechanic, encounter, member, guildObjects, event);

            return Optional.empty();
        }

        return Optional.of(
                ordered(member)
                        .setDescription(
                                message(
                                        "choose.role",
                                        Arrays.asList(
                                                mechanicName(mechanic, encounter, guildObjects),
                                                encounterName(encounter, guildObjects)),
                                        guildObjects.settings().locale(),
                                        guildObjects.messageSource()))
                        .setChoices(rolesIds(mechanic))
                        .setSelection(
                                roleSelection(mechanic, encounter, member, guildObjects, event))
                        .build());
    }

    private BiConsumer<Message, Integer> roleSelection(
            Mechanic mechanic,
            Encounter encounter,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return (m, n) -> setRole(n - 1, mechanic, encounter, member, guildObjects, event);
    }

    private void setRole(
            int role,
            Mechanic mechanic,
            Encounter encounter,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        Locale locale = guildObjects.settings().locale();

        guildObjects.messageSource()
                .set(codeBuilder.role(encounter, mechanic, role), member.getEffectiveName());

        String message = message(
                "taken",
                Arrays.asList(
                        member.getEffectiveName(),
                        role(role),
                        mechanicName(mechanic, encounter, guildObjects),
                        encounterName(encounter, guildObjects)),
                locale,
                guildObjects.messageSource());

        event.reply(message);

        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        if (vc == null) {
            return;
        }

        AudioManager am = vc.getGuild().getAudioManager();
        am.openAudioConnection(vc);

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
        return encounterRepository.list()
                .stream()
                .filter(e -> mechanics(e).findFirst().isPresent())
                .sorted(Comparator.comparing(Encounter::id));
    }

    private Mechanic mechanic(int n, Encounter encounter) {
        return mechanics(encounter).skip(n).findFirst().get();
    }

    private String[] mechanicsNames(Encounter encounter, GuildObjects guildObjects) {
        return mechanics(encounter).map(m -> mechanicName(m, encounter, guildObjects))
                .toArray(String[]::new);
    }

    private String mechanicName(Mechanic mechanic, Encounter encounter, GuildObjects guildObjects) {
        return MessageResolver.message(
                encounter.id() + "." + mechanic.id() + ".name",
                guildObjects.settings().locale(),
                guildObjects.messageSource());

    }

    private Stream<Mechanic> mechanics(Encounter encounter) {
        return encounter.mechanics().stream().filter(m -> m.roles() != 0).sorted(
                Comparator.comparing(Mechanic::id));
    }

    private String[] rolesIds(Mechanic mechanic) {
        return IntStream.rangeClosed(1, mechanic.roles()).mapToObj(Integer::toString).toArray(
                String[]::new);
    }
}
