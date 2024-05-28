package de.fantasypixel.rework.modules.menu.design;

import org.bukkit.event.inventory.InventoryType;

public abstract class MenuDesign {

    public abstract InventoryType getInventoryType();
    public abstract Integer[] getItemSlots();
    public abstract Integer[] getSpecialItemSlots();

}
