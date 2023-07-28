package de.fantasypixel.rework.modules.test;

import de.fantasypixel.rework.utils.provider.Controller;
import de.fantasypixel.rework.utils.provider.Service;
import de.fantasypixel.rework.utils.events.OnDisable;
import de.fantasypixel.rework.utils.events.OnEnable;
import org.bukkit.Bukkit;

@Controller
public class TestController {

    @Service(name = "test-service")
    public TestService service;

    @OnEnable
    public void onEnable() {
        Bukkit.getLogger().info("onEnable called on TestController, testing the service...");
        this.service.test();
    }

    @OnDisable
    public void onDisable() {
        Bukkit.getLogger().info("onDisable called on TestController");
    }
}
