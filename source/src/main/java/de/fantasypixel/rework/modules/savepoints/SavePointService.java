package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.database.Query;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.discord.DiscordService;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import discord4j.rest.util.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@ServiceProvider
public class SavePointService {

    @JsonData private JsonDataContainer<SavePoint> savePoints;
    @DataRepo private DataRepoProvider<UnlockedSavePoint> dataRepo;
    @Config private SavePointConfig config;
    @Auto private FPLogger logger;
    @Service private DiscordService discordService;

    @Nullable
    private SavePoint getSavePoint(@Nullable UnlockedSavePoint unlockedSavePoint) {
        if (unlockedSavePoint == null)
            return null;

        for (SavePoint savePoint : this.savePoints.getEntries()) {
            if (Objects.equals(savePoint.getId(), unlockedSavePoint.getSavePointId())) {
                return savePoint;
            }
        }

        return null;
    }

    @Nullable
    public SavePoint getSavePointInRange(@Nonnull Location location) {
        for (SavePoint savePoint : this.savePoints.getEntries()) {
            var savePointLocation = savePoint.getPosition().toLocation();
            if (savePointLocation.distance(location) <= this.config.getRange())
                return savePoint;
        }

        return null;
    }

    @Nonnull
    public Set<SavePoint> getUnlockedSavePoints(int characterId) {
        return this.dataRepo.getMultiple(new Query("characterId", characterId))
                .stream()
                .map(this::getSavePoint)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Nonnull
    public Set<SavePoint> getLockedSavePoints(int characterId) {
        var unlockedSavePoints = this.getUnlockedSavePoints(characterId);
        var result = new HashSet<SavePoint>();

        for (SavePoint savePoint : this.savePoints.getEntries()) {
            var found = false;

            for (SavePoint unlockedSavePoint : unlockedSavePoints) {
                if (Objects.equals(savePoint.getId(), unlockedSavePoint.getId())) {
                    found = true;
                    break;
                }
            }

            if (!found)
                result.add(savePoint);
        }

        return result;
    }

    public boolean isSavePointUnlocked(int characterId, int savePointId) {
        return this.dataRepo.exists(
                new Query()
                        .where("characterId", characterId)
                        .where("savePointId", savePointId)
        );
    }

    public void unlockSavePoint(int characterId, int savePointId) {
        this.logger.info("Unlocking save point {0} for character {1}", savePointId, characterId);

        this.dataRepo.insert(
                new UnlockedSavePoint(null, characterId, savePointId)
        );
    }

    // ADMIN

    public boolean createSavePoint(@Nonnull Player player, @Nonnull JsonPosition position, @Nonnull String name, @Nonnull Optional<String> iconMaterial) {
        if (iconMaterial.isPresent() && Material.getMaterial(iconMaterial.get()) == null) {
            this.logger.warning("Someone tried to create a save-point with the non-existing material {0}.", iconMaterial.get());
            return false;
        }

        SavePoint savePoint = new SavePoint(
                null,
                name,
                iconMaterial.orElse(this.config.getDefaultIconMaterial()),
                position
        );

        if (!this.savePoints.create(savePoint))
            return false;

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_ADMIN,
                Color.GREEN,
                "Save-point create",
                "Player \"{0}\" created the save-point \"{1}\" ({2}).",
                player.getName(),
                name,
                savePoint.getId()
        );
        return true;
    }

    public boolean deleteSavePoint(@Nonnull Player player, int savePointId) {
        if (!this.savePoints.delete(savePointId))
            return false;

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_ADMIN,
                Color.RED,
                "Save-point delete",
                "Player \"{0}\" deleted the save-point {1}.",
                player.getName(),
                savePointId
        );
        return true;
    }

    /**
     * @return an array of strings representing the loaded save points or a message of non found.
     */
    @Nonnull
    public String getSavePointsList() {
        return !this.savePoints.getEntries().isEmpty()
                ?
                    String.join(
                        "\n",
                        this.savePoints.getEntries().stream()
                            .map(e -> String.format("%d - %s", e.getId(), e.getName()))
                            .toArray(String[]::new)
                    )
                : "Non found.";
    }

    /**
     * Unlocks all save-points for the given character.
     */
    public void unlockAllSavePoints(int characterId) {
        this.logger.info("Unlocking all save-points for character {0}.", characterId);

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_ADMIN,
                Color.GREEN,
                "Save-point unlock all",
                "Player-character {0} is unlocking all save-points.",
                characterId
        );

        var allSavePointIds = this.savePoints.getEntries()
                .stream()
                .map(SavePoint::getId)
                .collect(Collectors.toSet());

        allSavePointIds.forEach(savePointId -> {
            if (!this.isSavePointUnlocked(characterId, savePointId))
                this.unlockSavePoint(characterId, savePointId);
        });
    }

    /**
     * Repositions a save-point.
     * @param savePointId the save point to be moved
     * @param location the new location of the save point
     * @return whether the update was successful
     * @throws IllegalArgumentException if no savepoint was found with the given ID
     */
    public boolean repositionSavePoint(@Nonnull Player player, int savePointId, @Nonnull Location location) throws IllegalArgumentException {
        var newPosition = new JsonPosition(location);
        var savePoint = this.savePoints.getEntries()
                .stream()
                .filter(e -> Objects.equals(e.getId(), savePointId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        this.logger.info("Repositioning save-point {0} to {1}.", savePoint.getName(), location);

        savePoint.setPosition(newPosition);
        if (!this.savePoints.modify(savePoint))
            return false;

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_ADMIN,
                Color.GREEN,
                "Save-point reposition",
                "Player \"{0}\" repositioned the save-point {1}.",
                player.getName(),
                savePointId
        );

        return true;
    }

    /**
     * Renames a save-point.
     * @param savePointId the save point to be renamed
     * @param name the new name of the save point
     * @return whether the update was successful
     * @throws IllegalArgumentException if no save point was found with the given ID
     */
    public boolean renameSavePoint(@Nonnull Player player, int savePointId, @Nonnull String name) throws IllegalArgumentException {
        var savePoint = this.savePoints.getEntries()
                .stream()
                .filter(e -> Objects.equals(e.getId(), savePointId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        this.logger.info("Renaming save-point {0} to \"{1}\".", savePointId, name);

        savePoint.setName(name);

        if (!this.savePoints.modify(savePoint))
            return false;

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_ADMIN,
                Color.GREEN,
                "Save-point rename",
                "Player \"{0}\" renamed the save-point {1} to \"{2}\".",
                player.getName(),
                savePointId,
                name
        );

        return true;
    }

}
