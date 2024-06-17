package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.fantasypixel.rework.modules.character.Characters;
import de.fantasypixel.rework.modules.items.Items;

/**
 * Can be applied on a Set of T. All classes extending T will be instantiated and added to this set.
 * For instance, used by {@link Items#ITEMS} or {@link Characters#CHARACTERS}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Extending {}
