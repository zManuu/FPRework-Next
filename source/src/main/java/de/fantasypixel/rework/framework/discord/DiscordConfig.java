package de.fantasypixel.rework.framework.discord;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

import java.util.Map;

@Getter
@ConfigProvider(path = "config/discord")
public class DiscordConfig {

    private Map<FPDiscordChannel, String> channelMap;

}
