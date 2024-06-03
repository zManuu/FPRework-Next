package de.fantasypixel.rework.modules.shops;

import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.items.Item;
import de.fantasypixel.rework.modules.items.ItemService;
import de.fantasypixel.rework.modules.items.Items;
import de.fantasypixel.rework.modules.items.items.currency.Currency1;
import de.fantasypixel.rework.modules.language.LanguageService;
import de.fantasypixel.rework.modules.menu.Menu;
import de.fantasypixel.rework.modules.menu.MenuItem;
import de.fantasypixel.rework.modules.menu.design.MenuDesign;
import de.fantasypixel.rework.modules.menu.design.SimpleMenuDesign;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.npc.NpcService;
import de.fantasypixel.rework.modules.npc.npcs.Villager;
import de.fantasypixel.rework.modules.playercharacter.PlayerCharacter;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@ServiceProvider
public class ShopService {

    private final static String CLASS_NAME = ShopService.class.getSimpleName();

    @JsonData private JsonDataContainer<Shop> shops;
    @Service private LanguageService languageService;
    @Service private ItemService itemService;
    @Service private NotificationService notificationService;
    @Service private AccountService accountService;
    @Service private NpcService npcService;
    @Auto private FPLogger logger;

    /**
     * Creates a shop.
     * @param jsonPosition the position of the shop
     * @param name the name of the shop (can be null)
     * @return the id of the created shop
     * @throws NullPointerException if the shop could not be created
     */
    public int createShop(@Nonnull JsonPosition jsonPosition, @Nullable String name, @Nullable String professionName) throws NullPointerException {
        org.bukkit.entity.Villager.Profession profession;

        try {
            profession = org.bukkit.entity.Villager.Profession.valueOf(
                    professionName != null
                        ? professionName
                        : org.bukkit.entity.Villager.Profession.NONE.name()
            );
        } catch (IllegalArgumentException ex) {
            this.logger.warning("Someone tried to create a shop with non-existent villager profession {0}.", professionName);
            throw new NullPointerException("Invalid professionName.");
        }

        var shop = new Shop(
                null,
                name,
                jsonPosition,
                profession,
                Collections.emptySet(),
                Collections.emptySet()
        );

        if (this.shops.create(shop))
            return Objects.requireNonNull(shop.getId());
        else
            throw new NullPointerException("Couldn't create shop!");
    }

    /**
     * Gets a shop.
     * @param shopId the shop's id
     * @return the shop found from cache / json files
     * @throws IllegalArgumentException if no shop is found with the id
     */
    public Shop getById(int shopId) throws IllegalArgumentException {
        return this.shops.getEntries().stream()
                .filter(e -> Objects.equals(e.getId(), shopId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Gets the final price of a shop-item or the default-price of that item.
     * @throws NullPointerException if the shop-item has a custom price but none was found or no item was found with the specified identifier.
     */
    public int getShopItemPrice(Shop.ShopItem shopItem) throws NullPointerException {
        if (Boolean.TRUE.equals(shopItem.getIsCustomPrice()))
            return Objects.requireNonNull(shopItem.getPrice());

        Item item = Items.getByIdentifier(shopItem.getItemIdentifier()).orElseThrow(NullPointerException::new);
        return item.getDefaultPrice();
    }

    /**
     * Builds a shop-menu to the player.
     * @param shopMenuType the type of the shop menu (selecting, buy, sell)
     */
    public Menu buildShopMenu(@Nonnull Player player, @Nonnull PlayerCharacter playerCharacter, @Nonnull ShopMenuType shopMenuType, @Nonnull Shop shop) {
        this.logger.debug("Building shop menu. Player: {0}, Shop-ID: {1}, Menu-Type: {2}", player.getName(), shop.getId(), shopMenuType);

        int playerCharacterId = playerCharacter.getId();
        Set<MenuItem> menuItems = new LinkedHashSet<>();
        Set<MenuItem> menuSpecialItems = new LinkedHashSet<>();
        String menuTitle = null;
        MenuDesign menuDesign = null;
        boolean pagedMenu = false;

        MenuItem sellNavItem = MenuItem.builder()
                .material(Material.EMERALD)
                .displayName("§a" + this.languageService.getTranslation(playerCharacterId, "shop-sell"))
                .subMenu(() -> this.buildShopMenu(player, playerCharacter, ShopMenuType.SELL, shop))
                .build();

        MenuItem buyNavItem = MenuItem.builder()
                .material(Material.EMERALD)
                .displayName("§a" + this.languageService.getTranslation(playerCharacterId, "shop-buy"))
                .subMenu(() -> this.buildShopMenu(player, playerCharacter, ShopMenuType.BUY, shop))
                .build();

        if (shopMenuType == ShopMenuType.LOBBY) {
            menuDesign = new SimpleMenuDesign(InventoryType.HOPPER);
            menuItems = Set.of(sellNavItem, buyNavItem);
        } else if (shopMenuType == ShopMenuType.SELL) {
            menuDesign = new SimpleMenuDesign(InventoryType.CHEST);
            menuTitle = (shop.getName() != null ? shop.getName() : "Shop") + " §3(" + this.languageService.getTranslation(playerCharacterId, "shop-sell") + ")";
            pagedMenu = true;

            for (Shop.ShopItem sellItem : shop.getSellItems()) {
                Item sellItemItem = Items.getByIdentifier(sellItem.getItemIdentifier()).orElse(null);

                if (sellItemItem == null) {
                    this.logger.error(CLASS_NAME, "openShopMenu->sell", "Couldn't find item with identifier \"{0}\" in shop with id {1}. The shop menu won't be opened to player {2}!", sellItem.getItemIdentifier(), shop.getId(), player.getName());
                    return null;
                }

                int itemPrice;

                try {
                    itemPrice = this.getShopItemPrice(sellItem);
                } catch (NullPointerException ex) {
                    this.logger.warning(CLASS_NAME, "openShopMenu->sell", "Couldn't find item-price with item-identifier \"{0}\" in shop with id {1}. The shop menu won't be opened to player {2}! Error following...", sellItem.getItemIdentifier(), shop.getId(), player.getName());
                    this.logger.error(CLASS_NAME, "openShopMenu->sell", ex);
                    return null;
                }

                boolean isDiscount = Boolean.TRUE.equals(sellItem.getIsDiscount());
                ItemStack sellItemStack = this.itemService.buildItem(sellItemItem, playerCharacter, 1, itemPrice, isDiscount);
                MenuItem sellMenuItem = new MenuItem(sellItemStack);
                sellMenuItem.setOnSelect(() -> this.tryBuyItem(player, shopMenuType, sellItemItem, itemPrice));
                menuItems.add(sellMenuItem);
            }

            menuSpecialItems.add(buyNavItem);
        } else if (shopMenuType == ShopMenuType.BUY) {
            menuDesign = new SimpleMenuDesign(InventoryType.CHEST);
            menuTitle = (shop.getName() != null ? shop.getName() : "Shop") + " §3(" + this.languageService.getTranslation(playerCharacterId, "shop-buy") + ")";
            pagedMenu = true;

            for (Shop.ShopItem buyItem : shop.getBuyItems()) {
                Item buyItemItem = Items.getByIdentifier(buyItem.getItemIdentifier()).orElse(null);

                if (buyItemItem == null) {
                    this.logger.error(CLASS_NAME, "openShopMenu->buy", "Couldn't find item with identifier \"{0}\" in shop with id {1}. The shop menu won't be opened to player {2}!", buyItem.getItemIdentifier(), shop.getId(), player.getName());
                    return null;
                }

                int itemPrice;

                try {
                    itemPrice = this.getShopItemPrice(buyItem);
                } catch (NullPointerException ex) {
                    this.logger.warning(CLASS_NAME, "openShopMenu->buy", "Couldn't find item-price with item-identifier \"{0}\" in shop with id {1}. The shop menu won't be opened to player {2}! Error following...", buyItem.getItemIdentifier(), shop.getId(), player.getName());
                    this.logger.error(CLASS_NAME, "openShopMenu->buy", ex);
                    return null;
                }

                boolean isDiscount = Boolean.TRUE.equals(buyItem.getIsDiscount());
                ItemStack buyItemStack = this.itemService.buildItem(buyItemItem, playerCharacter, 1, itemPrice, isDiscount);
                MenuItem buyMenuItem = new MenuItem(buyItemStack);
                buyMenuItem.setOnSelect(() -> this.tryBuyItem(player, shopMenuType, buyItemItem, itemPrice));
                menuItems.add(buyMenuItem);
            }

            menuSpecialItems.add(sellNavItem);
        }

        String finalMenuTitle = "§3§l" + (menuTitle != null ? menuTitle : (shop.getName() != null ? shop.getName() : "Shop"));

        return Menu.builder()
                .title(finalMenuTitle)
                .design(menuDesign)
                .closeButton(true)
                .pages(pagedMenu)
                .items(menuItems)
                .specialItems(menuSpecialItems)
                .build();
    }

    /**
     * Make a player buy an item.
     * @param player the player
     * @param shopMenuType the type of buy (buy or sell)
     * @param item the bought / sold item
     * @param itemPrice the price
     */
    public void tryBuyItem(@Nonnull Player player, @Nonnull ShopMenuType shopMenuType, @Nonnull Item item, int itemPrice) {
        this.logger.debug("Player {0} tries to {1} the item {2} for {3}.", player.getName(), shopMenuType, item.getIdentifier(), itemPrice);

        var itemIdentifier = item.getIdentifier();
        var accountId = this.accountService.getAccount(player.getUniqueId()).getId();

        if (shopMenuType == ShopMenuType.SELL) {
            var hasSellItems = this.itemService.hasItemAmount(player, itemIdentifier, 1);
            if (!hasSellItems) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "shop-insufficient-funds");
                return;
            }

            Items.getByIdentifier(Currency1.IDENTIFIER).ifPresentOrElse(
                    (currencyItem) -> {
                        var translatedItemName = this.itemService.getItemDisplayName(item, accountId);
                        this.itemService.giveItem(player, currencyItem, null, itemPrice);
                        this.itemService.removeItem(player, itemIdentifier, 1);
                        this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "shop-item-sold", Map.of("AMOUNT", 1, "ITEM", translatedItemName, "PRICE", itemPrice));
                    },
                    () -> this.logger.error(CLASS_NAME, "tryBuyItem->sell", "Couldn't determine currency-item!!")
            );
        } else if (shopMenuType == ShopMenuType.BUY) {
            var hasFunds = this.itemService.hasItemAmount(player, Currency1.IDENTIFIER, itemPrice);
            if (!hasFunds) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "shop-insufficient-funds");
                return;
            }

            var translatedItemName = this.itemService.getItemDisplayName(item, accountId);
            this.itemService.giveItem(player, item, null, 1);
            this.itemService.removeItem(player, Currency1.IDENTIFIER, itemPrice);
            this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "shop-item-bought", Map.of("AMOUNT", 1, "ITEM", translatedItemName, "PRICE", itemPrice));
        }
    }

    /**
     * Checks if the given shop currently has any discounts.
     */
    public boolean hasShopDiscount(@Nonnull Shop shop) {
        for (Shop.ShopItem sellItem : shop.getSellItems()) {
            if (Boolean.TRUE.equals(sellItem.getIsDiscount()))
                return true;
        }

        for (Shop.ShopItem buyItem : shop.getBuyItems()) {
            if (Boolean.TRUE.equals(buyItem.getIsDiscount()))
                return true;
        }

        return false;
    }

    /**
     * Creates all NPCs for the shops.
     */
    public void createShopNPCs() {
        this.logger.debug("Creating all shop NPCs...");

        for (Shop shop : this.shops.getEntries()) {
            var npcHologram = new ArrayList<String>();
            var shopName = "§3§l" + (shop.getName() != null ? shop.getName() : "Shop");

            if (this.hasShopDiscount(shop))
                npcHologram.add("DISCOUNT !!!");

            var npc = new Villager(
                    shopName,
                    true,
                    shop.getVillagerProfession(),
                    npcHologram,
                    new ShopNpcMetaData(shop.getId())
            );

            this.npcService.createNpc(npc, shop.getPosition().toLocation());
        }

        this.logger.debug("Successfully created all shop NPCs.");
    }

}
