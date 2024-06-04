package de.fantasypixel.rework.framework.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class FPLoggerConfig {

    private FPLogger.LogLevel logLevel;
    private int sectionIndentation;
    private boolean allGroups;
    private Map<String, Boolean> groups;

}
