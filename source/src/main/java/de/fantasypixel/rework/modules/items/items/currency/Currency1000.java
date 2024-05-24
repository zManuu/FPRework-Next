package de.fantasypixel.rework.modules.items.items.currency;

import org.bukkit.Material;

public class Currency1000 extends CurrencyItem {

    @Override
    public int getWorth() {
        return 1000;
    }

    @Override
    public String getIdentifier() {
        return "CURRENCY_1000";
    }

    @Override
    public String getName() {
        return "Currency 1000";
    }

    @Override
    public Material getMaterial() {
        return Material.EMERALD_BLOCK;
    }

    @Override
    public int getDefaultPrice() {
        return -1000;
    }

}
