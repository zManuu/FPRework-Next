package de.fantasypixel.rework.framework.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * All possible auth-levels.
 */
@Getter
@AllArgsConstructor
public enum AuthLevel {

    ADMIN(9),
    MODIFY(2),
    VIEW(1);

    private final int level;

    public static AuthLevel ofHttpMethod(String method) {
        return switch (method) {
            case "GET" -> VIEW;
            case "POST", "PUT", "DELETE" -> MODIFY;
            default -> throw new IllegalArgumentException("Unexpected value: " + method);
        };
    }
}
