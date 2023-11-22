package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.provider.Controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on methods in {@link Controller} classes to mark the method as an HTTP-DELETE handler.
 * The method must return a {@link WebResponse}.
 * If a route-parameter is required, you can use the first argument of the method, make sure it is of type String.
 * If access to the body is required, use the second argument of the method. You can use some kind of payload-record, it will automatically be instantiated with {@link com.google.gson.Gson}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebDelete {

    String name();
    String route();

}
