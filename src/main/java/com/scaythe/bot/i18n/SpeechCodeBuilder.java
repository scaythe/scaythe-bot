package com.scaythe.bot.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.encounter.Warning;

@Component
public class SpeechCodeBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public String warning(Encounter encounter, Mechanic mechanic, Warning warning) {
        Collection<String> codeParts = codePrefix(encounter, mechanic);

        codeParts.add("warning");
        codeParts.add(warning.id());

        return joinCode(codeParts);
    }

    public String duty(Encounter encounter, Mechanic mechanic, int counter) {
        Collection<String> codeParts = codePrefix(encounter, mechanic);

        codeParts.add("duty");
        codeParts.add(position(counter, mechanic.duties()));

        return joinCode(codeParts);
    }

    public String role(Encounter encounter, Mechanic mechanic, int counter) {
        Collection<String> codeParts = codePrefix(encounter, mechanic);

        codeParts.add("role");
        codeParts.add(position(counter, mechanic.roles()));

        return joinCode(codeParts);
    }

    private List<String> codePrefix(Encounter encounter, Mechanic mechanic) {
        List<String> codeParts = new ArrayList<>();

        codeParts.add(encounter.id());
        codeParts.add(mechanic.id());

        return codeParts;
    }

    private String joinCode(Collection<String> codeParts) {
        return codeParts.stream().collect(Collectors.joining("."));
    }

    private String position(int counter, int possibleValues) {
        return Integer.toString(modulo(counter, possibleValues));
    }

    private int modulo(int operand, int operator) {
        if (operator < 1) {
            return 0;
        }

        return operand % operator;
    }
}
