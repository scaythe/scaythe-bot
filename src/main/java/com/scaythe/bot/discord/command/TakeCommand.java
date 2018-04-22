package com.scaythe.bot.discord.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;
import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.EncounterRepository;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.i18n.SpeechCodeBuilder;

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

    public TakeCommand(
            EncounterRepository encounterRepository,
            SpeechCodeBuilder codeBuilder) {
        super(I18N_PREFIX);
        
        this.encounterRepository = encounterRepository;
        this.codeBuilder = codeBuilder;

        this.name = "take";
        this.help = "claim a role";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        String args = event.getArgs().trim();

        if (args.isEmpty()) {
            event.reply(
                    message("empty", guildObjects.config().locale(), guildObjects.messageSource()));
            return;
        }

        List<String> argsList = Arrays.asList(args.split("\\s+", 3));

        if (argsList.size() < 3) {
            event.reply(
                    message("empty", guildObjects.config().locale(), guildObjects.messageSource()));
            return;
        }

        String encounterArg = argsList.get(0);
        String mechanicArg = argsList.get(1);

        Optional<Encounter> encounterOpt = encounterRepository.get(encounterArg);

        if (!encounterOpt.isPresent()) {
            event.reply(
                    message(
                            "encounter-not-found",
                            Collections.singletonList(encounterArg),
                            guildObjects.config().locale(),
                            guildObjects.messageSource()));
            return;
        }

        Encounter encounter = encounterOpt.get();

        Optional<Mechanic> mechanicOpt = mechanic(mechanicArg, encounter);

        if (!mechanicOpt.isPresent()) {
            event.reply(
                    message(
                            "mechanic-not-found",
                            Arrays.asList(mechanicArg, encounterArg),
                            guildObjects.config().locale(),
                            guildObjects.messageSource()));
            return;
        }
        
        Mechanic mechanic = mechanicOpt.get();

        String roleArg = argsList.get(2);
        Optional<Integer> roleOpt = role(roleArg, mechanic);

        if (!roleOpt.isPresent()) {
            event.reply(
                    message(
                            "role-not-found",
                            Arrays.asList(roleArg, mechanicArg, encounterArg),
                            guildObjects.config().locale(),
                            guildObjects.messageSource()));
            return;
        }
        
        Integer role = roleOpt.get();
        
        String code = codeBuilder.role(encounter, mechanic, role);
        
        String memberName = event.getMember().getEffectiveName();
        
        guildObjects.messageSource().set(memberName, code, guildObjects.config().locale());
        
        String message = message(
                "taken",
                Arrays.asList(memberName, role(role), mechanic.id(), encounter.id()),
                guildObjects.config().locale(),
                guildObjects.messageSource());
        
        event.reply(message);
        
        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        if (vc == null) {
            return;
        }
        AudioManager am = vc.getGuild().getAudioManager();

        am.openAudioConnection(vc);
        
        guildObjects.player().play(message, guildObjects.config().locale());
    }
}
