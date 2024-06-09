package de.fantasypixel.rework.modules.discord;

import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.events.OnDisable;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;

@Controller
public class DiscordController {

    @Service private DiscordService discordService;

    @OnEnable
    public void onEnable() {
        this.discordService.sendMessage(FPDiscordChannel.SERVER_STATUS, "Server is online.");
    }

    @OnDisable
    public void onDisable() {
        this.discordService.sendMessage(FPDiscordChannel.SERVER_STATUS, "Server is offline.");
    }

}
