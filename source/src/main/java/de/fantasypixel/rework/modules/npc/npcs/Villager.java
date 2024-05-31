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
    private final Profession profession;
    private final Object metaData;

    @Override
    public EntityType getType() {
        return EntityType.VILLAGER;
    }

}
