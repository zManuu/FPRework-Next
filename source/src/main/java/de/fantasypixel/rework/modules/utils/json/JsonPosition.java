package de.fantasypixel.rework.modules.utils.json;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A simple position class containing x, y and z fields.
 */
@Getter
@Setter
@Builder
public class JsonPosition {

    private double x;
    private double y;
    private double z;

    public Location toLocation(String worldName) {
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

}
