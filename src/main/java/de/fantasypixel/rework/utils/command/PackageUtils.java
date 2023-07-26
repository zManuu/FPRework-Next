package de.fantasypixel.rework.utils.command;

import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class PackageUtils {

    public static Set<Class<?>> getClassesForPackage(ClassLoader classLoader, String packageName) {
        var reflections = new Reflections("de.fantasypixel.rework");
        return reflections.getTypesAnnotatedWith(CommandHandler.class);
    }

    public static void invoke(Method method, Object target, Object... args) {
        method.setAccessible(true);
        try {

            // some commands will ignore args -> only one arg passed

            if (method.getParameterCount() == 1)
                method.invoke(target, args[0]);
            else
                method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
