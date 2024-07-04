package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.database.Entity;
import lombok.*;

import javax.annotation.Nullable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "save_points")
public class UnlockedSavePoint {

    @Nullable private Integer id;
    private int characterId;
    private int savePointId;

}
