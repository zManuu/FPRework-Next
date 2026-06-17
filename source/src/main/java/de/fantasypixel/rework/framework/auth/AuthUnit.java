package de.fantasypixel.rework.framework.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a single permission.
 */
@Getter
@AllArgsConstructor
public class AuthUnit {

    private final String name;
    private final String pathSpec;
    private final String parentUnit;

}
