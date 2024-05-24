package de.fantasypixel.rework.modules.items;

import org.bukkit.Material;

abstract public class Item {

    public static final int DEFAULT_XP_PER_LEVEL = 100;
    public static final int UNDEFINED_MIN_LEVEL = Integer.MIN_VALUE;

    // required configuration
    public abstract String getIdentifier();
    public abstract String getName();
    public abstract Material getMaterial();

    // optional configuration
    public int getMinLevel() { return UNDEFINED_MIN_LEVEL; }

}