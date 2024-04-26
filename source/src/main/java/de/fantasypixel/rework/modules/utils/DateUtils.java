package de.fantasypixel.rework.modules.utils;

import de.fantasypixel.rework.framework.provider.ServiceProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// todo: create utils layer that is accessible like services for service-providers
@ServiceProvider(name = "date_utils")
public class DateUtils {

    // Formats:
    // date: yyyy-MM-dd
    // date-time: yyyy-MM-dd hh:mm:ss

    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

}
