package de.fantasypixel.rework.modules.admin;

import de.fantasypixel.rework.framework.database.DatabaseManager;
import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.ReloadManager;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.discord.DiscordService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.sound.Sound;
import de.fantasypixel.rework.modules.sound.SoundService;
import de.fantasypixel.rework.modules.utils.ServerUtils;
import discord4j.rest.util.Color;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@ServiceProvider
public class AdminService {

    @Auto private FPLogger logger;
    @Auto private ReloadManager reloadManager;
    @Auto private DatabaseManager databaseManager;
    @Service private NotificationService notificationService;
    @Service private SoundService soundService;
    @Service private DiscordService discordService;
    @Service private ServerUtils serverUtils;

    /**
     * Performs a reload and keeps the player up-to-date on the status.
     */
    public void doReload(@Nonnull Player fromPlayer) {
        this.logger.info("Player {0} issued a reload. Proceeding...", fromPlayer.getName());
        this.notificationService.sendChatMessage(fromPlayer, "admin-reload-start");

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_ADMIN,
                Color.RED,
                "Reload",
                "Player \"{0}\" issued a reload.",
                fromPlayer.getName()
        );

        this.reloadManager.doReload();

        this.notificationService.sendChatMessage(fromPlayer, "admin-reload-end");
        this.soundService.playSound(fromPlayer, Sound.SUCCESS);
    }

    /**
     * Kicks all players and then clears the whole database.
     */
    public void clearDatabase(@Nonnull Player fromPlayer) {
        this.logger.info("Player {0} issued a database-clear. Proceeding...", fromPlayer.getName());
        this.notificationService.sendChatMessage(fromPlayer, "admin-cleardb-start");

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_ADMIN,
                Color.RED,
                "Database-Clear",
                "Player \"{0}\" issued a database-clear.",
                fromPlayer.getName()
        );

        this.serverUtils.kickAllPlayers("... Database-Clear ...");
        this.databaseManager.clearDatabase();
    }
}
