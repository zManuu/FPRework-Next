package de.fantasypixel.rework.modules.notification;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/notification")
public class NotificationConfig {

    private String chatPluginPrefix;

}
