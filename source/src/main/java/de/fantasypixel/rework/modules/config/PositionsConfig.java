package de.fantasypixel.rework.modules.config;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/positions")
public class PositionsConfig {

    private JsonPosition blackBox;
    private JsonPosition firstSpawn;

}
