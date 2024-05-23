package de.fantasypixel.rework.modules.items;

import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import de.fantasypixel.rework.modules.utils.NamespacedKeyUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ServiceProvider
public class ItemService {

    @Service private NamespacedKeyUtils namespacedKeyUtils;
    @Service private PlayerCharacterService playerCharacterService;
    @Config private ItemsConfig itemsConfig;

    /**
     * Gets the item (-type) of an itemStack.
     * @param itemStack the item stack
     * @return the inventory-item id
     * @throws NullPointerException if no item-id was found on the item-stack or no item-stack was passed
     */
    @Nonnull
    public Item getItemOf(@Nullable ItemStack itemStack) throws NullPointerException {
        Objects.requireNonNull(itemStack);
        var itemMeta = Objects.requireNonNull(itemStack.getItemMeta());
        var persistentDataContainer = itemMeta.getPersistentDataContainer();
        var itemIdentifierNamespacedKey = this.namespacedKeyUtils.getNamespacedKey(NamespacedKeyUtils.NamespacedKeyType.ITEM_IDENTIFIER);
        var itemIdentifier = Objects.requireNonNull(persistentDataContainer.get(itemIdentifierNamespacedKey, PersistentDataType.STRING));

        return Items.getByIdentifier(itemIdentifier)
                .orElseThrow(NullPointerException::new);
    }

    /**
     * Builds a lore based on item and player-character.
     * <br><br>
     * <b>Lore format:</b>
     * <br><br>
     * {@code {DESCRIPTION}}<br>
     * --------------------<br>
     * ⚔ Schwert<br>
     * Schaden: {@code {WEAPON_DAMAGE}]</pre>
     */
    @Nonnull
    private List<String> buildItemLore(@Nonnull Item item, @Nonnull PlayerCharacter playerCharacter) {
        var lore = new ArrayList<String>();

        lore.add("§7" + item.getDescription());
        lore.add(this.itemsConfig.getLoreLine());

        if (item instanceof Weapon weaponItem) {
            var weaponHitDamage = weaponItem.getHitDamage();

            lore.add("§7➥ §4Waffe");
            lore.add("§7  ➳ Schaden: §c" + weaponHitDamage);
        }

        return lore;
    }

    /**
     * Build an {@link Item} to an item-stack.
     * @param item the item (-type)
     * @param amount the amount of the item-stack
     * @return the built item-stack
     * @throws NullPointerException if no item is passed
     */
    @Nonnull
    private ItemStack buildItem(@Nullable Item item, @Nullable PlayerCharacter playerCharacter, int amount) throws NullPointerException {
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(playerCharacter, "Player-character cannot be null");

        var itemNamespacedKey = this.namespacedKeyUtils.getNamespacedKey(NamespacedKeyUtils.NamespacedKeyType.ITEM_IDENTIFIER);
        var itemStack = new ItemStack(item.getMaterial(), amount);
        var itemMetaData = Objects.requireNonNull(itemStack.getItemMeta());
        var itemPersistentDataContainer = itemMetaData.getPersistentDataContainer();

        // meta data
        itemMetaData.setDisplayName(item.getDisplayName());
        itemMetaData.setLore(this.buildItemLore(item, playerCharacter));
        itemMetaData.setUnbreakable(true);

        // persistent data
        itemPersistentDataContainer.set(itemNamespacedKey, PersistentDataType.STRING, item.getIdentifier());

        itemStack.setItemMeta(itemMetaData);
        return itemStack;
    }

    /**
     * Gives a player an item. Uses {@link #buildItem(Item, PlayerCharacter, int)} to build the item to an item-stack.
     * @param player the player to be given an item
     * @param item the item to be given
     * @param slot the slot to put the item in. if null is passed, the item will be placed somewhere where there is place
     * @param amount the amount to give to the player
     */
    public void giveItem(@Nonnull Player player, @Nonnull Item item, @Nullable Integer slot, int amount) throws NullPointerException {
        var playerCharacter = this.playerCharacterService.getPlayerCharacter(player);
        var itemStack = this.buildItem(item, playerCharacter, amount);
        player.getInventory().addItem(itemStack);
    }

}
