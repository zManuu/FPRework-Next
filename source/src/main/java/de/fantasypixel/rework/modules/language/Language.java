package de.fantasypixel.rework.modules.language;

import de.fantasypixel.rework.framework.jsondata.JsonDataProvider;
import lombok.Getter;

import java.util.Map;

@Getter
@JsonDataProvider(path = "language")
public class Language {

    private String key;
    private Map<String, String> dictionary;

}
