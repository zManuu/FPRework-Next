package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides easy utility access to namespaced-keys.
 */
@ServiceProvider
public class NamespacedKeyUtils {

    public enum NamespacedKeyType { ITEM_IDENTIFIER }

    @Auto private Plugin plugin;

    private final Map<NamespacedKeyType, NamespacedKey> keyMap;

    public NamespacedKeyUtils() {
        this.keyMap = new HashMap<>();
    }

    @Nonnull
    public NamespacedKey getNamespacedKey(NamespacedKeyType key) {
        if (!keyMap.containsKey(key))
            keyMap.put(key, new NamespacedKey(this.plugin, key.name()));

        return keyMap.get(key);
    }

}
