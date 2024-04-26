package de.fantasypixel.rework.framework.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on fields in the service-layer to be marked as injected from configuration files.
 * The type of the field must be a Class annotated with the {@link ConfigProvider} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {}
