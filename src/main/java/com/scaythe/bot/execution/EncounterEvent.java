package com.scaythe.bot.execution;

import org.immutables.value.Value.Immutable;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.encounter.Warning;

@Immutable
public interface EncounterEvent {
    public Encounter encounter();
    public Mechanic mechanic();
    public Warning warning();
    public int mechanicCount();
}
