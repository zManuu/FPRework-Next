package de.fantasypixel.rework.framework.timer;

import de.fantasypixel.rework.framework.provider.Controller;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied to methods in {@link Controller} classes.
 * Those methods will be executed repeatedly with the given delay and interval in an async or context as specified.
 * Under the hood, the {@link BukkitScheduler} is used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timer {

    /**
     * The amount of TICKS to wait before the timer is started.
     */
    long delay() default 0;

    /**
     * The amount of TICKS to wait after each execution.
     */
    long interval();

    TimerManager.TimerType type() default TimerManager.TimerType.ASYNC;

}
