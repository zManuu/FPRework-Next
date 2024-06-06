package de.fantasypixel.rework.modules.npc.npcs;

import de.fantasypixel.rework.modules.npc.Npc;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;

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
    private final Object metaData;

    @Override
    public EntityType getType() {
        return EntityType.VILLAGER;
    }

}
