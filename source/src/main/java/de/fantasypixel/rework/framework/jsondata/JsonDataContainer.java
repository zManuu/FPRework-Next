package de.fantasypixel.rework.framework.jsondata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Represents a container for data loaded from config json files.
 * <br>
 * Supports creation, modification and deletion of entries.
 * <br><br>
 * <b>Note:</b> The T class must have a nullable id field.
 * <br>
 * If there is a name field, it will be used as the filename for creation.
 */
public abstract class JsonDataContainer <T> {

    /**
     * @return all entries currently loaded
     */
    @Nonnull public abstract Set<T> getEntries();

    /**
     * Creates an entry. The json file will be named after the entries name field or the generated id. The id field will also be set.
     * @param entry the entry to create
     * @return whether the entry could be created
     */
    public abstract boolean create(@Nullable T entry);

    /**
     * Modifies an entry.
     * @param entry the entry to modify
     * @return whether the entry could be modified
     */
    public abstract boolean modify(@Nullable T entry);

    /**
     * Deletes an entry.
     * @param entryId the entry's unique identifier
     * @return whether the entry was deleted
     */
    public abstract boolean delete(int entryId);

}
