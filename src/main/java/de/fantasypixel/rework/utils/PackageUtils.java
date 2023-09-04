package de.fantasypixel.rework.utils;

import org.bukkit.Bukkit;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class PackageUtils {

    public static void loadMysqlDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().throwing(PackageUtils.class.getSimpleName(), "loadMysqlDriver", e);
        }
    }

    public static Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotationClass) {
        var reflections = new Reflections("de.fantasypixel.rework");
        return reflections.getTypesAnnotatedWith(annotationClass);
    }

    public static Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotationClass, Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(e -> !e.isSynthetic())
                .peek(field -> field.setAccessible(true))
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }

    public static Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass, Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }

    public static <T> T instantiate(Class<T> clazz, Object... args) {
        try {

            var argTypes = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);

            var constructor = clazz.getConstructor(argTypes);

            return constructor.newInstance(args);

        } catch (Exception e) {
            Bukkit.getLogger().throwing(PackageUtils.class.getSimpleName(), "instantiate", e);
            return null;
        }
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
            Bukkit.getLogger().throwing(PackageUtils.class.getSimpleName(), "invoke", e);
        }
    }

    public static Object getFieldValueSafe(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            Bukkit.getLogger().throwing(PackageUtils.class.getSimpleName(), "getFieldValueSafe", e);
            return null;
        }
    }

}
