package de.fantasypixel.rework.utils.spigotevents;

import de.fantasypixel.rework.FPRework;
import org.bukkit.event.*;
import org.bukkit.plugin.RegisteredListener;

/**
 * Catches all Spigot events and redirects them to the respective listeners registered via {@link SpigotEvent}.
 */
public class SpigotEventManager implements Listener {

    private final FPRework plugin;

    public SpigotEventManager(FPRework plugin) {
        this.plugin = plugin;

        var registeredListener = new RegisteredListener(
                this,
                (listener, event) -> this.plugin.getProviderManager().invokeEvent(event),
                EventPriority.NORMAL, plugin,
                false
        );

        for (var handler : HandlerList.getHandlerLists())
            handler.register(registeredListener);
    }
}
