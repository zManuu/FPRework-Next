package de.fantasypixel.rework.modules.items;

import de.fantasypixel.rework.modules.items.items.currency.Currency100;
import de.fantasypixel.rework.modules.items.items.currency.Currency1000;
import de.fantasypixel.rework.modules.items.items.currency.Currency1;
import de.fantasypixel.rework.modules.items.items.edible.Apple;
import de.fantasypixel.rework.modules.items.items.potions.HealthPotion;
import de.fantasypixel.rework.modules.items.items.potions.SpeedPotion;
import de.fantasypixel.rework.modules.items.items.weapons.axes.TestAxe;
import de.fantasypixel.rework.modules.items.items.weapons.swords.TestSword;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class Items {

    // todo: automatically load (implementation in framework)
    public static Set<Item> ITEMS = Set.of(
            new TestSword(),
            new TestAxe(),
            new Apple(),
            new SpeedPotion(),
            new HealthPotion(),
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
