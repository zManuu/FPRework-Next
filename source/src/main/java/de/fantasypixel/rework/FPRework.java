package de.fantasypixel.rework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fantasypixel.rework.framework.FPLogger;
import de.fantasypixel.rework.framework.PackageUtils;
import de.fantasypixel.rework.framework.adapters.RecordTypeAdapterFactory;
import de.fantasypixel.rework.framework.provider.ProviderManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FPRework extends JavaPlugin {

    private Gson gson;
    private PackageUtils packageUtils;
    private ProviderManager providerManager;
    private FPLogger fpLogger;

    @Override
    public void onEnable() {
        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory(this))
                .setPrettyPrinting()
                .create();

        this.fpLogger = new FPLogger(this.gson, System.out);
        this.packageUtils = new PackageUtils(this);
        this.providerManager = new ProviderManager(this);
    }

    @Override
    public void onDisable() {
        this.providerManager.onDisable();
    }

}
