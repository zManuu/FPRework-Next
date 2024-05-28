package de.fantasypixel.rework.modules.items.items.edible;

import org.bukkit.Material;

public class Steak extends Edible {
    @Override
    public double getHealth() {
        return 0.5;
    }

    @Override
    public int getHunger() {
        return 5;
    }

    @Override
    public String getIdentifier() {
        return "STEAK";
    }

    @Override
    public Material getMaterial() {
        return Material.COOKED_BEEF;
    }

    @Override
    public int getDefaultPrice() {
        return 5;
    }
}
