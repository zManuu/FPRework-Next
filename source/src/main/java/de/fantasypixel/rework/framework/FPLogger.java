package de.fantasypixel.rework.framework;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * A logger that supports more log levels than the spigot logger.
 * Under the hood, everything is logged to the INFO level.
 * The formats are hardcoded because the config also relies on the logger.
 */
public class FPLogger {

    public enum LogLevel { ERROR, WARNING, INFO, ENTERING, EXITING, DEBUG }

    private final static int sectionIndentation = 25;
    private final PrintStream printStream;

    public FPLogger(@Nonnull PrintStream printStream) {
        this.printStream = printStream;
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
                        "-".repeat(sectionIndentation),
                        section
                )
        );
    }

    public void sectionEnd(@Nonnull String section) {
        this.printStream.println(
                MessageFormat.format(
                        "{0}[ End of {1} ]{0}",
                        "-".repeat(sectionIndentation + 1),
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
