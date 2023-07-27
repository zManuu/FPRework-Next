package de.fantasypixel.rework.utils.provider;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.utils.FPConfig;
import de.fantasypixel.rework.utils.PackageUtils;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class ProviderManager {

    private final FPRework plugin;
    private FPConfig config;
    private Map<String, Object> serviceProviders;
    private Set<Object> controllers;

    public ProviderManager(FPRework plugin) {
        this.plugin = plugin;

        this.initServiceProviders();
        this.initControllers();
        this.initServiceHooks();
        this.loadConfig();
        this.initConfigHooks();
    }

    private void initServiceProviders() {
        Set<Class<?>> serviceProviderClasses = PackageUtils.getClassesAnnotatedWith(ServiceProvider.class);
        this.serviceProviders = new HashMap<>();

        serviceProviderClasses.forEach(serviceProviderClass -> {
            var serviceName = serviceProviderClass.getAnnotation(ServiceProvider.class).name();
            var serviceProviderInstance = PackageUtils.instantiate(serviceProviderClass);
            this.serviceProviders.put(serviceName, serviceProviderInstance);
        });
    }

    private void initControllers() {
        Set<Class<?>> controllerClasses = PackageUtils.getClassesAnnotatedWith(Controller.class);
        this.controllers = new HashSet<>();

        controllerClasses.forEach(controllerClass -> {
            var controllerInstance = PackageUtils.instantiate(controllerClass);
            this.controllers.add(controllerInstance);
        });
    }

    private void initServiceHooks() {
        this.controllers.forEach(controller -> {
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
                        e.printStackTrace();
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
            e.printStackTrace();
            this.config = null;
        }
    }

    private void initConfigHooks() {
        this.serviceProviders.values().forEach(serviceProvider -> {
            var configHooks = PackageUtils.getFieldsAnnotatedWith(Config.class, serviceProvider.getClass());
            configHooks.forEach(configHook -> {
                try {
                    configHook.set(serviceProvider, this.config);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public void reloadConfig() {
        // hooks can be overridden
        this.loadConfig();
        this.initConfigHooks();
    }

}
