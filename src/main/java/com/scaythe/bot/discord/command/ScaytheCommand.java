package com.scaythe.bot.discord.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.scaythe.bot.encounter.Encounter;
import com.scaythe.bot.encounter.Mechanic;
import com.scaythe.bot.i18n.MessageResolver;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

public abstract class ScaytheCommand extends Command {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String i18nPrefix;

    public ScaytheCommand(String i18nPrefix) {
        this.i18nPrefix = i18nPrefix;
    }

    public Logger log() {
        return log;
    }

    public String message(String code, List<String> args, Locale locale, MessageSource source) {
        return MessageResolver.message(i18nPrefix + code, args, locale, source);
    }

    public String message(String code, Locale locale, MessageSource source) {
        return MessageResolver.message(i18nPrefix + code, locale, source);
    }

    public static <T> Collection<Collection<T>> split(Collection<T> collection, int max) {
        Collection<Collection<T>> result = new ArrayList<>();

        List<T> remaining = new ArrayList<>(collection);

        while (!remaining.isEmpty()) {
            List<T> sublist = remaining.subList(0, Math.min(max, remaining.size()));
            result.add(new ArrayList<>(sublist));

            sublist.clear();
        }

        return result;
    }

    public Optional<Mechanic> mechanic(String mechanic, Encounter encounter) {
        return encounter.mechanics().stream().filter(m -> m.id().equals(mechanic)).findFirst();
    }

    public Optional<Integer> role(String role, Mechanic mechanic) {
        try {
            int n = Integer.parseInt(role) - 1;
            if (n < mechanic.roles()) {
                return Optional.of(n);
            }
        } catch (NumberFormatException e) {}

        return Optional.empty();
    }

    public String role(int role) {
        return Integer.toString(role + 1);
    }

    public OrderedMenu.Builder ordered(Member member, EventWaiter eventWaiter) {
        return new OrderedMenu.Builder().setUsers(member.getUser())
                .setText(member.getEffectiveName())
                .setEventWaiter(eventWaiter)
                .allowTextInput(false)
                .useCancelButton(true)
                .setCancel(this::delete)
                .setTimeout(1, TimeUnit.HOURS);
    }

    private void delete(Message message) {
        message.delete().queue(v -> {}, t -> {});
    }
}
