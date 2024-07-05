package de.fantasypixel.rework.framework.provider;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.command.CommandManager;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.database.DatabaseType;
import de.fantasypixel.rework.framework.discord.DiscordConfig;
import de.fantasypixel.rework.framework.discord.DiscordManager;
import de.fantasypixel.rework.framework.events.AfterReload;
import de.fantasypixel.rework.framework.events.BeforeReload;
import de.fantasypixel.rework.framework.events.OnDisable;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.jsondata.JsonDataManager;
import de.fantasypixel.rework.framework.jsondata.JsonDataProvider;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.framework.web.*;
import de.fantasypixel.rework.framework.database.DatabaseConfig;
import de.fantasypixel.rework.framework.config.*;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.web.WebConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
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
    private ReloadManager reloadManager;
    private Map<Method, Object> beforeReloadHooks;
    private Map<Method, Object> afterReloadHooks;
    private final CitizensManager citizensManager;
    private DiscordManager discordManager;

    public ProviderManager(FPRework plugin) {
        this.plugin = plugin;
        var logger = plugin.getFpLogger();

        // managers with no access to config
        this.timerManager = new TimerManager(plugin);
        this.commandManager = new CommandManager(plugin);
        this.jsonDataManager = new JsonDataManager(plugin);

        // controllers & services
        logger.sectionStart("Controllers & Services");
        this.initServiceProviders();
        this.initServiceToServiceHooks();
        this.initControllers();
        this.initControllerToServiceHooks();
        logger.sectionEnd("Controllers & Services");

        // config
        logger.sectionStart("Config");
        this.loadConfigs();
        this.hookConfigs();
        logger.sectionEnd("Config");

        // json-data
        logger.sectionStart("Json-Data");
        this.loadJsonData();
        this.hookJsonData();
        logger.sectionEnd("Json-Data");

        // web
        logger.sectionStart("Web");
        this.webManager = new WebManager(plugin, this.getConfig(WebConfig.class));
        this.initWebHandlers(WebManager.HttpMethod.GET, WebGet.class);
        this.initWebHandlers(WebManager.HttpMethod.POST, WebPost.class);
        this.initWebHandlers(WebManager.HttpMethod.PUT, WebPut.class);
        this.initWebHandlers(WebManager.HttpMethod.DELETE, WebDelete.class);
        logger.sectionEnd("Web");

        // reload
        logger.sectionStart("Reload-Manager");
        this.initReloadManager();
        this.initReloadHooks();
        logger.sectionEnd("Reload-Manager");

        // citizens
        this.citizensManager = new CitizensManager(false, logger);
        Bukkit.getPluginManager().registerEvents(this.citizensManager, this.plugin);

        // discord
        logger.sectionStart("Discord");
        this.discordManager = new DiscordManager(this.plugin, this.getConfig(DiscordConfig.class));
        logger.sectionEnd("Discord");

        // auto rigging
        logger.sectionStart("Auto-Rigging");
        this.initAutoRigging(Set.of(
                logger,
                this.plugin.getGson(),
                this.plugin.getServer(),
                this.plugin,
                this.reloadManager,
                this.discordManager.getFpDiscordClient()
        ));
        logger.sectionEnd("Auto-Rigging");

        // extending
        logger.sectionStart("Extending class singletons");
        this.initExtendingClassSingletons();
        logger.sectionEnd("Extending class singletons");

        // database
        logger.sectionStart("Database");
        this.createDataRepos();
        logger.sectionEnd("Database");

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
     * Creates the reload-manager (with implementation).
     */
    private void initReloadManager() {
        this.reloadManager = () -> {
            this.plugin.getFpLogger().sectionStart("Reload");

            // before
            this.plugin.getFpLogger().debug("Executing {0} before-reload hooks...", this.beforeReloadHooks.size());
            this.beforeReloadHooks.forEach((method, object) -> {
                try {
                    method.invoke(object);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    this.plugin.getFpLogger().warning("There was an exception while executing a before-reload hook. Error following...");
                    this.plugin.getFpLogger().error(CLASS_NAME, "initReloadManager->before", ex);
                }
            });

            // config
            this.loadConfigs();
            this.hookConfigs();

            // json-data
            this.loadJsonData();
            this.hookJsonData();

            // data-repos
            this.dataProviders.values().forEach(DataRepoProvider::clearCache);

            // after
            this.plugin.getFpLogger().debug("Executing {0} after-reload hooks...", this.afterReloadHooks.size());
            this.afterReloadHooks.forEach((method, object) -> {
                try {
                    method.invoke(object);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    this.plugin.getFpLogger().warning("There was an exception while executing a after-reload hook. Error following...");
                    this.plugin.getFpLogger().error(CLASS_NAME, "initReloadManager->after", ex);
                }
            });

            this.plugin.getFpLogger().sectionEnd("Reload");
        };
        this.plugin.getFpLogger().debug("Reload-Manager was created.");
    }

    /**
     * Populates the maps {@link #beforeReloadHooks} and {@link #afterReloadHooks}.
     */
    private void initReloadHooks() {
        this.plugin.getFpLogger().debug("Initializing reload hooks...");
        this.beforeReloadHooks = new HashMap<>();
        this.afterReloadHooks = new HashMap<>();

        this.controllers.forEach(controller -> {
            var controllerClass = controller.getClass();

            var beforeReloadMethods = this.plugin.getFpUtils().getMethodsAnnotatedWith(BeforeReload.class, controllerClass);
            beforeReloadMethods.forEach(beforeReloadMethod -> this.beforeReloadHooks.put(beforeReloadMethod, controller));

            var afterReloadMethods = this.plugin.getFpUtils().getMethodsAnnotatedWith(AfterReload.class, controllerClass);
            afterReloadMethods.forEach(afterReloadMethod -> this.afterReloadHooks.put(afterReloadMethod, controller));
        });

        this.plugin.getFpLogger().debug("Initialized {0} before- and {1} after-reload hooks.", this.beforeReloadHooks.size(), this.afterReloadHooks.size());
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
     * Finds fields annotated with {@link Extending} and populates them with an instance of all classes extending T (has to be a Set of T).
     */
    private void initExtendingClassSingletons() {
        var extendingHooks = this.plugin.getFpUtils().getFieldsAnnotatedWith(Extending.class);

        this.plugin.getFpLogger().debug("Hooking singletons for extending {0} extending classes.", extendingHooks.size());

        for (Field extendingHook : extendingHooks) {
            var extendingHookType = extendingHook.getGenericType();
            var extendingHookTypeParam = (ParameterizedType) extendingHookType;
            var extendingHookTypeArguments = extendingHookTypeParam.getActualTypeArguments();

            if (extendingHookTypeArguments.length != 1) {
                this.plugin.getFpLogger().warning("Tried to initialize extending-class-set {0} in {1}, but there wasn't exactly one type parameter. Be sure to use Set! Skipping this set.");
                continue;
            }

            var extendingType = extendingHookTypeArguments[0];
            Set<?> classesExtending;
            Set<Object> singletonSet = new HashSet<>();

            if (extendingType instanceof Class<?> extendingClass) {
                classesExtending = this.plugin.getFpUtils().getClassesExtending(extendingClass);
            } else {
                throw new IllegalArgumentException("extendingType is not a Class");
            }

            for (Object typeObj : classesExtending) {

                if (!(typeObj instanceof Class<?> clazz)) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "initExtendingClassSingletons", "A typeObj isn't of type Class!");
                    return;
                }

                if (clazz.isAnnotationPresent(ExtendingIgnore.class)) {
                    this.plugin.getFpLogger().debug("Skipping extending-class {0}.", clazz.getSimpleName());
                    continue;
                }

                try {
                    var typeInstance = clazz.getConstructor().newInstance();
                    singletonSet.add(typeInstance);

                    this.plugin.getFpLogger().debug("Instantiated extending-class {0}.", clazz.getSimpleName());
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                    this.plugin.getFpLogger().warning("Tried to instantiate extending-type {0}, didn't work! Is there an empty constructor present?", clazz.getSimpleName());
                }
            }

            try {
                extendingHook.set(null, singletonSet);

                this.plugin.getFpLogger().debug("Created {0} extending-class-singletons for {1}.", singletonSet.size(), extendingType.getTypeName());
            } catch (IllegalAccessException ex) {
                this.plugin.getFpLogger().error(CLASS_NAME, "initExtendingClassSingletons", ex);
            }
        }
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
            try (var reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
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
        DatabaseConfig databaseConfig;
        try {
            databaseConfig = new DatabaseConfig(
                    DatabaseType.valueOf(this.plugin.getFpUtils().getEnvironmentVar("FP_NEXT_DATABASE_TYPE").orElseThrow()),
                    this.plugin.getFpUtils().getEnvironmentVar("FP_NEXT_DATABASE_HOST").orElseThrow(),
                    this.plugin.getFpUtils().getEnvironmentVar("FP_NEXT_DATABASE_USER").orElseThrow(),
                    this.plugin.getFpUtils().getEnvironmentVar("FP_NEXT_DATABASE_PASSWORD", ""),
                    this.plugin.getFpUtils().getEnvironmentVar("FP_NEXT_DATABASE_PORT").orElseThrow(),
                    this.plugin.getFpUtils().getEnvironmentVar("FP_NEXT_DATABASE_NAME").orElseThrow()
            );
        } catch (NoSuchElementException ex) {
            this.plugin.getFpLogger().warning("Tried to connect to database, at least one environment variable is missing! Please set FP_NEXT_DATABASE_TYPE, FP_NEXT_DATABASE_HOST, FP_NEXT_DATABASE_USER, FP_NEXT_DATABASE_PASSWORD?, FP_NEXT_DATABASE_PORT, FP_NEXT_DATABASE_NAME.");
            this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", ex);
            return;
        } catch (IllegalArgumentException ex) {
            this.plugin.getFpLogger().warning("Tried to connect to database, but the type in the environment variables isn't MYSQL, POSTGRESQL, SQLITE!");
            this.plugin.getFpLogger().error(CLASS_NAME, "createDataRepos", ex);
            return;
        }

        if (!DataRepoProvider.loadSqlDriver(this.plugin, databaseConfig.getType())) {
            this.plugin.getFpLogger().warning("Tried to connect to database, but the sql-driver could not be loaded! Server will shutdown...");
            this.plugin.getServer().shutdown();
        }

        if (!DataRepoProvider.testDatabaseConnection(this.plugin, databaseConfig)) {
            this.plugin.getFpLogger().warning("Tried to connect to database, but was unsuccessfully! Server will shutdown...");
            this.plugin.getServer().shutdown();
        }

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
     * <b>Note:</b> This method waits for the {@link net.citizensnpcs.api.event.CitizensEnableEvent} so that NPCs are available.
     */
    private void enableControllers() {
        this.plugin.getFpLogger().debug("Enabling controllers, waiting for CitizensEnableEvent if necessary.");

        while (!this.citizensManager.isEnabled()) {}

        this.controllers.forEach(controller -> {
            var controllerClass = controller.getClass();

            this.plugin.getFpLogger().debug("Enabling controller {0}...", controllerClass.getSimpleName());

            // call onEnable
            this.plugin.getFpUtils().getMethodsAnnotatedWith(OnEnable.class, controllerClass).forEach(onEnableFunc -> {
                try {
                    this.plugin.getFpLogger().debug("Calling OnEnable of {0}.", controllerClass.getSimpleName());
                    onEnableFunc.invoke(controller);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    this.plugin.getFpLogger().warning("There was an error in the onEnable method of controller {0}.", controllerClass.getSimpleName());
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
     * Calls onDisable on all controllers, stops the timers, webserver & discord-bot.
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
        this.discordManager.stop();
    }
}
