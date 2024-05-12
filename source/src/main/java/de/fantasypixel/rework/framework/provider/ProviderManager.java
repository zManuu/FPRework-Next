package de.fantasypixel.rework.framework.provider;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.command.CommandManager;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.events.OnDisable;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.jsondata.JsonDataManager;
import de.fantasypixel.rework.framework.jsondata.JsonDataProvider;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.framework.web.*;
import de.fantasypixel.rework.modules.config.DatabaseConfig;
import de.fantasypixel.rework.framework.config.*;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.modules.config.WebConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.*;

/**
 * Manages the framework-layer.
 */
public class ProviderManager {

    private final String CLASS_NAME = ProviderManager.class.getSimpleName();

    private final FPRework plugin;
    private Set<Class<?>> serviceProviderClasses;
    private Map<Class<?>, Object> serviceProviders;
    private Set<Object> controllers;
    private Map<Class<?>, Object> configs;
    private Map<Class<?>, JsonDataContainer<?>> jsonData;
    private Map<Class<?>, DataRepoProvider<?>> dataProviders;
    private final TimerManager timerManager;
    private final CommandManager commandManager;
    private final WebManager webManager;
    private final JsonDataManager jsonDataManager;

    public ProviderManager(FPRework plugin) {
        this.plugin = plugin;

        // managers with no access to config
        this.timerManager = new TimerManager(plugin);
        this.commandManager = new CommandManager(plugin);
        this.jsonDataManager = new JsonDataManager(plugin);

        // controllers & services
        this.plugin.getFpLogger().sectionStart("Controllers & Services");
        this.initServiceProviders();
        this.initServiceToServiceHooks();
        this.initControllers();
        this.initControllerToServiceHooks();
        this.plugin.getFpLogger().sectionEnd("Controllers & Services");

        // config
        this.plugin.getFpLogger().sectionStart("Config");
        this.loadConfigs();
        this.hookConfigs();
        this.plugin.getFpLogger().sectionEnd("Config");

        // json-data
        this.plugin.getFpLogger().sectionStart("Json-Data");
        this.loadJsonData();
        this.hookJsonData();
        this.plugin.getFpLogger().sectionEnd("Json-Data");

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
        this.initAutoRigging(Set.of(
                this.plugin.getFpLogger(),
                this.plugin.getGson(),
                this.plugin.getServer(),
                this.plugin
        ));
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
            var serviceProviderInstance = this.plugin.getFpUtils().instantiate(serviceProviderClass);
            this.serviceProviders.put(serviceProviderClass, serviceProviderInstance);
            this.plugin.getFpLogger().debug("Successfully created service-provider {0}.", serviceProviderClass.getSimpleName());
        });
    }

    /**
     * Initializes service hooks coming from other services.
     */
    private void initServiceToServiceHooks() {
        this.serviceProviders.forEach((serviceProviderClass, serviceProvider) -> {
            var serviceToServiceHooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(Service.class, serviceProviderClass);

            serviceToServiceHooks.forEach(serviceToServiceHook -> {
                var depServiceProviderClass = serviceToServiceHook.getType();
                var depServiceProvider = this.serviceProviders.get(depServiceProviderClass);

                if (depServiceProvider == null) {
                    plugin.getFpLogger().warning("Tried to initialize service-2-service hook, but no matching serviceProvider was found for class {0}.", depServiceProviderClass.getName());
                    return;
                }

                try {
                    serviceToServiceHook.set(serviceProvider, depServiceProvider);
                    this.plugin.getFpLogger().debug("Setup service-2-service hook for field {0} ({1}) in service {2}.", serviceToServiceHook.getName(), depServiceProviderClass.getSimpleName(), serviceProviderClass.getSimpleName());
                } catch (IllegalAccessException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "initServiceToServiceHooks", ex);
                }
            });
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
                this.plugin.getFpLogger().debug("Created controller {0}.", controllerClass.getSimpleName());
            } else {
                this.plugin.getFpLogger().warning("Couldn't instantiate controller " + controllerClass.getName());
            }
        });
    }

    /**
     * Auto riggs the specific service-provider instance from {@link #serviceProviders} to controllers requiring it.
     */
    private void initControllerToServiceHooks() {
        this.controllers.forEach(controller -> {
            var serviceHooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(Service.class, controller.getClass());

            serviceHooks.forEach(serviceHook -> {
                var serviceClass = serviceHook.getType();
                var serviceProvider = this.serviceProviders.get(serviceClass);

                if (serviceProvider == null) {
                    this.plugin.getFpLogger().warning("The controller {0} is accessing the non-existent service {1}.", controller.getClass().getName(), serviceClass.getName());
                } else {
                    try {
                        serviceHook.set(controller, serviceProvider);
                        this.plugin.getFpLogger().debug("Setup service hook for field {0} ({1}) in controller {2}.", serviceHook.getName(), serviceClass.getSimpleName(), controller.getClass().getSimpleName());
                    } catch (IllegalAccessException ex) {
                        this.plugin.getFpLogger().error(CLASS_NAME, "initServiceHooks", ex);
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
     * Riggs the given values into fields in service-providers annotated with the {@link Auto} annotation.
     * Note: Only one value per type is allowed!
     * @param values the values to be rigged
     */
    private void initAutoRigging(Set<Object> values) {
        this.serviceProviders.forEach((serviceProviderClass, serviceProvider) -> {
            var hooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(Auto.class, serviceProviderClass);

            hooks.forEach(hook -> {

                var hookType = hook.getType();
                var hookValue = values.stream()
                        .filter(e -> hookType.isAssignableFrom(e.getClass()))
                        .findAny()
                        .orElse(null);

                if (hookValue == null) {
                    this.plugin.getFpLogger().warning("Tried to auto-rig field {0} in service {1}, but no value was found with the type.", hook.getName(), serviceProviderClass.getSimpleName());
                    return;
                }

                try {
                    hook.set(serviceProvider, hookValue);
                    this.plugin.getFpLogger().debug("Successfully auto-rigged field {0} ({1}) in service {2}.", hook.getName(), hookType.getSimpleName(), serviceProviderClass.getSimpleName());
                } catch (IllegalAccessException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "initAutoRigging", ex);
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
            this.plugin.getFpLogger().debug(
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
        this.serviceProviders.forEach((serviceProviderClass, serviceProvider) -> {
            var hooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(Config.class, serviceProvider.getClass());
            hooks.forEach(hook -> {
                try {
                    var configValue = this.getConfig(hook.getType());

                    if (configValue == null) {
                        this.plugin.getFpLogger().warning("Tried to setup config-hook {0} in service {1}, but no corresponding config found for {2}!", hook.getName(), serviceProviderClass.getName(), hook.getType().getName());
                        return;
                    }

                    hook.set(serviceProvider, configValue);
                    this.plugin.getFpLogger().debug("Setup config-hook {0} ({1}) in service {2}.", hook.getName(), configValue.getClass().getSimpleName(), serviceProviderClass.getSimpleName());
                } catch (IllegalAccessException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "hookConfigs", ex);
                }
            });
        });
    }

    /**
     * Retrieves the configuration currently loaded for the given configuration class. Note that this class must be annotated with the {@link ConfigProvider} annotation.
     */
    @Nullable
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
     * Loads all json-data into memory.
     */
    private void loadJsonData() {
        this.jsonData = new HashMap<>();

        var jsonDataProviders = this.plugin.getFpUtils().getClassesAnnotatedWith(JsonDataProvider.class);
        jsonDataProviders.forEach(jsonDataProvider -> {
            this.plugin.getFpLogger().debug("Loading JSON-Data for provider {0}.", jsonDataProvider.getSimpleName());

            var baseDirectory = new File(
                    this.plugin.getDataFolder(),
                    jsonDataProvider.getAnnotation(JsonDataProvider.class).path()
            );

            this.jsonData.put(
                    jsonDataProvider,
                    this.jsonDataManager.convertEntriesToJsonDataContainer(
                        baseDirectory,
                        jsonDataProvider,
                        this.jsonDataManager.loadJsonData(baseDirectory, jsonDataProvider)
                    )
            );
        });
    }

    /**
     * Hooks all json-data from the memory to the corresponding {@link JsonData} fields.
     */
    private void hookJsonData() {
        this.serviceProviders.forEach((serviceProviderClass, serviceProvider) -> {
            var hooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(JsonData.class, serviceProvider.getClass());
            hooks.forEach(hook -> {
                try {
                    var providerSetType = hook.getGenericType();
                    var providerSetTypeParam = (ParameterizedType) providerSetType;
                    var providerSetTypeArguments = providerSetTypeParam.getActualTypeArguments();

                    if (providerSetTypeArguments.length == 1) {
                        var providerType = providerSetTypeArguments[0];
                        hook.set(serviceProvider, this.jsonData.get(providerType));
                        this.plugin.getFpLogger().debug("Successfully hooked json-data to field {0} in service {1}.", hook.getName(), serviceProviderClass.getSimpleName());
                    } else {
                        this.plugin.getFpLogger().error(CLASS_NAME, "hookJsonData", "Type of variable {0} in service {1} doesn't have any arguments, one was expected!", hook.getName(), serviceProvider.getClass().getSimpleName());
                    }

                } catch (IllegalAccessException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "hookJsonData", ex);
                }
            });
        });
    }

    /**
     * Creates all data-repository instances.
     */
    private void createDataRepos() {
        var databaseConfig = this.getConfig(DatabaseConfig.class);

        if (databaseConfig == null) {
            this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", "Database configuration missing!");
            return;
        }

        this.plugin.getFpUtils().loadMysqlDriver();
        DataRepoProvider.testDatabaseConnection(this.plugin, databaseConfig);

        this.dataProviders = new HashMap<>();

        this.serviceProviderClasses.forEach(serviceProviderClass -> {
            var dataRepoHooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(DataRepo.class, serviceProviderClass);
            var serviceProvider = this.serviceProviders.get(serviceProviderClass);

            dataRepoHooks.forEach(dataRepoHook -> {

                var dataRepoHookType = dataRepoHook.getGenericType();
                if (!(dataRepoHookType instanceof ParameterizedType dataRepoHookParameterizedType)) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", "The data-repo-hook {0} in class {1} doesn't seem to be parameterized!", dataRepoHook.getName(), serviceProviderClass.getSimpleName());
                    return;
                }

                var dataRepoHookTypeParameters = dataRepoHookParameterizedType.getActualTypeArguments();
                if (dataRepoHookTypeParameters.length != 1) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", "The data-repo-hook {0} in class {1} is parameterized but hasn't got 1 parameter-type as expected!", dataRepoHook.getName(), serviceProviderClass.getSimpleName());
                    return;
                }

                var dataRepoHookTypeParameter = dataRepoHookTypeParameters[0];

                if (!(dataRepoHookTypeParameter instanceof Class<?> dataRepoEntityType)) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", "The data-repo-hook {0} in class {1} is parameterized the type isn't a class!", dataRepoHook.getName(), serviceProviderClass.getSimpleName());
                    return;
                }

                if (!this.dataProviders.containsKey(dataRepoEntityType)) {
                    var dataRepoInstance = (DataRepoProvider<?>) this.plugin.getFpUtils().instantiate(DataRepoProvider.class, dataRepoEntityType, this.plugin, databaseConfig);
                    this.dataProviders.put(dataRepoEntityType, dataRepoInstance);
                    this.plugin.getFpLogger().debug("Created data-repo {0}.", dataRepoEntityType.getSimpleName());
                }

                var dataRepo = this.dataProviders.get(dataRepoEntityType);

                try {
                    dataRepoHook.set(serviceProvider, dataRepo);
                    this.plugin.getFpLogger().debug("Setup data-repo hook {0} in service {1}.", dataRepoEntityType.getSimpleName(), serviceProviderClass.getSimpleName());
                } catch (IllegalAccessException e) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", e);
                }
            });
        });
    }

    /**
     * Calls onEnable on all controllers, registers listeners & commands and starts the timers.
     */
    private void enableControllers() {
        this.controllers.forEach(controller -> {
            var controllerClass = controller.getClass();

            this.plugin.getFpLogger().debug("Enabling controller {0}...", controllerClass.getSimpleName());

            // call onEnable
            this.plugin.getFpUtils().getMethodsAnnotatedWith(OnEnable.class, controllerClass).forEach(onEnableFunc -> {
                try {
                    this.plugin.getFpLogger().debug("Calling OnEnable of {0}.", controllerClass.getSimpleName());
                    onEnableFunc.invoke(controller);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "enableControllers", ex);
                }
            });

            // register possible listeners
            if (controller instanceof Listener controllerListener) {
                this.plugin.getFpLogger().debug("Registering controller-listener {0}.", controllerClass.getSimpleName());
                Bukkit.getPluginManager().registerEvents(controllerListener, plugin);
            }

            this.commandManager.registerCommands(controller);

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
