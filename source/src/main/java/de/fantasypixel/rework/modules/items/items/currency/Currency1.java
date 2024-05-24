package de.fantasypixel.rework.modules.items.items.currency;

import org.bukkit.Material;

public class Currency1 extends CurrencyItem {

    public final static String IDENTIFIER = "CURRENCY_1";

    @Override
    public int getWorth() {
        return 1;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getName() {
        return "Currency one";
    }

    @Override
    public Material getMaterial() {
        return Material.EMERALD;
    }

    @Override
    public int getDefaultPrice() {
        return -1;
    }

}
