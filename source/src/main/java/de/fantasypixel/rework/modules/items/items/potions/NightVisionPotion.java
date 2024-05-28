package de.fantasypixel.rework.modules.items.items.potions;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.LinkedHashSet;
import java.util.Set;

public class NightVisionPotion extends Potion {

    @Override
    public PotionType getPotionMaterial() {
        return PotionType.NIGHT_VISION;
    }

    @Override
    public Set<PotionEffect> getEffects() {
        return new LinkedHashSet<>(Set.of(
                new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 1)
        ));
    }

    @Override
    public String getIdentifier() {
        return "NIGHT_VISION_POTION";
    }

    @Override
    public int getDefaultPrice() {
        return 20;
    }

}
