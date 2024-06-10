package de.fantasypixel.rework.modules.discord;

import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.events.OnDisable;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.utils.DateUtils;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.text.MessageFormat;

@Controller
public class DiscordController {

    @Service private DiscordService discordService;
    @Service private DateUtils dateUtils;

    @OnEnable
    public void onEnable() {
        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_SERVER,
                EmbedCreateSpec.builder()
                        .color(Color.GREEN)
                        .title("Server status")
                        .description(MessageFormat.format("The server is online now. ({0})", this.dateUtils.getCurrentDateTime()))
                        .build()
        );
    }

    @OnDisable
    public void onDisable() {
        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_SERVER,
                EmbedCreateSpec.builder()
                        .color(Color.RED)
                        .title("Server status")
                        .description(MessageFormat.format("The server is offline now. ({0})", this.dateUtils.getCurrentDateTime()))
                        .build()
        );
    }

}
