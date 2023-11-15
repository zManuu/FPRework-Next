package de.fantasypixel.rework.utils;

import org.bukkit.Bukkit;

public class FPLogger {

    public enum LogLevel {
        ERROR,
        WARNING,
        INFO,
        ENTERING,
        EXITING,
        DEBUG
    }

    private final String messageFormat;
    private final String errorFormat;

    public FPLogger() {
        this.messageFormat = "<LEVEL> | <MESSAGE>";
        this.errorFormat = "<CLASS>::<METHOD> <ERROR>";
    }

    public void info(String message) {
        this.resolve(LogLevel.INFO, message);
    }

    public void error(String fromClass, String fromMethod, Throwable throwable) {
        this.resolve(
                LogLevel.ERROR,
                this.errorFormat
                        .replaceAll("<CLASS>", fromClass)
                        .replaceAll("<METHOD>", fromMethod)
                        .replaceAll("<ERROR>", throwable.getMessage())
        );
    }

    public void warning(String message) {
        this.resolve(LogLevel.WARNING, message);
    }

    public void debug(String message) {
        this.resolve(LogLevel.DEBUG, message);
    }

    public void entering(String fromClass, String fromMethod) {
        this.resolve(
                LogLevel.ENTERING,
                fromClass + "::" + fromMethod
        );
    }

    public void exiting(String fromClass, String fromMethod) {
        this.resolve(
                LogLevel.EXITING,
                fromClass + "::" + fromMethod
        );
    }

    private void resolve(LogLevel level, String message) {
        Bukkit.getLogger().info(
                this.messageFormat
                    .replaceAll("<LEVEL>", level.name())
                    .replaceAll("<MESSAGE>", message)
        );
    }

}
