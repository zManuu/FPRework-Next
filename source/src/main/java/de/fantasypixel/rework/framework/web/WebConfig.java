package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/web")
public class WebConfig {

    private int port;

}
