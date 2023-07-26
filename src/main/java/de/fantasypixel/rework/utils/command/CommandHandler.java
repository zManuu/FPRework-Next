package de.fantasypixel.rework.utils.command;

import java.lang.annotation.*;

/**
 * Annotation used to mark a class as a command handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandHandler {
}
