package de.fantasypixel.rework.utils;

import org.bukkit.Bukkit;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Ref;
import java.util.*;

public class PackageUtils {

    public static Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotationClass) {
        var reflections = new Reflections("de.fantasypixel.rework");
        return reflections.getTypesAnnotatedWith(annotationClass);
    }

    public static Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        var reflections = new Reflections("de.fantasypixel.rework");
        return reflections.getFieldsAnnotatedWith(annotationClass);
    }

    public static Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotationClass, Class<?> clazz) {
        var fields = clazz.getFields();
        var fieldsWithAnnotations = new HashSet<Field>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotationClass))
                fieldsWithAnnotations.add(field);
        }
        return fieldsWithAnnotations;
    }

    public static Object instantiate(Class<?> clazz, Object... args) {
        try {

            var argTypes = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);

            var constructor = clazz.getConstructor(argTypes);

            return constructor.newInstance(args);

        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

}
