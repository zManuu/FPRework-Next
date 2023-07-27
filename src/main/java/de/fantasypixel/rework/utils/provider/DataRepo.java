package de.fantasypixel.rework.utils.provider;

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

    String name();
    Class<?> type();

}
