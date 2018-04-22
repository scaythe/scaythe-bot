package com.scaythe.bot.discord.command;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;

import net.dv8tion.jda.core.EmbedBuilder;

@CommandInfo(name = { "Set" }, description = "override speech text")
@Author("Scaythe")
@Component
public class SetI18nCommand extends I18nCommand {

    private static final String I18N_PREFIX = "discord.command.set.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public SetI18nCommand() {
        super(I18N_PREFIX);

        this.name = "set";
        this.help = "override speech text";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());
        
        String args = event.getArgs().trim();
        
        if (args.isEmpty()) {
            event.reply(message("empty", guildObjects.config().locale(), guildObjects.messageSource()));
            return;
        }
        
        List<String> argsList = Arrays.asList(args.split("\\s+", 2));

        EmbedBuilder builder = new EmbedBuilder();
        
        String code = argsList.get(0);
        if (argsList.size() == 1) {
            String old = guildObjects.messageSource().unset(code, guildObjects.config().locale());
            
            if (old == null) {
                event.reply(message("not-set", guildObjects.config().locale(), guildObjects.messageSource()));
                return;
            }
            
            String value = guildObjects.messageSource().getMessage(code, null, guildObjects.config().locale());
            builder.addField(code, message("unset", Arrays.asList(value, old), guildObjects.config().locale(), guildObjects.messageSource()), false);
        } else {
            String value = argsList.get(1);
    
            String old = guildObjects.messageSource().set(value, code, guildObjects.config().locale());
            
            if (old == null) {
                builder.addField(code, message("set", Arrays.asList(value), guildObjects.config().locale(), guildObjects.messageSource()), false);
            } else {
                builder.addField(code, message("replace", Arrays.asList(value, old), guildObjects.config().locale(), guildObjects.messageSource()), false);
            }
        }
        
        event.reply(builder.build());
    }
}
