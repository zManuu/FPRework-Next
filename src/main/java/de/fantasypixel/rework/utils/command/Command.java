package de.fantasypixel.rework.utils.command;

import java.lang.annotation.*;

/**
 * Annotation used to mark a method as a command handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    /**
     * The name of the command.
     */
    String name();

    /**
     * The permission required to execute the command. (Optional)
     */
    String permission() default "";

    /**
     * The aliases for the command. (Optional)
     */
    String[] aliases() default {};

    /**
     * The target of the command (PLAYER, CONSOLE, or BOTH). (Default: PLAYER)
     */
    CommandTarget target() default CommandTarget.PLAYER;
}
