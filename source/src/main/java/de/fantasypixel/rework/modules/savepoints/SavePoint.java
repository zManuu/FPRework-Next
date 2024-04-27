package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.jsondata.JsonDataProvider;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import lombok.Getter;

@Getter
@JsonDataProvider(path = "save-points")
public class SavePoint {

    private int id;
    private String name;
    private String iconMaterial;
    private JsonPosition position;

}
