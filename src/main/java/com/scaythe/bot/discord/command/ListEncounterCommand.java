package com.scaythe.bot.discord.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;
import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.EncounterRepository;

import net.dv8tion.jda.core.entities.Guild;

@CommandInfo(name = { "List Encounters" }, description = "List available encounters")
@Author("Scaythe")
@Component
public class ListEncounterCommand extends ScaytheCommand {
    
    private static final String I18N_PREFIX = "discord.command.list.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EncounterRepository encounterRepository;
    private final MessageSource source;

    public ListEncounterCommand(EncounterRepository encounterRepository, MessageSource source) {
        super(I18N_PREFIX);
        
        this.encounterRepository = encounterRepository;
        this.source = source;
        
        this.name = "list";
        this.help = "list available encounters";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        Guild guild = event.getGuild();
        Locale locale;
        MessageSource source;
        
        if (guild != null) {
            GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());
            locale = guildObjects.config().locale();
            source = guildObjects.messageSource();
        } else {
            locale = Locale.ENGLISH;
            source = this.source;
        }
        
        List<String> messageParts = new ArrayList<>();
        
        messageParts.add(message("val", locale, source));
        
        encounterRepository.list().stream().map(Encounter::id).map(e -> "- " + e).forEach(messageParts::add);
        
        event.reply(messageParts.stream().collect(Collectors.joining("\n")));
    }
}
