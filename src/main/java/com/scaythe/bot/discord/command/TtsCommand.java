package com.scaythe.bot.discord.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.scaythe.bot.discord.guild.GuildObjects;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

@CommandInfo(name = { "TTS" }, description = "Reads the provided text")
@Author("Scaythe")
@Component
public class TtsCommand extends Command {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public TtsCommand() {
        this.name = "tts";
        this.help = "reads the provided text";
        this.aliases = new String[] { "s" };
    }

    @Override
    protected void execute(CommandEvent event) {
        Member member = event.getMember();
        VoiceChannel vc = member.getVoiceState().getChannel();
        if (vc == null) {
            return;
        }
        AudioManager am = vc.getGuild().getAudioManager();

        am.openAudioConnection(vc);

        String text = event.getArgs();

        log.info("saying {}", text);
        
        GuildObjects guildObjects = event.getClient().getSettingsFor(event.getGuild());
        
        guildObjects.player().play(text, guildObjects.config().locale());
    }
}
