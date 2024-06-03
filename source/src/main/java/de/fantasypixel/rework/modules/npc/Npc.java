package de.fantasypixel.rework.modules.npc;

import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class Npc {

    // required configuration
    public abstract String getName();
    public abstract EntityType getType();
    public abstract ArrayList<String> getHologramLines();

    // optional configuration
    @Nullable public Object getMetaData() { return null; }

    /**
     * Whether the NPC is passive. Passive NPCs don't move and don't take damage.
     */
    public boolean isPassive() { return false; }

}
