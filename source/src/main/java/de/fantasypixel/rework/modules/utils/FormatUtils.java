package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.framework.provider.ServiceProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Provides utility methods for logging / language purposes.
 */
@ServiceProvider
public class FormatUtils {

    /**
     * Formats a pattern with the given parameters. Those are resolved in the format %NAME%.
     * @param pattern the pattern / message to be used
     * @param params the parameters (mapping from name to value)
     * @return the formatted message
     */
    @Nonnull
    public String format(@Nonnull String pattern, @Nullable Map<String, Object> params) {
        if (params == null || params.isEmpty())
            return pattern;

        var formattedString = pattern;
        for (var entry : params.entrySet()) {
            var paramKey = entry.getKey();
            var paramValue = entry.getValue();

            if (paramValue != null) {
                var placeholder = "%" + paramKey + "%";
                formattedString = formattedString.replaceAll(placeholder, paramValue.toString());
            }
        }

        return formattedString;
    }

    /**
     * Formats a pattern with the given parameters. Those are resolved in the format {INDEX}.
     * @param pattern the pattern / message to be used
     * @param params the parameters
     * @return the formatted message
     */
    @Nonnull
    public String format(@Nonnull String pattern, @Nullable Object... params) {
        if (params == null || params.length == 0)
            return pattern;

        var formattedString = pattern;
        for (var i = 0; i < params.length; i++) {
            var placeholder = "\\{" + i + "}";
            formattedString = formattedString.replaceAll(placeholder, params[i].toString());
        }

        return formattedString;
    }

}
