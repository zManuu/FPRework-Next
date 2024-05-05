package de.fantasypixel.rework.modules.character;

import de.fantasypixel.rework.modules.character.characters.CharacterArcher;
import de.fantasypixel.rework.modules.character.characters.CharacterWarrior;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Holds and manages the static character instances.
 */
public class Characters {

    public static final CharacterWarrior CHARACTER_WARRIOR = new CharacterWarrior();
    public static final CharacterArcher CHARACTER_ARCHER = new CharacterArcher();

    public static Set<Character> CHARACTERS = Set.of(CHARACTER_WARRIOR, CHARACTER_ARCHER);

    /**
     * Returns an optional. For a Nonnull result, use {@link PlayerCharacter#getCharacter()}
     */
    @Nonnull
    public static Optional<Character> getByIdentifier(@Nullable String characterClassIdentifier) {
        if (characterClassIdentifier == null)
            return Optional.empty();

        return switch (characterClassIdentifier) {
            case "Warrior" -> Optional.of(CHARACTER_WARRIOR);
            case "Archer" -> Optional.of(CHARACTER_ARCHER);
            default -> Optional.empty();
        };
    }


}
