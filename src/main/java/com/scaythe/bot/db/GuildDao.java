package com.scaythe.bot.db;

import org.springframework.stereotype.Component;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

import net.dv8tion.jda.core.entities.Guild;

@Component
public class GuildDao {

    private static final String GUILD_KIND = "Guild";

    private final Datastore datastore;

    public GuildDao(Datastore datastore) {
        this.datastore = datastore;
    }

    public boolean exists(Guild guild) {
        return datastore.get(key(guild)) != null;
    }

    public void save(Guild guild) {
        Key key = key(guild);

        Entity entity = Entity.newBuilder(key).build();

        datastore.put(entity);
    }

    public void delete(Guild guild) {
        datastore.delete(key(guild));
    }

    private Key key(Guild guild) {
        return datastore.newKeyFactory().setKind(GUILD_KIND).newKey(guild.getIdLong());
    }
    
    public static PathElement pathElement(Guild guild) {
        return PathElement.of(GUILD_KIND, guild.getIdLong());
    }
}
