package de.fantasypixel.rework.framework.database;

import de.fantasypixel.rework.framework.provider.ServiceProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on a Field, the data-reo will be created and loaded automatically.
 * Only accessible in a {@link ServiceProvider}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataRepo {

    /**
     * The entity-type that will be handled.
     */
    Class<?> type();

}
