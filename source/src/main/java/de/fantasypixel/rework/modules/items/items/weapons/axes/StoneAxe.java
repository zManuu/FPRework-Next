package de.fantasypixel.rework.modules.items.items.weapons.axes;

import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import org.bukkit.Material;

public class StoneAxe extends Weapon {

    @Override
    public int getHitDamage() {
        return 4;
    }

    @Override
    public String getIdentifier() {
        return "STONE_AXE";
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_AXE;
    }

    @Override
    public int getDefaultPrice() {
        return 15;
    }

}
