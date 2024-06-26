package de.fantasypixel.rework.modules.character;

import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public abstract class Character {

    public static final int DEFAULT_XP_PER_LEVEL = 100;
    public static final int DEFAULT_MAX_LEVEL = 30;

    // required configuration
    public abstract String getIdentifier();
    public abstract String getName();
    public abstract String getDescription();
    public abstract Material getIconMaterial();

    // optional configuration
    public int getMaxLevel() { return DEFAULT_MAX_LEVEL; }
    public int getXpPerLevel() { return DEFAULT_XP_PER_LEVEL; }

    // optional events
    public void onLevelUp(@Nonnull Player player, @Nonnull PlayerCharacter playerCharacter, int oldLevel, int newLevel) {}
    public void onCharacterCreate(@Nonnull Player player, @Nonnull PlayerCharacter playerCharacter) {}

}