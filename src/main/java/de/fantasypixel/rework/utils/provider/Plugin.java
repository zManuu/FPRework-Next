package de.fantasypixel.rework.utils.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on a Field, the plugin-instance will be auto-rigged.
 * Only accessible in a {@link ServiceProvider}.
 * Note: The plugin isn't available in the constructor as it is set after creation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Plugin {}