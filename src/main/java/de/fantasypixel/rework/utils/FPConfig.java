package de.fantasypixel.rework.utils;

import lombok.Getter;

/**
 * Holds the configuration, loaded from plugins/FP-Rework/config.json via gson.
 */
@Getter
public class FPConfig {

    private String databaseHost;
    private String databaseUser;
    private String databasePassword;
    private String databasePort;
    private String databaseName;
    private String version;
    private int controllerStartupTimeout;

}
