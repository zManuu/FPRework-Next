package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import org.bukkit.Location;
import org.bukkit.Material;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@ServiceProvider
public class SavePointService {

    @JsonData private JsonDataContainer<SavePoint> savePoints;
    @DataRepo private DataRepoProvider<UnlockedSavePoint> dataRepo;
    @Config private SavePointConfig config;
    @Plugin private FPRework plugin;

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
    public SavePoint getSavePointInRange(Location location) {
        for (SavePoint savePoint : this.savePoints.getEntries()) {
            var savePointLocation = savePoint.getPosition().toLocation();
            if (savePointLocation.distance(location) <= this.config.getRange())
                return savePoint;
        }

        return null;
    }

    public Set<SavePoint> getUnlockedSavePoints(int characterId) {
        return this.dataRepo.getMultiple("characterId", characterId)
                .stream()
                .map(this::getSavePoint)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

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
        // todo: implement code in data-repo to filter by multiple columns (query builder)!
        return this.getUnlockedSavePoints(characterId)
                .stream()
                .anyMatch(e -> Objects.equals(e.getId(), savePointId));
    }

    public void unlockSavePoint(int characterId, int savePointId) {
        this.dataRepo.insert(
                new UnlockedSavePoint(null, characterId, savePointId)
        );
    }

    // ADMIN

    public boolean createSavePoint(JsonPosition position, String name, Optional<String> iconMaterial) {
        if (iconMaterial.isPresent() && Material.getMaterial(iconMaterial.get()) == null) {
            this.plugin.getFpLogger().warning("Someone tried to create a save-point with the non-existing material {0}.", iconMaterial.get());
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
     * @return a set of strings representing the loaded save points or a message of non found.
     */
    public Set<String> getSavePointsList() {
        return !this.savePoints.getEntries().isEmpty()
                ?
                    this.savePoints.getEntries().stream()
                        .map(e -> String.format("%d - %s", e.getId(), e.getName()))
                        .collect(Collectors.toSet())
                : Collections.singleton("Non found.");
    }

}
