package de.fantasypixel.rework.framework.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a web-servlet class. Those classes must extend {@link jakarta.servlet.http.HttpServlet}.<br/>
 * It is possible to inject services with the {@link de.fantasypixel.rework.framework.provider.Service} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Servlet {

    /**
     * Path-spec.
     */
    String value();

}
