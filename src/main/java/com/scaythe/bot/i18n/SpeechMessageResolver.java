package com.scaythe.bot.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.execution.EncounterEvent;

@Component
public class SpeechMessageResolver {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SpeechCodeBuilder codeBuilder;

    public SpeechMessageResolver(SpeechCodeBuilder codeBuilder) {
        this.codeBuilder = codeBuilder;
    }

    public String message(EncounterEvent event, Locale locale, MessageSource source) {
        return MessageResolver.message(
                codeBuilder.warning(event.encounter(), event.mechanic(), event.warning()),
                args(event.encounter(), event.mechanic(), event.mechanicCount(), locale, source),
                locale,
                source);
    }

    private List<String> args(
            Encounter encounter,
            Mechanic mechanic,
            int counter,
            Locale locale,
            MessageSource source) {
        List<String> args = new ArrayList<>();

        if (mechanic.duties() != 0) {
            args.add(
                    MessageResolver.message(
                            codeBuilder.duty(encounter, mechanic, counter),
                            locale,
                            source));
        }

        if (mechanic.roles() != 0) {
            args.add(
                    MessageResolver.message(
                            codeBuilder.role(encounter, mechanic, counter),
                            locale,
                            source));
        }

        return args;
    }
}
