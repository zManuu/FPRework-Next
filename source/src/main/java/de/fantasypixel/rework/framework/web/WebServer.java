package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.auth.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;

import java.util.EnumSet;
import java.util.Map;

public class WebServer {

    private Server server;
    private final WebConfig webConfig;
    private final Map<HttpServlet, String> servlets;
    private final AuthService authService;

    public WebServer(WebConfig webConfig, Map<HttpServlet, String> servlets, AuthService authService) {
        this.webConfig = webConfig;
        this.servlets = servlets;
        this.authService = authService;
    }

    public void start() {
        server = new Server(webConfig.getPort());

        WebAuthorizationFilter authorizationFilter = new WebAuthorizationFilter(authService);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        servlets.forEach((servlet, pathSpec) -> {
            context.addServlet(servlet, pathSpec);
            Class<?> servletClass = servlet.getClass();
            if (servletClass.isAnnotationPresent(Authenticated.class)) {
                context.addFilter(authorizationFilter, pathSpec, EnumSet.allOf(DispatcherType.class));
            }
        });
        server.setHandler(context);

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "WEB-SERVER");
        serverThread.start();
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
