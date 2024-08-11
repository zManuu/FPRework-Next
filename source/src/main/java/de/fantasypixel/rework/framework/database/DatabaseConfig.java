package de.fantasypixel.rework.framework.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The db-config. Note: This config is read from environment variables, no gson file!
 */
@Getter
@AllArgsConstructor
public class DatabaseConfig {

    DatabaseType type;
    String host;
    String user;
    String password;
    String port;
    String name;
    boolean preload;
    int preloadLimit;

}
