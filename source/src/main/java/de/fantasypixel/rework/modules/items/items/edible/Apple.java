package de.fantasypixel.rework.modules.items.items.edible;

import org.bukkit.Material;

public class Apple extends Edible {

    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public int getHunger() {
        return 1;
    }

    @Override
    public String getIdentifier() {
        return "APPLE";
    }

    @Override
    public Material getMaterial() {
        return Material.APPLE;
    }

    @Override
    public int getDefaultPrice() {
        return 1;
    }

}
