package de.fantasypixel.rework.framework.database;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.FPConfig;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataRepoProvider<E> {

    private final Class<E> typeParameterClass;
    private final Map<Integer, E> cachedEntities;
    private final FPRework plugin;
    private final String tableName;
    private final FPConfig config;

    public DataRepoProvider(Class<E> typeParameterClass, FPRework plugin, FPConfig config) {
        this.typeParameterClass = typeParameterClass;
        this.cachedEntities = new HashMap<>();
        this.plugin = plugin;
        this.config = config;

        var entityAnnotation = typeParameterClass.getAnnotation(Entity.class);
        if (entityAnnotation != null)
            this.tableName = entityAnnotation.tableName();
        else {
            this.tableName = "ERROR";
            this.plugin.getFpLogger().warning("Data-provider couldn't be setup correctly with typeParameterClass " + typeParameterClass.getName() + " as the passed class doesn't have Entity annotated. The server will shutdown.");
            this.plugin.getServer().shutdown();
        }

        testDatabaseConnection();
    }

    private void testDatabaseConnection() {
        try (
                var conn = this.getConnection();
                var stmt = conn.prepareStatement("SELECT VERSION()");
                var rs = stmt.executeQuery();
        ) {
            if (!rs.next()) {
                this.plugin.getFpLogger().warning("The database connection could be established but couldn't return a version. The connection-values can be edited in plugins/FP-Next/config.json. Server will continue operating as normal.");
                return;
            }

            this.plugin.getFpLogger().info("The database connection was established. Database-Version: {0}", rs.getString(1));
        } catch (Exception e) {
            this.plugin.getFpLogger().warning("Couldn't connect to the database. The connection-values can be edited in plugins/FP-Next/config.json. Server will shutdown, if configured.");
            if (this.config.isDatabaseRequired())
                this.plugin.getServer().shutdown();
        }
    }

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
            this.plugin.getFpLogger().error("DataRepoProvider", "getId", e);
            return 0;
        }
    }

    @Nonnull
    private Connection getConnection() {
        try {
            return DriverManager.getConnection(
                String.format(
                        "jdbc:mysql://%s:%s/%s",
                        this.config.getDatabaseHost(),
                        this.config.getDatabasePort(),
                        this.config.getDatabaseName()
                ),
                    this.config.getDatabaseUser(),
                    this.config.getDatabasePassword()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsWithId(int id) {
        if (this.cachedEntities.containsKey(id))
            return true;

        var statementStr = MessageFormat.format("SELECT * FROM {0} WHERE `id` = ?", this.tableName);
        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr);
        ) {
            statement.setInt(1, id);
            var rs = statement.executeQuery();
            return rs.next();
        } catch (Exception e) {
            this.plugin.getFpLogger().error("DataRepoProvider", "existsWithId", e);
            return false;
        }
    }

    public E getById(int id) {
        if (this.cachedEntities.containsKey(id))
            return this.cachedEntities.get(id);

        var statementStr = MessageFormat.format("SELECT * FROM {0} WHERE `id` = ?", this.tableName);
        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr);
        ) {
            statement.setInt(1, id);
            var rs = statement.executeQuery();
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
            this.plugin.getFpLogger().error("DataRepoProvider", "getById", e);
            return null;
        }
    }

    /**
     * @return whether the operation was successful
     */
    public boolean delete(E entity) {
        var entityId = this.getId(entity);
        if (entityId == 0) {
            this.plugin.getFpLogger().warning("Couldn't delete entity of type " + entity.getClass().getName() + " as the id-field is not valid.");
            return false;
        }

        if (!this.existsWithId(entityId)) {
            this.plugin.getFpLogger().warning("Couldn't delete entity of type " + entity.getClass().getName() + " as no entity with that id exists.");
            return false;
        }

        this.cachedEntities.remove(entityId);

        var statementStr = MessageFormat.format("DELETE FROM {0} WHERE `id` = ?", this.tableName);
        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr)
        ) {
            statement.setInt(1, entityId);
            statement.execute();
            return true;
        } catch (Exception e) {
            this.plugin.getFpLogger().error("DataRepoProvider", "delete", e);
            return false;
        }
    }

    /**
     * Only used to override entities present in the database. To insert, use {@link DataRepoProvider#insert(E entity)}.
     * @return whether the operation was successful
     */
    public boolean save(E entity) {
        var entityId = this.getId(entity);
        if (entityId == 0) {
            this.plugin.getFpLogger().warning("Couldn't save entity of type " + entity.getClass().getName() + " as the id-field is not valid.");
            return false;
        }

        var fields = Arrays.stream(this.typeParameterClass.getDeclaredFields())
                .peek(e -> e.setAccessible(true))
                .filter(e -> !e.isAnnotationPresent(Ignore.class))
                .toList();

        var statementStr = MessageFormat.format(
                "UPDATE {0} SET {1} WHERE `id` = ?",
                this.tableName,
                fields.stream()
                        .map(Field::getName)
                        .map(name -> "`" + name + "` = ?")
                        .collect(Collectors.joining(", "))
        );

        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr)
        ) {
            int index = 1;
            for (var field : fields) {
                statement.setObject(index++, this.plugin.getPackageUtils().getFieldValueSafe(field, entity));
            }
            statement.setInt(index, entityId);
            statement.execute();
            return true;
        } catch (Exception e) {
            this.plugin.getFpLogger().error("DataRepoProvider", "save", e);
            return false;
        }
    }


    /**
     * Inserts the entity into the database, updates the id of passed entity after completion (if successful).
     * @return whether the operation was successful
     */
    public boolean insert(E entity) {
        var entityId = this.getId(entity);
        if (entityId != 0) {
            this.plugin.getFpLogger().warning("Couldn't insert entity of type " + entity.getClass().getName() + " as the entity has an id-value already.");
            return false;
        }

        var fields = Arrays.stream(this.typeParameterClass.getDeclaredFields())
                .peek(e -> e.setAccessible(true))
                .filter(e -> !e.isAnnotationPresent(Ignore.class))
                .collect(Collectors.toList());

        var statementStr = String.format(
                "INSERT INTO %s VALUES (%s)",
                this.tableName,
                fields.stream()
                        .map(e -> "?")
                        .collect(Collectors.joining(", "))
        );

        logSqlStatement(statementStr);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS)
        ) {
            int index = 1;
            for (var field : fields) {
                var val = this.plugin.getPackageUtils().getFieldValueSafe(field, entity);
                if (val != null) {
                    statement.setObject(index++, val);
                } else {
                    statement.setNull(index++, Types.NULL);
                }
            }

            statement.execute();

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                this.plugin.getFpLogger().warning("Couldn't insert entity of type " + entity.getClass().getName() + " as the statement didn't return keys.");
                return false;
            }

            var generatedId = generatedKeys.getInt(1);
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, generatedId);

            return true;
        } catch (Exception e) {
            this.plugin.getFpLogger().error("DataRepoProvider", "insert", e);
            return false;
        }
    }


    private void logSqlStatement(String statementStr) {
        this.plugin.getFpLogger().debug("Executing SQL: \"{0}\"", statementStr);
    }

}
