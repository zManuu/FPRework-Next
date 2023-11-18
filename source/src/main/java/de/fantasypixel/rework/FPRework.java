package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.framework.FPLogger;
import de.fantasypixel.rework.framework.PackageUtils;
import de.fantasypixel.rework.framework.provider.ProviderManager;
import de.fantasypixel.rework.framework.web.WebResponse;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FPRework extends JavaPlugin {

    @Getter
    private Gson gson;

    private PackageUtils packageUtils;
    private ProviderManager providerManager;

    @Getter
    private FPLogger fpLogger;

    @Override
    public void onEnable() {
        gson = new Gson();

        this.fpLogger = new FPLogger(this);
        this.packageUtils = new PackageUtils(this);
        this.providerManager = new ProviderManager(this);
    }

    @Override
    public void onDisable() {
        this.providerManager.onDisable();
    }

}
