package de.fantasypixel.rework.framework.web;

import com.google.gson.Gson;
import de.fantasypixel.rework.framework.FPLogger;

public class Constants {

    public static final Gson gson = new Gson();
    public static final FPLogger logger = new FPLogger(gson, System.out);

    private static boolean initialized;
    public static void beforeOnce() {
        if (!initialized) {
            initialized = true;
            logger.warning("There might be intended warnings below. Be sure to check them.");
        }
    }

}
