package de.fantasypixel.rework.modules.items.items.weapons;

import de.fantasypixel.rework.framework.provider.ExtendingIgnore;
import de.fantasypixel.rework.modules.items.Item;

@ExtendingIgnore
public abstract class Weapon extends Item {

    public abstract int getHitDamage();

}
