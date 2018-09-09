package com.scaythe.bot.execution;

import org.springframework.stereotype.Component;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.i18n.EncounterMessageResolver;
import com.scaythe.bot.i18n.MessageResolver;

import reactor.core.publisher.Flux;

@Component
public class EncounterMessagePublisherBuilder {

    private final EncounterMessageResolver encounterMessageResolver;
    private final EncounterEventPublisherBuilder encounterEventPublisherBuilder;

    public EncounterMessagePublisherBuilder(
            EncounterMessageResolver encounterMessageResolver,
            EncounterEventPublisherBuilder encounterEventPublisherBuilder) {
        this.encounterMessageResolver = encounterMessageResolver;
        this.encounterEventPublisherBuilder = encounterEventPublisherBuilder;
    }

    public Flux<String> build(Encounter encounter, MessageResolver messageResolver) {
        return encounterEventPublisherBuilder.build(encounter)
                .map(e -> encounterMessageResolver.message(e, messageResolver));
    }

    public Flux<String> buildWithDelay(Encounter encounter, MessageResolver messageResolver) {
        return Flux
                .concat(buildDelay(encounter, messageResolver), build(encounter, messageResolver));
    }

    private Flux<String> buildDelay(Encounter encounter, MessageResolver messageResolver) {
        DelayMessageEmitter emitter = new DelayMessageEmitter(encounter, messageResolver);

        return Flux.create(emitter::start)
                .doOnCancel(emitter::dispose)
                .doOnError(t -> emitter.dispose());
    }
}
