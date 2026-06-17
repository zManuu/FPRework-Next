package de.fantasypixel.rework.framework.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a caller (user / service) registered in the auth-system.
 */
@Getter
@AllArgsConstructor
public class AuthCaller {

    private final String authName;
    private final String password;

}
