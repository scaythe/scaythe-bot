package com.scaythe.bot.discord.command;

import java.util.Collections;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;

@CommandInfo(name = { "Language" }, description = "Set the bot language")
@Author("Scaythe")
@Component
public class LanguageCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.lang.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public LanguageCommand() {
        super(I18N_PREFIX);

        this.name = "lang";
        this.help = "set the bot language";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        String languageTag = event.getArgs().trim();

        if (languageTag.isEmpty()) {
            event.reply(
                    message("empty", guildObjects.config().locale(), guildObjects.messageSource()));
            return;
        }

        Locale locale = Locale.forLanguageTag(languageTag);

        guildObjects.config().locale(locale);

        event.reply(
                message(
                        "set",
                        Collections.singletonList(locale.toString()),
                        guildObjects.config().locale(),
                        guildObjects.messageSource()));
    }
}
