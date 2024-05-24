package de.fantasypixel.rework.modules.items.items.edible;

import de.fantasypixel.rework.modules.items.Item;

public abstract class Edible extends Item {

    /**
     * The health to add when consumed.
     * <b>Note:</b> the health isn't stored per character, when the player reconnect he has full health.
     */
    public abstract double getHealth();

    /**
     * The "hunger" to remove when consumed.
     * <b>Note:</b> the health is stored per character!
     */
    public abstract int getHunger();

}
