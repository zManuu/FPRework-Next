package de.fantasypixel.rework.utils.database;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.utils.PackageUtils;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataRepoProvider<E> {

    private final Class<E> typeParameterClass;
    private final Map<Integer, E> cachedEntities;
    private final FPRework plugin;
    private final String tableName;

    public DataRepoProvider(Class<E> typeParameterClass, FPRework plugin) {
        this.typeParameterClass = typeParameterClass;
        this.cachedEntities = new HashMap<>();
        this.plugin = plugin;

        var entityAnnotation = typeParameterClass.getAnnotation(Entity.class);
        if (entityAnnotation != null)
            this.tableName = entityAnnotation.tableName();
        else {
            this.tableName = "ERROR";
            this.plugin.getLogger().warning("Data-provider couldn't be setup correctly with typeParameterClass " + typeParameterClass.getName() + " as the passed class doesn't have Entity annotated. The server will shutdown.");
            this.plugin.getServer().shutdown();
        }

        //testDatabaseConnection();
    }

    //private void testDatabaseConnection() {
    //    try (
    //            var conn = this.getConnection();
    //            var stmt = conn.prepareStatement("SELECT VERSION()");
    //            var rs = stmt.executeQuery();
    //    ) {
    //        if (!rs.next()) {
    //            this.plugin.getLogger().warning("The database connection could be established but couldn't return a version. The connection-values can be edited in plugins/FP-Next/config.json. Server will continue operating as normal.");
    //            return;
    //        }
    //
    //        this.plugin.getLogger().info("The database connection was established. Database-Version: " + rs.getString(1));
    //    } catch (Exception e) {
    //        this.plugin.getLogger().severe("Couldn't connect to the database. The connection-values can be edited in plugins/FP-Next/config.json. Server will shutdown.");
    //        this.plugin.getServer().shutdown();
    //    }
    //}

    /**
     * @return the entities id or 0 if not set / an error occurred
     */
    private int getId(E entity) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            var idValue = idField.get(entity);
            return (idValue instanceof Integer) ? (Integer) idValue : 0;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.plugin.getLogger().throwing("DataRepoProvider", "getId", e);
            return 0;
        }
    }

    /**
     * Checks the passed object for sql-injections. It is recommended to check before every sql-command is executed.
     */
    private boolean checkIntegrity(Object object) {

        if (String.class.isAssignableFrom(object.getClass()))
            return !String.valueOf(object).contains(";");

        var fields = Arrays.stream(object.getClass()
                .getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> !field.isAnnotationPresent(Ignore.class))
                .toArray(Field[]::new);

        for (Field field : fields) {
            var fieldType = field.getType();
            var fieldValue = this.plugin.getPackageUtils().getFieldValueSafe(field, object);

            if (fieldValue == null)
                continue;

            if (Object.class.isAssignableFrom(fieldType))
                return checkIntegrity(fieldValue);
        }

        return true;
    }

    @Nonnull
    private Connection getConnection() {
        try {
            var config = this.plugin.getProviderManager().getConfig();
            return DriverManager.getConnection(
                String.format(
                        "jdbc:mysql://%s:%s/%s",
                        config.getDatabaseHost(),
                        config.getDatabasePort(),
                        config.getDatabaseName()
                ),
                    config.getDatabaseUser(),
                    config.getDatabasePassword()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsWithId(int id) {
        if (this.cachedEntities.containsKey(id))
            return true;

        var statementStr = String.format("SELECT * FROM %s WHERE `id` = '%d'", this.tableName, id);
        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr);
                var rs = statement.executeQuery()
        ) {
            return rs.next();
        } catch (Exception e) {
            this.plugin.getLogger().throwing("DataRepoProvider", "existsWithId", e);
            return false;
        }
    }

    public E getById(int id) {
        if (this.cachedEntities.containsKey(id))
            return this.cachedEntities.get(id);

        var statementStr = String.format("SELECT * FROM %s WHERE `id` = '%d'", this.tableName, id);
        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr);
                var rs = statement.executeQuery()
        ) {
            if (!rs.next())
                return null;

            var columnCount = rs.getMetaData().getColumnCount();
            var entityInstance = this.plugin.getPackageUtils().instantiate(this.typeParameterClass);

            for (var i=0; i<columnCount; i++) {
                var columnName = rs.getMetaData().getColumnName(i);
                var columnField = this.typeParameterClass.getDeclaredField(columnName);
                columnField.setAccessible(true);
                var columnValue = rs.getObject(i);
                columnField.set(entityInstance, columnValue);
            }

            this.cachedEntities.put(id, entityInstance);
            return entityInstance;
        } catch (Exception e) {
            this.plugin.getLogger().throwing("DataRepoProvider", "getById", e);
            return null;
        }
    }

    /**
     * @return whether the operation was successful
     */
    public boolean delete(E entity) {
        if (!checkIntegrity(entity)) {
            this.plugin.getLogger().warning("Couldn't delete entity of type " + entity.getClass().getName() + " as the object didn't pass the integrity test.");
            return false;
        }

        var entityId = this.getId(entity);
        if (entityId == 0) {
            this.plugin.getLogger().warning("Couldn't delete entity of type " + entity.getClass().getName() + " as the id-field is not valid.");
            return false;
        }

        if (!this.existsWithId(entityId)) {
            this.plugin.getLogger().warning("Couldn't delete entity of type " + entity.getClass().getName() + " as no entity with that id exists.");
            return false;
        }

        this.cachedEntities.remove(entityId);

        var statementStr = String.format("DELETE FROM %s WHERE `id` = '%d'", this.tableName, entityId);
        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr)
        ) {
            statement.execute();
            return true;
        } catch (Exception e) {
            this.plugin.getLogger().throwing("DataRepoProvider", "delete", e);
            return false;
        }
    }

    /**
     * Only used to override entities present in the database. To insert, use {@link DataRepoProvider#insert(E entity)}.
     * @return whether the operation was successful
     */
    public boolean save(E entity) {
        if (!checkIntegrity(entity)) {
            this.plugin.getLogger().warning("Couldn't save entity of type " + entity.getClass().getName() + " as the object didn't pass the integrity test.");
            return false;
        }

        var entityId = this.getId(entity);
        if (entityId == 0) {
            this.plugin.getLogger().warning("Couldn't save entity of type " + entity.getClass().getName() + " as the id-field is not valid.");
            return false;
        }

        var statementStr = String.format(
                "UPDATE %s SET %s WHERE `id` = '%d'",
                this.tableName,
                Arrays.stream(this.typeParameterClass.getDeclaredFields())
                        .peek(e -> e.setAccessible(true))
                        .filter(e -> !e.isAnnotationPresent(Ignore.class))
                        .map(e -> String.format("`%s` = '%s'", e.getName(), this.plugin.getPackageUtils().getFieldValueSafe(e, entity)))
                        .collect(Collectors.joining(", ")),
                entityId
        );

        logSqlStatement(statementStr);

        try (
            var conn = this.getConnection();
            var statement = conn.prepareStatement(statementStr)
        ) {
            statement.execute();
            return true;
        } catch (Exception e) {
            this.plugin.getLogger().throwing("DataRepoProvider", "save", e);
            return false;
        }
    }

    /**
     * Inserts the entity into the database, updates the id of passed entity after completion (if successful).
     * @return whether the operation was successful
     */
    public boolean insert(E entity) {
        if (!checkIntegrity(entity)) {
            this.plugin.getLogger().warning("Couldn't insert entity of type " + entity.getClass().getName() + " as the object didn't pass the integrity test.");
            return false;
        }

        var entityId = this.getId(entity);
        if (entityId != 0) {
            this.plugin.getLogger().warning("Couldn't insert entity of type " + entity.getClass().getName() + " as the entity has an id-value already.");
            return false;
        }

        var statementStr = String.format(
                "INSERT INTO %s VALUES (%s)",
                this.tableName,
                Arrays.stream(this.typeParameterClass.getDeclaredFields())
                        .peek(e -> e.setAccessible(true))
                        .filter(e -> !e.isAnnotationPresent(Ignore.class))
                        .map(e -> {
                            var val = this.plugin.getPackageUtils().getFieldValueSafe(e, entity);
                            return val != null ? String.format("'%s'", val) : "null";
                        })
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "))
        );

        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.execute();

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                this.plugin.getLogger().warning("Couldn't insert entity of type " + entity.getClass().getName() + " as the statement didn't return keys.");
                return false;
            }

            var generatedId = generatedKeys.getInt(1);
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, generatedId);

            return true;
        } catch (Exception e) {
            this.plugin.getLogger().throwing("DataRepoProvider", "insert", e);
            return false;
        }
    }

    private void logSqlStatement(String statementStr) {
        this.plugin.getLogger().fine(String.format("Executing SQL: \"%s\"", statementStr));
    }

}
