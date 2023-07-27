package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.utils.command.CommandManager;
import de.fantasypixel.rework.utils.provider.ProviderManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FPRework extends JavaPlugin {

    private static FPRework instance;
    private Gson gson;
    private CommandManager commandManager;
    private ProviderManager providerManager;

    @Override
    public void onEnable() {
        instance = this;
        gson = new Gson();
        commandManager = new CommandManager(this);
        providerManager = new ProviderManager(this);
    }


    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Gson getGson() {
        return gson;
    }

    public static FPRework getInstance() {
        return instance;
    }

    public ProviderManager getProviderManager() {
        return providerManager;
    }
}
