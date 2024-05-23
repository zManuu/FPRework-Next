package de.fantasypixel.rework.modules.items;

import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

abstract public class Item {

    public static final int DEFAULT_XP_PER_LEVEL = 100;
    public static final int UNDEFINED_MIN_LEVEL = Integer.MIN_VALUE;

    // required configuration
    public abstract String getIdentifier();
    public abstract String getName();
    public abstract String getDescription();
    public abstract Material getMaterial();
    public abstract String getDisplayName();

    // optional configuration
    public int getMinLevel() { return UNDEFINED_MIN_LEVEL; }

    // optional events
    public void onRightClick(@Nonnull Player player, @Nonnull PlayerCharacter playerCharacter) {}
    public void onLeftClick(@Nonnull Player player, @Nonnull PlayerCharacter playerCharacter) {}
    public void onPickup(@Nonnull Player player, @Nonnull PlayerCharacter playerCharacter) {}

}