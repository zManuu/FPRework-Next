package de.fantasypixel.rework.modules.items.items.potions;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.LinkedHashSet;
import java.util.Set;

public class SpeedPotion extends Potion {

    @Override
    public PotionType getPotionMaterial() {
        return PotionType.SPEED;
    }

    @Override
    public Set<PotionEffect> getEffects() {
        return new LinkedHashSet<>(Set.of(
                new PotionEffect(PotionEffectType.SPEED, 100, 2)
        ));
    }

    @Override
    public String getIdentifier() {
        return "SPEED_POTION";
    }

    @Override
    public Material getMaterial() {
        return Material.POTION;
    }

    @Override
    public int getDefaultPrice() {
        return 25;
    }

}
