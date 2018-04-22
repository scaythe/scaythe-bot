package com.scaythe.bot.encounter;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.scaythe.bot.config.encounter.EncounterConfig;
import com.scaythe.bot.config.encounter.MechanicConfig;
import com.scaythe.bot.config.encounter.WarningConfig;

@Component
public class EncounterConfigMapper {

    public Collection<Encounter> encounters(Collection<EncounterConfig> config) {
        return config.stream().map(this::encounter).collect(Collectors.toList());
    }

    public Encounter encounter(EncounterConfig config) {
        return EncounterImmutable.builder()
                .id(config.getId())
                .duration(config.getDuration())
                .addAllMechanics(mechanics(config.getMechanics()))
                .build();
    }

    public Collection<Mechanic> mechanics(Collection<MechanicConfig> config) {
        return config.stream().map(this::mechanic).collect(Collectors.toList());
    }

    public Mechanic mechanic(MechanicConfig config) {
        return MechanicImmutable.builder()
                .id(config.getId())
                .initialDelay(config.getInitialDelay())
                .period(config.getPeriod())
                .repeat(config.getRepeat())
                .duties(config.getDuties())
                .roles(config.getRoles())
                .addAllWarnings(warnings(config.getWarnings()))
                .build();
    }

    public Collection<Warning> warnings(Collection<WarningConfig> config) {
        return config.stream().map(this::warning).collect(Collectors.toList());
    }

    public Warning warning(WarningConfig config) {
        return WarningImmutable.builder().id(config.getId()).offset(config.getOffset()).build();
    }
}
