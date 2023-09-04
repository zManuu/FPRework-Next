package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.utils.command.CommandManager;
import de.fantasypixel.rework.utils.provider.ProviderManager;
import de.fantasypixel.rework.utils.spigotevents.SpigotEventManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FPRework extends JavaPlugin {

    private Gson gson;
    private CommandManager commandManager;
    private ProviderManager providerManager;


    @Override
    public void onEnable() {
        gson = new Gson();
        commandManager = new CommandManager(this);
        providerManager = new ProviderManager(this);
        Bukkit.getPluginManager().registerEvents(new SpigotEventManager(this), this);
    }

    @Override
    public void onDisable() {
        this.providerManager.disableControllers();
    }


}
