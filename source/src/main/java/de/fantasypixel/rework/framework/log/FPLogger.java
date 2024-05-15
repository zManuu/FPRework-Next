package de.fantasypixel.rework.framework.log;

import com.google.gson.Gson;
import de.fantasypixel.rework.FPRework;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.text.MessageFormat;
import java.util.Collections;

/**
 * A logger that supports more log levels than the spigot logger.
 * Under the hood, everything is logged to the INFO level.
 * The formats are hardcoded because the config also relies on the logger.
 */
public class FPLogger {

    public enum LogLevel { ERROR, WARNING, INFO, ENTERING, EXITING, DEBUG }

    private final static String CLASS_NAME = FPLogger.class.getSimpleName();

    private final PrintStream printStream;
    private FPLoggerConfig config = new FPLoggerConfig(25, Collections.emptyMap()); // has to be initialized here because the loading might produce logs.

    /**
     * Constructs a logger. Gson is used to load the configuration from resources.
     */
    public FPLogger(@Nonnull PrintStream printStream, @Nonnull Gson gson) {
        this.printStream = printStream;

        // load config
        try (
                var configResource = FPRework.class.getResourceAsStream("logging.json")
        ) {
            if (configResource == null)
                throw new RuntimeException();

            var configReader = new InputStreamReader(configResource);
            this.config = gson.fromJson(configReader, FPLoggerConfig.class);
            configReader.close();
        } catch (IOException | RuntimeException ex) {
            this.warning("Couldn't load logging.json! Using fallback configuration.");
        }

    }

    private boolean isGroupActive(String group) {
        return this.config.getGroups().containsKey(group)
                && this.config.getGroups().get(group);
    }

    public void info(@Nonnull String message) {
        this.resolve(LogLevel.INFO, message);
    }

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param pattern the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void info(@Nonnull String pattern, @Nullable Object... args) {
        this.resolve(LogLevel.INFO, MessageFormat.format(pattern, args));
    }

    public void error(@Nonnull String fromClass, @Nonnull String fromMethod, @Nonnull Throwable throwable) {
        var errorMessage = throwable.getMessage() == null
                ? throwable.toString()
                : throwable.getMessage();

        this.resolve(
                LogLevel.ERROR,
                "CLASS::METHOD ERROR"
                        .replace("CLASS", fromClass)
                        .replace("METHOD", fromMethod)
                        .replace("ERROR", errorMessage)
        );
        this.sectionStart("Start-Trace");
        throwable.printStackTrace();
        this.sectionEnd("Start-Trace");
    }

    public void error(@Nonnull String fromClass, @Nonnull String fromMethod, @Nonnull String message) {
        this.error(fromClass, fromMethod, new Exception(message));
    }

    public void error(@Nonnull String fromClass, @Nonnull String fromMethod, @Nonnull String pattern, @Nullable Object... args) {
        this.error(fromClass, fromMethod, new Exception(MessageFormat.format(pattern, args)));
    }

    public void sectionStart(@Nonnull String section) {
        this.printStream.println(
                MessageFormat.format(
                        "{0}[ Start of {1} ]{0}",
                        "-".repeat(this.config.getSectionIndentation()),
                        section
                )
        );
    }

    public void sectionEnd(@Nonnull String section) {
        this.printStream.println(
                MessageFormat.format(
                        "{0}[ End of {1} ]{0}",
                        "-".repeat(this.config.getSectionIndentation() + 1),
                        section
                )
        );
    }

    public void warning(@Nonnull String message) {
        this.resolve(LogLevel.WARNING, message);
    }

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param pattern the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void warning(@Nonnull String pattern, @Nullable Object... args) {
        this.resolve(LogLevel.WARNING, MessageFormat.format(pattern, args));
    }

    public void warn(@Nonnull String fromClass, @Nonnull String fromMethod, @Nonnull String message) {
        this.resolve(
                LogLevel.WARNING,
                "CLASS::METHOD MESSAGE"
                        .replace("CLASS", fromClass)
                        .replace("METHOD", fromMethod)
                        .replace("MESSAGE", message)
        );
    }

    public void warn(@Nonnull String fromClass, @Nonnull String fromMethod, @Nonnull String pattern, @Nullable Object... args) {
        this.resolve(
                LogLevel.WARNING,
                "CLASS::METHOD MESSAGE"
                        .replace("CLASS", fromClass)
                        .replace("METHOD", fromMethod)
                        .replace("MESSAGE", MessageFormat.format(pattern, args))
        );
    }

    public void debug(@Nonnull String message) {
        this.resolve(LogLevel.DEBUG, message);
    }

    /**
     * Uses {@link MessageFormat} to format the message.
     * @param pattern the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void debug(@Nonnull String pattern, @Nullable Object... args) {
        this.resolve(LogLevel.DEBUG, MessageFormat.format(pattern, args));
    }

    public void debugGrouped(@Nonnull String group, @Nonnull String message) {
        if (this.isGroupActive(group))
            this.resolve(LogLevel.DEBUG, message);
    }

    /**
     * Logs the given message if the group is active. Groups can be toggled in the logging.json
     * Uses {@link MessageFormat} to format the message.
     * @param group the log-group's name
     * @param pattern the message pattern. Can include placeholders like {0}, {1}, ...
     * @param args the arguments to be passed to the pattern
     */
    public void debugGrouped(@Nonnull String group, @Nonnull String pattern, @Nullable Object... args) {
        if (this.isGroupActive(group))
            this.resolve(LogLevel.DEBUG, MessageFormat.format(pattern, args));
    }

    public void entering(@Nonnull String fromClass, @Nonnull String fromMethod) {
        this.resolve(
                LogLevel.ENTERING,
                fromClass + "::" + fromMethod
        );
    }

    public void exiting(@Nonnull String fromClass, @Nonnull String fromMethod) {
        this.resolve(
                LogLevel.EXITING,
                fromClass + "::" + fromMethod
        );
    }

    private void resolve(@Nonnull LogLevel level, @Nonnull String message) {
        this.printStream.println(
                "LEVEL | MESSAGE"
                    .replace("LEVEL", level.name())
                    .replace("MESSAGE", message)
        );
    }

}
