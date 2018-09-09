package com.scaythe.bot.encounter;

import org.immutables.value.Value.Immutable;

@Immutable
public interface Warning {

    public String id();

    public int offset();
}
