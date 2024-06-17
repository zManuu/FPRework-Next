package de.fantasypixel.rework.modules.items;

import de.fantasypixel.rework.modules.items.items.currency.Currency100;
import de.fantasypixel.rework.modules.items.items.currency.Currency1000;
import de.fantasypixel.rework.modules.items.items.currency.Currency1;
import de.fantasypixel.rework.modules.items.items.edible.*;
import de.fantasypixel.rework.modules.items.items.potions.HealthPotion;
import de.fantasypixel.rework.modules.items.items.potions.NightVisionPotion;
import de.fantasypixel.rework.modules.items.items.potions.SpeedPotion;
import de.fantasypixel.rework.modules.items.items.weapons.axes.IronAxe;
import de.fantasypixel.rework.modules.items.items.weapons.axes.StoneAxe;
import de.fantasypixel.rework.modules.items.items.weapons.swords.IronSword;
import de.fantasypixel.rework.modules.items.items.weapons.swords.StoneSword;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class Items {

    // @Extending
    public static Set<Item> ITEMS = Set.of(
            new StoneAxe(),
            new IronAxe(),
            new StoneSword(),
            new IronSword(),
            new Apple(),
            new BakedPotato(),
            new Bread(),
            new Carrot(),
            new Steak(),
            new SpeedPotion(),
            new HealthPotion(),
            new NightVisionPotion(),
            new Currency1(),
            new Currency100(),
            new Currency1000()
    );

    @Nonnull
    public static Optional<Item> getByIdentifier(@Nullable String itemIdentifier) {
        if (itemIdentifier == null)
            return Optional.empty();

        return ITEMS.stream()
                .filter(e -> e.getIdentifier().equalsIgnoreCase(itemIdentifier))
                .findFirst();
    }

}
