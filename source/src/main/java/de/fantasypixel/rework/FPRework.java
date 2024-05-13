package de.fantasypixel.rework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fantasypixel.rework.framework.FPLogger;
import de.fantasypixel.rework.framework.FPUtils;
import de.fantasypixel.rework.framework.adapters.RecordTypeAdapterFactory;
import de.fantasypixel.rework.framework.provider.ProviderManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FPRework extends JavaPlugin {

    private final static String CLASS_NAME = FPRework.class.getSimpleName();

    private Gson gson;
    private FPUtils fpUtils;
    private ProviderManager providerManager;
    private FPLogger fpLogger;

    @Override
    public void onEnable() {
        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory(this))
                .setPrettyPrinting()
                .create();

        this.fpLogger = new FPLogger(System.out);

        this.fpUtils = new FPUtils(this);
        this.providerManager = new ProviderManager(this);
    }

    @Override
    public void onDisable() {
        this.providerManager.onDisable();
    }

}
