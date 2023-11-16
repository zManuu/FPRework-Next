package de.fantasypixel.rework.utils.web;

import de.fantasypixel.rework.utils.provider.Controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on methods in {@link Controller} classes to mark the method as an HTTP-PUT handler.
 * The method must return a {@link WebResponse}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebPut {

    public String route();

}
