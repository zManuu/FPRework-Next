package de.fantasypixel.rework.framework.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.UtilClasses;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.ProviderManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.function.BiFunction;

/**
 * Manages web-integration for {@link Controller} classes.
 * Is managed by the {@link ProviderManager}.
 */
public class WebManager {

    public enum HttpMethod { GET, POST, PUT, DELETE }

    /**
     * Represents a route.
     * Example-1: "/api/v1/player/all/",
     * Example-2: "/api/v1/player/{name}/",
     * @param name The name of the route. Only used for logging purposes.
     * @param route The URL of the route. Must be unique, is not case-sensitive. Example-1: "/api/v1/player/all/", Example-2: "/api/v1/player/", Example-3: "/api/v1/player/"
     * @param httpMethod The HTTP method of the route
     * @param handler The handler that is called when the route is requested. Example-1: "() -> {...}", Example-2: "(String name) -> {...}"
     */
    public record WebRoute (
            @Nonnull String name,
            @Nonnull String route,
            @Nonnull HttpMethod httpMethod,
            @Nonnull Class<?> bodyClass,
            int timeout,
            @Nonnull BiFunction<String, Object, WebResponse> handler
    ) {}

    private final static String CLASS_NAME = WebManager.class.getSimpleName();

    private final FPRework plugin;
    private final WebRouteMatcher routeMatcher;
    private final WebRouteValidator routeValidator;
    private HttpHandler handler;
    private HttpServer server;

    public WebManager(@Nonnull FPRework plugin, @Nonnull WebConfig config) {
        this.plugin = plugin;
        this.routeMatcher = new WebRouteMatcher(plugin.getFpLogger());
        this.routeValidator = new WebRouteValidator(plugin.getFpLogger(), this.routeMatcher);

        try {
            this.plugin.getFpLogger().debug("The web-server is starting on port {0}.", String.valueOf(config.getPort()));

            this.setupHandler();
            server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
            server.setExecutor(null);
            server.createContext("/", this.handler);
            server.start();

            this.plugin.getFpLogger().debug("The web-server was started successfully.");
        } catch (IOException ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "constructor", ex);
        }
    }

    /**
     * Initializes the handler for the central route.
     */
    private void setupHandler() {
        this.handler = exchange -> {
            var requestedRoute = exchange.getRequestURI().toString();
            exchange.getRemoteAddress().getAddress().getHostAddress();
            var matchingRoute = routeMatcher.matchRoute(requestedRoute, HttpMethod.valueOf(exchange.getRequestMethod()));
            var response = WebResponse.INTERNAL_SERVER_ERROR;

            if (matchingRoute.isEmpty()) {
                plugin.getFpLogger().warning(
                        "The route \"{0}\" was requested, no matching handler was found.",
                        requestedRoute
                );

                response = WebResponse.NOT_FOUND;
            } else {
                var route = matchingRoute.get();

                this.plugin.getFpLogger().debug(
                        "The route \"{0}\" was requested, will be handled by \"{1}\".",
                        requestedRoute,
                        route.name()
                );

                try {
                    var routeParameter = this.routeMatcher.prettifyRoute(requestedRoute.substring(route.route().length()));
                    var bodyStream = exchange.getRequestBody();
                    var bodyJson = new String(bodyStream.readAllBytes());
                    var body = this.plugin.getGson().fromJson(bodyJson, route.bodyClass());
                    response = route.handler().apply(routeParameter, body);
                } catch (Exception ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "setupHandler->handle", ex);
                    response = WebResponse.INTERNAL_SERVER_ERROR;
                }
            }

            try {
                exchange.sendResponseHeaders(response.getCode(), response.getBody().length());
                var os = exchange.getResponseBody();
                os.write(response.getBody().getBytes());
                os.close();
            } catch (IOException ex) {
                plugin.getFpLogger().error(CLASS_NAME, "setupHandler->handle", ex);
            }
        };
    }

    /**
     * Registers a route.
     * @param name the name of the route (used for logging)
     * @param route the route pattern
     * @param httpMethod the associated http method
     * @param timeout the timeout to assign after each access
     * @param method the method to be executed
     * @param object the object holding the method
     */
    public void registerRoute(@Nonnull String name, @Nonnull String route, @Nonnull HttpMethod httpMethod, int timeout, @Nonnull Method method, @Nonnull Object object) {
        this.plugin.getFpLogger().info(
                "Registering a web handler for route \"{0} {1}\": {2}::{3}",
                httpMethod.toString(),
                route,
                object.getClass().getSimpleName(),
                method.getName()
        );

        if (!this.routeValidator.validate(name, route, httpMethod, method)) {
            this.plugin.getFpLogger().debug(
                    "Route {0}::{1} couldn't be registered as it is not valid.",
                    route,
                    object.getClass().getSimpleName(),
                    method.getName()
            );
            return;
        }

        var handler = new BiFunction<String, Object, WebResponse>() {
            @Override
            public WebResponse apply(@Nonnull String reqRoute, @Nullable Object reqBody) {
                try {

                    // invoke the handler with matching parameters
                    Object handlerResponseObj;
                    if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == String.class) {
                        handlerResponseObj = method.invoke(object, reqRoute, reqBody);
                    } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == String.class) {
                        handlerResponseObj = method.invoke(object, reqRoute);
                    } else if (method.getParameterCount() == 1) {
                        handlerResponseObj = method.invoke(object, reqBody);
                    } else if (method.getParameterCount() == 0) {
                        handlerResponseObj = method.invoke(object);
                    } else {
                        plugin.getFpLogger().warning(
                                "Web handler {0}::{1} (for route \"{2}\") couldn't be handled as the handler types aren't matching!",
                                object.getClass().getSimpleName(),
                                method.getName(),
                                route
                        );

                        return WebResponse.INTERNAL_SERVER_ERROR;
                    }

                    if (!(handlerResponseObj instanceof WebResponse handlerResponse)) {
                        plugin.getFpLogger().warning(
                                "Web handler {0}::{1} (for route \"{2}\") couldn't be handled as it didn't return a WebResponse!",
                                object.getClass().getSimpleName(),
                                method.getName(),
                                route
                        );

                        return WebResponse.INTERNAL_SERVER_ERROR;
                    }

                    return handlerResponse;
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "registerHandler->handle", ex);
                    return WebResponse.INTERNAL_SERVER_ERROR;
                }
            }
        };

        // determine the body type
        Class<?> bodyType;
        if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == String.class) {
            bodyType = method.getParameterTypes()[1];
        } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == String.class) {
            bodyType = UtilClasses.None.class;
        } else if (method.getParameterCount() == 1) {
            bodyType = method.getParameterTypes()[0];
        } else if (method.getParameterCount() == 0) {
            bodyType = UtilClasses.None.class;
        } else {
            plugin.getFpLogger().warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be handled as the body type couldn't be determined!",
                    object.getClass().getSimpleName(),
                    method.getName(),
                    route
            );

            return;
        }

        this.routeMatcher.registerRoute(
                new WebRoute(
                        name,
                        route,
                        httpMethod,
                        bodyType,
                        timeout,
                        handler
                )
        );
    }

    /**
     * Stops the web-server.
     */
    public void stop() {
        this.plugin.getFpLogger().debug("The web-server is stopping.");
        this.server.stop(0);
    }

}