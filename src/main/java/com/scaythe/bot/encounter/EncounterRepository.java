package com.scaythe.bot.encounter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.scaythe.bot.config.Gw2Config;

@Service
public class EncounterRepository {

    private final Map<String, Encounter> encounters;

    public EncounterRepository(Gw2Config config, EncounterConfigMapper configMapper) {
        this.encounters = configMapper.encounters(config.getEncounters()).stream().collect(
                Collectors.toMap(Encounter::id, Function.identity()));
    }
    
    public Optional<Encounter> get(String id) {
        return Optional.ofNullable(encounters.get(id));
    }
    
    public Collection<Encounter> list() {
        return Collections.unmodifiableCollection(encounters.values());
    }
}
