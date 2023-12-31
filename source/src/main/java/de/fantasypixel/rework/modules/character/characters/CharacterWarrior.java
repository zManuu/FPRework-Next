package de.fantasypixel.rework.modules.character.characters;

import de.fantasypixel.rework.modules.character.Character;

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

}
