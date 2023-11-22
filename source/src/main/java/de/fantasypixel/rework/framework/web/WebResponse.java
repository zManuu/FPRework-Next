package de.fantasypixel.rework.framework.web;

import com.google.gson.Gson;
import de.fantasypixel.rework.FPRework;
import lombok.Getter;
import lombok.Setter;

@Getter
public class WebResponse {

    private final int code;
    private final String body;

    // the FPRework's gson object can't be wired to here.
    private static final Gson gson = new Gson();

    public WebResponse(int code, Object body) {
        this.code = code;
        this.body = gson.toJson(body);
    }

    public record WebCodeResponse(String message) {}

    // Successful responses
    public static WebResponse OK = new WebResponse(200, new WebCodeResponse("OK"));
    public static WebResponse CREATED = new WebResponse(201, new WebCodeResponse("Created"));
    public static WebResponse ACCEPTED = new WebResponse(202, new WebCodeResponse("Accepted"));

    // Client error responses
    public static WebResponse BAD_REQUEST = new WebResponse(400, new WebCodeResponse("Bad Request"));
    public static WebResponse UNAUTHORIZED = new WebResponse(401, new WebCodeResponse("Unauthorized"));
    public static WebResponse FORBIDDEN = new WebResponse(403, new WebCodeResponse("Forbidden"));
    public static WebResponse NOT_FOUND = new WebResponse(404, new WebCodeResponse("Not Found"));

    // Server error responses
    public static WebResponse INTERNAL_SERVER_ERROR = new WebResponse(500, new WebCodeResponse("Internal Server Error"));
    public static WebResponse NOT_IMPLEMENTED = new WebResponse(501, new WebCodeResponse("Not Implemented"));
    public static WebResponse SERVICE_UNAVAILABLE = new WebResponse(503, new WebCodeResponse("Service Unavailable"));

    public static class Codes {

        // Successful responses
        public static int OK = 200;
        public static int CREATED = 201;
        public static int ACCEPTED = 202;

        // Client error responses
        public static int BAD_REQUEST = 400;
        public static int UNAUTHORIZED = 401;
        public static int FORBIDDEN = 403;
        public static int NOT_FOUND = 404;

        // Server error responses
        public static int INTERNAL_SERVER_ERROR = 500;
        public static int NOT_IMPLEMENTED = 501;
        public static int SERVICE_UNAVAILABLE = 503;

    }

}
