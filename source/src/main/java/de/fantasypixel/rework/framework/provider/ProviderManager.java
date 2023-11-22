package de.fantasypixel.rework.framework.provider;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.FPConfig;
import de.fantasypixel.rework.framework.command.CommandManager;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.events.OnDisable;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.provider.autorigging.Config;
import de.fantasypixel.rework.framework.provider.autorigging.Gson;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.framework.web.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProviderManager {

    private final String CLASS_NAME = ProviderManager.class.getSimpleName();

    private final FPRework plugin;
    @Getter
    private FPConfig config;
    private Set<Class<?>> serviceProviderClasses;
    private Map<String, Object> serviceProviders;
    private Set<Object> controllers;
    private Map<Class<?>, DataRepoProvider<?>> dataProviders;
    private TimerManager timerManager;
    private final CommandManager commandManager;
    private final WebManager webManager;

    public ProviderManager(FPRework plugin) {
        this.plugin = plugin;
        this.commandManager = new CommandManager(plugin);

        this.loadConfig();

        this.webManager = new WebManager(plugin, this.config);

        this.initServiceProviders();
        this.initControllers();
        this.initServiceHooks();
        this.initWebHandlers();
        this.initHooks("Plugin", Plugin.class, plugin);
        this.initHooks("Gson", Gson.class, plugin.getGson());
        this.initHooks("Config", Config.class, this.config);
        this.createDataRepos();

        // controllers have to be enabled async so this constructor closes and the providerManager instance is available
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                this.plugin,
                this::enableControllers,
                this.config.getControllerStartupTimeout()
        );
    }

    /**
     * Creates instances of all service-providers and populates them into {@link #serviceProviders}.
     */
    private void initServiceProviders() {
        this.serviceProviderClasses = this.plugin.getPackageUtils().getClassesAnnotatedWith(ServiceProvider.class);
        this.serviceProviders = new HashMap<>();

        serviceProviderClasses.forEach(serviceProviderClass -> {
            var serviceName = serviceProviderClass.getAnnotation(ServiceProvider.class).name();
            var serviceProviderInstance = this.plugin.getPackageUtils().instantiate(serviceProviderClass);
            this.serviceProviders.put(serviceName, serviceProviderInstance);
            this.plugin.getFpLogger().info("Created service-provider " + serviceName);
        });
    }

    /**
     * Creates instances of all controllers and populates them into {@link #controllers}.
     */
    private void initControllers() {
        Set<Class<?>> controllerClasses = this.plugin.getPackageUtils().getClassesAnnotatedWith(Controller.class);
        this.controllers = new HashSet<>();

        controllerClasses.forEach(controllerClass -> {
            var controllerInstance = this.plugin.getPackageUtils().instantiate(controllerClass);

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
            var serviceHooks = this.plugin.getPackageUtils().getFieldsAnnotatedWith(Service.class, controller.getClass());

            serviceHooks.forEach(serviceHook -> {
                var serviceName = serviceHook.getAnnotation(Service.class).name();
                var serviceProvider = this.serviceProviders.get(serviceName);

                if (serviceProvider == null) {
                    this.plugin.getFpLogger().warning("No service provider found for " + serviceName + ".");
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
     * Loads the configuration from the file and saves it into a local variable.
     * To reload the configuration, use {@link #reloadConfig()}.
     */
    private void loadConfig() {
        var configFile = new File(this.plugin.getDataFolder(), "config.json");

        if (!configFile.exists()) {
            this.plugin.getFpLogger().warning("Couldn't find config.json!");
            return;
        }

        try (var fileReader = new FileReader(configFile)) {
            this.config = this.plugin.getGson().fromJson(fileReader, FPConfig.class);
        } catch (Exception e) {
            this.plugin.getFpLogger().error(CLASS_NAME, "loadConfig", e);
            this.config = null;
        }
    }

    /**
     * Registers all the web annotations.
     */
    private void initWebHandlers() {
        this.controllers.forEach(controller -> {
            this.plugin.getPackageUtils().getMethodsAnnotatedWith(WebGet.class, controller.getClass()).forEach(getHandler -> {
                var data = getHandler.getAnnotation(WebGet.class);
                this.webManager.registerRoute(data.name(), data.route(), WebManager.HttpMethod.GET, getHandler, controller);
            });

            this.plugin.getPackageUtils().getMethodsAnnotatedWith(WebPost.class, controller.getClass()).forEach(postHandler -> {
                var data = postHandler.getAnnotation(WebPost.class);
                this.webManager.registerRoute(data.name(), data.route(), WebManager.HttpMethod.POST, postHandler, controller);
            });
        });
    }

    /**
     * Autoriggs a given value into Services.
     * @param name the name of the rigged value (only used for logging)
     * @param annotationClass the annotation type of all objects that will be auto rigged
     * @param value the value of the auto rigging process
     */
    private void initHooks(String name, Class<? extends Annotation> annotationClass, Object value) {
        this.serviceProviders.values().forEach(serviceProvider -> {
            this.plugin.getFpLogger().info(
                    MessageFormat.format(
                            "Auto rigging {0} for service {1}",
                            name,
                            serviceProvider.getClass().getName()
                    )
            );

            var hooks = this.plugin.getPackageUtils().getFieldsAnnotatedWith(annotationClass, serviceProvider.getClass());
            hooks.forEach(hook -> {
                try {
                    hook.set(serviceProvider, value);
                } catch (IllegalAccessException e) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "initGsonHooks", e);
                }
            });
        });
    }

    /**
     * Creates all data-repository instances and populates them.
     */
    private void createDataRepos() {
        this.plugin.getPackageUtils().loadMysqlDriver();

        this.dataProviders = new HashMap<>();

        this.serviceProviderClasses.forEach(serviceProviderClass -> {
            var dataRepoHooks = this.plugin.getPackageUtils().getFieldsAnnotatedWith(DataRepo.class, serviceProviderClass);
            var serviceProvider = this.serviceProviders.get(serviceProviderClass.getAnnotation(ServiceProvider.class).name());

            dataRepoHooks.forEach(dataRepoHook -> {

                var dataRepoEntityType = dataRepoHook.getAnnotation(DataRepo.class).type();
                if (!this.dataProviders.containsKey(dataRepoEntityType)) {
                    this.plugin.getFpLogger().info("Creating data-repo for " + dataRepoEntityType.getName());
                    var dataRepoInstance = (DataRepoProvider<?>) this.plugin.getPackageUtils().instantiate(DataRepoProvider.class, dataRepoEntityType, this.plugin);
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
     * Reloads the configuration and provides the updated configuration to all Services.
     */
    public void reloadConfig() {
        // hooks can be overridden
        this.loadConfig();
        this.initHooks("Config", Config.class, this.config);
    }

    /**
     * Calls onEnable on all controllers and starts the timers.
     */
    private void enableControllers() {
        var timerMap = new HashMap<Method, Object>();

        this.controllers.forEach(controller -> {
            // call onEnable
            this.plugin.getPackageUtils().getMethodsAnnotatedWith(OnEnable.class, controller.getClass()).forEach(onEnableFunc -> {
                try {
                    this.plugin.getFpLogger().info(MessageFormat.format("Enabling controller {0}.", controller.getClass().getName()));
                    onEnableFunc.invoke(controller);

                    if (controller instanceof Listener controllerListener) {
                        this.plugin.getFpLogger().info(MessageFormat.format("Registering controller-listener {0}.", controller.getClass().getName()));
                        Bukkit.getPluginManager().registerEvents(controllerListener, plugin);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "enableControllers", e);
                }
            });

            // load timers into map
            this.plugin.getPackageUtils().getMethodsAnnotatedWith(Timer.class, controller.getClass()).forEach((timerFunc) -> timerMap.put(timerFunc, controller));
        });

        this.timerManager = new TimerManager(this.plugin, timerMap);
    }

    /**
     * Calls onDisable on all controllers, stops the timers and the webserver.
     */
    public void onDisable() {
        this.controllers.forEach(controller -> {
            this.plugin.getPackageUtils().getMethodsAnnotatedWith(OnDisable.class, controller.getClass()).forEach(onDisableFunc -> {
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
