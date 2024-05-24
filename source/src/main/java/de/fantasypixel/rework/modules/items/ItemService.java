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
     * @param itemIdentifier the item's identifier
     * @param playerCharacterId the player's character id
     * @return the item's description (colored!) or null of no description is registered (in the player's language)
     */
    @Nullable
    private String getItemDescription(@Nonnull String itemIdentifier, @Nullable Integer playerCharacterId) {
        var description = this.languageService.getTranslationOptional(playerCharacterId, MessageFormat.format("item-desc.{0}", itemIdentifier), null);
        return description == null
                ? null
                : "§7" + description;
    }

    /**
     * @param item the item to get the display name of
     * @param playerCharacterId the player's character id
     * @return the display name of the item (colored!)
     */
    @Nonnull
    private String getItemDisplayName(@Nonnull Item item, @Nullable Integer playerCharacterId) {
        var displayName = this.languageService.getTranslation(playerCharacterId, MessageFormat.format("item-name.{0}", item.getIdentifier()));
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
     */
    @Nonnull
    private List<String> buildItemLore(@Nonnull Item item, @Nonnull PlayerCharacter playerCharacter) {
        var lore = new ArrayList<String>();
        var playerCharacterId = playerCharacter.getId();

        // description
        var itemDescription = this.getItemDescription(item.getIdentifier(), playerCharacterId);
        if (itemDescription != null) {
            lore.add(itemDescription);
            lore.add("§8" + this.itemsConfig.getLoreLine());
        }

        // type-specific lore
        if (item instanceof Weapon weapon) {

            lore.add(MessageFormat.format("§7➥ §4§l{0}", this.languageService.getTranslation(playerCharacterId, "weapon")));
            lore.add(MessageFormat.format("§7  ➳ {0}: §c", this.languageService.getTranslation(playerCharacterId, "damage")) + weapon.getHitDamage());

        } else if (item instanceof Potion potion) {

            lore.add(MessageFormat.format("§7➥ §5§l{0}", this.languageService.getTranslation(playerCharacterId, "potion")));

            for (PotionEffect effect : potion.getEffects()) {
                var effectName = effect.getType().getName();
                var effectNameTranslation = this.languageService.getTranslationOptional(playerCharacterId, "effect." + effectName, null);
                var finalEffectName = effectNameTranslation == null ? effectName : effectNameTranslation;

                if (effect.getDuration() != Potion.NULL_DURATION)
                    lore.add(String.format("§7  ➳ §d%s §7(§d%sx§7): §d%st", finalEffectName, effect.getAmplifier(), effect.getDuration()));
                else
                    lore.add(String.format("§7  ➳ §d%s §7(§d%sx§7)", finalEffectName, effect.getAmplifier()));
            }

        } else if (item instanceof Edible edible) {

            lore.add(MessageFormat.format("§7➥ §2§l{0}", this.languageService.getTranslation(playerCharacterId, "edible")));
            lore.add(MessageFormat.format("§7  ➳ {0}: §a", this.languageService.getTranslation(playerCharacterId, "health-impact")) + edible.getHealth());
            lore.add(MessageFormat.format("§7  ➳ {0}: §a", this.languageService.getTranslation(playerCharacterId, "hunger-impact")) + edible.getHunger());

        } else if (item instanceof CurrencyItem currency) {

            lore.add(MessageFormat.format("§7➥ §2§l{0}", this.languageService.getTranslation(playerCharacterId, "currency")));
            lore.add(MessageFormat.format("§7  ➳ {0}: §a", this.languageService.getTranslation(playerCharacterId, "currency-worth")) + currency.getWorth());

        }

        return lore;
    }

    /**
     * Build an {@link Item} to an item-stack.
     * @param item the item (-type)
     * @param amount the amount of the item-stack
     * @return the built item-stack
     * @throws NullPointerException if one of the required data couldn't be determined
     */
    @Nonnull
    private ItemStack buildItem(@Nullable Item item, @Nullable PlayerCharacter playerCharacter, int amount) throws NullPointerException {
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(playerCharacter, "Player-character cannot be null");
        Objects.requireNonNull(playerCharacter.getId(), "Player-character(id) cannot be null");

        var itemNamespacedKey = this.namespacedKeyUtils.getNamespacedKey(NamespacedKeyUtils.NamespacedKeyType.ITEM_IDENTIFIER);
        var itemStack = new ItemStack(item.getMaterial(), amount);
        var itemMetaData = Objects.requireNonNull(itemStack.getItemMeta());
        var itemPersistentDataContainer = itemMetaData.getPersistentDataContainer();

        // meta data
        itemMetaData.setDisplayName(this.getItemDisplayName(item, playerCharacter.getId()));
        itemMetaData.setLore(this.buildItemLore(item, playerCharacter));
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
