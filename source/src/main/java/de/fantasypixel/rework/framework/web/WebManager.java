package de.fantasypixel.rework.framework.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.ProviderManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.function.Function;

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
            String name,
            String route,
            HttpMethod httpMethod,
            Function<String, WebResponse> handler
    ) {}

    private final static String CLASS_NAME = WebManager.class.getSimpleName();

    private final FPRework plugin;
    private final WebRouteMatcher routeMatcher;
    private HttpHandler handler;
    private HttpServer server;

    public WebManager(FPRework plugin) {
        this.plugin = plugin;
        this.routeMatcher = new WebRouteMatcher(plugin);

        try {
            this.setupHandler();
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.setExecutor(null);
            server.createContext("/", this.handler);
            server.start();
        } catch (IOException ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "constructor", ex);
        }
    }

    private void setupHandler() {
        this.handler = exchange -> {
            var requestedRoute = exchange.getRequestURI().toString();
            var matchingRoute = routeMatcher.matchRoute(requestedRoute);
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
                        "The route \"{0}\" was requested, will be handled by the route \"{1}\".",
                        requestedRoute,
                        route.name()
                );

                try {
                    response = route.handler().apply(requestedRoute);
                } catch (Exception ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "registerWebHandler->handle", ex);
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

    public void registerRoute(String name, String route, HttpMethod httpMethod, Method method, Object object) {
        this.plugin.getFpLogger().debug(
                "Registering a web handler for route \"{0}\": {1}::{2}",
                route,
                object.getClass().getSimpleName(),
                method.getName()
        );

        // check if the method returns a WebResponse
        if (!WebResponse.class.isAssignableFrom(method.getReturnType())) {
            this.plugin.getFpLogger().warning(
                    "Web handler {0}::{1} (for route \"{2}\") couldn't be registered as it doesn't return a WebResponse!",
                    object.getClass().getSimpleName(),
                    method.getName(),
                    route
            );
            return;
        }

        var handler = new Function<String, WebResponse>() {
            @Override
            public WebResponse apply(String s) {
                try {
                    Object handlerResponseObj = method.getParameterCount() == 0
                            ? method.invoke(object)
                            : method.invoke(object, s);

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
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        this.routeMatcher.registerRoute(
                new WebRoute(
                        name,
                        route,
                        httpMethod,
                        handler
                )
        );

        this.plugin.getFpLogger().debug(
                "Successfully registered a web handler for route \"{0}\": {1}::{2}",
                route,
                object.getClass().getSimpleName(),
                method.getName()
        );
    }

    public void stop() {
        this.server.stop(0);
    }

}
