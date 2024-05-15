package de.fantasypixel.rework;

import com.google.gson.Gson;
import de.fantasypixel.rework.framework.log.FPLogger;

public class Constants {

    public static final Gson gson = new Gson();
    public static final FPLogger logger = new FPLogger(System.out, gson);

    private static boolean initialized;
    public static void beforeOnce() {
        if (!initialized) {
            initialized = true;
            logger.warning("There might be intended warnings below. Be sure to check them.");
        }
    }

}
