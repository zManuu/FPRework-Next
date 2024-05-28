package de.fantasypixel.rework.modules.items.items.edible;

import org.bukkit.Material;

public class BakedPotato extends Edible {
    @Override
    public double getHealth() {
        return 0.5;
    }

    @Override
    public int getHunger() {
        return 4;
    }

    @Override
    public String getIdentifier() {
        return "BAKED_POTATO";
    }

    @Override
    public Material getMaterial() {
        return Material.BAKED_POTATO;
    }

    @Override
    public int getDefaultPrice() {
        return 4;
    }
}
