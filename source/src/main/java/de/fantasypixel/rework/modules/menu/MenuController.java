package de.fantasypixel.rework.modules.menu;

import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Optional;

@Controller
public class MenuController implements Listener {

    @Service(name = "menu")
    private MenuService menuService;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!this.menuService.hasOpenedMenu(player))
            return;

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        Menu menu = this.menuService.getOpenedMenu(player);
        MenuItem menuItem = menu.getItems()
                .stream()
                .filter(e -> e.getSlot() == event.getSlot())
                .findFirst()
                .orElse(null);

        this.menuService.handleMenuItemSelect(menuItem, player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;

        if (!this.menuService.hasOpenedMenu(player))
            return;

        Menu menu = this.menuService.getOpenedMenu(player);

        this.menuService.handleMenuClose(player, menu);
    }

}
