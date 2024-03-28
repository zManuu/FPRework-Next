package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.provider.Controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on methods in {@link Controller} classes to mark the method as an HTTP-GET handler.
 * The method must return a {@link WebResponse}.
 * If a route-parameter is required, you can use the first argument of the method, make sure it is of type String.
 * Note: The HTTP method GET doesn't have access to the request body.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebGet {

    String name();
    String route();
    int timeout() default 50;

}
