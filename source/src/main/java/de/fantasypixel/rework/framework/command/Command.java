package de.fantasypixel.rework.framework.command;

import de.fantasypixel.rework.framework.provider.Controller;
import org.bukkit.entity.Player;
import org.bukkit.command.ConsoleCommandSender;

import java.lang.annotation.*;

/**
 * Annotation used to mark a method as a command handler, only available in {@link Controller} classes.
 * The method must accept two arguments: CommandSender sender, String[] args.
 * The sender argument can also be of type {@link Player} or {@link ConsoleCommandSender}.
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
