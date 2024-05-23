package de.fantasypixel.rework.modules.items;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

@Controller
public class ItemsController implements Listener {

    @Service private ItemService itemService;
    @Service private PlayerCharacterService playerCharacterService;
    @Service private NotificationService notificationService;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player from) || (!(event.getEntity() instanceof Player to))) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }

        ItemStack itemStack = from.getInventory().getItemInMainHand();
        Item item;
        PlayerCharacter fromPlayerCharacter;
        PlayerCharacter toPlayerCharacter;

        try {
            item = this.itemService.getItemOf(itemStack);
            fromPlayerCharacter = this.playerCharacterService.getPlayerCharacter(from);
            toPlayerCharacter = this.playerCharacterService.getPlayerCharacter(to);
        } catch (NullPointerException | IllegalArgumentException ex) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }

        if (!(item instanceof Weapon weapon)) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }

        var damage = weapon.getHitDamage();
        event.setDamage(damage);
    }

    @Command(name = "item")
    public void giveItemCommand(Player player, String[] args) {
        if (args.length == 1 || args.length == 2) {
            Item item;
            int itemAmount;

            try {
                item = Items.getByIdentifier(args[0]).orElseThrow(IllegalArgumentException::new);
            } catch (IllegalArgumentException ex) {
                this.notificationService.sendChatMessage(player, "unknown-item-identifier", args[0]);
                return;
            }

            try {
                itemAmount = Integer.parseInt(args.length == 2 ? args[1] : "1");
            } catch (NumberFormatException ex) {
                this.notificationService.sendChatMessage(player, "invalid-amount");
                return;
            }

            this.itemService.giveItem(player, item, null, itemAmount);
            this.notificationService.sendChatMessage(player, "item-given");
        }
    }

}
