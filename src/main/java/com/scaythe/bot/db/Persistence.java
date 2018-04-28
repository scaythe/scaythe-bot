package com.scaythe.bot.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

@Configuration
public class Persistence {
    @Bean
    public Datastore datastore() {
        return DatastoreOptions.newBuilder()
                .build()
                .getService();
    }
}
