package de.fantasypixel.rework.modules.admin.web;

import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.web.Authenticated;
import de.fantasypixel.rework.framework.web.Servlet;
import de.fantasypixel.rework.modules.admin.AdminService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Servlet("/admin/reload")
@Authenticated
public class AdminReloadServlet extends HttpServlet {

    @Service
    private AdminService adminService;

    @Override
    @Operation(summary = "Reload the FPRework plugin.")
    @APIResponse(responseCode = "200", description = "Reload was issued.")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            adminService.doReload();
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }
        resp.getOutputStream().write("Reload was issued.".getBytes(StandardCharsets.UTF_8));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

}
