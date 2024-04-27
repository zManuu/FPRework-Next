package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ServiceProvider(name = "save-points")
public class SavePointService {

    @JsonData private Set<SavePoint> savePoints;
    @DataRepo(type = UnlockedSavePoint.class) private DataRepoProvider<UnlockedSavePoint> dataRepo;
    @Config private SavePointConfig config;

    @Nullable
    private SavePoint getSavePoint(@Nullable UnlockedSavePoint unlockedSavePoint) {
        if (unlockedSavePoint == null || unlockedSavePoint.getId() == null)
            return null;

        for (SavePoint savePoint : this.savePoints) {
            if (savePoint.getId() == unlockedSavePoint.getId()) {
                return savePoint;
            }
        }

        return null;
    }

    @Nullable
    public SavePoint getSavePointInRange(Location location) {
        for (SavePoint savePoint : this.savePoints) {
            var savePointLocation = savePoint.getPosition().toLocation(location.getWorld());
            if (savePointLocation.distance(location) <= this.config.getRange())
                return savePoint;
        }

        return null;
    }

    public Set<SavePoint> getUnlockedSavePoints(int characterId) {
        return this.dataRepo.getMultiple("characterId", characterId)
                .stream()
                .map(this::getSavePoint)
                .collect(Collectors.toSet());
    }

    public boolean isSavePointUnlocked(int characterId, int savePointId) {
        // todo: implement code in data-repo to filter by multiple columns (query builder)!
        return this.getUnlockedSavePoints(characterId)
                .stream()
                .anyMatch(e -> e.getId() == savePointId);
    }

    public void unlockSavePoint(int characterId, int savePointId) {
        this.dataRepo.insert(
                new UnlockedSavePoint(null, characterId, savePointId)
        );
    }

}
