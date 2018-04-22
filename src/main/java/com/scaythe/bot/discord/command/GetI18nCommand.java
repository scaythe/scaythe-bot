package com.scaythe.bot.discord.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;
import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.EncounterRepository;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.encounter.Warning;
import com.scaythe.bot.i18n.ConfigurableMessageSource;
import com.scaythe.bot.i18n.SpeechCodeBuilder;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

@CommandInfo(name = { "Get" }, description = "View speech text possible codes and defined values")
@Author("Scaythe")
@Component
public class GetI18nCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.get.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EncounterRepository encounterRepository;
    private final SpeechCodeBuilder codeBuilder;
    private final MessageSource source;

    public GetI18nCommand(
            EncounterRepository encounterRepository,
            SpeechCodeBuilder codeBuilder,
            MessageSource source) {
        super(I18N_PREFIX);

        this.encounterRepository = encounterRepository;
        this.codeBuilder = codeBuilder;
        this.source = source;

        this.name = "get";
        this.help = "view speech text possible codes and defined values";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        String code = event.getArgs().trim();
        
        List<Field> fields = new ArrayList<>();

        if (code.isEmpty()) {
            codes().stream()
                    .map(
                            c -> configLine(
                                    c,
                                    guildObjects.messageSource(),
                                    guildObjects.config().locale()))
                    .forEach(fields::add);
        } else {
            fields.add(configLine(code, guildObjects.messageSource(), guildObjects.config().locale()));
        }
        
        for (Collection<Field> fieldsPart : split(fields, 20)) {
            EmbedBuilder builder = new EmbedBuilder();
            
            fieldsPart.forEach(builder::addField);
            
            event.reply(builder.build());
        }
        
        return;
    }

    private List<String> codes() {
        List<String> codes = new ArrayList<>();

        for (Encounter encounter : encounterRepository.list()) {
            for (Mechanic mechanic : encounter.mechanics()) {
                for (Warning warning : mechanic.warnings()) {
                    codes.add(codeBuilder.warning(encounter, mechanic, warning));
                }

                for (int i = 0; i < mechanic.duties(); i++) {
                    codes.add(codeBuilder.duty(encounter, mechanic, i));
                }

                for (int i = 0; i < mechanic.roles(); i++) {
                    codes.add(codeBuilder.role(encounter, mechanic, i));
                }
            }
        }

        return codes;
    }

    private Field configLine(String code, ConfigurableMessageSource guildSource, Locale locale) {
        String defaultValue;
        try {
            defaultValue = source.getMessage(code, null, locale);
        } catch (NoSuchMessageException e) {
            return new Field(code, message("unknown-code", locale, guildSource), false);
        }

        if (guildSource.isSet(code, locale)) {
            String value = message(
                    "line-set",
                    Arrays.asList(guildSource.getMessage(code, null, locale), defaultValue),
                    locale,
                    guildSource);
            return new Field(code, value, false);
        }
        
        return new Field(code, message("line-default", Arrays.asList(defaultValue), locale, guildSource), false);
    }
}
