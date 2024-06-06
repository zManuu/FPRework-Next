package de.fantasypixel.rework.modules.build;

import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.account.options.AccountOptions;
import de.fantasypixel.rework.modules.account.options.AccountOptionsService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.sound.Sound;
import de.fantasypixel.rework.modules.sound.SoundService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import javax.annotation.Nonnull;

/**
 * Provides the link between the account-option {@link AccountOptions#isBuildMode()} and spigot.
 */
@Controller
public class BuildController implements Listener {

    @Service private AccountService accountService;
    @Service private AccountOptionsService accountOptionsService;
    @Service private NotificationService notificationService;
    @Service private SoundService soundService;

    /**
     * Checks whether a given player is in the build-mode.
     * (via the {@link AccountOptionsService})
     */
    private boolean isInBuildMode(@Nonnull Player player) {
        var account = this.accountService.getAccount(player.getUniqueId());

        if (account == null || account.getId() == null)
            return false;

        var accountOptions = this.accountOptionsService.getOptions(account.getId());
        return accountOptions.isBuildMode();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var player = event.getPlayer();

        if (!this.isInBuildMode(player)) {
            event.setCancelled(true);
            event.setExpToDrop(0);
            event.setDropItems(false);
            this.notificationService.sendChatMessage(NotificationType.WARNING, player, "options-build-denied");
            this.soundService.playSound(player, Sound.DENIED);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();

        if (!this.isInBuildMode(player)) {
            event.setCancelled(true);
            event.setBuild(false);
            this.notificationService.sendChatMessage(NotificationType.WARNING, player, "options-build-denied");
            this.soundService.playSound(player, Sound.DENIED);
        }
    }

}
