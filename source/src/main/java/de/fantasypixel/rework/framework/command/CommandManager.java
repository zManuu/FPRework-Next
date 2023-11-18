package de.fantasypixel.rework.framework.command;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.ProviderManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Manages commands for {@link Controller} classes.
 * Is managed by the {@link ProviderManager}.
 */
public class CommandManager {

    private final FPRework plugin;

    public CommandManager(FPRework plugin) {
        this.plugin = plugin;
    }

    public void registerCommands(Object commandExecutor) {
        for (Method method : commandExecutor.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class))
                continue;

            var commandAnnotation = method.getAnnotation(Command.class);
            var commandName = commandAnnotation.name();
            var commandTarget = commandAnnotation.target();
            var pluginCommand = plugin.getCommand(commandName);

            if (pluginCommand == null) {
                plugin.getFpLogger().warning("Couldn't find command " + commandName + ". You may need to add it to the plugin.yml!");
                continue;
            }

            pluginCommand.setExecutor((sender, command, label, args) -> {

                if (!canCommandBeUsed(sender, commandTarget)) {
                    sender.sendMessage("You can't use that command.");
                    return false;
                }

                this.plugin.getPackageUtils().invoke(method, commandExecutor, sender, args);
                return true;
            });

            pluginCommand.setPermission(commandAnnotation.permission());
            pluginCommand.setAliases(List.of(commandAnnotation.aliases()));

            plugin.getFpLogger().debug("Registered command {0}.", commandAnnotation.name());
        }
    }

    private boolean canCommandBeUsed(CommandSender sender, CommandTarget target) {
        if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof Player))
            return false;

        if (target == CommandTarget.BOTH)
            return true;

        if (sender instanceof Player && target == CommandTarget.PLAYER)
            return true;

        return sender instanceof ConsoleCommandSender && target == CommandTarget.CONSOLE;
    }

}
