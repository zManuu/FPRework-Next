package de.fantasypixel.rework.modules.menu.design;

import de.fantasypixel.rework.modules.menu.Menu;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A menu design splitting the inventory dynamically based on items and special-items.
 * Items can also be placed centered ({@link #placeItemsCentered}).
 */
public class SimpleMenuDesign extends MenuDesign {

    @Getter private final InventoryType inventoryType; // getter to override from super
    @Setter private int itemCount;
    @Setter private int specialItemCount;
    private final boolean placeItemsCentered;

    public SimpleMenuDesign(InventoryType inventoryType, boolean placeItemsCentered) {
        this.inventoryType = inventoryType;
        this.placeItemsCentered = placeItemsCentered;
    }

    public SimpleMenuDesign(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
        this.placeItemsCentered = false;
    }

    public SimpleMenuDesign() {
        this.inventoryType = InventoryType.CHEST;
        this.placeItemsCentered = false;
    }

    /**
     * Computes all slots in a row.
     */
    @Nonnull
    private Integer[] getSlotsInRow(int row, int inventoryHeight, int inventoryWidth) {
        var results = new ArrayList<Integer>();
        for (int lineIndex=0; lineIndex<inventoryHeight; lineIndex++) {
            var lineStartSlot = lineIndex * inventoryWidth;
            var columnSlot = lineStartSlot + row;
            results.add(columnSlot);
        }
        return results.toArray(Integer[]::new);
    }

    /**
     * Computes all available slots based on a given row-count with {@link #getSlotsInRow(int, int, int)}.
     * Starts at row and index 0.
     */
    private Integer[] getItemSlotsAvailable(int rows, int inventoryHeight, int inventoryWidth) {
        var results = new Integer[rows * inventoryHeight];

        if (this.specialItemCount == 0) {
            // all slots are available for items
            var inventorySize = this.inventoryType.getDefaultSize();
            for (int i = 0; i < inventorySize; i++)
                results[i] = i;
            return results;
        }

        var slotIndex = 0;
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            var slotsInRow = this.getSlotsInRow(rowIndex, inventoryHeight, inventoryWidth);
            for (Integer slot : slotsInRow) {
                results[slotIndex] = slot;
                slotIndex++;
            }
        }

        Arrays.sort(results); // the slots have to be sorted because else they are sorted by row-first (0,9,18,1,10,19,... instead of 0,1,9,10,18,19,...)
        return results;
    }

    /**
     * Computes all available slots based on a given row-count with {@link #getSlotsInRow(int, int, int)}.
     * Starts at the last row & index.
     */
    private Integer[] getSpecialItemSlotsAvailable(int rows, int inventoryHeight, int inventoryWidth) {
        if (this.specialItemCount == 0)
            return new Integer[0];

        var results = new Integer[rows * inventoryHeight];
        var slotIndex = 0;
        var lastRow = inventoryWidth - 1;

        for (int rowIndex=0; rowIndex<rows; rowIndex++) {
            var slotsInRow = this.getSlotsInRow(lastRow - rowIndex, inventoryHeight, inventoryWidth);
            for (Integer slot : slotsInRow) {
                results[slotIndex] = slot;
                slotIndex++;
            }
        }

        Arrays.sort(results, Comparator.reverseOrder()); // the slots have to be sorted because else they are sorted by row-first (8,17,26,... instead of 26,17,8,...)
        return results;
    }

    /**
     * Calculates the slots the items can use.
     * <b>Note:</b> the returned array length is >= {@link #itemCount}.
     */
    @Override
    public Integer[] getItemSlots() {
        var inventorySize = this.inventoryType.getDefaultSize();
        var inventoryHeight = Menu.getInventoryHeight(this.inventoryType);
        var inventoryWidth = inventorySize / inventoryHeight;
        var rowsTakenBySpecialItems = (int) Math.ceil((double) this.specialItemCount / inventoryHeight);
        var rowsAvailableForItems = inventoryWidth - rowsTakenBySpecialItems;

        if (this.specialItemCount > 0)
            rowsAvailableForItems -= 1; // 1 line for separators

        var slotsAvailableForItems = this.getItemSlotsAvailable(rowsAvailableForItems, inventoryHeight, inventoryWidth);

        // System.out.println("[R] inventorySize = " + inventorySize);
        // System.out.println("[R] itemCount = " + itemCount);
        // System.out.println("[R] inventoryHeight = " + inventoryHeight);
        // System.out.println("[R] inventoryWidth = " + inventoryWidth);
        // System.out.println("[R] rowsTakenBySpecialItems = " + rowsTakenBySpecialItems);
        // System.out.println("[R] rowsAvailableForItems = " + rowsAvailableForItems);
        // System.out.println("[R] slotsAvailableForItems = " + Arrays.toString(slotsAvailableForItems));

        if (this.placeItemsCentered) {
            // return only the centered, used slots
            int startIndex = (slotsAvailableForItems.length - this.itemCount) / 2;
            return Arrays.copyOfRange(slotsAvailableForItems, startIndex, startIndex + itemCount);
        }

        // return all slots available (might be more than used)
        return slotsAvailableForItems;
    }

    /**
     * Calculates the slots the special items can use.
     */
    @Override
    public Integer[] getSpecialItemSlots() {
        var inventorySize = this.inventoryType.getDefaultSize();
        var inventoryHeight = Menu.getInventoryHeight(this.inventoryType);
        var inventoryWidth = inventorySize / inventoryHeight;
        var rowsAvailableForSpecialItems = (int) Math.ceil((double) this.specialItemCount / inventoryHeight);
        var slotsAvailableForSpecialItems = this.getSpecialItemSlotsAvailable(rowsAvailableForSpecialItems, inventoryHeight, inventoryWidth);

        // System.out.println("[S] inventorySize = " + inventorySize);
        // System.out.println("[S] specialItemCount = " + specialItemCount);
        // System.out.println("[S] inventoryHeight = " + inventoryHeight);
        // System.out.println("[S] inventoryWidth = " + inventoryWidth);
        // System.out.println("[S] rowsAvailableForSpecialItems = " + rowsAvailableForSpecialItems);
        // System.out.println("[S] slotsAvailableForSpecialItems = " + Arrays.toString(slotsAvailableForSpecialItems));

        return slotsAvailableForSpecialItems;
    }

}
