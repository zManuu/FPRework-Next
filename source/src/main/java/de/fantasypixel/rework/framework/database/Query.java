package de.fantasypixel.rework.framework.database;

import javax.annotation.Nonnull;
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
     * For instance: {@code accountId = ? AND active = ?}
     * <br><br>
     * Note that the values aren't inputted here but in the {@link DataRepoProvider}.
     */
    @Nonnull
    private String getWhereString() {
        var whereStrings = new ArrayList<String>();
        this.where.keySet().forEach((key) -> whereStrings.add(String.format("%s = ?", key)));
        return String.join(" AND ", whereStrings);
    }

    /**
     * Constructs a select-query with the where mappings.
     * For instance: {@code SELECT * FROM `{0}` WHERE accountId = ? AND active = ?}
     * <br><br>
     * Note that the table-name and values aren't inputted here but in the {@link DataRepoProvider}.
     */
    @Nonnull
    public String toSelectQuery() {
        var whereString = this.getWhereString();
        return String.format(
                "SELECT * FROM `{0}` WHERE %s",
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

}
