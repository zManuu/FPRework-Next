package de.fantasypixel.rework.modules.items;

import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.items.items.currency.CurrencyItem;
import de.fantasypixel.rework.modules.items.items.edible.Edible;
import de.fantasypixel.rework.modules.items.items.potions.Potion;
import de.fantasypixel.rework.modules.items.items.weapons.Weapon;
import de.fantasypixel.rework.modules.language.LanguageService;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacterService;
import de.fantasypixel.rework.modules.utils.NamespacedKeyUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ServiceProvider
public class ItemService {

    @Service private LanguageService languageService;
    @Service private NamespacedKeyUtils namespacedKeyUtils;
    @Service private PlayerCharacterService playerCharacterService;
    @Config private ItemsConfig itemsConfig;

    /**
     * Gets the item-identifier from an itemStack.
     * @throws NullPointerException if no item-identifier was found or no item-stack was passed
     */
    public String getItemIdentifier(@Nullable ItemStack itemStack) throws NullPointerException {
        Objects.requireNonNull(itemStack);
        var itemMeta = Objects.requireNonNull(itemStack.getItemMeta());
        var persistentDataContainer = itemMeta.getPersistentDataContainer();
        var itemIdentifierNamespacedKey = this.namespacedKeyUtils.getNamespacedKey(NamespacedKeyUtils.NamespacedKeyType.ITEM_IDENTIFIER);
        return Objects.requireNonNull(persistentDataContainer.get(itemIdentifierNamespacedKey, PersistentDataType.STRING));
    }

    /**
     * Gets the item (-type) of an itemStack.
     * @throws NullPointerException if no item-identifier was found or no item-stack was passed
     */
    @Nonnull
    public Item getItemOf(@Nullable ItemStack itemStack) throws NullPointerException {
        return Items.getByIdentifier(this.getItemIdentifier(itemStack))
                .orElseThrow(NullPointerException::new);
    }

    /**
     * @param itemIdentifier the item's identifier
     * @param accountId the player's account id
     * @return the item's description (colored!) or null of no description is registered (in the player's language)
     */
    @Nullable
    private String getItemDescription(@Nonnull String itemIdentifier, @Nullable Integer accountId) {
        var description = this.languageService.getTranslationOptional(accountId, MessageFormat.format("item-desc.{0}", itemIdentifier), null);
        return description == null
                ? null
                : "§7" + description;
    }

    @Nonnull
    public String getItemDisplayName(@Nonnull Item item, @Nullable Integer accountId) {
        return this.languageService.getTranslation(accountId, MessageFormat.format("item-name.{0}", item.getIdentifier()));
    }

    /**
     * @param item the item to get the display name of
     * @param accountId the player's account id
     * @return the display name of the item (colored!)
     */
    @Nonnull
    private String getColoredItemDisplayName(@Nonnull Item item, @Nullable Integer accountId) {
        var displayName = this.getItemDisplayName(item, accountId);
        if (item instanceof Weapon)
            displayName = "§c" + displayName;
        else if (item instanceof Potion)
            displayName = "§d" + displayName;
        else if (item instanceof Edible)
            displayName = "§a" + displayName;
        else if (item instanceof CurrencyItem)
            displayName = "§a" + displayName;
        return displayName;
    }

    /**
     * Builds a lore based on item and player-character.
     * Only used by {@link #buildItem(Item, PlayerCharacter, int, Integer, boolean)}!
     */
    @Nonnull
    private List<String> buildItemLore(@Nonnull Item item, @Nonnull PlayerCharacter playerCharacter, @Nullable Integer price, boolean isDiscounted) {
        var lore = new ArrayList<String>();
        var accountId = playerCharacter.getAccountId();

        // price (if specified)
        if (price != null) {
            lore.add(MessageFormat.format("§7{0}: §8§l", this.languageService.getTranslation(accountId, "price")) + price);

            if (isDiscounted)
                lore.add("§7" + this.languageService.getTranslation(accountId, "shop-discount"));

            lore.add(" ");
            lore.add("§8" + this.itemsConfig.getLoreLine());
        }

        // description
        var itemDescription = this.getItemDescription(item.getIdentifier(), accountId);
        if (itemDescription != null) {
            lore.add(itemDescription);
            lore.add("§8" + this.itemsConfig.getLoreLine());
        }

        // type-specific lore
        if (item instanceof Weapon weapon) {

            lore.add(MessageFormat.format("§7➥ §4§l{0}", this.languageService.getTranslation(accountId, "weapon")));
            lore.add(MessageFormat.format("§7  ➳ {0}: §c", this.languageService.getTranslation(accountId, "damage")) + weapon.getHitDamage());

        } else if (item instanceof Potion potion) {

            lore.add(MessageFormat.format("§7➥ §5§l{0}", this.languageService.getTranslation(accountId, "potion")));

            for (PotionEffect effect : potion.getEffects()) {
                var effectName = effect.getType().getName();
                var effectNameTranslation = this.languageService.getTranslationOptional(accountId, "effect." + effectName, null);
                var finalEffectName = effectNameTranslation == null ? effectName : effectNameTranslation;

                if (effect.getDuration() != Potion.NULL_DURATION)
                    lore.add(String.format("§7  ➳ §d%s §7(§d%sx§7): §d%st", finalEffectName, effect.getAmplifier(), effect.getDuration()));
                else
                    lore.add(String.format("§7  ➳ §d%s §7(§d%sx§7)", finalEffectName, effect.getAmplifier()));
            }

        } else if (item instanceof Edible edible) {

            lore.add(MessageFormat.format("§7➥ §2§l{0}", this.languageService.getTranslation(accountId, "edible")));
            lore.add(MessageFormat.format("§7  ➳ {0}: §a", this.languageService.getTranslation(accountId, "health-impact")) + edible.getHealth());
            lore.add(MessageFormat.format("§7  ➳ {0}: §a", this.languageService.getTranslation(accountId, "hunger-impact")) + edible.getHunger());

        } else if (item instanceof CurrencyItem currency) {

            lore.add(MessageFormat.format("§7➥ §2§l{0}", this.languageService.getTranslation(accountId, "currency")));
            lore.add(MessageFormat.format("§7  ➳ {0}: §a", this.languageService.getTranslation(accountId, "currency-worth")) + currency.getWorth());

        }

        return lore;
    }

    /**
     * Build an {@link Item} to an item-stack.
     * @param item the item (-type)
     * @param amount the amount of the item-stack
     * @param price the price of the item-stack (only used for shops). Will be displayed in the lore.
     * @param isDiscounted if the item is currently discounted (only used for shops). Will be displayed in the lore.
     * @return the built item-stack
     * @throws NullPointerException if one of the required data couldn't be determined
     */
    @Nonnull
    public ItemStack buildItem(@Nullable Item item, @Nullable PlayerCharacter playerCharacter, int amount, @Nullable Integer price, boolean isDiscounted) throws NullPointerException {
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(playerCharacter, "Player-character cannot be null");
        Objects.requireNonNull(playerCharacter.getId(), "Player-character(id) cannot be null");

        var itemNamespacedKey = this.namespacedKeyUtils.getNamespacedKey(NamespacedKeyUtils.NamespacedKeyType.ITEM_IDENTIFIER);
        var itemStack = new ItemStack(item.getMaterial(), amount);
        var itemMetaData = Objects.requireNonNull(itemStack.getItemMeta());
        var itemPersistentDataContainer = itemMetaData.getPersistentDataContainer();

        // meta data
        itemMetaData.setDisplayName(this.getColoredItemDisplayName(item, playerCharacter.getId()));
        itemMetaData.setLore(this.buildItemLore(item, playerCharacter, price, isDiscounted));
        itemMetaData.setUnbreakable(true);
        itemMetaData.addItemFlags(ItemFlag.values());

        // persistent data
        itemPersistentDataContainer.set(itemNamespacedKey, PersistentDataType.STRING, item.getIdentifier());

        // potion?
        if (item instanceof Potion potionItem) {
            var potionMeta = (PotionMeta) itemMetaData;
            potionMeta.setBasePotionData(new PotionData(potionItem.getPotionMaterial()));
        }

        itemStack.setItemMeta(itemMetaData);
        return itemStack;
    }

    /**
     * Gives a player an item. Uses {@link #buildItem(Item, PlayerCharacter, int, Integer, boolean)} to build the item to an item-stack.
     * @param player the player to be given an item
     * @param item the item to be given
     * @param slot the slot to put the item in. if null is passed, the item will be placed somewhere where there is place
     * @param amount the amount to give to the player
     */
    public void giveItem(@Nonnull Player player, @Nonnull Item item, @Nullable Integer slot, int amount) throws NullPointerException {
        var playerCharacter = this.playerCharacterService.getPlayerCharacter(player);
        var itemStack = this.buildItem(item, playerCharacter, amount, null, false);
        player.getInventory().addItem(itemStack);
    }

    /**
     * Checks if the player has the given amount of an item.
     */
    public boolean hasItemAmount(@Nonnull Player player, @Nonnull String itemIdentifier, int amount) {
        var foundAmount = 0;

        for (var inventoryItemStack : player.getInventory().getContents()) {
            if (inventoryItemStack == null)
                continue;

            var inventoryItemMeta = inventoryItemStack.getItemMeta();

            if (inventoryItemMeta == null)
                continue;

            String inventoryItemIdentifier;

            try {
                inventoryItemIdentifier = this.getItemIdentifier(inventoryItemStack);
            } catch (NullPointerException ex) {
                continue;
            }

            if (inventoryItemIdentifier.equalsIgnoreCase(itemIdentifier)) {
                foundAmount += inventoryItemStack.getAmount();
            }
        }

        // System.out.println("Found-Amount for item " + itemIdentifier + ": " + foundAmount + ", needed: " + amount);
        // System.out.println(foundAmount >= amount);

        return foundAmount >= amount;
    }

    /**
     * Removes items from the player's inventory.
     */
    public void removeItem(@Nonnull Player player, @Nonnull String itemIdentifier, int amountToRemove) {
        var remainingAmountToRemove = amountToRemove;

        for (var inventoryItemStack : player.getInventory().getContents()) {
            if (inventoryItemStack == null)
                continue;

            var inventoryItemMeta = inventoryItemStack.getItemMeta();

            if (inventoryItemMeta == null)
                continue;

            String inventoryItemIdentifier;

            try {
                inventoryItemIdentifier = this.getItemIdentifier(inventoryItemStack);
            } catch (NullPointerException ex) {
                continue;
            }

            if (inventoryItemIdentifier.equalsIgnoreCase(itemIdentifier)) {
                var inventoryItemAmount = inventoryItemStack.getAmount();

                if (remainingAmountToRemove > inventoryItemAmount) {
                    // remove item completely
                    inventoryItemStack.setAmount(0);
                    remainingAmountToRemove -= inventoryItemStack.getAmount();
                } else {
                    // remove amount from item
                    inventoryItemStack.setAmount(inventoryItemStack.getAmount() - remainingAmountToRemove);
                    return;
                }
            }
        }
    }

}
