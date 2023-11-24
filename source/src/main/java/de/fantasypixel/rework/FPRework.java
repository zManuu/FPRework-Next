package de.fantasypixel.rework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fantasypixel.rework.framework.FPConfig;
import de.fantasypixel.rework.framework.FPLogger;
import de.fantasypixel.rework.framework.FPUtils;
import de.fantasypixel.rework.framework.adapters.RecordTypeAdapterFactory;
import de.fantasypixel.rework.framework.provider.ProviderManager;
import de.fantasypixel.rework.framework.provider.autorigging.Config;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;

@Getter
public class FPRework extends JavaPlugin {

    private final static String CLASS_NAME = FPRework.class.getSimpleName();

    private Gson gson;
    private FPUtils fpUtils;
    private ProviderManager providerManager;
    private FPLogger fpLogger;
    private FPConfig fpConfig;

    @Override
    public void onEnable() {
        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory(this))
                .setPrettyPrinting()
                .create();

        this.fpLogger = new FPLogger(this.gson, System.out);

        this.loadConfig();

        this.fpUtils = new FPUtils(this);
        this.providerManager = new ProviderManager(this);
    }

    /**
     * Loads the configuration from the file and saves it into a local variable.
     * To reload the configuration, use {@link #reloadConfig()}.
     */
    private void loadConfig() {
        var configFile = new File(this.getDataFolder(), "config.json");

        if (!configFile.exists()) {
            this.getFpLogger().warning("Couldn't find config.json!");
            return;
        }

        try (var fileReader = new FileReader(configFile)) {
            this.fpConfig = this.getGson().fromJson(fileReader, FPConfig.class);
        } catch (Exception e) {
            this.getFpLogger().error(CLASS_NAME, "loadConfig", e);
            this.fpConfig = null;
        }
    }

    /**
     * Reloads the configuration and provides the updated configuration to all Services.
     */
    public void reloadConfig() {
        this.loadConfig();
        // hooks can be overridden
        this.providerManager.initHooks("Config", Config.class, this.fpConfig);
    }

    @Override
    public void onDisable() {
        this.providerManager.onDisable();
    }

}
