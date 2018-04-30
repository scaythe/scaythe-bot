package com.scaythe.bot.db;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

import net.dv8tion.jda.core.entities.Guild;

@Component
public class ValueDao {

    private static final String VALUE_FIELD = "value";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Datastore datastore;

    public ValueDao(Datastore datastore) {
        this.datastore = datastore;
    }

    @Cacheable(cacheNames = "properties", key = "{#guild.idLong, #kind, #identifier, #locale}")
    public Optional<String> get(
            Guild guild,
            String kind,
            String identifier,
            Optional<Locale> locale) {
        Optional<Entity> entity
                = Optional.ofNullable(datastore.get(key(guild, kind, identifier, locale)));

        return entity.map(e -> e.getString(VALUE_FIELD));
    }

    @CacheEvict(cacheNames = "properties", key = "{#guild.idLong, #kind, #identifier, #locale}")
    public void save(
            Guild guild,
            String kind,
            String identifier,
            Optional<Locale> locale,
            String value) {
        Key key = key(guild, kind, identifier, locale);

        Entity entity = Entity.newBuilder(key).set(VALUE_FIELD, value).build();

        datastore.put(entity);
    }

    @CacheEvict(cacheNames = "properties", key = "{#guild.idLong, #kind, #identifier, #locale}")
    public void delete(Guild guild, String kind, String identifier, Optional<Locale> locale) {
        datastore.delete(key(guild, kind, identifier, locale));
    }

    private Key key(Guild guild, String kind, String identifier, Optional<Locale> locale) {
        return datastore.newKeyFactory()
                .addAncestor(GuildDao.pathElement(guild))
                .setKind(kind)
                .newKey(buildIdentifier(identifier, locale));
    }

    private String buildIdentifier(String identifier, Optional<Locale> locale) {
        return identifier + locale.map(l -> "@" + l.toLanguageTag()).orElse("");
    }
}
