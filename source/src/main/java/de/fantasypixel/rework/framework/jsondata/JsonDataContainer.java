package de.fantasypixel.rework.framework.jsondata;

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

    public abstract Set<T> getEntries();
    public abstract boolean create(T entry);
    public abstract boolean modify(T entry);
    public abstract boolean delete(int entryId);

}
