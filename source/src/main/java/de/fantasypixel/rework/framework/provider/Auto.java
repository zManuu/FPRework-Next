package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used on fields in {@link de.fantasypixel.rework.framework.provider.ServiceProvider} classes. Those fields will be auto-rigged with the corresponding loaded value.
 * <br>
 * Supported types:<br>
 * - {@link com.google.gson.Gson}<br>
 * - {@link de.fantasypixel.rework.framework.FPLogger}<br>
 * - {@link org.bukkit.Server}<br>
 * - {@link org.bukkit.plugin.Plugin}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Auto {}
