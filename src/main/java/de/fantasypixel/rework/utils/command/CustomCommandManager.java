package de.fantasypixel.rework.utils.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;

public class CustomCommandManager {
    private JavaPlugin plugin;

    public CustomCommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands(Object commandExecutor) {
        for (Method method : commandExecutor.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                Command commandAnnotation = method.getAnnotation(Command.class);
                String commandName = commandAnnotation.name();

                PluginCommand pluginCommand = plugin.getCommand(commandName);
                if (pluginCommand != null) {
                    pluginCommand.setExecutor(new CommandExecutor() {
                        @Override
                        public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
                            try {
                                method.setAccessible(true);
                                method.invoke(commandExecutor, sender, args);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });

                    pluginCommand.setPermission(commandAnnotation.permission());
                    pluginCommand.setAliases(List.of(commandAnnotation.aliases()));
                } else {
                    // Handle command registration failure
                }
            }
        }
    }
}
