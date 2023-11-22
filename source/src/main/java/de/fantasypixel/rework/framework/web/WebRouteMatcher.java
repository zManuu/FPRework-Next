package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.FPRework;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A utility class used by the {@link WebManager} to match routes.
 * <pre>
 * Notes:
 * Currently you can only use one route argument. This argument is always placed at the end of the route. You must also not specify two routes with the same base property.
 * For example, "api/v1/user/{id}" would work fine. However, when using "api/v1/user/{id}/..." or "api/v1/{user}/{...}", the route will not be accepted.
 */
public class WebRouteMatcher {

    private final Set<WebManager.WebRoute> routes;

    public WebRouteMatcher() {
        this.routes = new HashSet<>();
    }

    public boolean existsRoute(String route) {
        return this.routes.stream()
                .anyMatch(e -> e.route().equalsIgnoreCase(route));
    }

    public boolean existsRouteWithName(String routeName) {
        return this.routes.stream()
                .anyMatch(e -> e.name().equalsIgnoreCase(routeName));
    }

    public void registerRoute(@Nonnull WebManager.WebRoute route) {
        this.routes.add(route);
    }

    /**
     * Matches a route to the registered routes.
     * @param route the requested route
     * @return the matching route or null if not found.
     */
    // todo: unit tests
    // todo: check HTTP-method
    public Optional<WebManager.WebRoute> matchRoute(String route) {
        Optional<WebManager.WebRoute> matchingRoute = Optional.empty();
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
                var registeredRouteBase = this.prettifyRoute(registeredRoute.route());
                if (registeredRouteBase.equalsIgnoreCase(iteratedRoute.toString())) {
                    matchingRoute = Optional.of(registeredRoute);
                    break;
                }
            }

        }

        if (route.equals("/")) {
            // The / route can't be matched by the default route matching mechanism. Therefore, here is workaround.
            matchingRoute = this.routes.stream()
                    .filter(e -> e.route().equals("/"))
                    .findAny();
        }

        return matchingRoute;
    }

    /**
     * Removes slashes at the beginning and end of the route. Also replaces double slashes and backslashes.
     */
    // todo: unit tests
    public String prettifyRoute(String route) {
        if (route == null || route.isEmpty() || route.isBlank() || route.equals("/"))
            return "/";

        if (route.startsWith("/"))
            route = route.substring(1);
        if (route.endsWith("/"))
            route = route.substring(0, route.length() - 1);
        route = route.replaceAll("\\\\", "/");
        route = route.replaceAll("//", "/");

        return route;
    }

}
