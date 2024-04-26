package de.fantasypixel.rework.framework.provider;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.command.CommandManager;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.events.OnDisable;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.provider.autorigging.Gson;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.framework.web.*;
import de.fantasypixel.rework.modules.config.DatabaseConfig;
import de.fantasypixel.rework.framework.config.*;
import de.fantasypixel.rework.modules.config.WebConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages the framework-layer.
 */
public class ProviderManager {

    private final String CLASS_NAME = ProviderManager.class.getSimpleName();

    private final FPRework plugin;
    private Set<Class<?>> serviceProviderClasses;
    private Map<String, Object> serviceProviders;
    private Set<Object> controllers;
    private Map<Class<?>, Object> configs;
    private Map<Class<?>, DataRepoProvider<?>> dataProviders;
    private final TimerManager timerManager;
    private final CommandManager commandManager;
    private final WebManager webManager;

    public ProviderManager(FPRework plugin) {
        this.plugin = plugin;

        // managers with no access to config
        this.timerManager = new TimerManager(plugin);
        this.commandManager = new CommandManager(plugin);

        // controllers & services
        this.plugin.getFpLogger().sectionStart("Controllers & Services");
        this.initServiceProviders();
        this.initControllers();
        this.initServiceHooks();
        this.plugin.getFpLogger().sectionEnd("Controllers & Services");

        // config
        this.plugin.getFpLogger().sectionStart("Config");
        this.loadConfigs();
        this.hookConfigs();
        this.plugin.getFpLogger().sectionEnd("Config");

        // web
        this.plugin.getFpLogger().sectionStart("Web");
        this.webManager = new WebManager(plugin, this.getConfig(WebConfig.class));
        this.initWebHandlers(WebManager.HttpMethod.GET, WebGet.class);
        this.initWebHandlers(WebManager.HttpMethod.POST, WebPost.class);
        this.initWebHandlers(WebManager.HttpMethod.PUT, WebPut.class);
        this.initWebHandlers(WebManager.HttpMethod.DELETE, WebDelete.class);
        this.plugin.getFpLogger().sectionEnd("Web");

        // auto rigging
        this.plugin.getFpLogger().sectionStart("Auto-Rigging");
        this.initHooks("Plugin", Plugin.class, plugin);
        this.initHooks("Gson", Gson.class, plugin.getGson());
        this.plugin.getFpLogger().sectionEnd("Auto-Rigging");

        // database
        this.plugin.getFpLogger().sectionStart("Database");
        this.createDataRepos();
        this.plugin.getFpLogger().sectionEnd("Database");

        // controllers (delayed)
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                this.plugin,
                this::enableControllers,
                1
        );
    }

    /**
     * Creates instances of all service-providers and populates them into {@link #serviceProviders}.
     */
    private void initServiceProviders() {
        this.serviceProviderClasses = this.plugin.getFpUtils().getClassesAnnotatedWith(ServiceProvider.class);
        this.serviceProviders = new HashMap<>();

        serviceProviderClasses.forEach(serviceProviderClass -> {
            var serviceName = serviceProviderClass.getAnnotation(ServiceProvider.class).name();
            var serviceProviderInstance = this.plugin.getFpUtils().instantiate(serviceProviderClass);
            this.serviceProviders.put(serviceName, serviceProviderInstance);
            this.plugin.getFpLogger().info("Created service-provider " + serviceName);
        });
    }

    /**
     * Creates instances of all controllers and populates them into {@link #controllers}.
     */
    private void initControllers() {
        Set<Class<?>> controllerClasses = this.plugin.getFpUtils().getClassesAnnotatedWith(Controller.class);
        this.controllers = new HashSet<>();

        controllerClasses.forEach(controllerClass -> {
            var controllerInstance = this.plugin.getFpUtils().instantiate(controllerClass);

            if (controllerInstance != null) {
                this.controllers.add(controllerInstance);
                this.commandManager.registerCommands(controllerInstance);
                this.plugin.getFpLogger().info("Created controller " + controllerClass.getName());
            } else {
                this.plugin.getFpLogger().warning("Couldn't instantiate controller " + controllerClass.getName());
            }
        });
    }

    /**
     * Auto riggs the specific service-provider instance from {@link #serviceProviders} to controllers requiring it.
     * Similar to {@link #initHooks(String, Class, Object)}, but here the {@link #controllers} are iterated.
     */
    private void initServiceHooks() {
        this.controllers.forEach(controller -> {
            this.plugin.getFpLogger().info("Auto rigging service hooks for controller " + controller.getClass().getName());
            var serviceHooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(Service.class, controller.getClass());

            serviceHooks.forEach(serviceHook -> {
                var serviceName = serviceHook.getAnnotation(Service.class).name();
                var serviceProvider = this.serviceProviders.get(serviceName);

                if (serviceProvider == null) {
                    this.plugin.getFpLogger().warning("The controller {0} is accessing the non-existent service {1}.", controller.getClass().getName(), serviceName);
                } else {
                    try {
                        serviceHook.set(controller, serviceProvider);
                    } catch (IllegalAccessException e) {
                        this.plugin.getFpLogger().error(CLASS_NAME, "initServiceHooks", e);
                    }
                }
            });
        });
    }

    /**
     * Registers all the web annotations.
     */
    private void initWebHandlers(WebManager.HttpMethod httpMethod, Class<? extends Annotation> annotationClass) {
        this.controllers.forEach(controller -> {
            this.plugin.getFpUtils().getMethodsAnnotatedWith(annotationClass, controller.getClass()).forEach(handler -> {
                var data = handler.getAnnotation(annotationClass);
                // Unfortunately java doesn't support interface inheritance or dynamics so this repetitive code is needed.
                var name = "";
                var route = "";
                var timeout = 0;
                if (data instanceof WebGet getData) {
                    name = getData.name();
                    route = getData.route();
                    timeout = getData.timeout();
                } else if (data instanceof WebPost postData) {
                    name = postData.name();
                    route = postData.route();
                    timeout = postData.timeout();
                } else if (data instanceof WebPut putData) {
                    name = putData.name();
                    route = putData.route();
                    timeout = putData.timeout();
                } else if (data instanceof WebDelete deleteData) {
                    name = deleteData.name();
                    route = deleteData.route();
                    timeout = deleteData.timeout();
                }

                this.webManager.registerRoute(name, route, httpMethod, timeout, handler, controller);
            });
        });
    }

    /**
     * Auto riggs a given value into Services.
     * @param name the name of the rigged value (only used for logging)
     * @param annotationClass the annotation type of all objects that will be auto rigged
     * @param value the value to be rigged
     */
    public void initHooks(String name, Class<? extends Annotation> annotationClass, Object value) {
        this.serviceProviders.values().forEach(serviceProvider -> {
            this.plugin.getFpLogger().info(
                    MessageFormat.format(
                            "Auto rigging {0} for service {1}",
                            name,
                            serviceProvider.getClass().getName()
                    )
            );

            var hooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(annotationClass, serviceProvider.getClass());
            hooks.forEach(hook -> {
                try {
                    hook.set(serviceProvider, value);
                } catch (IllegalAccessException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "initGsonHooks", ex);
                }
            });
        });
    }

    /**
     * Loads all configurations (classes annotated with the {@link ConfigProvider} annotation) into memory.
     */
    private void loadConfigs() {
        this.configs = new HashMap<>();

        var configProviders = this.plugin.getFpUtils().getClassesAnnotatedWith(ConfigProvider.class);
        configProviders.forEach(configProvider -> {
            this.plugin.getFpLogger().info(
                    "Loading configuration {0}.",
                    configProvider.getSimpleName()
            );

            var configFilePath = MessageFormat.format(
                    "{0}.json",
                    configProvider.getAnnotation(ConfigProvider.class).path()
            );

            var configFile = new File(this.plugin.getDataFolder(), configFilePath);
            try (var reader = new FileReader(configFile)) {
                var config = this.plugin.getGson().fromJson(reader, configProvider);
                this.configs.put(configProvider, config);
            } catch (IOException ex) {
                this.plugin.getFpLogger().error(CLASS_NAME, "loadConfigs", ex);
            }
        });
    }

    /**
     * Hooks all configurations from memory into the corresponding {@link Config} fields.
     */
    private void hookConfigs() {
        this.serviceProviders.values().forEach(serviceProvider -> {
            this.plugin.getFpLogger().info(
                    MessageFormat.format(
                            "Rigging configurations for {0}.",
                            serviceProvider.getClass().getName()
                    )
            );

            var hooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(Config.class, serviceProvider.getClass());
            hooks.forEach(hook -> {
                try {
                    hook.set(serviceProvider, this.getConfig(hook.getType()));
                } catch (IllegalAccessException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "hookConfigs", ex);
                }
            });
        });
    }

    /**
     * Retrieves the configuration currently loaded for the given configuration class. Note that this class must be annotated with the {@link ConfigProvider} annotation.
     */
    @SuppressWarnings("unchecked")
    private <T> T getConfig(Class<T> clazz) {
        Object configObj = this.configs.get(clazz);
        if (configObj == null || !configObj.getClass().equals(clazz)) {
            this.plugin.getFpLogger().error(CLASS_NAME, "getConfig", "Tried to get configuration of type {0} -> type mismatch.", clazz.getName());
            return null;
        } else {
            return (T) configObj;
        }
    }

    /**
     * Creates all data-repository instances.
     */
    private void createDataRepos() {
        this.plugin.getFpUtils().loadMysqlDriver();

        this.dataProviders = new HashMap<>();

        this.serviceProviderClasses.forEach(serviceProviderClass -> {
            var dataRepoHooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(DataRepo.class, serviceProviderClass);
            var serviceProvider = this.serviceProviders.get(serviceProviderClass.getAnnotation(ServiceProvider.class).name());

            dataRepoHooks.forEach(dataRepoHook -> {

                var dataRepoEntityType = dataRepoHook.getAnnotation(DataRepo.class).type();
                if (!this.dataProviders.containsKey(dataRepoEntityType)) {
                    this.plugin.getFpLogger().info("Creating data-repo for " + dataRepoEntityType.getName());
                    var dataRepoInstance = (DataRepoProvider<?>) this.plugin.getFpUtils().instantiate(DataRepoProvider.class, dataRepoEntityType, this.plugin, this.getConfig(DatabaseConfig.class));
                    this.dataProviders.put(dataRepoEntityType, dataRepoInstance);
                }

                var dataRepo = this.dataProviders.get(dataRepoEntityType);

                try {
                    dataRepoHook.set(serviceProvider, dataRepo);
                } catch (IllegalAccessException e) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", e);
                }

                this.plugin.getFpLogger().info("Auto rigged data-repo " + dataRepoEntityType.getName() + " for service-provider " + serviceProviderClass.getName());

            });
        });
    }

    /**
     * Calls onEnable on all controllers, registers listeners and starts the timers.
     */
    private void enableControllers() {
        this.controllers.forEach(controller -> {
            this.plugin.getFpLogger().info("Enabling controller {0}...", controller.getClass().getName());

            // call onEnable
            this.plugin.getFpUtils().getMethodsAnnotatedWith(OnEnable.class, controller.getClass()).forEach(onEnableFunc -> {
                try {
                    this.plugin.getFpLogger().info("Calling OnEnable von {0}.", controller.getClass().getName());
                    onEnableFunc.invoke(controller);

                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "enableControllers", e);
                }
            });

            // register possible listeners
            if (controller instanceof Listener controllerListener) {
                this.plugin.getFpLogger().info("Registering controller-listener {0}.", controller.getClass().getName());
                Bukkit.getPluginManager().registerEvents(controllerListener, plugin);
            }

            // start timers in controller
            this.plugin.getFpUtils()
                    .getMethodsAnnotatedWith(Timer.class, controller.getClass())
                    .forEach((timerFunc) -> this.timerManager.startTimer(timerFunc, controller));
        });
    }

    /**
     * Calls onDisable on all controllers, stops the timers and the webserver.
     */
    public void onDisable() {
        this.controllers.forEach(controller -> {
            this.plugin.getFpUtils().getMethodsAnnotatedWith(OnDisable.class, controller.getClass()).forEach(onDisableFunc -> {
                try {
                    onDisableFunc.invoke(controller);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "onDisable", e);
                }
            });
        });

        this.timerManager.stopTimers();
        this.webManager.stop();
    }
}
