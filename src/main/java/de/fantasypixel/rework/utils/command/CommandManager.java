package de.fantasypixel.rework.utils.command;

import de.fantasypixel.rework.utils.PackageUtils;
import de.fantasypixel.rework.utils.provider.Controller;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    private final JavaPlugin plugin;

    public CommandManager(JavaPlugin plugin) {
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
                plugin.getLogger().warning("Couldn't find command " + commandName + ". You may need to add it to the plugin.yml!");
                continue;
            }

            pluginCommand.setExecutor((sender, command, label, args) -> {

                if (!canCommandBeUsed(sender, commandTarget)) {
                    sender.sendMessage("You can't use that command.");
                    return false;
                }

                PackageUtils.invoke(method, commandExecutor, sender, args);
                return true;
            });

            pluginCommand.setPermission(commandAnnotation.permission());
            pluginCommand.setAliases(List.of(commandAnnotation.aliases()));

            plugin.getLogger().info("Registered command " + commandName + ".");
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
