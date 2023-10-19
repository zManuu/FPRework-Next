package de.fantasypixel.rework.modules.playercharacter;

import de.fantasypixel.rework.modules.character.Characters;
import de.fantasypixel.rework.utils.events.OnEnable;
import de.fantasypixel.rework.utils.provider.Controller;
import de.fantasypixel.rework.utils.provider.Service;

@Controller
public class PlayerCharacterController {

    @Service(name = "player_character") private PlayerCharacterService playerCharacterService;

    @OnEnable
    public void onEnable() {
        playerCharacterService.sayHello();
        var createdPlayerCharacter = playerCharacterService.createPlayerCharacter(1, "manu", Characters.CHARACTER_WARRIOR);
        this.playerCharacterService.getPlugin().getLogger().info(this.playerCharacterService.getPlugin().getGson().toJson(createdPlayerCharacter));
    }

}
