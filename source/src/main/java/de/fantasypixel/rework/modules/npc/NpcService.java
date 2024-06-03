package de.fantasypixel.rework.modules.npc;

import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.events.AfterReload;
import de.fantasypixel.rework.modules.npc.npcs.Villager;
import de.fantasypixel.rework.modules.utils.ServerUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.VillagerProfession;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@ServiceProvider
public class NpcService {

    @Auto private FPLogger logger;
    @Service private ServerUtils serverUtils;
    private NPCRegistry npcRegistry;
    private Map<NPC, Npc> createdNPCs;

    /**
     * Clears the {@link #npcRegistry} (before reload)
     */
    public void clearNpcRegistry() {
        if (this.npcRegistry == null)
            this.logger.warning("Tried to clear the npc-registry, but it wasn't setup yet.");
        else {
            this.npcRegistry.deregisterAll();
            this.logger.debug("Cleared the npc-registry.");
        }
    }

    /**
     * Creates & spawns a NPC. <b>Note:</b> This method is executed synchronously.
     * <br><br>
     * <b>NPCs currently aren't persistent, they need to be recreated ({@link OnEnable}, {@link AfterReload}).</b>
     * @param npc the npc to spawn
     * @param location the location of the npc
     */
    public void createNpc(@Nonnull Npc npc, @Nonnull Location location) {
        if (this.npcRegistry == null) {
            this.logger.debug("No InMemoryRegistry found, creating one.");
            this.npcRegistry = CitizensAPI.createInMemoryNPCRegistry("fp-npc-inmemory");
        }

        if (this.createdNPCs == null) {
            this.logger.debug("No createdNPCs-map found, creating one.");
            this.createdNPCs = new HashMap<>();
        }

        this.serverUtils.runTaskSynchronously(() -> {
            var citizensNpc = this.npcRegistry.createNPC(npc.getType(), npc.getName());
            citizensNpc.setProtected(npc.isPassive());
            citizensNpc.setUseMinecraftAI(false);

            // hologram
            var citizensNpcHologramTraitInfo = TraitInfo.create(HologramTrait.class);
            var npcHologramLines = npc.getHologramLines();
            if (npcHologramLines != null) {
                var citizensNpcHologramTrait = citizensNpcHologramTraitInfo
                        .withSupplier(() -> {
                            var hologramTrait = new HologramTrait();

                            System.out.println("npcHologramLines.getClass().getSimpleName() = " + npcHologramLines.getClass().getSimpleName());
                            System.out.println("npcHologramLines = " + npcHologramLines);

                            int npcHologramLineIndex = 0;
                            for (String npcHologramLine : npcHologramLines)
                                hologramTrait.setLine(npcHologramLineIndex++, npcHologramLine);
                            return hologramTrait;
                        })
                        .tryCreateInstance();

                citizensNpc.addTrait(citizensNpcHologramTrait);
            }

            // villager
            if (npc instanceof Villager villager)
                citizensNpc.getOrAddTrait(VillagerProfession.class).setProfession(villager.getProfession());

            citizensNpc.spawn(location);
            this.createdNPCs.put(citizensNpc, npc);
        });
    }

    /**
     * Tests whether an entity is a Citizens-NPC.
     */
    public boolean isNpc(@Nullable Entity entity) {
        return entity != null && entity.hasMetadata("NPC");
    }

    /**
     * Gets a citizens-npc for the corresponding entity.
     * @throws IllegalArgumentException if the entity isn't a NPC
     */
    @Nonnull
    public NPC getCitizensNpc(@Nullable Entity entity) throws IllegalArgumentException {
        if (!this.isNpc(entity))
            throw new IllegalArgumentException("The passed entity is not a npc.");

        var citizensNpc = this.npcRegistry.getNPC(entity);
        if (citizensNpc == null)
            throw new IllegalArgumentException("The entity has NPC meta-data but wasn't found in the npc-registry!");

        return citizensNpc;
    }

    /**
     * Gets a npc for the corresponding entity.
     * @throws IllegalArgumentException if the entity isn't a NPC
     */
    @Nonnull
    public Npc getNpc(@Nullable Entity entity) throws IllegalArgumentException {
        var citizensNpc = this.getCitizensNpc(entity);

        if (!this.createdNPCs.containsKey(citizensNpc))
            throw new IllegalArgumentException("The citizens-NPC was found, but not registered in the createdNPCs map!");

        return this.createdNPCs.get(citizensNpc);
    }

}
