package de.fantasypixel.rework.modules.config;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/database")
public class DatabaseConfig {

    String host;
    String user;
    String password;
    String port;
    String name;

}
