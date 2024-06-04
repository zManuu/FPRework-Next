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
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JsonPosition {

    private String world;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public JsonPosition(Location location) {
        this.world = location.getWorld() != null ? location.getWorld().getName() : null;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
    }

    public Location toLocation() {
        return new Location(
                Bukkit.getWorld(this.world),
                this.x, this.y, this.z,
                this.yaw, this.pitch
        );
    }

}
