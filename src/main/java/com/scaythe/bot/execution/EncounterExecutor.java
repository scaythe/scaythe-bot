package com.scaythe.bot.execution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.encounter.Warning;

public class EncounterExecutor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<Mechanic, Counter> counters = new HashMap<>();
    private final Collection<Consumer<EncounterEvent>> listeners;

    public EncounterExecutor(Encounter encounter, Collection<Consumer<EncounterEvent>> listeners) {
        this.listeners = listeners.stream().collect(Collectors.toList());

        log.debug("starting executor for {}", encounter);

        initCounters(encounter);
        scheduleEncounter(encounter);
    }

    public void stop() {
        log.debug("stopping");
        executor.shutdownNow();
    }

    private void initCounters(Encounter encounter) {
        encounter.mechanics().forEach(m -> counters.put(m, new Counter()));

        log.debug("initialized counters : {}", counters);
    }

    private void scheduleEncounter(Encounter encounter) {
        log.debug("scheduling encounter : {}", encounter.id());

        encounter.mechanics().forEach(m -> scheduleMechanic(m, encounter));

        if (encounter.duration() > 0) {
            executor.schedule(this::stop, encounter.duration(), TimeUnit.SECONDS);
        }
    }

    private void scheduleMechanic(Mechanic mechanic, Encounter encounter) {
        log.debug("scheduling mechanic : {}", mechanic.id());

        scheduleMechanicCounterIncrement(mechanic);

        mechanic.warnings().forEach(w -> scheduleWarning(w, mechanic, encounter));
    }

    private void scheduleMechanicCounterIncrement(Mechanic mechanic) {
        int period = mechanic.period();

        if (period > 0) {
            int initialDelay = mechanic.initialDelay()
                    + mechanic.warnings().stream().mapToInt(Warning::offset).max().orElse(0)
                    + 1;

            while (initialDelay < 0) {
                initialDelay += period;
                counters.get(mechanic).increment();
            }

            log.debug(
                    "schedule mechanic increment : {} : {} : {}",
                    initialDelay,
                    period,
                    mechanic.id());

            executor.scheduleAtFixedRate(
                    counters.get(mechanic)::increment,
                    initialDelay,
                    period,
                    TimeUnit.SECONDS);
        }
    }

    private void scheduleWarning(Warning warning, Mechanic mechanic, Encounter encounter) {
        log.debug("scheduling warning : {}", warning.id());

        int period = mechanic.period();
        int initialDelay = mechanic.initialDelay() + warning.offset();

        if (period > 0) {
            while (initialDelay < 0) {
                initialDelay += period;
            }

            executor.scheduleAtFixedRate(
                    () -> sendToListeners(warning, mechanic, encounter),
                    initialDelay,
                    period,
                    TimeUnit.SECONDS);
        } else {
            executor.schedule(
                    () -> sendToListeners(warning, mechanic, encounter),
                    initialDelay,
                    TimeUnit.SECONDS);
        }
    }

    private EncounterEvent buildEvent(Warning warning, Mechanic mechanic, Encounter encounter, int counter) {
        log.debug("building event {} {} {} {}", counter, encounter.id(), mechanic.id(), warning.id());
        
        return EncounterEventImmutable.builder()
                .encounter(encounter)
                .mechanic(mechanic)
                .warning(warning)
                .mechanicCount(counter)
                .build();
    }

    private void sendToListeners(Warning warning, Mechanic mechanic, Encounter encounter) {
        int counter = counters.get(mechanic).get();
        if (mechanic.repeat() < 1 || counter < mechanic.repeat()) {
            listeners.stream()
            .forEach(l -> l.accept(buildEvent(warning, mechanic, encounter, counter)));
        }
    }
}
