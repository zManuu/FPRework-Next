package de.fantasypixel.rework.modules.items;

import de.fantasypixel.rework.framework.provider.Extending;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class Items {

    @Extending
    public static Set<Item> ITEMS;

    @Nonnull
    public static Optional<Item> getByIdentifier(@Nullable String itemIdentifier) {
        if (itemIdentifier == null)
            return Optional.empty();

        return ITEMS.stream()
                .filter(e -> e.getIdentifier().equalsIgnoreCase(itemIdentifier))
                .findFirst();
    }

}
