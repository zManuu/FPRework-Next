package de.fantasypixel.rework.modules.shops;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.menu.Menu;
import de.fantasypixel.rework.modules.menu.MenuService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

@Controller
public class ShopController {

    @Service private ShopService shopService;
    @Service private NotificationService notificationService;
    @Service private PlayerCharacterService playerCharacterService;
    @Service private MenuService menuService;

    @Command(name = "shop")
    public void onShopCommand(Player player, String[] args) {
        if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("create")) {

            // create shop
            JsonPosition jsonPosition = new JsonPosition(player.getLocation());
            String shopName = args.length == 3 ? args[2] : null;
            int shopId;

            try {
                shopId = this.shopService.createShop(jsonPosition, shopName);
            } catch (IllegalArgumentException ex) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "unknown-shop-type");
                return;
            } catch (NullPointerException ex) {
                this.notificationService.sendChatMessage(NotificationType.ERROR, player, "shop-create-error");
                return;
            }

            this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "shop-create-success", Map.of("SHOP_ID", shopId));

        } else if (args.length == 2 && args[0].equalsIgnoreCase("open")) {

            // open shop ui (buy / sell)
            PlayerCharacter playerCharacter;
            int shopId;
            Shop shop;

            try {
                playerCharacter = Objects.requireNonNull(this.playerCharacterService.getPlayerCharacter(player));
                shopId = Integer.parseInt(args[1]);
                shop = this.shopService.getById(shopId);
            } catch (NumberFormatException ex) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "invalid-amount");
                return;
            } catch (IllegalArgumentException ex) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "unknown-shop");
                return;
            }

            Menu shopMenu = this.shopService.buildShopMenu(player, playerCharacter, ShopMenuType.LOBBY, shop);
            this.menuService.openMenu(player, shopMenu);
        }
    }

}
