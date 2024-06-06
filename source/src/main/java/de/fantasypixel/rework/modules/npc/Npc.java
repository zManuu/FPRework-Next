package de.fantasypixel.rework.modules.npc;

import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;

public abstract class Npc {

    // required configuration
    public abstract String getName();
    public abstract EntityType getType();

    // optional configuration

    /**
     * Optional meta-data stored on the NPC.
     * @see de.fantasypixel.rework.modules.shops.ShopNpcMetaData
     */
    @Nullable public Object getMetaData() { return null; }

    /**
     * Whether the NPC is passive. Passive NPCs don't move and don't take damage.
     */
    public boolean isPassive() { return false; }

}
