package de.fantasypixel.rework.modules.npc;

import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;

public abstract class Npc {

    // required configuration
    public abstract EntityType getType();
    public abstract String getName();

    // optional configuration
    @Nullable public Object getMetaData() { return null; }

    /**
     * Whether the NPC is passive. Passive NPCs don't move and don't take damage.
     */
    public boolean isPassive() { return false; }

}
