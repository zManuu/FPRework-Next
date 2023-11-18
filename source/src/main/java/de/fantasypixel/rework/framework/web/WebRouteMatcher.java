package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.FPRework;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Notes:
 * Currently you can only use one route argument. This argument is always placed at the end of the route. You must also not specify two routes with the same base property.
 * For example, "api/v1/user/{id}" would work fine. However, when using "api/v1/user/{id}/..." or "api/v1/{user}/{...}", the route will not be accepted.
 */
public class WebRouteMatcher {

    /**
     * Represents a route.
     * Example-1: "/api/v1/player/all/",
     * Example-2: "/api/v1/player/{name}/",
     * @param name The name of the route. Only used for logging purposes.
     * @param base The base URL of the route. Must be unique, is not case-sensitive. Example-1: "/api/v1/player/all/", Example-2: "/api/v1/player/", Example-3: "/api/v1/player/"
     * @param handler The handler that is called when the route is requested. Example-1: "() -> {...}", Example-2: "(String name) -> {...}"
     * @param <T> The type of the argument the handler is invoked with. Example-1: None, Example-2: String
     */
    public record WebRoute<T> (
            @Nonnull String name,
            @Nonnull String base,
            @Nonnull Consumer<T> handler
            ) {}

    private final FPRework plugin;
    private final Set<WebRoute<?>> routes;

    public WebRouteMatcher(FPRework plugin) {
        this.plugin = plugin;
        this.routes = new HashSet<>();
    }

    public void registerRoute(@Nonnull WebRoute<?> route) {
        this.plugin.getFpLogger().debug(
                "Registering route {0}",
                route.name()
        );

        this.routes.add(route);
    }

    /**
     * Matches a route to the registered routes.
     * @param route the requested route
     * @return the matching route or null if not found.
     */
    // todo: unit tests
    @Nullable
    public Consumer<?> matchRoute(String route) {
        WebRoute<?> matchingRoute = null;
        route = this.prettifyRoute(route);

        /*
         * The concept of matching a route:
         * We separate the route into parts. We go from the last part to the first and check if the currently iterated route-part with the ones before that match a registered route.
         */

        var routeParts = route.split("/");
        for (var i=routeParts.length-1; i>=0; i--) {
            // build the iterated route
            var iteratedRoute = new StringBuilder(routeParts[0]);
            for (var j=1; j<=i; j++) {
                iteratedRoute.append("/").append(routeParts[j]);
            }

            // find routes matching
            for (var registeredRoute : this.routes) {
                var registeredRouteBase = this.prettifyRoute(registeredRoute.base());
                if (registeredRouteBase.equalsIgnoreCase(iteratedRoute.toString())) {
                    matchingRoute = registeredRoute;
                    break;
                }
            }

        }

        if (matchingRoute == null) {
            this.plugin.getFpLogger().debug(
                    "A route was requested but couldn't be matched: \"{0}\"",
                    route
            );
            return null;
        }

        this.plugin.getFpLogger().debug(
                "The route \"{0}\" was requested, will be handled by the route \"{1}\".",
                route,
                matchingRoute.name()
        );

        return matchingRoute.handler();
    }

    /**
     * Removes slashes at the beginning and end of the route. Also replaces double slashes and backslashes.
     */
    // todo: unit tests
    private String prettifyRoute(String route) {
        if (route.startsWith("/"))
            route = route.substring(1);
        if (route.endsWith("/"))
            route = route.substring(0, route.length() - 1);
        route = route.replaceAll("\\\\", "/");
        route = route.replaceAll("//", "/");
        return route;
    }

}
