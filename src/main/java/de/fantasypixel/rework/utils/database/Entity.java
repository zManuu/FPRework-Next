package de.fantasypixel.rework.utils.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an entity-class.
 * Entity-classes need to follow the following rules:
 * All properties must be public, an id property must exist.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    /**
     * The table name in the database.
     */
    String tableName();

}
