package de.fantasypixel.rework.framework.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an entity-class.
 * Entity-classes must have an id field,
 * the properties need to be declared in the same order as the database columns.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    /**
     * The table name in the database.
     */
    String tableName();

}
