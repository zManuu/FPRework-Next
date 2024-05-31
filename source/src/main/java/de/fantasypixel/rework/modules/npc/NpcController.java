package de.fantasypixel.rework.modules.npc;

import de.fantasypixel.rework.framework.events.BeforeReload;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;

@Controller
public class NpcController {

    @Service private NpcService npcService;

    @BeforeReload
    public void clearNPCs() {
        this.npcService.clearNpcRegistry();
    }

}
