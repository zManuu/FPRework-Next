package de.fantasypixel.rework.modules.items.items.currency;

import de.fantasypixel.rework.framework.provider.ExtendingIgnore;
import de.fantasypixel.rework.modules.items.Item;

@ExtendingIgnore
public abstract class CurrencyItem extends Item {

    public abstract int getWorth();

}
