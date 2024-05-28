package de.fantasypixel.rework.modules.savepoints;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.language.LanguageService;
import de.fantasypixel.rework.modules.menu.Menu;
import de.fantasypixel.rework.modules.menu.MenuItem;
import de.fantasypixel.rework.modules.menu.MenuService;
import de.fantasypixel.rework.modules.menu.design.SimpleMenuDesign;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import de.fantasypixel.rework.modules.utils.ServerUtils;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class SavePointController {

    @Service private SavePointService savePointService;
    @Service private AccountService accountService;
    @Service private PlayerCharacterService characterService;
    @Service private MenuService menuService;
    @Service private NotificationService notificationService;
    @Service private LanguageService languageService;
    @Service private ServerUtils serverUtils;

    @Timer(interval = 10, type = TimerManager.TimerType.SYNC)
    public void unlockTimer() {
        for (var onlinePlayer : this.serverUtils.getOnlinePlayers()) {
            var savePointInRange = this.savePointService.getSavePointInRange(onlinePlayer.getLocation());

            if (savePointInRange == null || savePointInRange.getId() == null)
                continue;

            var account = this.accountService.getAccount(onlinePlayer.getUniqueId());
            var character = this.characterService.getActivePlayerCharacter(account);

            if (!this.savePointService.isSavePointUnlocked(character.getId(), savePointInRange.getId())) {
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, onlinePlayer, "savepoint-unlocked", Map.of("NAME", savePointInRange.getName()));
                this.savePointService.unlockSavePoint(character.getId(), savePointInRange.getId());
            }
        }
    }

    @Command(name = "savepoints")
    public void savepointCommand(Player player, String[] args) {
        if (args.length == 0) {

            // open menu
            var account = this.accountService.getAccount(player.getUniqueId());
            var character = this.characterService.getActivePlayerCharacter(account);
            var unlockedSavePoints = this.savePointService.getUnlockedSavePoints(character.getId());
            var lockedSavePoints = this.savePointService.getLockedSavePoints(character.getId());

            var items = new LinkedHashSet<MenuItem>();

            items.addAll(
                    unlockedSavePoints.stream()
                            .map(savePoint -> MenuItem.builder()
                                    .closesMenu(true)
                                    .displayName(savePoint.getName())
                                    .lore(List.of(this.languageService.getTranslation(account.getId(), "savepoint-distance", Math.round(savePoint.getPosition().toLocation().distance(player.getLocation())))))
                                    .material(Material.getMaterial(savePoint.getIconMaterial()))
                                    .onSelect(() -> player.teleport(savePoint.getPosition().toLocation()))
                                    .build())
                            .collect(Collectors.toSet())
            );

            items.addAll(
                    lockedSavePoints.stream()
                            .map(savePoint -> MenuItem.builder()
                                    .closesMenu(false)
                                    .displayName(savePoint.getName())
                                    .lore(List.of(this.languageService.getTranslation(account.getId(), "savepoint-distance", Math.round(savePoint.getPosition().toLocation().distance(player.getLocation())))))
                                    .material(Material.GRAY_DYE)
                                    .build())
                            .collect(Collectors.toSet())
            );

            var menu = Menu.builder()
                    .closeButton(true)
                    .title("Save Points")
                    .design(new SimpleMenuDesign())
                    .items(items)
                    .build();

            this.menuService.openMenu(player, menu);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {

            // delete save-point
            int savePointId;
            try {
                savePointId = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "savepoint-delete-expected-number");
                return;
            }
            if (this.savePointService.deleteSavePoint(savePointId)) {
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "savepoint-delete-success", Map.of("ID", savePointId));
            } else {
                this.notificationService.sendChatMessage(NotificationType.ERROR, player, "savepoint-delete-error", Map.of("ID", savePointId));
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {

            // list save-points
            this.notificationService.sendChatMessage(player, "savepoint-list", this.savePointService.getSavePointsList());

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
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "savepoint-create-success");
            } else {
                this.notificationService.sendChatMessage(NotificationType.ERROR, player, "savepoint-create-error");
            }

        }

    }

}
