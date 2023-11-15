package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.utils.FPLogger;
import de.fantasypixel.rework.utils.PackageUtils;
import de.fantasypixel.rework.utils.command.CommandManager;
import de.fantasypixel.rework.utils.provider.ProviderManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

@Getter
public class FPRework extends JavaPlugin {

    private PackageUtils packageUtils;
    private Gson gson;
    private CommandManager commandManager;
    private ProviderManager providerManager;

    @Getter
    private FPLogger fpLogger;

    @Override
    public void onEnable() {
        this.fpLogger = new FPLogger();
        this.gson = new Gson();
        this.packageUtils = new PackageUtils(this);
        this.commandManager = new CommandManager(this);
        this.providerManager = new ProviderManager(this);
    }

    @Override
    public void onDisable() {
        this.providerManager.onDisable();
    }

}
