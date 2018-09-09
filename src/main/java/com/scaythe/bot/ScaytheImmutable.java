package com.scaythe.bot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.immutables.value.Value.Style;

@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
@Style(
        stagedBuilder = true,
        typeImmutable = "*Immutable",
        typeModifiable = "*Modifiable",
        depluralize = true,
        depluralizeDictionary = {"duty:duties"})
public @interface ScaytheImmutable {}
