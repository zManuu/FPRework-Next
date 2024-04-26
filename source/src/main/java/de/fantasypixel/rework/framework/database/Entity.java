package de.fantasypixel.rework.framework.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an entity-class.
 * <br><br>
 * There are a few rules entity-classes must follow:<br>
 * - There must be a nullable field id.<br>
 * - The fields must be declared in the same order as the columns in the database.<br>
 * - There must be a constructor with no arguments as this is used when instantiating an entity.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    /**
     * The table name in the database.
     */
    String tableName();

}
