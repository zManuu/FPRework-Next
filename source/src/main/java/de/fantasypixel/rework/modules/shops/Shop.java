package de.fantasypixel.rework.modules.shops;

import de.fantasypixel.rework.framework.jsondata.JsonDataProvider;
import de.fantasypixel.rework.modules.items.Item;
import de.fantasypixel.rework.modules.utils.json.JsonPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Villager;

import javax.annotation.Nullable;
import java.util.Set;

@Getter
@AllArgsConstructor
@JsonDataProvider(path = "shops")
public class Shop {

    @Nullable private Integer id;
    @Nullable private String name;
    private JsonPosition position;
    private Villager.Profession villagerProfession;
    private Set<ShopItem> sellItems;
    private Set<ShopItem> buyItems;

    @Getter
    @AllArgsConstructor
    public static class ShopItem {

        /**
         * Whether this price was customized or follows the default
         */
        @Nullable private Boolean isCustomPrice;

        /**
         * Whether this shop-item is currently on discount. Works for both selling and buying, for selling a bonus is added.
         */
        @Nullable private Boolean isDiscount;

        /**
         * The price for the item (one unit).
         * If not specified, the {@link Item#getDefaultPrice()} is used.
         */
        @Nullable private Integer price;

        /**
         * The item identifier (not case-sensitive)
         */
        private String itemIdentifier;

    }

}
