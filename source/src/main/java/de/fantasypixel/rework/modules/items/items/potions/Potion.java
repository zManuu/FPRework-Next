package de.fantasypixel.rework.modules.items.items.potions;

import de.fantasypixel.rework.modules.items.Item;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.Set;

public abstract class Potion extends Item {

    /**
     * Can be used to mark a potion-effect as instant.
     * In the item-lore the duration won't be displayed.
     */
    public static final int NULL_DURATION = 1;

    public abstract PotionType getPotionMaterial();
    public abstract Set<PotionEffect> getEffects();

    @Override
    public Material getMaterial() {
        return Material.POTION;
    }

}
