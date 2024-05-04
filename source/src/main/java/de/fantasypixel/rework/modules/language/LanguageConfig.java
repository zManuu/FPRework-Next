package de.fantasypixel.rework.modules.language;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/language")
public class LanguageConfig {

    private String defaultLanguageKey;

}
