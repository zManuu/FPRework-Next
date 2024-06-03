package de.fantasypixel.rework.modules.shops;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.events.AfterReload;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.menu.Menu;
import de.fantasypixel.rework.modules.menu.MenuService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.npc.Npc;
import de.fantasypixel.rework.modules.npc.NpcService;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Map;
import java.util.Objects;

@Controller
public class ShopController implements Listener {

    @Service private ShopService shopService;
    @Service private NotificationService notificationService;
    @Service private PlayerCharacterService playerCharacterService;
    @Service private NpcService npcService;
    @Service private MenuService menuService;

    @Command(name = "shop")
    public void onShopCommand(Player player, String[] args) {
        if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("create")) {

            // create shop
            JsonPosition jsonPosition = new JsonPosition(player.getLocation());
            String shopName = args.length == 2 ? args[1] : null;
            String professionName = args.length == 3 ? args[2] : null;
            int shopId;

            try {
                shopId = this.shopService.createShop(jsonPosition, shopName, professionName);
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

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Npc npc;

        try {
            npc = this.npcService.getNpc(entity);
        } catch (IllegalArgumentException ex) {
            return;
        }

        if (!(npc.getMetaData() instanceof ShopNpcMetaData npcShopMetaData))
            return;

        int shopId = npcShopMetaData.getShopId();
        Shop shop;
        PlayerCharacter playerCharacter;

        try {
            shop = this.shopService.getById(shopId);
            playerCharacter = this.playerCharacterService.getPlayerCharacter(player);
        } catch (IllegalArgumentException | NullPointerException ex) {
            this.notificationService.sendChatMessage(player, "500");
            throw new IllegalStateException(ex);
        }

        Menu shopMenu = this.shopService.buildShopMenu(player, playerCharacter, ShopMenuType.LOBBY, shop);
        this.menuService.openMenu(player, shopMenu);
    }

    @OnEnable
    @AfterReload
    public void setupNPCs() {
        this.shopService.createShopNPCs();
    }

}
