package de.fantasypixel.rework.modules.items.items.currency;

import org.bukkit.Material;

public class Currency1 extends CurrencyItem {

    @Override
    public int getWorth() {
        return 1;
    }

    @Override
    public String getIdentifier() {
        return "CURRENCY_1";
    }

    @Override
    public String getName() {
        return "Currency one";
    }

    @Override
    public Material getMaterial() {
        return Material.EMERALD;
    }

}
