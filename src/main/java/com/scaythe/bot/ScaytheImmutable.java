package com.scaythe.bot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.immutables.serial.Serial.Version;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Version(1)
@Style(
        stagedBuilder = true,
        typeImmutable = "*Immutable",
        typeModifiable = "*Modifiable",
        depluralize = true,
        depluralizeDictionary = {"duty:duties"},
        defaults = @Immutable(copy = false))
public @interface ScaytheImmutable {}
