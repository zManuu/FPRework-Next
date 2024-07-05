package de.fantasypixel.rework.framework;

import de.fantasypixel.rework.FPRework;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds utility methods for the framework. Mainly focused on reflections but can also be used for misc purposes.
 * <br>
 * Note: When adding new reflection methods, make sure to add the corresponding {@link Scanners} in the constructor.
 */
public class FPUtils {

    private final String CLASS_NAME = FPUtils.class.getSimpleName();
    private final FPRework plugin;
    private final Reflections reflections;

    public FPUtils(@Nonnull FPRework plugin) {
        this.plugin = plugin;
        this.reflections = new Reflections(
                "de.fantasypixel.rework",
                Scanners.TypesAnnotated,
                Scanners.FieldsAnnotated,
                Scanners.SubTypes.filterResultsBy(s -> true)
        );
    }

    /**
     * @param annotationClass the annotation class to be searched for
     * @return all classes in the module package that have the annotation
     */
    @Nonnull
    public Set<Class<?>> getClassesAnnotatedWith(@Nonnull Class<? extends Annotation> annotationClass) {
        return this.reflections.getTypesAnnotatedWith(annotationClass);
    }

    /**
     * @param annotationClass the annotation class to be searched for
     * @param clazz the class holding the fields to be searched
     * @return the found fields having the annotation
     */
    @Nonnull
    public Set<Field> getFieldsAnnotatedWith(@Nonnull Class<? extends Annotation> annotationClass, @Nonnull Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(e -> !e.isSynthetic())
                .peek(field -> field.setAccessible(true))
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }

    /**
     * @param annotationClass the annotation class to be searched for
     * @return the found fields having the annotation
     */
    @Nonnull
    public Set<Field> getFieldsAnnotatedWith(@Nonnull Class<? extends Annotation> annotationClass) {
        return this.reflections.getFieldsAnnotatedWith(annotationClass)
                .stream()
                .filter(e -> !e.isSynthetic())
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toSet());
    }

    public <T> Set<Class<? extends T>> getClassesExtending(@Nonnull Class<T> superClass) {
        return this.reflections.getSubTypesOf(superClass);
    }

    /**
     * @param annotationClass the annotation class to be searched for
     * @param clazz the class holding the methods to be searched
     * @return the found methods having the annotation
     */
    @Nonnull
    public Set<Method> getMethodsAnnotatedWith(@Nonnull Class<? extends Annotation> annotationClass, @Nonnull Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }

    /**
     * Instantiates an object of the given class with the given arguments. Note that null-values are not supported because the constructor can't be determined.
     */
    @Nullable
    public <T> T instantiate(@Nonnull Class<T> clazz, @Nullable Object... args) {
        try {

            var argTypes = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);

            var constructor = clazz.getConstructor(argTypes);

            return constructor.newInstance(args);

        } catch (Exception ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "instantiate", "There was an issue while instantiating {0} with args [{1}]", clazz.getSimpleName(), this.plugin.getGson().toJson(args));
            this.plugin.getFpLogger().error(CLASS_NAME, "instantiate", ex);
            return null;
        }
    }

    /**
     * Safely invokes a method with given args.
     * @param method the method to be invoked
     * @param target the holder object of the method
     * @param args the arguments to be passed to the method
     */
    public void invoke(@Nonnull Method method, @Nonnull Object target, @Nullable Object... args) {
        method.setAccessible(true);
        try {

            // some commands will ignore args -> only one arg passed
            if (method.getParameterCount() == 1 && args != null && args.length == 1)
                method.invoke(target, args[0]);
            else
                method.invoke(target, args);

        } catch (IllegalAccessException | InvocationTargetException ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "invoke", ex);
        }
    }

    /**
     * Gets a field value safely. If no value is found, null is returned.
     * @param field the field to be accessed
     * @param object the object holding the field
     * @return the field's value or null
     */
    @Nullable
    public Object getFieldValueSafe(@Nonnull Field field, @Nonnull Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException ex) {
            this.plugin.getFpLogger().error(CLASS_NAME, "getFieldValueSafe", ex);
            return null;
        }
    }

    /**
     * Gets an environment variable.
     */
    @Nonnull
    public Optional<String> getEnvironmentVar(@Nonnull String key) {
        return Optional.ofNullable(System.getenv(key));
    }

    /**
     * Gets an environment variable. If not found, the specified default value is returned.
     */
    @Nonnull
    public String getEnvironmentVar(@Nonnull String key, @Nonnull String defaultValue) {
        return this.getEnvironmentVar(key).orElse(defaultValue);
    }

}
