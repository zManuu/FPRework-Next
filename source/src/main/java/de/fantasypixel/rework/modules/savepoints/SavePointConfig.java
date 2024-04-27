package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/save-points")
public class SavePointConfig {

    private double range;

}
