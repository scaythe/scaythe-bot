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
    private static final String SET_ACTION = "set";
    private static final String UNSET_ACTION = "unset";

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

        String action = event.getArgs().trim();
        
        if (!action.equals(UNSET_ACTION)) {
            action = SET_ACTION;
        }

        MenuObjects objects = MenuObjectsImmutable.builder()
                .member(event.getMember())
                .guildObjects(guildObjects)
                .event(event)
                .action(action)
                .build();

        encounterMenu(objects).display(event.getChannel());
    }

    private OrderedMenu.Builder ordered(Member member) {
        return ordered(member, eventWaiter);
    }

    private Menu encounterMenu(MenuObjects objects) {
        return ordered(objects.member())
                .setDescription(
                        message(
                                "choose.encounter",
                                objects.guildObjects().settings().locale(),
                                objects.guildObjects().messageSource()))
                .setChoices(encountersNames(objects.guildObjects()))
                .setSelection(encounterSelection(objects))
                .build();
    }

    private BiConsumer<Message, Integer> encounterSelection(MenuObjects objects) {
        return (m, n) -> mechanicMenu(encounter(n - 1), objects)
                .ifPresent(menu -> menu.display(m.getChannel()));
    }

    private Optional<Menu> mechanicMenu(Encounter encounter, MenuObjects objects) {
        String[] mechanicsNames = mechanicsNames(encounter, objects.guildObjects());

        if (mechanicsNames.length == 1) {
            return roleMenu(mechanic(0, encounter), encounter, objects);
        }

        return Optional.of(
                ordered(objects.member())
                        .setDescription(
                                message(
                                        "choose.mechanic",
                                        Arrays.asList(
                                                encounterName(encounter, objects.guildObjects())),
                                        objects.guildObjects().settings().locale(),
                                        objects.guildObjects().messageSource()))
                        .setChoices(mechanicsNames)
                        .setSelection(mechanicSelection(encounter, objects))
                        .build());
    }

    private BiConsumer<Message, Integer> mechanicSelection(
            Encounter encounter,
            MenuObjects objects) {
        return (m, n) -> roleMenu(mechanic(n - 1, encounter), encounter, objects)
                .ifPresent(menu -> menu.display(m.getChannel()));
    }

    private Optional<Menu> roleMenu(Mechanic mechanic, Encounter encounter, MenuObjects objects) {
        if (mechanic.roles() == 1) {
            setRole(0, mechanic, encounter, objects);

            return Optional.empty();
        }

        return Optional.of(
                ordered(objects.member()).setDescription(
                        message(
                                "choose.role",
                                Arrays.asList(
                                        mechanicName(mechanic, encounter, objects.guildObjects()),
                                        encounterName(encounter, objects.guildObjects())),
                                objects.guildObjects().settings().locale(),
                                objects.guildObjects().messageSource()))
                        .setChoices(rolesIds(mechanic))
                        .setSelection(roleSelection(mechanic, encounter, objects))
                        .build());
    }

    private BiConsumer<Message, Integer> roleSelection(
            Mechanic mechanic,
            Encounter encounter,
            MenuObjects objects) {
        return (m, n) -> setRole(n - 1, mechanic, encounter, objects);
    }

    private void setRole(int role, Mechanic mechanic, Encounter encounter, MenuObjects objects) {
        Locale locale = objects.guildObjects().settings().locale();
        
        if (!objects.action().isPresent()) {
            String message = message(
                    "no-action",
                    locale,
                    objects.guildObjects().messageSource());

            objects.event().reply(message);
            
            return;
        }
        
        String action = objects.action().get();

        if (action.equals(SET_ACTION)) {
            objects.guildObjects().messageSource().set(
                    codeBuilder.role(encounter, mechanic, role),
                    objects.member().getEffectiveName());
    
            String message = message(
                    "taken",
                    Arrays.asList(
                            objects.member().getEffectiveName(),
                            role(role),
                            mechanicName(mechanic, encounter, objects.guildObjects()),
                            encounterName(encounter, objects.guildObjects())),
                    locale,
                    objects.guildObjects().messageSource());
    
            objects.event().reply(message);
    
            VoiceChannel vc = objects.event().getMember().getVoiceState().getChannel();
            if (vc == null) {
                return;
            }
    
            AudioManager am = vc.getGuild().getAudioManager();
            am.openAudioConnection(vc);
    
            objects.guildObjects()
                    .player()
                    .play(message, locale, objects.guildObjects().settings().voice(locale));
        } else if (action.equals(UNSET_ACTION)) {
            objects.guildObjects().messageSource().unset(
                    codeBuilder.role(encounter, mechanic, role));
    
            String message = message(
                    "unset",
                    Arrays.asList(
                            role(role),
                            mechanicName(mechanic, encounter, objects.guildObjects()),
                            encounterName(encounter, objects.guildObjects())),
                    locale,
                    objects.guildObjects().messageSource());
    
            objects.event().reply(message);
        } else {
            String message = message(
                    "unknown-action",
                    locale,
                    objects.guildObjects().messageSource());

            objects.event().reply(message);
            
            return;
        }
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
