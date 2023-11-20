package de.fantasypixel.rework.framework.timer;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.ProviderManager;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Manages timers for {@link Controller} classes.
 * Is managed by the {@link ProviderManager}.
 */
public class TimerManager {

    public enum TimerType { ASYNC, SYNC }

    private final static String CLASS_NAME = TimerManager.class.getSimpleName();

    private final FPRework plugin;

    public TimerManager(FPRework plugin, Map<Method, Object> timerMethods) {
        this.plugin = plugin;
        timerMethods.forEach(this::startTimer);
    }

    private void startTimer(Method method, Object object) {
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

        if (timerData.type() == TimerType.ASYNC) {
            this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    this.plugin,
                    runnable,
                    timerData.delay(),
                    timerData.interval()
            );
        } else if (timerData.type() == TimerType.SYNC) {
            this.plugin.getServer().getScheduler().runTaskTimer(
                    this.plugin,
                    runnable,
                    timerData.delay(),
                    timerData.interval()
            );
        }
    }

    /**
     * Will stop ALL the plugin's timers.
     */
    public void stopTimers() {
        this.plugin.getFpLogger().debug("Stopping timers.");
        this.plugin.getServer().getScheduler().cancelTasks(this.plugin);
    }

}
