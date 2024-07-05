package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on extending-super-classes that should not be instantiated.
 * For instance, a Weapon is an Item and therefore extends Item. However, weapon is not a real Item we want to have but subclasses of Weapon like WoodenSword. In this case, we would want to specify the annotation on Weapon.
 * @see Extending
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtendingIgnore {}
