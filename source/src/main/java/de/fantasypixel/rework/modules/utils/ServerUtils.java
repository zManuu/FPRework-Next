package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Provides access to certain server-functionality like timers or player-iterating.
 */
@ServiceProvider
public class ServerUtils {

    private final static String CLASS_NAME = ServerUtils.class.getSimpleName();

    @Auto private Server server;
    @Auto private FPLogger logger;
    @Auto private Plugin plugin;

    /**
     * @return a collection of all players connected to the server
     */
    @NonNull
    public Collection<? extends Player> getOnlinePlayers() {
        return this.server.getOnlinePlayers();
    }

    /**
     * Runs a task later synchronously.
     * @param task the given task to run after the delay
     * @param delay the delay in TICKS
     */
    public void runTaskLater(@Nonnull Runnable task, long delay) {
        this.server.getScheduler().runTaskLater(this.plugin, task, delay);
    }

    /**
     * Runs a task later asynchronously.
     * @param task the given task to run after the delay
     * @param delay the delay in TICKS
     */
    public void runTaskLaterAsync(@Nonnull Runnable task, long delay) {
        this.server.getScheduler().runTaskLaterAsynchronously(this.plugin, task, delay);
    }

    /**
     * Runs a task synchronously.
     * @param task the given task to run
     */
    public void runTaskSynchronously(@Nonnull Runnable task) {
        this.server.getScheduler().runTask(this.plugin, task);
    }

    /**
     * Calls the given event. If an error occurs, it is handled internally.
     */
    public void callEvent(@Nonnull Event event) {
        try {
            this.server.getPluginManager().callEvent(event);
        } catch (IllegalStateException ex) {
            this.logger.warning("Tried to call event of type {0}, failed. Error following...", event.getEventName());
            this.logger.error(CLASS_NAME, "callEvent", ex);
        }
    }

}
