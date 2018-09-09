package com.scaythe.bot;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.EncounterRepository;
import com.scaythe.bot.encounter.Encounters;
import com.scaythe.bot.execution.EncounterMessagePublisherBuilder;
import com.scaythe.bot.execution.Execution;
import com.scaythe.bot.i18n.I18n;
import com.scaythe.bot.i18n.MessageResolver;

import reactor.core.scheduler.Schedulers;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({ Encounters.class, Execution.class, I18n.class })
public class EncounterFluxTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EncounterRepository repo;
    private final EncounterMessagePublisherBuilder builder;
    private final MessageSource messageSource;

    public EncounterFluxTest(
            EncounterRepository repo,
            EncounterMessagePublisherBuilder builder,
            MessageSource messageSource) {
        this.repo = repo;
        this.builder = builder;
        this.messageSource = messageSource;
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx
                = new SpringApplicationBuilder(EncounterFluxTest.class).web(WebApplicationType.NONE)
                        .run(args)) {
            Thread keepAlive = Thread.currentThread();

            ctx.getBean(EncounterFluxTest.class).execute(keepAlive);

            try {
                keepAlive.join();
            } catch (InterruptedException e) {}
        }
    }

    private void execute(Thread keepAlive) {
        Locale locale = Locale.FRENCH;
        Encounter encounter = repo.get("dhuum").get();

        builder.buildWithDelay(
                encounter,
                (c, a) -> MessageResolver.message(c, a, locale, messageSource))
                .elapsed()
                .doOnNext(e -> log.info("{} / {}", e.getT1(), e.getT2()))
                .doOnTerminate(keepAlive::interrupt)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }
}
