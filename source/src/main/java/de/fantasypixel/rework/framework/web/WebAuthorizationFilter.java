package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.auth.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Base64;

public class WebAuthorizationFilter implements Filter {

    private final AuthService authService;

    public WebAuthorizationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!((request instanceof HttpServletRequest httpRequest) && (response instanceof HttpServletResponse httpResponse))) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            denyRequest(httpResponse, "Missing Authorization header.");
            return;
        }

        if (authHeader.startsWith(HttpServletRequest.BASIC_AUTH)) {
            String basicData = authHeader.substring(HttpServletRequest.BASIC_AUTH.length() + 1);
            String basicDataDecoded = new String(Base64.getDecoder().decode(basicData));
            String[] basicDataSplit = basicDataDecoded.split(":");
            if (basicDataSplit.length != 2) {
                denyRequest(httpResponse, "Basic data is of wrong format.");
                return;
            }
            String username = basicDataSplit[0];
            String password = basicDataSplit[1];
            AuthCaller authCaller = new AuthCaller(username, password);
            AuthLevel authLevel = AuthLevel.ofHttpMethod(httpRequest.getMethod());
            try {
                authService.verify(authCaller, httpRequest.getServletPath(), authLevel);
                chain.doFilter(request, response);
            } catch (AuthException e) {
                if (e instanceof AuthenticationException) {
                    denyRequest(httpResponse, "Invalid credentials.");
                } else if (e instanceof AuthorizationException) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "No access.");
                } else {
                    httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
                }
            }
        } else {
            denyRequest(httpResponse, "Authorization types other than Basic are not supported.");
        }
    }

    private void denyRequest(HttpServletResponse response, String reason) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, reason);
    }

}
