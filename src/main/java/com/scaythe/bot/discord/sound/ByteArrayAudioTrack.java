package com.scaythe.bot.discord.sound;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

public class ByteArrayAudioTrack extends DelegatedAudioTrack {
    private final byte[] data;
    private final MediaContainerDescriptor containerTrackFactory;
    private final ByteArrayAudioSourceManager sourceManager;

    public ByteArrayAudioTrack(AudioTrackInfo trackInfo,
            byte[] data,
            MediaContainerDescriptor containerTrackFactory,
            ByteArrayAudioSourceManager sourceManager) {
        super(trackInfo);

        this.data = data;

        this.containerTrackFactory = containerTrackFactory;
        this.sourceManager = sourceManager;
    }

    public MediaContainerDescriptor getContainerTrackFactory() {
        return containerTrackFactory;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        try (ByteArraySeekableInputStream inputStream = new ByteArraySeekableInputStream(data)) {
            processDelegate((InternalAudioTrack) containerTrackFactory.createTrack(trackInfo,
                    inputStream), localExecutor);
        }
    }

    @Override
    public AudioTrack makeClone() {
        return new ByteArrayAudioTrack(trackInfo, data, containerTrackFactory, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}
