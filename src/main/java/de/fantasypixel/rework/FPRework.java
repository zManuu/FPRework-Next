package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.utils.command.CustomCommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FPRework extends JavaPlugin {

    private static FPRework instance;
    private Gson gson;
    private CustomCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        gson = new Gson();
        commandManager = new CustomCommandManager(this);
    }

    public CustomCommandManager getCommandManager() {
        return commandManager;
    }

    public Gson getGson() {
        return gson;
    }

    public static FPRework getInstance() {
        return instance;
    }
}
