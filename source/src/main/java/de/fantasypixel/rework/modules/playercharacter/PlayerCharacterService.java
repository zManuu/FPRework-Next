package de.fantasypixel.rework.modules.playercharacter;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.modules.character.Character;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import lombok.Getter;

@ServiceProvider(name = "player_character")
public class PlayerCharacterService {

    @Getter
    @Plugin
    private FPRework plugin;

    public void sayHello() {
        this.plugin.getFpLogger().info("Hello from PlayerCharacterService!");
    }

    public PlayerCharacter createPlayerCharacter(int accountId, String name, Character characterClass) {
        return PlayerCharacter.builder()
                .name(name)
                .accountId(accountId)
                .characterIdentifier(characterClass.getIdentifier())
                .build();
    }

}
