package de.fantasypixel.rework.modules.menu;

import de.fantasypixel.rework.modules.language.LanguageService;
import de.fantasypixel.rework.modules.menu.design.*;
import de.fantasypixel.rework.modules.sound.Sound;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A menu that can be opened to a player.
 */
@Builder
@Getter
public class Menu {

    private String title;
    private Boolean closable;
    private MenuDesign design;

    /**
     * <b>Note:</b> When passing, and you want them to be ordered, pass a {@link java.util.LinkedHashSet}.
     */
    private Set<MenuItem> items;

    /**
     * The passed runnable is executed once the inventory is closed (by the player pressing ESC).
     */
    private Runnable onClose;

    /**
     * <b>Note:</b> When passing, and you want them to be ordered, pass a {@link java.util.LinkedHashSet}.
     */
    private Set<MenuItem> specialItems;

    /**
     * Whether the menu should have multiple pages.
     */
    private boolean pages;

    /**
     * The page-index. Defaults to 0.
     */
    private Integer pageIndex;

    /**
     * When set to true, a close-button is added closing the menu.
     */
    private boolean closeButton;

    /**
     * Gets the static height of the passed inventory-type.
     * <br><br>
     * <b>When new inventory-types should be supported, they should be added here!</b>
     */
    public static int getInventoryHeight(InventoryType inventoryType) {
        return switch (inventoryType) {
            case HOPPER -> 1;
            case CHEST, ENDER_CHEST -> 3;
            default -> 0;
        };
    }

    /**
     * Combines {@link #items} and {@link #specialItems}.
     */
    public Set<MenuItem> getAllItems() {
        var mergedSet = new HashSet<MenuItem>();
        mergedSet.addAll(this.items);
        mergedSet.addAll(this.specialItems);
        return mergedSet;
    }

    /**
     * Builds the menu. All {@link MenuItem#isDisposable()} special-items are disposed before being added again.
     * @throws NullPointerException if no design was specified
     */
    @Nonnull
    public Inventory toInventory(@Nonnull LanguageService languageService, @Nonnull Player player) throws NullPointerException {
        Objects.requireNonNull(this.design, "No design specified.");

        // default values
        if (this.specialItems == null)
            this.specialItems = new LinkedHashSet<>();

        if (this.pageIndex == null)
            this.pageIndex = 0;

        if (this.closable == null)
            this.closable = true;

        // remove disposable special-items
        this.specialItems.removeIf(MenuItem::isDisposable);

        // close-btn
        if (this.closeButton)
            this.specialItems.add(
                    MenuItem.builder()
                            .closesMenu(true)
                            .material(Material.BARRIER)
                            .displayName("§c" + languageService.getTranslation(player, "btn-close-menu"))
                            .disposable(true)
                            .build()
            );

        // pass item-counts to design
        if (this.getDesign() instanceof SimpleMenuDesign simpleMenuDesign) {
            var itemCount = this.items.size();
            var specialItemCount = this.specialItems.size();

            // slots need to be available as the paging-mechanism needs them to calculate the amount of pages
            if (this.pages)
                specialItemCount += 2;

            simpleMenuDesign.setItemCount(itemCount);
            simpleMenuDesign.setSpecialItemCount(specialItemCount);
        }

        var itemArray = this.items.toArray(MenuItem[]::new);
        var itemSlots = this.design.getItemSlots();
        var itemCapacity = itemSlots.length;
        var totalPages = Math.round((double) itemArray.length / itemCapacity);
        var hasPreviousPage = this.pageIndex > 0;
        var hasNextPage = this.pageIndex < (totalPages - 1);

        // page items
        if (this.pages) {
            if (hasPreviousPage) {
                var previousPageIndex = this.pageIndex - 1;
                this.specialItems.add(
                        MenuItem.builder()
                                .subMenu(() -> new Menu(title, closable, design, items, onClose, specialItems, pages, previousPageIndex, closeButton))
                                .material(Material.ARROW)
                                .displayName("§8" + languageService.getTranslation(player, "menu-previous-page") + " (" + (previousPageIndex + 1) + ")")
                                .disposable(true)
                                .clickSound(Sound.PAGE)
                                .build()
                );
            } else {
                this.specialItems.add(
                        MenuItem.builder()
                                .material(Material.ARROW)
                                .displayName("§8§m" + languageService.getTranslation(player, "menu-previous-page"))
                                .disposable(true)
                                .clickSound(Sound.DENIED)
                                .build()
                );
            }

            if (hasNextPage) {
                var nextPageIndex = this.pageIndex + 1;
                this.specialItems.add(
                        MenuItem.builder()
                                .subMenu(() -> new Menu(title, closable, design, items, onClose, specialItems, pages, nextPageIndex, closeButton))
                                .material(Material.ARROW)
                                .displayName("§8" + languageService.getTranslation(player, "menu-next-page") + " (" + (nextPageIndex + 1) + ")")
                                .disposable(true)
                                .clickSound(Sound.PAGE)
                                .build()
                );
            } else {
                this.specialItems.add(
                        MenuItem.builder()
                                .material(Material.ARROW)
                                .displayName("§8§m" + languageService.getTranslation(player, "menu-next-page"))
                                .disposable(true)
                                .clickSound(Sound.DENIED)
                                .build()
                );
            }
        }

        var takenSlots = new HashSet<Integer>();
        var inventory = Bukkit.createInventory(
                null,
                this.design.getInventoryType(),
                this.title
        );

        // regular items
        for (var i=0; i<itemCapacity; i++) {
            var itemSlot = itemSlots[i];
            var pagedItemIndex = (this.pageIndex * itemCapacity) + i;
            takenSlots.add(itemSlot);

            // System.out.println("-----------------");
            // System.out.println("i = " + i);
            // System.out.println("itemArray.length = " + itemArray.length);
            // System.out.println("itemCapacity = " + itemCapacity);
            // System.out.println("pagedItemIndex = " + pagedItemIndex);

            if (pagedItemIndex < itemArray.length) {
                var item = itemArray[pagedItemIndex];
                item.setSlot(itemSlot);
                var itemStack = item.toItemStack();
                inventory.setItem(itemSlot, itemStack);
            }
        }

        // special items
        var specialItemArrayList = new ArrayList<>(this.specialItems);
        Collections.reverse(specialItemArrayList);
        var specialItemArray = specialItemArrayList.toArray(MenuItem[]::new);
        var specialItemSlots = this.design.getSpecialItemSlots();
        var specialItemCapacity = specialItemSlots.length;
        for (var i=0; i<specialItemCapacity; i++) {
            var itemSlot = specialItemSlots[i];
            takenSlots.add(itemSlot);

            if (specialItemArray.length > i) {
                var item = specialItemArray[i];
                item.setSlot(itemSlot);
                var itemStack = item.toItemStack();
                inventory.setItem(itemSlot, itemStack);
            }
        }

        // separators
        var lastSlotIndex = this.design.getInventoryType().getDefaultSize() - 1;
        for (var i=0; i<lastSlotIndex+1; i++) {
            if (takenSlots.contains(i))
                continue;

            inventory.setItem(
                    i,
                    MenuItem.builder()
                            .displayName(" ")
                            .material(Material.GRAY_STAINED_GLASS_PANE)
                            .build()
                            .toItemStack()
            );
        }

        return inventory;
    }

}
