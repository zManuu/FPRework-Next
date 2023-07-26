package de.fantasypixel.rework.utils.modules;

import org.bukkit.event.Listener;

public abstract class Controller<S extends Service<?, ?>> implements Listener {

    private final S service;

    public Controller(S service) {
        this.service = service;
    }

    public S getService() {
        return service;
    }

    public void onEnable() {}
    public void onDisable() {}

}
