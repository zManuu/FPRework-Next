package de.fantasypixel.rework.utils;

import org.bukkit.Bukkit;

import java.text.MessageFormat;

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

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param message the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void info(String message, Object... args) {
        this.resolve(LogLevel.INFO, MessageFormat.format(message, args));
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

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param message the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void warning(String message, Object... args) {
        this.resolve(LogLevel.WARNING, MessageFormat.format(message, args));
    }

    public void debug(String message) {
        this.resolve(LogLevel.DEBUG, message);
    }

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param message the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void debug(String message, Object... args) {
        this.resolve(LogLevel.DEBUG, MessageFormat.format(message, args));
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
