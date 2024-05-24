package de.fantasypixel.rework.modules.items.items.weapons.axes;

import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import org.bukkit.Material;

public class TestAxe extends Weapon {

    @Override
    public int getHitDamage() {
        return 3;
    }

    @Override
    public String getIdentifier() {
        return "TEST_AXE";
    }

    @Override
    public String getName() {
        return "test axe";
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_AXE;
    }

}
