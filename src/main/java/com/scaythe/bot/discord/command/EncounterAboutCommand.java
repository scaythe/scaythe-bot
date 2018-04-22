package com.scaythe.bot.discord.command;

import java.awt.Color;

import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.examples.command.AboutCommand;

import net.dv8tion.jda.core.Permission;

@Component
public class EncounterAboutCommand extends AboutCommand {
    private static final Color COLOR = Color.CYAN;
    private static final String DESCRIPTION = "a bot for GW2 raid bosses announcements";
    private static final String[] FEATURES = {};

    public EncounterAboutCommand() {
        super(COLOR, DESCRIPTION, FEATURES, new Permission[0]);
    }
}
