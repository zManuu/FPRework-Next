package de.fantasypixel.rework.modules.menu;

import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.language.LanguageService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.sound.Sound;
import de.fantasypixel.rework.modules.sound.SoundService;
import de.fantasypixel.rework.modules.utils.ServerUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@ServiceProvider
public class MenuService {

    @Auto private FPLogger logger;
    @Service private ServerUtils serverUtils;
    @Service private LanguageService languageService;
    @Service private NotificationService notificationService;
    @Service private SoundService soundService;
    private final Map<Player, Menu> openedMenus;
    private final Map<Player, Boolean> menuClosedManually;

    public MenuService() {
        this.openedMenus = new HashMap<>();
        this.menuClosedManually = new HashMap<>();
    }

    /**
     * @return The currently opened menu of the player.
     */
    public Menu getOpenedMenu(@Nonnull Player player) {
        return this.openedMenus.get(player);
    }

    /**
     * @return Whether the player has currently opened a menu.
     */
    public boolean hasOpenedMenu(@Nonnull Player player) {
        return this.openedMenus.containsKey(player);
    }

    /**
     * Opens a menu to the specified player.
     */
    public void openMenu(@Nonnull Player player, @Nonnull Menu menu) {
        if (this.hasOpenedMenu(player)) {
            this.logger.warning("Player \"{0}\" tried to open menu \"{1}\", another menu is open already: \"{2}\".", player.getName(), menu.getTitle(), this.getOpenedMenu(player).getTitle());
            return;
        }

        Inventory inventory = menu.toInventory(this.languageService, player);
        player.openInventory(inventory);
        this.openedMenus.put(player, menu);
    }

    /**
     * Opens a sub-menu to the specified player.
     * A sub-menu is not a menu on the "second layer" but one that is opened from another one.
     * The opening is delayed 2 Ticks.
     */
    public void openSubMenu(@Nonnull Player player, @Nonnull Menu menu) {
        this.serverUtils.runTaskLater(() -> {
            if (this.hasOpenedMenu(player)) {
                this.logger.warning("Player \"{0}\" tried to open menu \"{1}\", another menu is open already: \"{2}\".", player.getName(), menu.getTitle(), this.getOpenedMenu(player).getTitle());
                return;
            }

            this.openedMenus.put(player, menu);
            Inventory inventory = menu.toInventory(this.languageService, player);
            player.openInventory(inventory);
        }, 2);
    }

    /**
     * Closes the currently opened menu of the player.
     */
    public void closeMenu(@Nonnull Player player) {
        if (!this.hasOpenedMenu(player)) {
            this.logger.warning("Tried to close menu for player \"{0}\" but none is opened currently.", player.getName());
            return;
        }

        this.menuClosedManually.remove(player);
        this.openedMenus.remove(player);

        this.serverUtils.runTaskLater(player::closeInventory, 1);
    }

    protected void handleMenuItemSelect(@Nullable MenuItem item, @Nonnull Player player) {
        if (item != null) {
            var itemSubMenuSupplier = item.getSubMenu();
            if (itemSubMenuSupplier != null) {
                var itemSubMenu = itemSubMenuSupplier.get();

                if (itemSubMenu == null) {
                    this.logger.warning("Tried to open a sub-menu for player {0}, but the supplier returned null. Item-Name: {1}", player.getName(), item.getDisplayName());
                    return;
                }

                this.closeMenu(player);
                this.openSubMenu(player, itemSubMenu);

            } else {

                // click-sound, close-menu and on-select are not available for items with a registered sub-menu
                if (item.getClickSound() != null)
                    this.soundService.playSound(player, item.getClickSound());

                if (item.isClosesMenu())
                    this.closeMenu(player);

                if (item.getOnSelect() != null)
                    item.getOnSelect().run();
            }

        }
    }

    protected void handleMenuClose(@Nonnull Player player, @Nonnull Menu menu) {
        var manually = this.menuClosedManually.getOrDefault(player, true);

        if (!menu.getClosable() && manually) {
            this.menuClosedManually.remove(player);

            this.notificationService.sendChatMessage(NotificationType.WARNING, player, "menu-not-closable");
            this.soundService.playSound(player, Sound.DENIED);

            this.serverUtils.runTaskLater(() -> player.openInventory(menu.toInventory(this.languageService, player)), 1);
            return;
        }

        if (menu.getOnClose() != null)
            menu.getOnClose().run();

        this.menuClosedManually.remove(player);
        this.openedMenus.remove(player);
    }

}
