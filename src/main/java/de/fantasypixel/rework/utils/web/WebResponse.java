package de.fantasypixel.rework.utils.web;

import de.fantasypixel.rework.FPRework;
import lombok.Getter;

@Getter
public class WebResponse {

    private final int code;
    private final String body;

    public WebResponse(int code, Object body) {
        this.code = code;
        this.body = FPRework.getGson().toJson(body);
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

}
