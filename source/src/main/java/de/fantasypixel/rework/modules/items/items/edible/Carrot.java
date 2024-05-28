package de.fantasypixel.rework.modules.items.items.edible;

import org.bukkit.Material;

public class Carrot extends Edible {
    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public int getHunger() {
        return 2;
    }

    @Override
    public String getIdentifier() {
        return "CARROT";
    }

    @Override
    public Material getMaterial() {
        return Material.CARROT;
    }

    @Override
    public int getDefaultPrice() {
        return 2;
    }
}
