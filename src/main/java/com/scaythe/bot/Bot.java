package com.scaythe.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
@EnableConfigurationProperties
public class Bot {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Bot.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
