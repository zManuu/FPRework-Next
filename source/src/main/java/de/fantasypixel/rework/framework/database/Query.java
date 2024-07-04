package de.fantasypixel.rework.framework.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A query object holds multiple where entries for querying the database.
 * <br>
 * Currently, the query-builder is only used for select statements.
 */
public class Query {

    private final Map<String, Object> where;

    /**
     * Constructs a new query with an initial where mapping.
     * @param columnName the first column name
     * @param value the first value
     */
    public Query(@Nonnull String columnName, @Nonnull Object value) {
        this.where = new LinkedHashMap<>();
        this.where.put(columnName, value);
    }

    /**
     * Constructs a new query without an initial where mapping.
     */
    public Query() {
        this.where = new LinkedHashMap<>();
    }

    /**
     * Adds a where mapping to the query.
     * @param columnName the column's name
     * @param value the value to query for
     * @return the updated query
     * @throws QueryException if a where mapping with the column name exists
     */
    @Nonnull
    public Query where(@Nonnull String columnName, @Nonnull Object value) throws QueryException {
        if (this.where.containsKey(columnName))
            throw new QueryException("Duplicate column {0}!", columnName);
        else
            this.where.put(columnName, value);

        return this;
    }

    /**
     * Constructs a string representation of the where mapping.
     * For instance: {@code accountId = ? AND active = ?} or {@code "accountId" = ? AND "active" = ?} for {@link DatabaseType#POSTGRESQL}.
     * <br><br>
     * Note that the values aren't inputted here but in the {@link DataRepoProvider}.
     */
    @Nonnull
    private String getWhereString(@Nonnull DatabaseType databaseType) {
        var whereStrings = new ArrayList<String>();
        this.where
                .keySet()
                .forEach((key) -> whereStrings.add(String.format(
                                databaseType == DatabaseType.POSTGRESQL
                                    ? "\"%s\" = ?"
                                    : "%s = ?",
                                key)));
        return String.join(" AND ", whereStrings);
    }

    /**
     * Constructs a select-query with the where mappings.
     * For instance: {@code SELECT * FROM {0} WHERE accountId = ? AND active = ?} or {@code SELECT * FROM {0} WHERE "accountId" = ? AND "active" = ?} for {@link DatabaseType#POSTGRESQL}.
     * <br><br>
     * Note that the table-name and values aren't inputted here but in the {@link DataRepoProvider}.
     * @param select to colum to select (must be {@code *} or {@code id})
     * @throws QueryException if the select parameter wasn't {@code *} or {@code id}
     */
    @Nonnull
    public String toSelectQuery(@Nonnull String select, @Nonnull DatabaseType databaseType) throws QueryException {
        if (!select.equals("*") && !select.equals("id"))
            throw new QueryException("Invalid select: {0}! It should be * or id!", select);

        String whereString = this.getWhereString(databaseType);
        return String.format(
                "SELECT %s FROM {0} WHERE %s",
                select,
                whereString
        );
    }

    /**
     * @return the where-values in the same order they were specified
     */
    @Nonnull
    public Object[] getWhereValues() {
        return this.where.values().toArray();
    }

    @Nonnull
    public Map<String, Object> getWhereMap() {
        return this.where;
    }

    /**
     * Checks if another object is equal to this one.
     * If it is a Query, the comparison is based on the {@link #getWhereString(DatabaseType)} with {@link DatabaseType#MYSQL}.
     */
    @Override
    public boolean equals(@Nullable Object other) {
        if (!(other instanceof Query otherQuery))
            return false;

        return this.getWhereString(DatabaseType.MYSQL).equals(otherQuery.getWhereString(DatabaseType.MYSQL));
    }

}
