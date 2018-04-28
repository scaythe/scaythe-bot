package com.scaythe.bot.discord.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.jagrosh.jdautilities.menu.SelectionDialog;
import com.scaythe.bot.discord.guild.GuildObjects;
import com.scaythe.bot.tts.TtsException;
import com.scaythe.bot.tts.TtsService;
import com.scaythe.bot.tts.VoiceDescriptor;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

@CommandInfo(name = { "Voice" }, description = "Change the bot's voice for the current language")
@Author("Scaythe")
@Component
public class VoiceCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.voice.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final TtsService ttsService;
    private final EventWaiter eventWaiter;

    public VoiceCommand(TtsService ttsService, EventWaiter eventWaiter) {
        super(I18N_PREFIX);

        this.ttsService = ttsService;
        this.eventWaiter = eventWaiter;

        this.name = "voice";
        this.help = "change the bot's voice for the current language";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());
        Locale locale = guildObjects.settings().locale();

        try {
            List<VoiceDescriptor> voices = ttsService.voices(locale)
                    .stream()
                    .sorted(Comparator.comparing(VoiceDescriptor::name))
                    .collect(Collectors.toList());

            List<String> choices = Stream.concat(
                    voices.stream().map(v -> v.name() + "\n        " + v.description()),
                    Stream.of(message("default-choice", locale, guildObjects.messageSource())))

                    .collect(Collectors.toList());

            selector(event.getMember(), eventWaiter)
                    .setChoices(choices.stream().toArray(String[]::new))
                    .setSelectionConsumer(
                            voiceSelection(voices, event.getMember(), guildObjects, event))
                    .build()
                    .display(event.getChannel());
        } catch (TtsException e) {
            event.reply("error");
        }
    }

    private BiConsumer<Message, Integer> voiceSelection(
            List<VoiceDescriptor> voices,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return (m, n) -> setVoice(m, n - 1, voices, member, guildObjects, event);
    }

    private void setVoice(
            Message message,
            int i,
            List<VoiceDescriptor> voices,
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        Locale locale = guildObjects.settings().locale();

        if (i < voices.size()) {
            String voice = voices.get(i).name();

            guildObjects.settings().voice(locale, voice);

            event.reply(
                    message(
                            "chosen",
                            Arrays.asList(voice, locale.getDisplayName(locale)),
                            locale,
                            guildObjects.messageSource()));
        } else {
            guildObjects.settings().voiceUnset(locale);

            event.reply(
                    message(
                            "default",
                            Arrays.asList(locale.getDisplayName(locale)),
                            locale,
                            guildObjects.messageSource()));
        }

        delete(message);
    }

    public SelectionDialog.Builder selector(Member member, EventWaiter eventWaiter) {
        return new SelectionDialog.Builder().setUsers(member.getUser())
                .setText(member.getEffectiveName())
                .setEventWaiter(eventWaiter)
                .setCanceled(this::delete)
                .setSelectedEnds("> ", "")
                .setDefaultEnds("    ", "")
                .setTimeout(1, TimeUnit.HOURS);
    }

    private void delete(Message message) {
        message.delete().queue(v -> {}, t -> {});
    }
}
