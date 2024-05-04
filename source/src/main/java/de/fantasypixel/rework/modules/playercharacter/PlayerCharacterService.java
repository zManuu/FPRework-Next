package de.fantasypixel.rework.modules.playercharacter;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.modules.account.Account;
import de.fantasypixel.rework.modules.character.Character;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.config.PositionsConfig;
import org.bukkit.Location;

import java.util.Set;

@ServiceProvider
public class PlayerCharacterService {

    private final static String CLASS_NAME = PlayerCharacterService.class.getSimpleName();

    @Plugin private FPRework plugin;
    @DataRepo private DataRepoProvider<PlayerCharacter> playerCharacterRepo;
    @Config private PositionsConfig positionsConfig;

    public boolean hasCharacter(Account account) {
        return this.playerCharacterRepo.exists("accountId", account.getId());
    }

    public Set<PlayerCharacter> getPlayerCharacters(Account account) {
        return this.playerCharacterRepo.getMultiple("accountId", account.getId());
    }

    public PlayerCharacter getActivePlayerCharacter(Account account) {
        // todo: implement a way to filter by multiple fields in the DataRepository
        return this.getPlayerCharacters(account)
                .stream()
                .filter(PlayerCharacter::isActive)
                .findFirst()
                .orElse(null);
    }

    public Location getBlackBoxLocation() {
        return this.positionsConfig.getBlackBox().toLocation();
    }

    public PlayerCharacter createPlayerCharacter(int accountId, String name, Character characterClass, boolean autoActive) {
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
            this.plugin.getFpLogger().error(CLASS_NAME, "createPlayerCharacter", "Couldn't insert player-character into the database.");
            return null;
        }
        return playerCharacter;
    }

    public void savePlayerCharacterPosition(PlayerCharacter character, Location location) {
        if (location.getWorld() == null) {
            this.plugin.getFpLogger().error(CLASS_NAME, "savePlayerCharacterPosition", "Tried to save character position, no world found on the location object!");
            return;
        }

        character.setLocWorld(location.getWorld().getName());
        character.setLocX(location.getX());
        character.setLocY(location.getY());
        character.setLocZ(location.getZ());
        character.setLocYaw(location.getYaw());
        character.setLocPitch(location.getPitch());

        this.playerCharacterRepo.update(character);
        this.plugin.getFpLogger().debug("Successfully saved the character-position of {0}.", character.getId());
    }
}
