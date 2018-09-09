package com.scaythe.bot.i18n;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.execution.EncounterEvent;

@Component
public class EncounterMessageResolver {

    private final EncounterCodeBuilder codeBuilder;

    public EncounterMessageResolver(EncounterCodeBuilder codeBuilder) {
        this.codeBuilder = codeBuilder;
    }

    public String message(EncounterEvent event, MessageResolver messageResolver) {
        String code = codeBuilder.warning(event.encounter(), event.mechanic(), event.warning());
        List<String> args
                = args(event.encounter(), event.mechanic(), event.mechanicCount(), messageResolver);

        return messageResolver.resolve(code, args);
    }

    private List<String> args(
            Encounter encounter,
            Mechanic mechanic,
            int counter,
            MessageResolver messageResolver) {
        List<String> args = new ArrayList<>();

        if (mechanic.duties() != 0) {
            args.add(messageResolver.resolve(codeBuilder.duty(encounter, mechanic, counter)));
        }

        if (mechanic.roles() != 0) {
            args.add(messageResolver.resolve(codeBuilder.role(encounter, mechanic, counter)));
        }

        return args;
    }
}
