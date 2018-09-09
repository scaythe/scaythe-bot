package com.scaythe.bot.encounter;

import java.util.Set;

import org.immutables.value.Value.Immutable;

@Immutable
public interface Mechanic {

    public String id();

    public int initialDelay();

    public int period();

    public int repeat();

    public int duties();

    public int roles();

    public Set<Warning> warnings();
}
