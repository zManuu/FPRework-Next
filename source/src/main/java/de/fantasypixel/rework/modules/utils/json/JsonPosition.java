package de.fantasypixel.rework.modules.utils.json;

import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;

/**
 * A simple position class containing x, y and z fields.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JsonPosition {

    private double x;
    private double y;
    private double z;

    public JsonPosition(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public Location toLocation(String worldName) {
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

}
