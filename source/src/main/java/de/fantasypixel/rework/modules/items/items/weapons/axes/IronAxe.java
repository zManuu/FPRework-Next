package de.fantasypixel.rework.modules.items.items.weapons.axes;

import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import org.bukkit.Material;

public class IronAxe extends Weapon {

    @Override
    public int getHitDamage() {
        return 6;
    }

    @Override
    public String getIdentifier() {
        return "IRON_AXE";
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_AXE;
    }

    @Override
    public int getDefaultPrice() {
        return 25;
    }

}
