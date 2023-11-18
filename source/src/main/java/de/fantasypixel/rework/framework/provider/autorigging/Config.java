package de.fantasypixel.rework.framework.provider.autorigging;

import de.fantasypixel.rework.framework.provider.ServiceProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on a Field, the config will be loaded automatically.
 * Only accessible in a {@link ServiceProvider}.
 * Note: The config isn't available in the constructor as it is set after creation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {}
