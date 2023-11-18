package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.provider.Controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on methods in {@link Controller} classes to mark the method as an HTTP-POST handler.
 * The method must return a {@link WebResponse}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebPost {

    public String route();

}
