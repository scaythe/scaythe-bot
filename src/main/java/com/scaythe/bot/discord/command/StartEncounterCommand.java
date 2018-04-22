package com.scaythe.bot.discord.command;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;
import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.EncounterRepository;
import com.scaythe.bot.execution.EncounterPlayer;
import com.scaythe.bot.i18n.SpeechMessageResolver;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

@CommandInfo(name = { "start" }, description = "Starts an encounter")
@Author("Scaythe")
@Component
public class StartEncounterCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.start.";

    private final EncounterRepository encounterRepository;
    private final SpeechMessageResolver speechMessageResolver;

    @Autowired
    public StartEncounterCommand(
            EncounterRepository encounterRepository,
            SpeechMessageResolver speechMessageResolver) {
        super(I18N_PREFIX);

        this.encounterRepository = encounterRepository;
        this.speechMessageResolver = speechMessageResolver;

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

        String eventId = event.getArgs().trim();

        if (eventId.isEmpty()) {
            String code;
            if (player == null) {
                code = "empty";
            } else {
                code = "stopped";
            }
            
            event.reply(
                    message(
                            code,
                            guildObjects.config().locale(),
                            guildObjects.messageSource()));
            return;
        }

        Optional<Encounter> encounterOpt = encounterRepository.get(eventId);

        if (!encounterOpt.isPresent()) {
            event.reply(
                    message(
                            "not-found",
                            Collections.singletonList(eventId),
                            guildObjects.config().locale(),
                            guildObjects.messageSource()));
            return;
        }

        Member member = event.getMember();
        VoiceChannel vc = member.getVoiceState().getChannel();
        if (vc == null) {
            return;
        }
        AudioManager am = vc.getGuild().getAudioManager();

        am.openAudioConnection(vc);

        Encounter encounter = encounterOpt.get();

        guildObjects.config()
                .player(new EncounterPlayer(encounter, guildObjects, speechMessageResolver));

        event.reply(
                message(
                        "started",
                        Collections.singletonList(eventId),
                        guildObjects.config().locale(),
                        guildObjects.messageSource()));
    }
}
