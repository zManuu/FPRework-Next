package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied on controller classes.
 * Controllers play the role of connecting the game logic with the minecraft-server, the web-server and other endpoints.
 * Controllers communicate with the game logic through {@link Service} annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {}
