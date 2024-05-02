package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.menu.Menu;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.menu.MenuItem;
import de.fantasypixel.rework.modules.menu.MenuService;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class SavePointController {

    @Service(name = "save-points") private SavePointService savePointService;
    @Service(name = "account") private AccountService accountService;
    @Service(name = "player-character") private PlayerCharacterService characterService;
    @Service(name = "menu") private MenuService menuService;

    // todo: timer starting twice?
    @Timer(interval = 10, type = TimerManager.TimerType.SYNC)
    public void unlockTimer() {
        // todo: util layer for player management
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            var savePointInRange = this.savePointService.getSavePointInRange(onlinePlayer.getLocation());

            if (savePointInRange == null)
                continue;

            var account = this.accountService.getAccount(onlinePlayer.getUniqueId().toString());
            var character = this.characterService.getActivePlayerCharacter(account);

            if (!this.savePointService.isSavePointUnlocked(character.getId(), savePointInRange.getId())) {
                onlinePlayer.sendMessage("Du hast den Save-Point " + savePointInRange.getName() + " freigeschaltet! Mit /savepoints kannst du jetzt jederzeit wieder hier hinreisen.");
                this.savePointService.unlockSavePoint(character.getId(), savePointInRange.getId());
            }
        }
    }

    @Command(name = "savepoints")
    public void savepointCommand(Player player, String[] args) {
        var account = this.accountService.getAccount(player.getUniqueId().toString());
        var character = this.characterService.getActivePlayerCharacter(account);
        var savePoints = this.savePointService.getUnlockedSavePoints(character.getId());

        var itemSlotIndex = new AtomicInteger(0);
        var menu = Menu.builder()
                .closable(true)
                .title("Save Points")
                .type(InventoryType.CHEST)
                .items(
                        savePoints.stream()
                                .map(savePoint -> MenuItem.builder()
                                        .closesMenu(true)
                                        .displayName(savePoint.getName())
                                        .material(Material.getMaterial(savePoint.getIconMaterial()))
                                        .slot(itemSlotIndex.getAndIncrement())
                                        .onSelect(() -> player.teleport(savePoint.getPosition().toLocation(player.getWorld())))
                                        .build())
                                .collect(Collectors.toSet())
                )
                .build();

        this.menuService.openMenu(player, menu);
    }

}
