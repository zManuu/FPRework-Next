package de.fantasypixel.rework.modules.menu;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
public class Menu {

    private InventoryType type;
    private String title;
    private Set<MenuItem> items;
    private boolean closable;
    private Runnable onClose;

    public Inventory toInventory() {
        var inventory = Bukkit.createInventory(
                null,
                this.type,
                this.title
        );

        this.items.forEach(item -> inventory.setItem(item.getSlot(), item.toItemStack()));

        return inventory;
    }

}
