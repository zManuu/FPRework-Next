package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.FPRework;
import lombok.AllArgsConstructor;

import java.lang.reflect.Method;

@AllArgsConstructor
public class WebRouteValidator {

    private final FPRework plugin;
    private final WebRouteMatcher routeMatcher;

    public boolean validate(String name, String route, WebManager.HttpMethod httpMethod, Method method) {
        var className = method.getDeclaringClass().getSimpleName();
        var methodName = method.getName();

        // check if route exists
        if (this.routeMatcher.existsRoute(route)) {
            this.plugin.getFpLogger().warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as a route already exists!",
                    className,
                    methodName,
                    route
            );
            return false;
        }

        // check if route-name exists
        if (this.routeMatcher.existsRouteWithName(name)) {
            this.plugin.getFpLogger().warning(
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
            this.plugin.getFpLogger().warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as it doesn't return a WebResponse!",
                    className,
                    methodName,
                    route
            );
            return false;
        }

        // check if the argument types are supported
        if (method.getParameterCount() > 0) {
            if (!method.getParameterTypes()[0].equals(String.class)) {
                this.plugin.getFpLogger().warning(
                        "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as the first parameter isn't of type String as required!",
                        className,
                        methodName,
                        route
                );
                return false;
            }
            if (method.getParameterCount() > 2) {
                this.plugin.getFpLogger().warning(
                        "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as there are more than 2 parameters!",
                        className,
                        methodName,
                        route
                );
                return false;
            }
        }

        // check for get with body
        if (httpMethod == WebManager.HttpMethod.GET && method.getParameterCount() == 2) {
            this.plugin.getFpLogger().warning(
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
