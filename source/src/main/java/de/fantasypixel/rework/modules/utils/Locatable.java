package de.fantasypixel.rework.modules.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.annotation.Nonnull;

/**
 * Represents something that has a position including world-name and coordinates.
 */
public abstract class Locatable {

    public abstract String getLocWorld();
    public abstract double getLocX();
    public abstract double getLocY();
    public abstract double getLocZ();
    public abstract float getLocYaw();
    public abstract float getLocPitch();

    public abstract void setLocWorld(String world);
    public abstract void setLocX(double x);
    public abstract void setLocY(double y);
    public abstract void setLocZ(double z);
    public abstract void setLocYaw(float yaw);
    public abstract void setLocPitch(float pitch);

    @Nonnull
    public Location getLocation() {
        return new Location(
                Bukkit.getWorld(this.getLocWorld()),
                this.getLocX(),
                this.getLocY(),
                this.getLocZ(),
                this.getLocYaw(),
                this.getLocPitch()
        );
    }

}
