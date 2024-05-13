package de.fantasypixel.rework;

import de.fantasypixel.rework.framework.FPLogger;

public class Constants {

    public static final FPLogger logger = new FPLogger(System.out);

    private static boolean initialized;
    public static void beforeOnce() {
        if (!initialized) {
            initialized = true;
            logger.warning("There might be intended warnings below. Be sure to check them.");
        }
    }

}
