package de.fantasypixel.rework.modules.items.items.weapons.swords;

import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import org.bukkit.Material;

public class StoneSword extends Weapon {

    @Override
    public int getHitDamage() {
        return 4;
    }

    @Override
    public String getIdentifier() {
        return "STONE_SWORD";
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_SWORD;
    }

    @Override
    public int getDefaultPrice() {
        return 15;
    }

}
