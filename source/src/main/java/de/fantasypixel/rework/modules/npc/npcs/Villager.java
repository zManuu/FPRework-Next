package de.fantasypixel.rework.modules.npc.npcs;

import de.fantasypixel.rework.modules.npc.Npc;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class Villager extends Npc {

    private final String name;
    private final boolean passive;

    /**
     * The profession of the villager.
     * @see <a href="https://minecraft.fandom.com/wiki/Villager#Professions">List of professions</a>
     */
    private final Profession profession;

    /**
     * The hologram lines (in addition to the name that is always at the bottom).
     */
    private final ArrayList<String> hologramLines;
    private final Object metaData;

    @Override
    public EntityType getType() {
        return EntityType.VILLAGER;
    }

}
