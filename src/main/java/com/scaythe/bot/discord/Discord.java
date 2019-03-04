package com.scaythe.bot.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.scaythe.bot.discord.guild.GuildManager;
import com.scaythe.bot.discord.sound.ByteArrayAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;
import java.util.List;

@Configuration
@ComponentScan
public class Discord {

    @Bean
    public JDA jda(CommandClient commandClient, EventWaiter eventWaiter, DiscordConfig config)
            throws LoginException, InterruptedException {
        return new JDABuilder(config.getToken()).addEventListener(commandClient)
                .addEventListener(eventWaiter)
                .build()
                .awaitReady();
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
    public ByteArrayAudioSourceManager audioSourceManager() {
        return new ByteArrayAudioSourceManager();
    }

    @Bean
    public AudioPlayerManager playerManager(ByteArrayAudioSourceManager audioSourceManager) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(audioSourceManager);
        return playerManager;
    }

    @Bean(destroyMethod = "")
    public EventWaiter eventWaiter() {
        return new EventWaiter();
    }
}
