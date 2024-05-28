package de.fantasypixel.rework.modules.items.items.weapons.swords;

import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import org.bukkit.Material;

public class IronSword extends Weapon {

    @Override
    public int getHitDamage() {
        return 7;
    }

    @Override
    public String getIdentifier() {
        return "IRON_SWORD";
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getDefaultPrice() {
        return 30;
    }

}
