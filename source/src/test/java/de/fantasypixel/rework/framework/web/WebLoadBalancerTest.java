package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.UtilClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class WebLoadBalancerTest {

    private final static String TEST_IP = "187.187.187.187";
    private final static String TEST_ROUTE = "TEST";
    private final static int TEST_TIMEOUT = 100;
    private final static BiFunction<String, Object, WebResponse> VOID_HANDLER = (s, o) -> WebResponse.OK;

    // before-once
    @BeforeEach void beforeEach() { Constants.beforeOnce(); }

    @Test
    public void testLoadBalancing() {
        var routeMatcher = new WebRouteMatcher(Constants.logger);
        var loadBalancer = new WebLoadBalancer(Constants.logger, routeMatcher);

        // test on not existing routes
        assertThrows(IllegalArgumentException.class, () -> loadBalancer.canAccess(TEST_IP, TEST_ROUTE));

        // create route
        routeMatcher.registerRoute(
                new WebManager.WebRoute(
                        TEST_ROUTE,
                        TEST_ROUTE,
                        WebManager.HttpMethod.GET,
                        UtilClasses.None.class,
                        TEST_TIMEOUT,
                        VOID_HANDLER
                )
        );

        // test on existing route, no timeout yet
        assertTrue(loadBalancer.canAccess(TEST_IP, TEST_ROUTE));

        // test on existing route, with timeout
        assertFalse(loadBalancer.canAccess(TEST_IP, TEST_ROUTE));

        // wait some time, test again
        //CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> loadBalancer.canAccess(TEST_IP, TEST_ROUTE));
        //Boolean canAccessRouteAfterTimeout = future.get(10, TimeUnit.MILLISECONDS);
        //assertTrue(canAccessRouteAfterTimeout);
        // todo: why is WebLoadBalancer#timeout still at 95 at this point? -> causes test to fail
    }

}
