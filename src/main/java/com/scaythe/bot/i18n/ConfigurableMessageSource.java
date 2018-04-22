package com.scaythe.bot.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.util.Assert;

public class ConfigurableMessageSource extends AbstractMessageSource {
    private final Map<Locale, Map<String, String>> source = new HashMap<>();
    
    public ConfigurableMessageSource(MessageSource parent) {
        this.setParentMessageSource(parent);
    }
    
    public String set(String value, String code, Locale locale) {
        Assert.notNull(value, "can't set null value");
        
        Map<String, String> lang = source.get(locale);
        
        if (lang == null) {
            lang = new HashMap<>();
            source.put(locale, lang);
        }
        
        return lang.put(code, value);
    }
    
    public String unset(String code, Locale locale) {
        Map<String, String> lang = source.get(locale);
        
        if (lang == null) {
            return null;
        }
        
        String old = lang.remove(code);
        
        if (lang.isEmpty()) {
            source.remove(locale);
        }
        
        return old;
    }
    
    public boolean isSet(String code, Locale locale) {
        Map<String, String> lang = source.get(locale);
        
        if (lang == null) {
            return false;
        }
        
        return lang.containsKey(code);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        Map<String, String> lang = source.get(locale);
        
        if (lang == null) {
            return null;
        }
        
        String value = lang.get(code);
        
        if (value == null) {
            return null;
        }
        
        return new MessageFormat(value, locale);
    }
}
