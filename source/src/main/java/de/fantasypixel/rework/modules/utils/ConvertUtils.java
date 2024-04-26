package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.framework.provider.ServiceProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@ServiceProvider(name = "convert_utils")
public class ConvertUtils {

    public Location locatableToLocation(Locatable locatable) {
        return new Location(
                Bukkit.getWorld(locatable.getLocWorld()),
                locatable.getLocX(),
                locatable.getLocY(),
                locatable.getLocZ()
        );
    }

}
