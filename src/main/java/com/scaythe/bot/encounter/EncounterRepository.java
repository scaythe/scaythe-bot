package com.scaythe.bot.encounter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class EncounterRepository {

    private final Map<String, Encounter> encounters;

    public EncounterRepository(EncountersConfig config, EncounterConfigMapper configMapper) {
        this.encounters = configMapper.encounters(config.getList()).stream().collect(
                Collectors.toMap(Encounter::id, Function.identity()));
    }
    
    public Optional<Encounter> get(String id) {
        return Optional.ofNullable(encounters.get(id));
    }
    
    public Collection<Encounter> list() {
        return Collections.unmodifiableCollection(encounters.values());
    }
}
