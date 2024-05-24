package de.fantasypixel.rework.modules.playercharacter;

import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.modules.events.AccountLoginEvent;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.character.Characters;
import de.fantasypixel.rework.modules.menu.Menu;
import de.fantasypixel.rework.modules.menu.MenuItem;
import de.fantasypixel.rework.modules.menu.MenuService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class PlayerCharacterController implements Listener {

    @Service private AccountService accountService;
    @Service private PlayerCharacterService playerCharacterService;
    @Service private MenuService menuService;
    @Service private NotificationService notificationService;

    @EventHandler
    public void onAccountLogin(AccountLoginEvent event) {
        var player = event.getPlayer();
        var account = event.getAccount();

        if (!this.playerCharacterService.hasCharacter(account)) {
            // create character & then login
            this.openCharacterCreateMenu(player, account.getId(), true);
        } else {
            // login to existing character
            var character = this.playerCharacterService.getActivePlayerCharacter(account);
            this.login(player, character);
        }
    }

    /**
     * Opens the character-create menu for the player.
     * @param player the player
     * @param accountId the players accountId. If null, it will be fetched.
     * @param firstCharacter true, if the player hasn't got any characters yet. Will automatically set it to active.
     */
    private void openCharacterCreateMenu(Player player, @Nullable Integer accountId, boolean firstCharacter) {
        Integer finalAccountId = accountId == null
                ? this.accountService.getAccount(player.getUniqueId()).getId()
                : accountId;

        player.teleport(this.playerCharacterService.getBlackBoxLocation());

        var characterSlotIndex = new AtomicInteger(0);
        Menu menu = Menu.builder()
                .type(InventoryType.HOPPER)
                .title("Character Creation")
                .closable(false)
                .items(
                        Characters.CHARACTERS.stream()
                                .map((character) -> MenuItem.builder()
                                        .slot(characterSlotIndex.getAndIncrement())
                                        .material(character.getIconMaterial())
                                        .displayName(character.getName())
                                        .closesMenu(true)
                                        .onSelect(() -> this.login(player, this.playerCharacterService.createPlayerCharacter(finalAccountId, String.valueOf(accountId), character, firstCharacter)))
                                        .build()
                                )
                                .collect(Collectors.toSet())
                )
                .build();

        this.menuService.openMenu(player, menu);
    }

    /**
     * Logs a player into the given character.
     */
    private void login(Player player, PlayerCharacter playerCharacter) {
        this.notificationService.sendChatMessage(player, "login-start");
        player.teleport(playerCharacter.getLocation());
        player.setFoodLevel(playerCharacter.getFoodLevel());
        this.notificationService.sendChatMessage(player, "login-success");
    }

    @Timer(interval = 100, type = TimerManager.TimerType.ASYNC)
    public void characterSaveTimer() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            var account = this.accountService.getAccount(onlinePlayer.getUniqueId());
            var character = this.playerCharacterService.getActivePlayerCharacter(account);

            if (character == null)
                continue;

            this.playerCharacterService.savePlayerCharacter(
                    character,
                    onlinePlayer.getLocation(),
                    onlinePlayer.getFoodLevel()
            );
        }
    }

}
