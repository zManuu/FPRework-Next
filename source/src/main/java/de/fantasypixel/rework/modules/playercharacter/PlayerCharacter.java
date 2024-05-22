package de.fantasypixel.rework.modules.playercharacter;

import de.fantasypixel.rework.modules.character.Character;
import de.fantasypixel.rework.modules.character.Characters;
import de.fantasypixel.rework.framework.database.Entity;
import de.fantasypixel.rework.modules.utils.Locatable;
import lombok.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "characters")
public class PlayerCharacter extends Locatable {

    @Nullable
    private Integer id;
    private int accountId;
    private String name;
    private String locWorld;
    private double locX;
    private double locY;
    private double locZ;
    private float locYaw;
    private float locPitch;
    private boolean active;

    /**
     * The CHARACTER CLASS's unique identifier
     */
    private String characterClassIdentifier;

    /**
     * @return the CHARACTER CLASS
     */
    @Nonnull
    public Character getCharacter() throws IllegalArgumentException {
        var characterOptional = Characters.getByIdentifier(this.characterClassIdentifier);
        if (characterOptional.isPresent()) {
            return characterOptional.get();
        } else {
            throw new IllegalArgumentException("Character " + this.characterClassIdentifier + " does not exist!");
        }
    }

}
