package de.fantasypixel.rework.modules.character.characters;

import de.fantasypixel.rework.modules.character.Character;
import org.bukkit.Material;

public class CharacterArcher extends Character {

    @Override
    public String getIdentifier() {
        return "CHARACTER_ARCHER";
    }

    @Override
    public String getName() {
        return "Archer";
    }

    @Override
    public String getDescription() {
        return "An archer";
    }

    @Override
    public Material getIconMaterial() {
        return Material.BOW;
    }

}
