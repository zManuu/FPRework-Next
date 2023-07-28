package de.fantasypixel.rework.utils.provider.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When applied on a method in a {@link de.fantasypixel.rework.utils.provider.Controller} class, the method gets called when all services, controllers, ... are ready to use.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnEnable {}
