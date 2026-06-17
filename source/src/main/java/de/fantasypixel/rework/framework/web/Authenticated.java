package de.fantasypixel.rework.framework.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used in combination with {@link Servlet}. The servlets resources must only be accessed by authenticated callers.<br/>
 * Note: This annotation is also used for OpenAPI generation. All endpoints with this annotation will have default 401 and 403 responses in the OpenAPI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Authenticated {
}
