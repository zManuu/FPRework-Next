package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Provides access to certain server-functionality like timers or player-iterating.
 */
@ServiceProvider
public class ServerUtils {

    private final static String CLASS_NAME = ServerUtils.class.getSimpleName();

    @Plugin private FPRework plugin;

    /**
     * @return a collection of all players connected to the server
     */
    @NonNull
    public Collection<? extends Player> getOnlinePlayers() {
        return this.plugin.getServer().getOnlinePlayers();
    }

    /**
     * Runs a task later synchronously.
     * @param task the given task to run after the delay
     * @param delay the delay in TICKS
     */
    public void runTaskLater(@Nonnull Runnable task, long delay) {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, delay);
    }

    /**
     * Runs a task later asynchronously.
     * @param task the given task to run after the delay
     * @param delay the delay in TICKS
     */
    public void runTaskLaterAsync(@Nonnull Runnable task, long delay) {
        this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, task, delay);
    }

    /**
     * Calls the given event. If an error occurs, it is handled internally.
     */
    public void callEvent(@Nonnull Event event) {
        try {
            this.plugin.getServer().getPluginManager().callEvent(event);
        } catch (IllegalStateException ex) {
            this.plugin.getFpLogger().warning("Tried to call event of type {0}, failed. Error following...", event.getEventName());
            this.plugin.getFpLogger().error(CLASS_NAME, "callEvent", ex);
        }
    }

}
