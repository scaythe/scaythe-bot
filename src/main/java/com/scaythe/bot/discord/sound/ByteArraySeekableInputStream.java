package com.scaythe.bot.discord.sound;

import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ByteArraySeekableInputStream extends SeekableInputStream {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ByteArrayInputStream bais;

    private long position;


    public ByteArraySeekableInputStream(byte[] data) {
        super(data.length, data.length);

        this.bais = new ByteArrayInputStream(data);
        this.position = 0;
    }

    @Override
    public int read() throws IOException {
        int result = bais.read();
        if (result >= 0) {
            position++;
        }

        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        int read = bais.read(b, off, len);
        position += read;
        return read;
    }

    @Override
    public long skip(long n) {
        long skipped = bais.skip(n);
        position += skipped;
        return skipped;
    }

    @Override
    public int available() {
        return bais.available();
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        bais.close();
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public boolean canSeekHard() {
        return true;
    }

    @Override
    public List<AudioTrackInfoProvider> getTrackInfoProviders() {
        return Collections.emptyList();
    }

    @Override
    protected void seekHard(long position) {
        bais.reset();
        bais.skip(position);
        this.position = position;
    }
}
