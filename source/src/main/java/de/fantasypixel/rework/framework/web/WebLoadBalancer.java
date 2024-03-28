package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.FPLogger;
import de.fantasypixel.rework.framework.timer.TimerManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class for the {@link WebManager} that manages the balancing of route loading.
 * For instance, the implementation for {@link WebGet#timeout()}, {@link WebPut#timeout()}, etc. on is using this class.
 * <pre></pre>
 * It is using the {@link WebManager.WebRoute#name()} and the ip of the clients to determine timeouts.
 */
public class WebLoadBalancer {

    private record RouteTimeout (String routeName, AtomicInteger timeout) {}

    private static final String CLASS_NAME = WebLoadBalancer.class.getSimpleName();

    private final FPLogger logger;
    @Getter
    private final WebRouteMatcher routeMatcher;
    private final Map<String, Set<RouteTimeout>> timeouts;
    private Thread timeoutThread;

    public WebLoadBalancer(FPLogger logger, WebRouteMatcher routeMatcher) {
        this.logger = logger;
        this.routeMatcher = routeMatcher;
        this.timeouts = new HashMap<>();

        this.startTimeoutThread();
    }

    /**
     * Start a thread that counts down and removes timeouts.
     */
    private void startTimeoutThread() {
        this.logger.debug("Starting the web-route-timeout-thread.");

        this.timeoutThread = new Thread(() -> {
            while (!timeoutThread.isInterrupted()) {
                timeouts.forEach((ip, routeTimeouts) -> {
                    routeTimeouts.removeIf(e -> e.timeout().get() <= 0);
                    routeTimeouts.forEach(e -> e.timeout().decrementAndGet());
                });

                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    this.logger.error(CLASS_NAME, "startTimeoutThread->run", ex);
                }
            }
        });
        this.timeoutThread.start();

        this.logger.debug("Started the web-route-timeout-thread.");
    }

    /**
     * Checks if the client with the specified ip address can currently access the route with the specified name.
     * If the client can access the route, the specified {@link WebGet#timeout()}, {@link WebPut#timeout()}, etc. is assigned.
     * @throws IllegalArgumentException if no route was found with the specified name
     */
    public boolean canAccess(String ip, String routeName) throws IllegalArgumentException {
        // get the route timeout
        var routeTimeout = this.routeMatcher.getTimeoutForRoute(routeName);

        // client has no timeouts?
        if (!this.timeouts.containsKey(ip)) {
            this.assignTimeout(ip, routeName, routeTimeout);
            return true;
        }

        var timeoutSet = this.timeouts.get(ip);

        for (var timeout : timeoutSet) {
            // client has no timeout on route?
            if (timeout.routeName().equalsIgnoreCase(routeName))
                continue;

            this.assignTimeout(ip, routeName, routeTimeout);
            return true;
        }

        return false;
    }

    private void assignTimeout(String ip, String routeName, int timeout) {
        if (!this.timeouts.containsKey(ip))
            this.timeouts.put(ip, new HashSet<>());

        var timeoutSet = this.timeouts.get(ip);
        timeoutSet.removeIf(e -> e.routeName().equalsIgnoreCase(routeName));
        var timeoutPair = new RouteTimeout(routeName, new AtomicInteger(timeout));
        timeoutSet.add(timeoutPair);
    }

    public void onStop() {
        this.timeoutThread.interrupt();
    }

}
