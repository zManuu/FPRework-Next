package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.FPLogger;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

@AllArgsConstructor
public class WebRouteValidator {

    private final FPLogger logger;
    private final WebRouteMatcher routeMatcher;

    /**
     * Checks if the parameters are valid for constructing a route.
     * @param name the route's name (used for logging)
     * @param route the pattern
     * @param httpMethod the HTTP method to be used
     * @param method the method to be executed
     * @return whether the parameters are valid for constructing a route
     */
    public boolean validate(@Nonnull String name, @Nonnull String route, @Nonnull WebManager.HttpMethod httpMethod, @Nonnull Method method) {
        var className = method.getDeclaringClass().getSimpleName();
        var methodName = method.getName();

        // check if route exists
        if (this.routeMatcher.existsRoute(route)) {
            this.logger.warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as a route already exists!",
                    className,
                    methodName,
                    route
            );
            return false;
        }

        // check if route-name exists
        if (this.routeMatcher.existsRouteWithName(name)) {
            this.logger.warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as a route with the name \"{2}\" already exists!",
                    className,
                    methodName,
                    route,
                    name
            );
            return false;
        }

        // check if the method returns a WebResponse
        if (!WebResponse.class.isAssignableFrom(method.getReturnType())) {
            this.logger.warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as it doesn't return a WebResponse!",
                    className,
                    methodName,
                    route
            );
            return false;
        }

        // check if the argument types are supported
        if (method.getParameterCount() > 0) {
            if (method.getParameterCount() == 2 && !method.getParameterTypes()[0].equals(String.class)) {
                this.logger.warning(
                        "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as the first parameter isn't of type String as required!",
                        className,
                        methodName,
                        route
                );
                return false;
            }
            if (method.getParameterCount() > 2) {
                this.logger.warning(
                        "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as there are more than 2 parameters!",
                        className,
                        methodName,
                        route
                );
                return false;
            }
        }

        // check for incompatible combinations
        // get with body
        if (httpMethod == WebManager.HttpMethod.GET && method.getParameterCount() == 2) {
            this.logger.warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as it is a GET endpoint but tried to use the body (unsupported)!",
                    className,
                    methodName,
                    route
            );
            return false;
        }

        return true;
    }

}
