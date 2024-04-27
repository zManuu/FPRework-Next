package de.fantasypixel.rework.framework.jsondata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on classes that will then serve as providers and the type of json data.
 * Those json files can either be of type array or single objects for advanced configuration.
 * The data will also be read from all subdirectories of the given path.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonDataProvider {

    /**
     * The path to the directory where the json files are located.
     */
    String path();

}
