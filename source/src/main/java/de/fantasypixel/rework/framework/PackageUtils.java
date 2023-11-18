package de.fantasypixel.rework.framework;

import de.fantasypixel.rework.FPRework;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds utility methods for the framework. Mainly focused on reflections but can also be used for misc purposes.
 */
public class PackageUtils {

    private final String CLASS_NAME = PackageUtils.class.getSimpleName();
    private final FPRework plugin;

    public PackageUtils(FPRework plugin) {
        this.plugin = plugin;
    }

    public void loadMysqlDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            this.plugin.getFpLogger().error(CLASS_NAME, "loadMysqlDriver", e);
        }
    }

    public Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotationClass) {
        var reflections = new Reflections("de.fantasypixel.rework");
        return reflections.getTypesAnnotatedWith(annotationClass);
    }

    public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotationClass, Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(e -> !e.isSynthetic())
                .peek(field -> field.setAccessible(true))
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }

    public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass, Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }

    public <T> T instantiate(Class<T> clazz, Object... args) {
        try {

            var argTypes = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);

            var constructor = clazz.getConstructor(argTypes);

            return constructor.newInstance(args);

        } catch (Exception e) {
            this.plugin.getFpLogger().error(CLASS_NAME, "instantiate", e);
            return null;
        }
    }


    public void invoke(Method method, Object target, Object... args) {
        method.setAccessible(true);
        try {

            // some commands will ignore args -> only one arg passed

            if (method.getParameterCount() == 1)
                method.invoke(target, args[0]);
            else
                method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            this.plugin.getFpLogger().error(CLASS_NAME, "invoke", e);
        }
    }

    public Object getFieldValueSafe(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            this.plugin.getFpLogger().error(CLASS_NAME, "getFieldValueSafe", e);
            return null;
        }
    }

}
