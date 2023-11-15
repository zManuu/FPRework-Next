package de.fantasypixel.rework.modules.playercharacter;

import de.fantasypixel.rework.modules.character.Character;
import de.fantasypixel.rework.modules.character.Characters;
import de.fantasypixel.rework.utils.database.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
@Builder
@Entity(tableName = "character")
public class PlayerCharacter {

    @Nullable
    private Integer id;
    private int accountId;
    private String name;

    /**
     * The CHARACTER CLASS's unique identifier
     */
    private String characterIdentifier;

    /**
     * @return the CHARACTER CLASS
     */
    public Character getCharacter() {
        var characterOptional = Characters.getByName(characterIdentifier);
        if (characterOptional.isPresent()) {
            return characterOptional.get();
        } else {
            throw new IllegalArgumentException("Character " + characterIdentifier + " does not exist");
        }
    }

}
