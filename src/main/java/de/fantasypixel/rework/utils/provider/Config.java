package de.fantasypixel.rework.utils.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on a Field, the config will be loaded automatically.
 * Only accessible in a {@link ServiceProvider}.
 * Note: The config isn't available in the constructor as it is set after creation.
 * Make sure fields you apply the annotation to are public.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {}
