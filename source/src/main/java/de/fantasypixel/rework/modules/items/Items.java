package de.fantasypixel.rework.modules.items;

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

    public static Set<Item> ITEMS = Set.of(
            new TestSword(),
            new TestAxe(),
            new Apple(),
            new SpeedPotion(),
            new HealthPotion()
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
