package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.database.Query;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import org.bukkit.Location;
import org.bukkit.Material;

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
        this.dataRepo.insert(
                new UnlockedSavePoint(null, characterId, savePointId)
        );
    }

    // ADMIN

    public boolean createSavePoint(@Nonnull JsonPosition position, @Nonnull String name, @Nonnull Optional<String> iconMaterial) {
        if (iconMaterial.isPresent() && Material.getMaterial(iconMaterial.get()) == null) {
            this.logger.warning("Someone tried to create a save-point with the non-existing material {0}.", iconMaterial.get());
            return false;
        }

        return this.savePoints.create(
                new SavePoint(
                        null,
                        name,
                        iconMaterial.orElse(this.config.getDefaultIconMaterial()),
                        position
                )
        );
    }

    public boolean deleteSavePoint(int savePointId) {
        return this.savePoints.delete(savePointId);
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

}
