package de.fantasypixel.rework.framework.provider;

import de.fantasypixel.rework.framework.log.FPLogger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used on fields in {@link de.fantasypixel.rework.framework.provider.ServiceProvider} classes. Those fields will be auto-rigged with the corresponding loaded value.
 * <br>
 * Supported types:<br>
 * - {@link com.google.gson.Gson}<br>
 * - {@link FPLogger}<br>
 * - {@link org.bukkit.Server}<br>
 * - {@link org.bukkit.plugin.Plugin}<br>
 * - {@link ReloadManager}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Auto {}
