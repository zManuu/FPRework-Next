package de.fantasypixel.rework.modules.menu;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ServiceProvider
public class MenuService {

    @Plugin private FPRework plugin;
    private final Map<Player, Menu> openedMenus;
    private final Map<Player, Boolean> menuClosedManually;

    public MenuService() {
        this.openedMenus = new HashMap<>();
        this.menuClosedManually = new HashMap<>();
    }

    /**
     * @return The currently opened menu of the player.
     */
    public Menu getOpenedMenu(Player player) {
        return this.openedMenus.get(player);
    }

    /**
     * @return Whether the player has currently opened a menu.
     */
    public boolean hasOpenedMenu(Player player) {
        return this.openedMenus.containsKey(player);
    }

    /**
     * Opens a menu to the specified player.
     */
    public void openMenu(Player player, Menu menu) {
        if (this.hasOpenedMenu(player)) {
            this.plugin.getFpLogger().warning("Player '" + player.getName() + "' tried to open menu '" + menu.getTitle() + "', another is open already: '" + this.getOpenedMenu(player).getTitle() + "'.");
            return;
        }

        Inventory inventory = menu.toInventory();
        player.openInventory(inventory);
        this.openedMenus.put(player, menu);
    }

    /**
     * Closes the currently opened menu of the player.
     */
    public void closeMenu(Player player) {
        if (!this.hasOpenedMenu(player)) {
            this.plugin.getFpLogger().warning("Player '" + player.getName() + "' tried close menu but none is opened currently.");
            return;
        }

        this.menuClosedManually.put(player, false);
        this.openedMenus.remove(player);

        // todo: util layer for timers
        Bukkit.getScheduler().runTaskLater(this.plugin, player::closeInventory, 1);
    }

    protected void handleMenuItemSelect(@Nullable MenuItem item, @Nonnull Player player) {
        if (item != null) {
            if (item.getOnSelect() != null)
                item.getOnSelect().run();

            if (item.isClosesMenu())
                this.closeMenu(player);
        }
    }

    protected void handleMenuClose(Player player, Menu menu) {
        var manually = this.menuClosedManually.getOrDefault(player, true);

        if (!menu.isClosable() && manually) {
            this.menuClosedManually.remove(player);

            // todo: util layer for timers
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> player.openInventory(menu.toInventory()), 1);
            return;
        }

        if (menu.getOnClose() != null)
            menu.getOnClose().run();

        this.menuClosedManually.remove(player);
        this.openedMenus.remove(player);
    }

}
