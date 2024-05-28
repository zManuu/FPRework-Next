package de.fantasypixel.rework.modules.menu;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    private Material material;
    private String displayName;
    private List<String> lore;
    private Integer slot;
    private Integer amount;
    private boolean closesMenu;
    private Runnable onSelect;
    @Nullable private ItemStack importedItemStack;
    private boolean disposable;

    /**
     * A menu that is opened by clicking this item. For example, back-buttons, page, ... or real sub-menus
     */
    @Nullable private Supplier<Menu> subMenu;

    /**
     * The import constructor.
     * When this is used, {@link #toItemStack()} will return the passed item-stack.
     * Note that when using this constructor, you can't override values on the item-stack,
     * only slot, closesMenu and onSelect are overridable.
     */
    public MenuItem(@Nullable ItemStack itemStack) {
        this.importedItemStack = itemStack;
    }

    /**
     * Builds the item-stack. If this instance was created with the import constructor (with ItemStack param), the imported item-stack will be returned.
     */
    public ItemStack toItemStack() {
        if (this.importedItemStack != null)
            return this.importedItemStack;

        if (this.amount == null)
            this.amount = 1;

        ItemStack itemStack = new ItemStack(this.material, this.amount);
        ItemMeta itemMetaData = itemStack.getItemMeta();

        itemMetaData.setDisplayName(this.displayName);
        itemMetaData.setLore(this.lore);
        itemStack.setItemMeta(itemMetaData);

        return itemStack;
    }

}
