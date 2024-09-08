package de.fantasypixel.rework.framework.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.discord.FPDiscordClient;
import de.fantasypixel.rework.framework.database.DatabaseManager;

/**
 * Can be used on fields in {@link de.fantasypixel.rework.framework.provider.ServiceProvider} classes. Those fields will be auto-rigged with the corresponding loaded value.
 * <br><br>
 * Supported types (external):<br>
 * - {@link com.google.gson.Gson}<br>
 * - {@link org.bukkit.Server}<br>
 * - {@link org.bukkit.plugin.Plugin}<br>
 * <br>
 * Supported types (internal):<br>
 * - {@link ReloadManager}<br>
 * - {@link FPLogger}<br>
 * - {@link FPDiscordClient}<br>
 * - {@link DatabaseManager}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Auto {}
