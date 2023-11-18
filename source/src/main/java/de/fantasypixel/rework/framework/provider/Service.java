package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on a Field, the service-provider will be loaded automatically.
 * Only accessible in a {@link Controller}.
 * Note: Services aren't available in the constructor as they are set after creation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Service {

    String name();

}
