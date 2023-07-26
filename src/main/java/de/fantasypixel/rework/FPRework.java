package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.modules.account.AccountController;
import de.fantasypixel.rework.utils.command.CustomCommandManager;
import de.fantasypixel.rework.utils.modules.ModuleHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class FPRework extends JavaPlugin {

    private static FPRework instance;
    private ModuleHandler moduleHandler;
    private Gson gson;
    private CustomCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        gson = new Gson();
        commandManager = new CustomCommandManager(this);

        try {
            moduleHandler = new ModuleHandler(
                    AccountController.class
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onDisable() {
        moduleHandler.disable();
    }

    public CustomCommandManager getCommandManager() {
        return commandManager;
    }

    public ModuleHandler getModuleHandler() {
        return moduleHandler;
    }

    public Gson getGson() {
        return gson;
    }

    public static FPRework getInstance() {
        return instance;
    }
}
