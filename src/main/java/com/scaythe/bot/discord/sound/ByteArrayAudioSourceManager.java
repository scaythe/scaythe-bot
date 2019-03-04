package com.scaythe.bot.discord.sound;

import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class ByteArrayAudioSourceManager extends ProbingAudioSourceManager {
    private static final String NAME = "byte-array-source";

    private final Map<String, byte[]> data = new HashMap<>();
    private final AtomicLong identifier = new AtomicLong();

    public ByteArrayAudioSourceManager() {
        super(MediaContainerRegistry.DEFAULT_REGISTRY);
    }

    @Override
    public String getSourceName() {
        return NAME;
    }

    public String register(byte[] bytes) {
        String key = Long.toString(identifier.getAndIncrement());

        this.data.put(key, bytes);

        return key;
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        if (data.containsKey(reference.getIdentifier())) {
            return handleLoadResult(detectContainer(reference));
        } else {
            return null;
        }
    }

    private MediaContainerDetectionResult detectContainer(AudioReference reference) {
        try (ByteArraySeekableInputStream inputStream = new ByteArraySeekableInputStream(data.get(
                reference.getIdentifier()))) {
            return new MediaContainerDetection(containerRegistry,
                    reference,
                    inputStream,
                    MediaContainerHints.from(null, null)).detectContainer();
        } catch (IOException e) {
            throw new FriendlyException("Failed to open file for reading.", SUSPICIOUS, e);
        }
    }

    @Override
    protected AudioTrack createTrack(AudioTrackInfo trackInfo,
            MediaContainerDescriptor containerTrackFactory) {
        byte[] bytes = this.data.get(trackInfo.identifier);
        data.remove(trackInfo.identifier);

        return new ByteArrayAudioTrack(trackInfo, bytes, containerTrackFactory, this);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
