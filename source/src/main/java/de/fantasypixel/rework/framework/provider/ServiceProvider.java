package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Is to be applied on a class-declaration. Marks this class as a service provider.
 * Will be auto-rigged to {@link Service} fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceProvider {}
