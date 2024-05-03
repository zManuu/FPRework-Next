package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.menu.Menu;
import de.fantasypixel.rework.modules.menu.MenuItem;
import de.fantasypixel.rework.modules.menu.MenuService;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class SavePointController {

    @Service private SavePointService savePointService;
    @Service private AccountService accountService;
    @Service private PlayerCharacterService characterService;
    @Service private MenuService menuService;

    // todo: timer starting twice?
    @Timer(interval = 10, type = TimerManager.TimerType.SYNC)
    public void unlockTimer() {
        // todo: util layer for player management
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            var savePointInRange = this.savePointService.getSavePointInRange(onlinePlayer.getLocation());

            if (savePointInRange == null || savePointInRange.getId() == null)
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
        if (args.length == 0) {

            // open menu
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

        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {

            // delete save-point
            int savePointId;
            try {
                savePointId = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Die ID muss eine Zahl sein!");
                return;
            }
            if (this.savePointService.deleteSavePoint(savePointId)) {
                player.sendMessage("Der Save-Point " + savePointId + " wurde gelöscht!");
            } else {
                player.sendMessage("Der Save-Point " + savePointId + " konnte nicht gelöscht werden! Bitte stelle sicher, dass er existiert (/savepoints list) und die Datei valide ist.");
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {

            // list save-points
            var savePointsList = this.savePointService.getSavePointsList();
            player.sendMessage("Die aktuellen Save-Points:");
            player.sendMessage(savePointsList.toArray(String[]::new));

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("create")) {

            // create save-point
            Optional<String> iconMaterial = args.length == 3 ? Optional.of(args[2]) : Optional.empty();
            String name = args[1].replaceAll("_", " ");
            var success = this.savePointService.createSavePoint(
                    new JsonPosition(player.getLocation()),
                    name,
                    iconMaterial
            );


            if (success) {
                player.sendMessage("Der Save-Point wurde erstellt!");
            } else {
                player.sendMessage("Der Save-Point konnte nicht erstellt werden! Bitte überprüfe die Logs.");
            }

        }

    }

}
