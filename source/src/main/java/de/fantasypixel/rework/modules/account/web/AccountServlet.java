package de.fantasypixel.rework.modules.account.web;

import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.web.Authenticated;
import de.fantasypixel.rework.framework.web.Servlet;
import de.fantasypixel.rework.modules.account.Account;
import de.fantasypixel.rework.modules.account.AccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Servlet("/account")
@Authenticated
public class AccountServlet extends HttpServlet {

    @Service
    private AccountService accountService;

    @Override
    @Operation(summary = "Get user")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "text/plain", example = "Hello there!", schema = @Schema(implementation = String.class)))
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int accountId = Integer.parseInt(req.getParameter("accountId"));
        Account account = accountService.getAccount(accountId);
        if (account != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().write(("Name: " + account.getName()).getBytes(StandardCharsets.UTF_8));
            resp.getOutputStream().flush();
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
