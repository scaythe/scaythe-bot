package com.scaythe.bot.encounter;

import java.util.Set;

import org.immutables.value.Value.Immutable;

@Immutable
public interface Encounter {
    public String id();
    
    public Set<Mechanic> mechanics();
    
    public int duration();
}
