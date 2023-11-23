package de.fantasypixel.rework.framework;

import lombok.Getter;

/**
 * Holds the configuration, loaded from plugins/FP-Rework/config.json via gson.
 */
@Getter
public class FPConfig {

    String databaseHost;
    String databaseUser;
    String databasePassword;
    String databasePort;
    String databaseName;
    boolean databaseRequired;
    String version;
    int controllerStartupTimeout;
    int webServerPort;

}
