package de.fantasypixel.rework.modules.character;

import de.fantasypixel.rework.framework.provider.Extending;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * Holds and manages the static character instances.
 */
public class Characters {

    @Extending
    public static Set<Character> CHARACTERS;

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
