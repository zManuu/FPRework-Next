package de.fantasypixel.rework.modules.playercharacter;

import de.fantasypixel.rework.framework.FPLogger;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.database.Query;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.modules.account.Account;
import de.fantasypixel.rework.modules.character.Character;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.config.PositionsConfig;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

@ServiceProvider
public class PlayerCharacterService {

    private final static String CLASS_NAME = PlayerCharacterService.class.getSimpleName();

    @Auto private FPLogger logger;
    @DataRepo private DataRepoProvider<PlayerCharacter> playerCharacterRepo;
    @Config private PositionsConfig positionsConfig;

    public boolean hasCharacter(@Nonnull Account account) {
        return this.playerCharacterRepo.exists(new Query("accountId", account.getId()));
    }

    @Nonnull
    public Set<PlayerCharacter> getPlayerCharacters(@Nonnull Account account) {
        return this.playerCharacterRepo.getMultiple(new Query("accountId", account.getId()));
    }

    @Nullable
    public PlayerCharacter getActivePlayerCharacter(@Nonnull Account account) {
        return this.playerCharacterRepo.get(
                new Query()
                        .where("accountId", account.getId())
                        .where("active", true)
        );
    }

    @Nonnull
    public Location getBlackBoxLocation() {
        return this.positionsConfig.getBlackBox().toLocation();
    }

    @Nullable
    public PlayerCharacter createPlayerCharacter(int accountId, @Nonnull String name, @Nonnull Character characterClass, boolean autoActive) {
        PlayerCharacter playerCharacter = PlayerCharacter.builder()
                .accountId(accountId)
                .name(name)
                .characterClassIdentifier(characterClass.getIdentifier())
                .locWorld(this.positionsConfig.getFirstSpawn().getWorld())
                .locX(this.positionsConfig.getFirstSpawn().getX())
                .locY(this.positionsConfig.getFirstSpawn().getY())
                .locZ(this.positionsConfig.getFirstSpawn().getZ())
                .active(autoActive)
                .build();

        if (!this.playerCharacterRepo.insert(playerCharacter)) {
            this.logger.error(CLASS_NAME, "createPlayerCharacter", "Couldn't insert player-character into the database.");
            return null;
        }
        return playerCharacter;
    }

    public void savePlayerCharacterPosition(@Nonnull PlayerCharacter character, @Nonnull Location location) {
        this.logger.debug("Saving the character-position of {0}...", character.getId());

        if (location.getWorld() == null) {
            this.logger.error(CLASS_NAME, "savePlayerCharacterPosition", "Tried to save character position, no world found on the location object!");
            return;
        }

        character.setLocWorld(location.getWorld().getName());
        character.setLocX(location.getX());
        character.setLocY(location.getY());
        character.setLocZ(location.getZ());
        character.setLocYaw(location.getYaw());
        character.setLocPitch(location.getPitch());

        this.playerCharacterRepo.update(character);
        this.logger.debug("Successfully saved the character-position of {0}.", character.getId());
    }
}
