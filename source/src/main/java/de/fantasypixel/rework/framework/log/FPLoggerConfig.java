package de.fantasypixel.rework.framework.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class FPLoggerConfig {

    private int sectionIndentation;
    private Map<String, Boolean> groups;

}
