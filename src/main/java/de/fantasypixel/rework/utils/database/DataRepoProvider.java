package de.fantasypixel.rework.utils.database;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.utils.PackageUtils;

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
    private final String tableName;
    private final FPRework plugin;

    public DataRepoProvider(Class<E> typeParameterClass, FPRework plugin) {
        this.typeParameterClass = typeParameterClass;
        this.cachedEntities = new HashMap<>();
        this.plugin = plugin;

        var entityAnnotation = typeParameterClass.getAnnotation(Entity.class);
        if (entityAnnotation != null)
            this.tableName = entityAnnotation.tableName();
        else {
            this.tableName = "ERROR";
            this.plugin.getLogger().warning("Data-provider couldn't be setup correctly with typeParameterClass " + typeParameterClass.getName() + " as the passed class doesn't have Entity annotated. The data-repo will still be active but error upon requests.");
        }
    }

    /**
     * @return the entities id or 0 if not set / an error occurred
     */
    private int getId(E entity) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.getInt(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
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
            var fieldValue = PackageUtils.getFieldValueSafe(field, object);

            if (fieldValue == null) {
                this.plugin.getLogger().warning("SQL-integrity check for object of type " + object.getClass().getName() + " failed as a field-value was null.");
                return false;
            }

            if (Object.class.isAssignableFrom(fieldType))
                return checkIntegrity(fieldValue);
        }

        return true;
    }

    private Connection getConnection() {
        try {
            var config = this.plugin.getProviderManager().getConfig();
            return DriverManager.getConnection(
                String.format(
                        "jdbc:mysql://%s:%s/%s",
                        config.database_host,
                        config.database_port,
                        config.database_name
                ),
                    config.database_user,
                    config.database_password
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existsWithId(int id) {
        if (this.cachedEntities.containsKey(id))
            return true;

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(String.format("SELECT * FROM %s WHERE `id` = '%d'", this.tableName, id));
                var rs = statement.executeQuery()
        ) {
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public E getById(int id) {
        if (this.cachedEntities.containsKey(id))
            return this.cachedEntities.get(id);

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(String.format("SELECT * FROM %s WHERE `id` = '%d'", this.tableName, id));
                var rs = statement.executeQuery()
        ) {
            if (!rs.next())
                return null;

            var columnCount = rs.getMetaData().getColumnCount();
            var entityInstance = (E) PackageUtils.instantiate(this.typeParameterClass);

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
            e.printStackTrace();
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

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(String.format("DELETE FROM %s WHERE `id` = '%d'", this.tableName, entityId))
        ) {
            statement.execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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

        try (
            var conn = this.getConnection();
            var statement = conn.prepareStatement(
                    String.format(
                            "UPDATE %s SET %s WHERE `id` = '%d'",
                            this.tableName,
                            Arrays.stream(this.typeParameterClass.getDeclaredFields())
                                    .peek(e -> e.setAccessible(true))
                                    .filter(e -> !e.isAnnotationPresent(Ignore.class))
                                    .map(e -> String.format("`%s` = '%s'", e.getName(), PackageUtils.getFieldValueSafe(e, entity)))
                                    .collect(Collectors.joining(", ")),
                            entityId
                    )
            );
        ) {
            statement.execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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

        try (
                var conn = this.getConnection();
                var statement = conn.prepareStatement(
                    String.format(
                            "INSERT INTO %s VALUES (%s)",
                            this.tableName,
                            Arrays.stream(this.typeParameterClass.getDeclaredFields())
                                    .peek(e -> e.setAccessible(true))
                                    .filter(e -> !e.isAnnotationPresent(Ignore.class))
                                    .map(e -> String.format("'%s'", PackageUtils.getFieldValueSafe(e, entity)))
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "))
                    ),
                    Statement.RETURN_GENERATED_KEYS
            )
        ) {
            statement.executeUpdate();

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                this.plugin.getLogger().warning("Couldn't insert entity of type " + entity.getClass().getName() + " as the statement didn't return keys.");
                return false;
            }

            var generatedId = generatedKeys.getInt(1);
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.setInt(entity, generatedId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
