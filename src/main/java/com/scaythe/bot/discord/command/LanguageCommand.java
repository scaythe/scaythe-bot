package com.scaythe.bot.discord.command;

import java.util.Collections;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.scaythe.bot.config.LanguageConfig;
import com.scaythe.bot.discord.guild.GuildObjects;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

@CommandInfo(name = { "Language" }, description = "Set the bot language")
@Author("Scaythe")
@Component
public class LanguageCommand extends ScaytheCommand {

    private static final String I18N_PREFIX = "discord.command.lang.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final LanguageConfig config;
    private final EventWaiter eventWaiter;

    public LanguageCommand(LanguageConfig config, EventWaiter eventWaiter) {
        super(I18N_PREFIX);

        this.config = config;
        this.eventWaiter = eventWaiter;

        this.name = "lang";
        this.help = "set the bot language";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        languageMenu(event.getMember(), guildObjects, event).display(event.getChannel());
    }

    private OrderedMenu.Builder ordered(Member member) {
        return ordered(member, eventWaiter);
    }

    private Menu languageMenu(Member member, GuildObjects guildObjects, CommandEvent event) {
        Locale locale = guildObjects.settings().locale();
        
        return ordered(member).setDescription(
                message("choose", locale, guildObjects.messageSource()))
                .setChoices(
                        config.getLocales()
                                .stream()
                                .map(l -> l.getDisplayName(locale) + (l.equals(locale) ? "*" : ""))
                                .toArray(String[]::new))
                .setSelection(languageSelection(member, guildObjects, event))
                .build();
    }

    private BiConsumer<Message, Integer> languageSelection(
            Member member,
            GuildObjects guildObjects,
            CommandEvent event) {
        return (m, n) -> setLanguage(config.getLocales().get(n - 1), guildObjects, event);
    }

    private void setLanguage(Locale locale, GuildObjects guildObjects, CommandEvent event) {
        guildObjects.settings().locale(locale);

        event.reply(
                message(
                        "set",
                        Collections.singletonList(
                                locale.getDisplayName(guildObjects.settings().locale())),
                        guildObjects.settings().locale(),
                        guildObjects.messageSource()));
    }
}
