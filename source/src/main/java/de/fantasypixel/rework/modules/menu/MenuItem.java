package de.fantasypixel.rework.modules.menu;

import com.sun.source.doctree.SeeTree;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Set;

@Builder
@Getter
public class MenuItem {

    private Material material;
    private String displayName;
    private List<String> lore;
    private Integer slot;
    private Integer amount;
    private boolean closesMenu;
    private Runnable onSelect;

    public ItemStack toItemStack() {
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
