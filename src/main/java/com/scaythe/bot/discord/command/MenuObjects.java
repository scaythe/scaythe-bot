package com.scaythe.bot.discord.command;

import java.util.Optional;

import org.immutables.value.Value.Immutable;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.scaythe.bot.discord.guild.GuildObjects;

import net.dv8tion.jda.core.entities.Member;

@Immutable
public interface MenuObjects {
    public Member member();
    public GuildObjects guildObjects();
    public CommandEvent event();
    public Optional<String> action();
}
