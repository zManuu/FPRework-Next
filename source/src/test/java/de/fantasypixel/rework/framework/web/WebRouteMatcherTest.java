package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.UtilClasses;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class WebRouteMatcherTest {

    WebRouteMatcher routeMatcher = new WebRouteMatcher(Constants.logger);
    BiFunction<String, Object, WebResponse> voidHandler = (s, o) -> WebResponse.OK;

    @Test
    void testPrettifyRoute() {
        assertEquals(
                "test",
                routeMatcher.prettifyRoute("/test//")
        );
        assertEquals(
                "test/1/2/3",
                routeMatcher.prettifyRoute("test\\1//2/3/")
        );
    }


    @Test
    void testMatching() {

        for (var httpMethod : WebManager.HttpMethod.values()) {
            assertTrue(routeMatcher.matchRoute("/", httpMethod).isEmpty());
        }

        assertFalse(routeMatcher.existsRoute("/"));
        assertFalse(routeMatcher.existsRouteWithName("test"));

        routeMatcher.registerRoute(
                new WebManager.WebRoute(
                        "test",
                        "/",
                        WebManager.HttpMethod.GET,
                        UtilClasses.None.class,
                        0,
                        voidHandler
                )
        );

        assertTrue(routeMatcher.matchRoute("/", WebManager.HttpMethod.GET).isPresent());
        assertTrue(routeMatcher.existsRoute("/"));
        assertTrue(routeMatcher.existsRouteWithName("test"));

        assertTrue(routeMatcher.matchRoute("/", WebManager.HttpMethod.POST).isEmpty());
        assertTrue(routeMatcher.matchRoute("/", WebManager.HttpMethod.PUT).isEmpty());
        assertTrue(routeMatcher.matchRoute("/", WebManager.HttpMethod.DELETE).isEmpty());

        routeMatcher.registerRoute(
                new WebManager.WebRoute(
                        "post-test",
                        "/post",
                        WebManager.HttpMethod.POST,
                        UtilClasses.None.class,
                        0,
                        voidHandler
                )
        );

        assertTrue(routeMatcher.existsRoute("post"));
        assertTrue(routeMatcher.existsRouteWithName("post-test"));
        assertTrue(routeMatcher.matchRoute("post/abc", WebManager.HttpMethod.POST).isPresent());

    }

}