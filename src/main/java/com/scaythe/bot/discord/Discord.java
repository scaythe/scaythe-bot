package com.scaythe.bot.discord;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.scaythe.bot.config.DiscordConfig;
import com.scaythe.bot.discord.guild.GuildManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

@Configuration
public class Discord {

    @Bean
    public JDA jda(CommandClient commandClient, DiscordConfig config)
            throws LoginException, InterruptedException {
        return new JDABuilder(AccountType.BOT).setToken(config.getToken())
                .addEventListener(commandClient)
                .buildBlocking();
    }

    @Bean
    public CommandClient commandClient(
            List<Command> commands,
            DiscordConfig config,
            GuildManager guildManager) {
        return new CommandClientBuilder().setGuildSettingsManager(guildManager)
                .setOwnerId(config.getOwner())
                .setPrefix(config.getPrefix())
                .addCommands(commands.stream().toArray(Command[]::new))
                .addCommand(new PingCommand())
                .build();
    }

    @Bean
    public AudioPlayerManager playerManager() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        return playerManager;
    }
}