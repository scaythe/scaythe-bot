package com.scaythe.bot.tts;

import org.immutables.value.Value.Immutable;

@Immutable
public interface VoiceDescriptor {

    public String name();
    public String description();

    static VoiceDescriptor of(String name, String description) {
        return VoiceDescriptorImmutable.builder().name(name).description(description).build();
    }
}
