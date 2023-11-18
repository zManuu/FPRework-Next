package de.fantasypixel.rework.framework;

import de.fantasypixel.rework.FPRework;
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

    private final FPRework plugin;

    public FPLogger(FPRework plugin) {
        this.plugin = plugin;
    }

    // log methods with more complex arguments
    public void log(LogLevel level, String message) { this.resolve(level, message); }
    public void log(LogLevel level, String pattern, Object... args) { this.resolve(level, MessageFormat.format(pattern, args)); }
    public void log(LogLevel level, String pattern, boolean stringifyArgs, Object... args) { this.resolve(level, MessageFormat.format(pattern, this.stringifyArgs(args))); }

    public void info(String message) {
        this.resolve(LogLevel.INFO, message);
    }

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param pattern the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void info(String pattern, Object... args) {
        this.resolve(LogLevel.INFO, MessageFormat.format(pattern, args));
    }

    public void error(String fromClass, String fromMethod, Throwable throwable) {
        this.resolve(
                LogLevel.ERROR,
                "CLASS::METHOD ERROR"
                        .replace("CLASS", fromClass)
                        .replace("METHOD", fromMethod)
                        .replace("ERROR", throwable.getMessage())
        );
        throwable.printStackTrace();
    }

    public void warning(String message) {
        this.resolve(LogLevel.WARNING, message);
    }

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param pattern the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void warning(String pattern, Object... args) {
        this.resolve(LogLevel.WARNING, MessageFormat.format(pattern, args));
    }

    public void debug(String message) {
        this.resolve(LogLevel.DEBUG, message);
    }

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param pattern the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void debug(String pattern, Object... args) {
        this.resolve(LogLevel.DEBUG, MessageFormat.format(pattern, args));
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

    private String stringifyArgs(Object[] arguments) {
        var stringified = new StringBuilder(this.plugin.getGson().toJson(arguments[0]));
        for (int i = 1; i < arguments.length; i++) {
            stringified.append(", ").append(arguments[i]);
        }
        return stringified.toString();
    }

    private void resolve(LogLevel level, String message) {
        Bukkit.getLogger().info(
                "LEVEL | MESSAGE"
                    .replace("LEVEL", level.name())
                    .replace("MESSAGE", message)
        );
    }

}
