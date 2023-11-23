package de.fantasypixel.rework.framework.timer;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.ProviderManager;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages timers for {@link Controller} classes.
 * Is managed by the {@link ProviderManager}.
 */
public class TimerManager {

    public enum TimerType { ASYNC, SYNC }

    private final static String CLASS_NAME = TimerManager.class.getSimpleName();

    private final FPRework plugin;
    private final Set<Integer> timerIds;

    public TimerManager(FPRework plugin) {
        this.plugin = plugin;
        this.timerIds = new HashSet<>();
    }

    public void startTimer(Method method, Object object) {
        var timerData = method.getAnnotation(Timer.class);

        this.plugin.getFpLogger().debug(
                "Timer {0} is starting. delay={1}ms, interval={2}ms, type={3}",
                method.getDeclaringClass().getSimpleName() + "::" + method.getName(),
                timerData.delay(),
                timerData.interval(),
                timerData.type().name()
        );

        Runnable runnable = () -> {
            try {
                method.invoke(object);
            } catch (Exception ex) {
                this.plugin.getFpLogger().error(CLASS_NAME, "startTimer->invoke", ex);
            }
        };

        BukkitTask task = null;

        if (timerData.type() == TimerType.ASYNC) {
            task = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    this.plugin,
                    runnable,
                    timerData.delay(),
                    timerData.interval()
            );
        } else if (timerData.type() == TimerType.SYNC) {
            task = this.plugin.getServer().getScheduler().runTaskTimer(
                    this.plugin,
                    runnable,
                    timerData.delay(),
                    timerData.interval()
            );
        }

        if (task == null) {
            this.plugin.getFpLogger().warning(
                    "Failed to start timer {0}::{1}",
                    object.getClass().getSimpleName(),
                    method.getName()
            );
            return;
        }

        this.timerIds.add(task.getTaskId());
    }

    public void stopTimers() {
        this.plugin.getFpLogger().debug("Stopping timers.");
        this.timerIds.forEach(e -> this.plugin.getServer().getScheduler().cancelTask(e));
    }

}
