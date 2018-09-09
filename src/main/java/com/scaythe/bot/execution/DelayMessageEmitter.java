package com.scaythe.bot.execution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.i18n.MessageResolver;

import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class DelayMessageEmitter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Encounter encounter;
    private final MessageResolver messageResolver;
    private final Scheduler scheduler;

    public DelayMessageEmitter(Encounter encounter, MessageResolver messageResolver) {
        this.encounter = encounter;
        this.messageResolver = messageResolver;
        this.scheduler = Schedulers.newSingle(encounter.id() + "-delay");
    }

    public void start(FluxSink<String> sink) {
        scheduler.schedule(
                () -> sendToSink(
                        "delayed-start.launched",
                        Arrays.asList(encounterName(encounter)),
                        sink));
        scheduler.schedule(() -> sendToSink("delayed-start.3", sink), 3, TimeUnit.SECONDS);
        scheduler.schedule(() -> sendToSink("delayed-start.2", sink), 4, TimeUnit.SECONDS);
        scheduler.schedule(() -> sendToSink("delayed-start.1", sink), 5, TimeUnit.SECONDS);
        scheduler.schedule(() -> {
            sendToSink("delayed-start.go", sink);
            stop(sink);
        }, 6, TimeUnit.SECONDS);
    }

    private void stop(FluxSink<String> sink) {
        log.info("stopping");

        sink.complete();

        dispose();
    }

    public void dispose() {
        log.info("disposing");

        scheduler.dispose();
    }

    private void sendToSink(String code, FluxSink<String> sink) {
        sendToSink(code, Collections.emptyList(), sink);
    }

    private void sendToSink(String code, List<String> args, FluxSink<String> sink) {
        sink.next(messageResolver.resolve(code, args));
    }

    private String encounterName(Encounter encounter) {
        return messageResolver.resolve(encounter.id() + ".name");
    }
}
