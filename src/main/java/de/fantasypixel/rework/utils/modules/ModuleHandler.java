package de.fantasypixel.rework.utils.modules;

import de.fantasypixel.rework.FPRework;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ModuleHandler {

    private final List<Controller<?>> controllers;

    private Class<?>[] getGenericSuperClasses(Class<?> clazz) {
        List<Class<?>> genericSuperclasses = new ArrayList<>();
        Type genericSuperclass = clazz.getGenericSuperclass();

        while (genericSuperclass != null) {
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                for (Type typeArgument : actualTypeArguments) {
                    if (typeArgument instanceof Class<?> typeClass) {
                        genericSuperclasses.add(typeClass);
                    }
                }
            }

            // Move up to the next superclass
            clazz = clazz.getSuperclass();
            genericSuperclass = clazz.getGenericSuperclass();
        }

        return genericSuperclasses.toArray(new Class<?>[0]);
    }

    @Nullable
    private Constructor<?> getConstructor(Class<?> clazz, int argsCount) {
        var constructors = clazz.getConstructors();
        for (var constructor : constructors) {
            if (constructor.getParameterCount() == argsCount)
                return constructor;
        }
        return null;
    }

    private Object initConfig(Class<?> configClass) throws IOException {
        var configFileName = configClass.getSimpleName() + ".json";
        var configFile = new File(FPRework.getInstance().getDataFolder(), configFileName);

        if (!configFile.exists())
            throw new FileNotFoundException("Config file " + configFileName + " wasn't found.");

        try (var reader = new FileReader(configFile)) {
            return FPRework.getInstance().getGson().fromJson(reader, configClass);
        }
    }

    private EntityRepo<?> initRepo(Class<? extends EntityRepo<?>> repoClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var constructor = repoClass.getConstructor();
        return constructor.newInstance();
    }

    private Service<?, ?> initService(Class<? extends Service<?, ?>> serviceClass) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var superClasses = getGenericSuperClasses(serviceClass);
        var configClass = superClasses[0];
        var repoClass = superClasses[1];
        var constructor = serviceClass.getConstructor(configClass, repoClass);

        return constructor.newInstance(
                initConfig(configClass),
                initRepo((Class<? extends EntityRepo<?>>) repoClass)
        );
    }

    private Controller<?> initController(Class<? extends Controller<?>> controllerClazz) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var serviceClass = getGenericSuperClasses(controllerClazz)[0];
        var service = initService((Class<? extends Service<?, ?>>) serviceClass);
        var constructor = controllerClazz.getConstructor(serviceClass);
        return (Controller<?>) constructor.newInstance(service);
    }

    @SafeVarargs
    public ModuleHandler(Class<? extends Controller<?>>... controllerClasses) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.controllers = new ArrayList<>();

        for (Class<? extends Controller<?>> controllerClass : controllerClasses) {
            var controller = initController(controllerClass);
            this.controllers.add(controller);
            controller.onEnable();
            FPRework.getInstance().getCommandManager().registerCommands(controller);
        }
    }

    public void disable() {
        for (Controller<?> controller : this.controllers) {
            controller.onDisable();
        }
    }

}
