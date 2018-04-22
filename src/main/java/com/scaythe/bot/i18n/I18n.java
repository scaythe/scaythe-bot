package com.scaythe.bot.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class I18n {
    
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource rrbms = new ReloadableResourceBundleMessageSource();
        rrbms.setBasenames("classpath:i18n/text", "classpath:i18n/speech");
        rrbms.setDefaultEncoding("UTF-8");
        return rrbms;
    }
}
