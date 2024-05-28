package de.fantasypixel.rework.modules.character;

import de.fantasypixel.rework.modules.character.characters.CharacterArcher;
import de.fantasypixel.rework.modules.character.characters.CharacterWarrior;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Holds and manages the static character instances.
 */
public class Characters {

    public static Set<Character> CHARACTERS = new LinkedHashSet<>(Set.of(
            new CharacterWarrior(),
            new CharacterArcher()
    ));

    /**
     * Returns an optional. For a Nonnull result, use {@link PlayerCharacter#getCharacter()}
     */
    @Nonnull
    public static Optional<Character> getByIdentifier(@Nullable String characterClassIdentifier) {
        if (characterClassIdentifier == null)
            return Optional.empty();

        return CHARACTERS.stream()
                .filter(e -> e.getIdentifier().equalsIgnoreCase(characterClassIdentifier))
                .findFirst();
    }

}
