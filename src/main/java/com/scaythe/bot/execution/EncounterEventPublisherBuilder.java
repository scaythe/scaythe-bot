package com.scaythe.bot.execution;

import org.springframework.stereotype.Component;

import com.scaythe.bot.encounter.Encounter;

import reactor.core.publisher.Flux;

@Component
public class EncounterEventPublisherBuilder {

    public Flux<EncounterEvent> build(Encounter encounter) {
        EncounterEventEmitter emitter = new EncounterEventEmitter(encounter);

        return Flux.create(emitter::start)
                .doOnCancel(emitter::dispose)
                .doOnError(t -> emitter.dispose());
    }
}
