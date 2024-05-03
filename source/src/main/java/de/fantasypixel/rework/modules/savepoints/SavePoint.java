package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.jsondata.JsonDataProvider;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
@JsonDataProvider(path = "save-points")
@AllArgsConstructor
public class SavePoint {

    @Nullable private Integer id;
    private String name;
    private String iconMaterial;
    private JsonPosition position;

}
