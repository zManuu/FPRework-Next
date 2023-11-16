package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.utils.FPLogger;
import de.fantasypixel.rework.utils.PackageUtils;
import de.fantasypixel.rework.utils.provider.ProviderManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FPRework extends JavaPlugin {

    // The DI-pattern is broken here because WebResponse needs a static reference to a gson object.
    @Getter
    private static Gson gson;

    private PackageUtils packageUtils;
    private ProviderManager providerManager;

    @Getter
    private FPLogger fpLogger;

    @Override
    public void onEnable() {
        gson = new Gson();

        this.fpLogger = new FPLogger();
        this.packageUtils = new PackageUtils(this);
        this.providerManager = new ProviderManager(this);
    }

    @Override
    public void onDisable() {
        this.providerManager.onDisable();
    }

}
