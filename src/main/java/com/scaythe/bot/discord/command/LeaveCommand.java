package com.scaythe.bot.discord.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;

import reactor.core.Disposable;

@CommandInfo(name = { "Leave" }, description = "Disconnect from the user's server")
@Author("Scaythe")
@Component
public class LeaveCommand extends Command {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public LeaveCommand() {
        this.name = "leave";
        this.help = "disconnect from the user's server";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());

        Disposable player = guildObjects.currentPlayer();

        if (player != null) {
            player.dispose();
            guildObjects.currentPlayer(null);
        }
        
        event.getGuild().getAudioManager().closeAudioConnection();
    }
}
