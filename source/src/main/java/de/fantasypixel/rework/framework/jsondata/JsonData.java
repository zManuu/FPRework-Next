package de.fantasypixel.rework.framework.jsondata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on fields in the service-layer to be marked as injected from json data-files.
 * The generic type of the field must be a Class annotated with the {@link JsonDataProvider} annotation.
 * <br><br>
 * Note: The type of the field must be a {@link java.util.Set}!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonData {}
