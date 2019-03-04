package com.scaythe.bot.discord.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedFilePlayer extends AudioEventAdapter implements AudioLoadResultHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AudioPlayer player;
    private final Queue<AudioTrack> queue = new LinkedList<>();

    public QueuedFilePlayer(AudioPlayer player) {
        this.player = player;

        player.addListener(this);
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        log.debug("trackLoaded");
        
        addToQueue(track);

        playNext();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        log.debug("playlistLoaded");
        
        for (AudioTrack track : playlist.getTracks()) {
            addToQueue(track);
        }
        
        playNext();
    }

    @Override
    public void noMatches() {
        log.error("no match");
    }

    @Override
    public void loadFailed(FriendlyException e) {
        log.warn("problem loading track :  {}", e.getMessage());
        log.trace("", e);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.debug("onTrackEnd");

        if (endReason.mayStartNext) {
            log.debug("mayStartNext");
            
            playNext();
        }

//        deleteTrackFile(track);
    }

    @Override
    public void onTrackException(
            AudioPlayer player,
            AudioTrack track,
            FriendlyException exception) {
        log.debug("onTrackException");
    }
    
    synchronized private void addToQueue(AudioTrack track) {
        log.debug("addToQueue");
        queue.add(track);
    }

    synchronized private void playNext() {
        log.debug("playNext");
        if (!queue.isEmpty()) {
            log.debug("startTrack");
            boolean started = player.startTrack(queue.peek(), true);

            log.debug("started ? {}", started);
            
            if (started) {
                queue.remove();
            }
        } else {
            log.debug("queue empty");
        }
    }

//    private void deleteTrackFile(AudioTrack track) {
//        Path path = Paths.get(track.getIdentifier());
//        try {
//            fileService.deleteFile(path);
//        } catch (IOException e) {
//            log.warn("problem deleting file : {}", path);
//        }
//    }
}
