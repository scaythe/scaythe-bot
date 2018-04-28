package com.scaythe.bot.db;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.core.entities.Guild;

@Component
public class GuildPersistence {

    private final GuildDao dao;

    public GuildPersistence(GuildDao dao) {
        this.dao = dao;
    }

    public boolean exists(Guild guild) {
        return dao.exists(guild);
    }

    public void save(Guild guild) {
        dao.save(guild);
    }

    public void delete(Guild guild) {
        dao.delete(guild);
    }
}
