package de.fantasypixel.rework.modules.admin;

import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.ReloadManager;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.sound.Sound;
import de.fantasypixel.rework.modules.sound.SoundService;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@ServiceProvider
public class AdminService {

    @Auto private FPLogger logger;
    @Auto private ReloadManager reloadManager;
    @Service private NotificationService notificationService;
    @Service private SoundService soundService;

    /**
     * Performs a reload and keeps the player up-to-date on the status.
     */
    public void doReload(@Nonnull Player fromPlayer) {
        this.logger.info("Player {0} issued a reload. Proceeding...", fromPlayer.getName());
        this.notificationService.sendChatMessage(fromPlayer, "admin-reload-start");

        this.reloadManager.doReload();

        this.notificationService.sendChatMessage(fromPlayer, "admin-reload-end");
        this.soundService.playSound(fromPlayer, Sound.SUCCESS);
    }

}
