package de.fantasypixel.rework.framework.database;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.modules.config.DatabaseConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DataRepoProvider<E> {
    
    private final static String CLASS_NAME = DataRepoProvider.class.getSimpleName();

    private final Class<E> typeParameterClass;
    private final Map<Integer, E> cachedEntities;
    private final FPRework plugin;
    private final String tableName;
    private final DatabaseConfig config;

    public DataRepoProvider(Class<E> typeParameterClass, FPRework plugin, DatabaseConfig config) {
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
    }

    public static void testDatabaseConnection(FPRework plugin, DatabaseConfig config) {
        try (
                var conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s", config.getHost(), config.getPort(), config.getName()), config.getUser(), config.getPassword());
                var stmt = conn.prepareStatement("SELECT VERSION()");
                var rs = stmt.executeQuery();
        ) {
            if (!rs.next()) {
                plugin.getFpLogger().warning("The database connection could be established but couldn't return a version. The connection-values can be edited in plugins/FP-Next/config.json. Server will continue operating as normal.");
                return;
            }

            plugin.getFpLogger().info("The database connection was established. Database-Version: {0}", rs.getString(1));
        } catch (Exception e) {
            plugin.getFpLogger().warning("Couldn't connect to the database. The connection-values can be edited in plugins/FP-Next/config.json. Server will shutdown.");
            plugin.getServer().shutdown();
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
            this.plugin.getFpLogger().error(CLASS_NAME, "getId", e);
            return 0;
        }
    }

    @Nonnull
    private Connection getConnection() {
        try {
            return DriverManager.getConnection(
                String.format(
                        "jdbc:mysql://%s:%s/%s",
                        this.config.getHost(),
                        this.config.getPort(),
                        this.config.getName()
                ),
                    this.config.getUser(),
                    this.config.getPassword()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(String columnName, Object value) {
        var statementStr = MessageFormat.format("SELECT id FROM `{0}` WHERE `{1}` = ?", this.tableName, columnName);
        logSqlStatement(statementStr, value);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr);
        ) {
            statement.setObject(1, value);
            var rs = statement.executeQuery();
            return rs.next();
        } catch (Exception ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "exists", ex);
            return false;
        }
    }

    public boolean existsWithId(int id) {
        if (this.cachedEntities.containsKey(id))
            return true;

        return this.exists("id", id);
    }

    public E getById(int id) {
        if (this.cachedEntities.containsKey(id))
            return this.cachedEntities.get(id);

        return this.get("id", id);
    }

    @Nullable
    public E get(String columnName, Object value) {
        var statementStr = MessageFormat.format("SELECT * FROM `{0}` WHERE `{1}` = ?", this.tableName, columnName);
        logSqlStatement(statementStr, value);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr);
        ) {
            statement.setObject(1, value);
            var rs = statement.executeQuery();
            if (!rs.next())
                return null;

            var columnCount = rs.getMetaData().getColumnCount();
            var entityInstance = this.plugin.getFpUtils().instantiate(this.typeParameterClass);

            // populate entity
            for (var i=1; i<columnCount+1; i++) {
                var fieldName = rs.getMetaData().getColumnName(i);
                var fieldValue = rs.getObject(i);
                var field = entityInstance.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(entityInstance, fieldValue);
            }

            this.cachedEntities.put(
                    this.getId(entityInstance),
                    entityInstance
            );
            return entityInstance;
        } catch (Exception ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "get", ex);
            return null;
        }
    }

    public Set<E> getMultiple(String columnName, Object value) {
        var statementStr = MessageFormat.format("SELECT * FROM `{0}` WHERE `{1}` = ?", this.tableName, columnName);
        logSqlStatement(statementStr, value);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr);
        ) {
            statement.setObject(1, value);
            var rs = statement.executeQuery();
            Set<E> result = new HashSet<>();

            while (rs.next()) {
                var columnCount = rs.getMetaData().getColumnCount();
                var entityInstance = this.plugin.getFpUtils().instantiate(this.typeParameterClass);

                // populate entity
                for (var i=1; i<columnCount+1; i++) {
                    var fieldName = rs.getMetaData().getColumnName(i);
                    var fieldValue = rs.getObject(i);
                    var field = entityInstance.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(entityInstance, fieldValue);
                }

                this.cachedEntities.put(
                        this.getId(entityInstance),
                        entityInstance
                );

                result.add(entityInstance);
            }
            return result;
        } catch (Exception ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "getMultiple", ex);
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

        var statementStr = MessageFormat.format("DELETE FROM `{0}` WHERE `id` = ?", this.tableName);
        logSqlStatement(statementStr, entityId);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr)
        ) {
            statement.setInt(1, entityId);
            statement.execute();
            return true;
        } catch (Exception e) {
            this.plugin.getFpLogger().error(CLASS_NAME, "delete", e);
            return false;
        }
    }

    /**
     * Only used to override entities present in the database. To insert, use {@link DataRepoProvider#insert(E entity)}.
     * @return whether the operation was successful
     */
    public boolean update(E entity) {
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
                "UPDATE `{0}` SET {1} WHERE `id` = ?",
                this.tableName,
                fields.stream()
                        .map(Field::getName)
                        .map(name -> "`" + name + "` = ?")
                        .collect(Collectors.joining(", "))
        );

        logSqlStatement(
                statementStr,
                fields.stream().map(field -> this.plugin.getFpUtils().getFieldValueSafe(field, entity)).toArray()
        );

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr)
        ) {
            int index = 1;
            for (var field : fields) {
                statement.setObject(index++, this.plugin.getFpUtils().getFieldValueSafe(field, entity));
            }
            statement.setInt(index, entityId);
            statement.execute();
            return true;
        } catch (Exception e) {
            this.plugin.getFpLogger().error(CLASS_NAME, "save", e);
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
                .toList();

        var statementStr = String.format(
                "INSERT INTO `%s` VALUES (%s)",
                this.tableName,
                fields.stream()
                        .map(e -> "?")
                        .collect(Collectors.joining(", "))
        );

        logSqlStatement(
                statementStr,
                fields.stream().map(field -> this.plugin.getFpUtils().getFieldValueSafe(field, entity)).toArray()
        );

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS)
        ) {
            int index = 1;
            for (var field : fields) {
                var val = this.plugin.getFpUtils().getFieldValueSafe(field, entity);
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
            this.plugin.getFpLogger().error(CLASS_NAME, "insert", e);
            return false;
        }
    }


    private void logSqlStatement(String statementStr, Object... args) {
        StringBuilder formattedStatement = new StringBuilder("Executing SQL: \"");
        formattedStatement.append(statementStr).append("\" with arguments: [");

        for (int i = 0; i < args.length; i++) {
            formattedStatement.append(args[i]);
            if (i < args.length - 1) {
                formattedStatement.append(", ");
            }
        }

        formattedStatement.append("]");

        this.plugin.getFpLogger().debug(formattedStatement.toString());
    }



}
