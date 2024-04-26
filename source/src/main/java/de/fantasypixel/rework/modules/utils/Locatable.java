package de.fantasypixel.rework.modules.utils;

/**
 * Represents something that has a position including world-name and coordinates.
 */
public interface Locatable {

    String getLocWorld();
    double getLocX();
    double getLocY();
    double getLocZ();

    void setLocWorld(String world);
    void setLocX(double LocX);
    void setLocY(double LocY);
    void setLocZ(double LocZ);

}
