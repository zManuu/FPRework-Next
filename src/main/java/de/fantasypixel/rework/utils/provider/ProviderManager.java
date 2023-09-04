package de.fantasypixel.rework.utils.provider;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.utils.FPConfig;
import de.fantasypixel.rework.utils.PackageUtils;
import de.fantasypixel.rework.utils.database.DataRepo;
import de.fantasypixel.rework.utils.database.DataRepoProvider;
import de.fantasypixel.rework.utils.events.OnDisable;
import de.fantasypixel.rework.utils.events.OnEnable;
import de.fantasypixel.rework.utils.spigotevents.SpigotEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ProviderManager {

    private final FPRework plugin;
    @Getter
    private FPConfig config;
    private Set<Class<?>> serviceProviderClasses;
    private Map<String, Object> serviceProviders;
    private Set<Object> controllers;
    private Map<Class<?>, DataRepoProvider<?>> dataProviders;
    private Map<Class<?>, Set<Method>> events;

    public ProviderManager(FPRework plugin) {
        this.plugin = plugin;

        this.initServiceProviders();
        this.initControllers();
        this.initServiceHooks();
        this.loadConfig();
        this.initConfigHooks();
        this.initPluginHooks();
        this.initEventHooks();
        this.createDataRepos();

        // controllers have to be enabled async so this constructor closes and the providerManager instance is available
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                this.plugin,
                this::enableControllers,
                100
        );
    }

    private void initServiceProviders() {
        this.serviceProviderClasses = PackageUtils.getClassesAnnotatedWith(ServiceProvider.class);
        this.serviceProviders = new HashMap<>();

        serviceProviderClasses.forEach(serviceProviderClass -> {
            var serviceName = serviceProviderClass.getAnnotation(ServiceProvider.class).name();
            var serviceProviderInstance = PackageUtils.instantiate(serviceProviderClass);
            this.serviceProviders.put(serviceName, serviceProviderInstance);
            this.plugin.getLogger().info("Created service-provider " + serviceName);
        });
    }

    private void initControllers() {
        Set<Class<?>> controllerClasses = PackageUtils.getClassesAnnotatedWith(Controller.class);
        this.controllers = new HashSet<>();

        controllerClasses.forEach(controllerClass -> {
            var controllerInstance = PackageUtils.instantiate(controllerClass);

            if (controllerInstance != null) {
                this.controllers.add(controllerInstance);
                this.plugin.getCommandManager().registerCommands(controllerInstance);
                this.plugin.getLogger().info("Created controller " + controllerClass.getName());
            } else {
                this.plugin.getLogger().warning("Couldn't instantiate controller " + controllerClass.getName());
            }
        });
    }

    private void initServiceHooks() {
        this.controllers.forEach(controller -> {
            this.plugin.getLogger().info("Auto rigging service hooks for controller " + controller.getClass().getName());
            var serviceHooks = PackageUtils.getFieldsAnnotatedWith(Service.class, controller.getClass());

            serviceHooks.forEach(serviceHook -> {
                var serviceName = serviceHook.getAnnotation(Service.class).name();
                var serviceProvider = this.serviceProviders.get(serviceName);

                if (serviceProvider == null) {
                    this.plugin.getLogger().warning("No service provider found for " + serviceName + ".");
                } else {
                    try {
                        serviceHook.set(controller, serviceProvider);
                    } catch (IllegalAccessException e) {
                        this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "initServiceHooks", e);
                    }
                }
            });
        });
    }

    private void loadConfig() {
        var configFile = new File(this.plugin.getDataFolder(), "config.json");

        if (!configFile.exists()) {
            this.plugin.getLogger().warning("Couldn't find config.json!");
            return;
        }

        try (var fileReader = new FileReader(configFile)) {
            this.config = this.plugin.getGson().fromJson(fileReader, FPConfig.class);
        } catch (Exception e) {
            this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "loadConfig", e);
            this.config = null;
        }
    }

    private void initConfigHooks() {
        this.serviceProviders.values().forEach(serviceProvider -> {
            this.plugin.getLogger().info("Auto rigging config for service " + serviceProvider.getClass().getName());
            var configHooks = PackageUtils.getFieldsAnnotatedWith(Config.class, serviceProvider.getClass());
            configHooks.forEach(configHook -> {
                try {
                    configHook.set(serviceProvider, this.config);
                } catch (IllegalAccessException e) {
                    this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "initConfigHooks", e);
                }
            });
        });
    }

    private void initPluginHooks() {
        this.serviceProviders.values().forEach(serviceProvider -> {
            this.plugin.getLogger().info("Auto rigging plugin for service " + serviceProvider.getClass().getName());
            var pluginHooks = PackageUtils.getFieldsAnnotatedWith(Plugin.class, serviceProvider.getClass());
            pluginHooks.forEach(pluginHook -> {
                try {
                    pluginHook.set(serviceProvider, this.plugin);
                } catch (IllegalAccessException e) {
                    this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "initPluginHooks", e);
                }
            });
        });
    }

    private void initEventHooks() {
        this.events = new HashMap<>();

        this.controllers.forEach(controller -> {
            this.plugin.getLogger().info("Setting up event hooks for controller " + controller.getClass().getName());
            var eventHooks = PackageUtils.getMethodsAnnotatedWith(SpigotEvent.class, controller.getClass());
            eventHooks.forEach(eventHook -> {
                var eventType = eventHook.getParameterTypes()[0];
                if (this.events.containsKey(eventType))
                    this.events.get(eventType).add(eventHook);
                else
                    this.events.put(eventType, Set.of(eventHook));
            });
        });
    }

    private void createDataRepos() {
        PackageUtils.loadMysqlDriver();

        this.dataProviders = new HashMap<>();

        this.serviceProviderClasses.forEach(serviceProviderClass -> {
            var dataRepoHooks = PackageUtils.getFieldsAnnotatedWith(DataRepo.class, serviceProviderClass);
            var serviceProvider = this.serviceProviders.get(serviceProviderClass.getAnnotation(ServiceProvider.class).name());

            dataRepoHooks.forEach(dataRepoHook -> {

                var dataRepoEntityType = dataRepoHook.getAnnotation(DataRepo.class).type();
                if (!this.dataProviders.containsKey(dataRepoEntityType)) {
                    this.plugin.getLogger().info("Creating data-repo for " + dataRepoEntityType.getName());
                    var dataRepoInstance = (DataRepoProvider<?>) PackageUtils.instantiate(DataRepoProvider.class, dataRepoEntityType, this.plugin);
                    this.dataProviders.put(dataRepoEntityType, dataRepoInstance);
                }

                var dataRepo = this.dataProviders.get(dataRepoEntityType);

                try {
                    dataRepoHook.set(serviceProvider, dataRepo);
                } catch (IllegalAccessException e) {
                    this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "createDataRepos", e);
                }

                this.plugin.getLogger().info("Auto rigged data-repo " + dataRepoEntityType.getName() + " for service-provider " + serviceProviderClass.getName());

            });
        });
    }

    /**
     * Calls onEnable on all Controllers.
     */
    private void enableControllers() {
        this.controllers.forEach(controller -> {
            PackageUtils.getMethodsAnnotatedWith(OnEnable.class, controller.getClass()).forEach(onEnableFunc -> {
                try {
                    onEnableFunc.invoke(controller);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.plugin.getLogger().warning("An onEnable func threw an error. Stacktrace following...");
                    this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "enableControllers", e);
                }
            });
        });
    }

    public void disableControllers() {
        this.controllers.forEach(controller -> {
            PackageUtils.getMethodsAnnotatedWith(OnDisable.class, controller.getClass()).forEach(onDisableFunc -> {
                try {
                    onDisableFunc.invoke(controller);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.plugin.getLogger().warning("An onDisable func threw an error. Stacktrace following...");
                    this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "disableControllers", e);
                }
            });
        });
    }

    public void invokeEvent(Event event) {
        if (!this.events.containsKey(event.getClass()))
            return;

        this.events.get(event.getClass()).forEach(handler -> {
            try {
                handler.invoke(event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                this.plugin.getLogger().throwing(this.getClass().getSimpleName(), "invokeEvent", e);
            }
        });
    }
}
