package de.fantasypixel.rework.modules.sound;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Set;

@Getter
@ConfigProvider(path = "config/sound")
public class SoundConfig {

    /**
     * Contains the sounds mappings.
     */
    private Set<SoundMapping> soundMappings;

    /**
     * A single sound mapping (identifier to bukkit sound & category).
     */
    @Getter
    @AllArgsConstructor
    public static class SoundMapping {

        private String identifier;
        private String sound;
        @Nullable private String category;

    }

}
