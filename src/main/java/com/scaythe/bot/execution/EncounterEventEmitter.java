package com.scaythe.bot.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.encounter.Warning;

import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class EncounterEventEmitter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Encounter encounter;
    private final Scheduler scheduler;
    private final Map<Mechanic, AtomicInteger> counters = new HashMap<>();

    public EncounterEventEmitter(Encounter encounter) {
        this.encounter = encounter;
        this.scheduler = Schedulers.newSingle(encounter.id());
    }

    public void start(FluxSink<EncounterEvent> sink) {
        log.debug("starting emitter for {}", encounter);

        initCounters(encounter);
        scheduleEncounter(encounter, sink);
    }

    private void stop(FluxSink<EncounterEvent> sink) {
        log.info("stopping");

        sink.complete();

        dispose();
    }

    public void dispose() {
        log.info("disposing");

        scheduler.dispose();
    }

    private void initCounters(Encounter encounter) {
        encounter.mechanics().forEach(m -> counters.put(m, new AtomicInteger()));

        log.debug("initialized counters : {}", counters);
    }

    private void scheduleEncounter(Encounter encounter, FluxSink<EncounterEvent> sink) {
        log.debug("scheduling encounter : {}", encounter.id());

        encounter.mechanics().forEach(m -> scheduleMechanic(m, encounter, sink));

        if (encounter.duration() > 0) {
            scheduler.schedule(() -> stop(sink), encounter.duration(), TimeUnit.SECONDS);
        }
    }

    private void scheduleMechanic(
            Mechanic mechanic,
            Encounter encounter,
            FluxSink<EncounterEvent> sink) {
        log.debug("scheduling mechanic : {}", mechanic.id());

        scheduleMechanicCounterIncrement(mechanic);

        mechanic.warnings().forEach(w -> scheduleWarning(w, mechanic, encounter, sink));
    }

    private void scheduleMechanicCounterIncrement(Mechanic mechanic) {
        int period = mechanic.period();

        if (period > 0) {
            int initialDelay = mechanic.initialDelay()
                    + mechanic.warnings().stream().mapToInt(Warning::offset).max().orElse(0)
                    + 1;

            while (initialDelay < 0) {
                initialDelay += period;
                counters.get(mechanic).incrementAndGet();
            }

            log.debug(
                    "schedule mechanic increment : {} : {} : {}",
                    initialDelay,
                    period,
                    mechanic.id());

            scheduler.schedulePeriodically(
                    counters.get(mechanic)::incrementAndGet,
                    initialDelay,
                    period,
                    TimeUnit.SECONDS);
        }
    }

    private void scheduleWarning(
            Warning warning,
            Mechanic mechanic,
            Encounter encounter,
            FluxSink<EncounterEvent> sink) {
        log.debug("scheduling warning : {}", warning.id());

        int period = mechanic.period();
        int initialDelay = mechanic.initialDelay() + warning.offset();

        if (period > 0) {
            while (initialDelay < 0) {
                initialDelay += period;
            }

            scheduler.schedulePeriodically(
                    () -> sendToSink(warning, mechanic, encounter, sink),
                    initialDelay,
                    period,
                    TimeUnit.SECONDS);
        } else {
            scheduler.schedule(
                    () -> sendToSink(warning, mechanic, encounter, sink),
                    initialDelay,
                    TimeUnit.SECONDS);
        }
    }

    private void sendToSink(
            Warning warning,
            Mechanic mechanic,
            Encounter encounter,
            FluxSink<EncounterEvent> sink) {
        int counter = counters.get(mechanic).get();

        if (mechanic.repeat() < 1 || counter < mechanic.repeat()) {
            sink.next(buildEvent(warning, mechanic, encounter, counter));
        }
    }

    private EncounterEvent buildEvent(
            Warning warning,
            Mechanic mechanic,
            Encounter encounter,
            int counter) {
        log.debug(
                "building event {} {} {} {}",
                counter,
                encounter.id(),
                mechanic.id(),
                warning.id());

        return EncounterEventImmutable.builder()
                .encounter(encounter)
                .mechanic(mechanic)
                .warning(warning)
                .mechanicCount(counter)
                .build();
    }
}
