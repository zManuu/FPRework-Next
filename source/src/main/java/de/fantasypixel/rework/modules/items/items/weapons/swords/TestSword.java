package de.fantasypixel.rework.modules.items.items.weapons.swords;

import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import org.bukkit.Material;

public class TestSword extends Weapon {

    @Override
    public int getHitDamage() {
        return 1;
    }

    @Override
    public String getIdentifier() {
        return "TEST_SWORD";
    }

    @Override
    public String getName() {
        return "test sword";
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_SWORD;
    }

}
