package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied on controller classes.
 * Only these classes can access {@link de.fantasypixel.rework.framework.provider.Service} and {@link de.fantasypixel.rework.framework.command.Command}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {}
