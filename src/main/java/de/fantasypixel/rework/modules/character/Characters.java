package de.fantasypixel.rework.modules.character;

import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;

import java.util.Optional;

/**
 * Holds and manages the static character instances.
 */
public class Characters {

    public static final CharacterWarrior CHARACTER_WARRIOR = new CharacterWarrior();

    /**
     * Returns an optional. For a Nonnull result, use {@link PlayerCharacter#getCharacter()}
     */
    public static Optional<Character> getByName(String characterIdentifier) {
        return switch (characterIdentifier) {
            case "Warrior" -> Optional.of(CHARACTER_WARRIOR);
            default -> Optional.empty();
        };
    }

}
