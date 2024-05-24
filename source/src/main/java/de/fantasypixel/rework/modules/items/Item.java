package de.fantasypixel.rework.modules.items;

import org.bukkit.Material;

abstract public class Item {

    public static final int DEFAULT_XP_PER_LEVEL = 100;
    public static final int UNDEFINED_MIN_LEVEL = Integer.MIN_VALUE;
    public static final int DEFAULT_PRICE = 1;

    // required configuration
    public abstract String getIdentifier();
    public abstract String getName();
    public abstract Material getMaterial();
    public abstract int getDefaultPrice();

    // optional configuration
    public int getMinLevel() { return UNDEFINED_MIN_LEVEL; }

}