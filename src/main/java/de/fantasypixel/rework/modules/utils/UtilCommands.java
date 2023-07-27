package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.utils.command.Command;
import de.fantasypixel.rework.utils.command.CommandTarget;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class UtilCommands {

    @Command(name = "test1", target = CommandTarget.BOTH)
    public void test1(CommandSender sender) {
        sender.sendMessage("test1");
    }

    @Command(name = "test2", target = CommandTarget.PLAYER)
    public void test2(Player sender) {
        sender.sendMessage("test2");
    }

    @Command(name = "test3", target = CommandTarget.CONSOLE)
    public void test3(ConsoleCommandSender sender) {
        sender.sendMessage("test3");
    }

}
