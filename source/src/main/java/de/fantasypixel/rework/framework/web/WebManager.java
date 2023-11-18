package de.fantasypixel.rework.framework.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.ProviderManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

/**
 * Manages web-integration for {@link Controller} classes.
 * Is managed by the {@link ProviderManager}.
 */
public class WebManager {

    private final static String CLASS_NAME = WebManager.class.getSimpleName();

    private final FPRework plugin;
    private HttpServer server;

    public WebManager(FPRework plugin) {
        this.plugin = plugin;

        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.setExecutor(null);
            server.start();
        } catch (IOException ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "constructor", ex);
        }
    }

    public void registerWebHandler(Method method, Object object, String route) {
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

        var handler = new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                plugin.getFpLogger().debug(
                        "HTTP-Request with route \"{0}\" will be handled by {1}::{2}.",
                        route,
                        object.getClass().getSimpleName(),
                        method.getName()
                );

                WebResponse response;

                try {
                    var handlerResponseObj = method.invoke(object);
                    if (!(handlerResponseObj instanceof WebResponse handlerResponse)) {
                        plugin.getFpLogger().warning(
                                "Web handler {0}::{1} (for route \"{2}\") didn't return a WebResponse!",
                                object.getClass().getSimpleName(),
                                method.getName(),
                                route
                        );
                        return;
                    }

                    response = handlerResponse;
                } catch (Exception ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "registerWebHandler->handle", ex);
                    response = WebResponse.INTERNAL_SERVER_ERROR;
                }

                try {
                    exchange.sendResponseHeaders(response.getCode(), response.getBody().length());
                    var os = exchange.getResponseBody();
                    os.write(response.getBody().getBytes());
                    os.close();
                } catch (IOException ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "registerWebHandler->handle", ex);
                }

            }
        };

        this.server.createContext(route, handler);

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
