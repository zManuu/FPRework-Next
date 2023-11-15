package de.fantasypixel.rework.utils.provider.autorigging;

import de.fantasypixel.rework.utils.provider.ServiceProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on a Field, the gson-instance will be auto-rigged.
 * Only accessible in a {@link ServiceProvider}.
 * Note: The gson instance isn't available in the constructor as it is set after creation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Gson {}
