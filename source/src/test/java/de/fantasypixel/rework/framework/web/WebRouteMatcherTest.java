package de.fantasypixel.rework.framework.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebRouteMatcherTest {

    @Test
    void testPrettifyRoute() {

        var routeMatcher = new WebRouteMatcher();

        assertEquals(
                "test",
                routeMatcher.prettifyRoute("/test//")
        );

        assertEquals(
                "test/1/2/3",
                routeMatcher.prettifyRoute("test\\1//2/3/")
        );

    }
}