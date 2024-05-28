package de.fantasypixel.rework.modules.items.items.currency;

import org.bukkit.Material;

public class Currency100 extends CurrencyItem {
    @Override
    public int getWorth() {
        return 100;
    }

    @Override
    public String getIdentifier() {
        return "CURRENCY_100";
    }

    @Override
    public Material getMaterial() {
        return Material.EMERALD_BLOCK;
    }

    @Override
    public int getDefaultPrice() {
        return -100;
    }

}
