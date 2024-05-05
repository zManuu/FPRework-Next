package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.framework.provider.ServiceProvider;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ServiceProvider
public class DateUtils {

    // Formats:
    // date: yyyy-MM-dd
    // date-time: yyyy-MM-dd hh:mm:ss

    @Nonnull
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

}
