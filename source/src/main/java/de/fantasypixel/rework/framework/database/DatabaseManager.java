package de.fantasypixel.rework.framework.database;

/**
 * The database-manager exposed to modules with the {@link de.fantasypixel.rework.framework.provider.Auto} annotation.
 */
public interface DatabaseManager {

    /**
     * Clears the whole database. This method should be called with extreme care as there isn't any security / backups internally.
     */
    void clearDatabase();

}
