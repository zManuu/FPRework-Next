package de.fantasypixel.rework.modules.character.characters;

import de.fantasypixel.rework.modules.character.Character;
import org.bukkit.Material;

public class CharacterWarrior extends Character {

    @Override
    public String getIdentifier() {
        return "CHARACTER_WARRIOR";
    }

    @Override
    public String getName() {
        return "Warrior";
    }

    @Override
    public String getDescription() {
        return "A warrior";
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public Material getIconMaterial() {
        return Material.IRON_SWORD;
    }

}
