package de.fantasypixel.rework.framework.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthGrant {

    private final String authCaller;
    private final String authUnit;
    private final AuthLevel authLevel;

}
