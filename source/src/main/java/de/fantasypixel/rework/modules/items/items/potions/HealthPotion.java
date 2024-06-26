package de.fantasypixel.rework.modules.items.items.potions;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.LinkedHashSet;
import java.util.Set;

public class HealthPotion extends Potion {

    @Override
    public PotionType getPotionMaterial() {
        return PotionType.INSTANT_HEAL;
    }

    @Override
    public Set<PotionEffect> getEffects() {
        return new LinkedHashSet<>(Set.of(
                new PotionEffect(PotionEffectType.HEAL, Potion.NULL_DURATION, 1)
        ));
    }

    @Override
    public String getIdentifier() {
        return "HEALTH_POTION";
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
