package de.fantasypixel.rework.framework.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on classes that will serve as configuration-providers. Currently, only json files are supported.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigProvider {

    /**
     * The path / name of the json configuration file. For instance, player-character/config will be plugins/FPRework-Next/player-character/config.json.
     */
    String path();

}
