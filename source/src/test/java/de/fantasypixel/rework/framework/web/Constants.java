package de.fantasypixel.rework.framework.web;

import com.google.gson.Gson;
import de.fantasypixel.rework.framework.FPLogger;

public class Constants {

    public static final Gson gson = new Gson();
    public static final FPLogger logger = new FPLogger(gson, System.out);

}
