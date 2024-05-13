package de.fantasypixel.rework.framework.database;

import java.text.MessageFormat;

/**
 * Thrown when an error occurs when building / executing a {@link Query}.
 */
public class QueryException extends RuntimeException {

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String pattern, Object... args) {
        super(MessageFormat.format(pattern, args));
    }

}
