package de.fantasypixel.rework.utils.modules;

import java.util.HashMap;
import java.util.Map;

public abstract class EntityRepo<E extends Entity> {

    /**
     * Mapped by id.
     * Is empty from the beginning, get filled when using {@link de.fantasypixel.rework.utils.modules.EntityRepo#getById(int)}
     */
    private final Map<Integer, E> cachedEntities;

    public EntityRepo() {
        this.cachedEntities = new HashMap<>();
    }

    /**
     * Gets an entity. If the entity has been accessed already, the cache is used. If the entity isn't yet cached, will be cached.
     * @param id the identifier of the entity
     * @return the found Entity-Object or null
     */
    public E getById(int id) {
        return null;
    }

    /**
     * Updates the entity in the database.
     */
    public void save(E entity) {
        System.out.println("Saving...");
    }

    /**
     * Updates the cached entity by pulling the data from the database.
     * @param id the identifier of the entity
     */
    public void update(int id) {
        System.out.println("Updating...");
    }

    /**
     * Deletes the entity from the database and the cache.
     */
    public void delete(E entity) {
        System.out.println("Deleting...");
    }

}
