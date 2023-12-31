package de.fantasypixel.rework.framework.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When applied on a method in a {@link de.fantasypixel.rework.framework.provider.Controller} class, the method gets called when the plugin shuts down.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnDisable {}
