package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.micro_utils.common.Warning
import dev.inmo.micro_utils.coroutines.runCatchingLogging
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.fsm.common.StatesManager
import dev.inmo.micro_utils.fsm.common.managers.*
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.startup.launcher.StartLauncherPlugin
import dev.inmo.plagubot.config.*
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.KtorRequestsExecutorBuilder
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.webhook.deleteWebhook
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import java.io.File

/**
 * Central plugin-object that:
 * - Provides DI bindings for the bot, configuration and optional database
 * - Creates and configures Telegram bot client
 * - Builds BehaviourBuilder with FSM support and launches long polling
 * - Loads and initializes other plugins
 * - Exposes several start(...) overloads to bootstrap the application from JSON or strongly-typed configs
 *
 * Typical usage:
 * - Prepare JSON or typed configs
 * - Call one of start(...) methods
 * - The returned Job represents the bot lifecycle; awaiting it keeps the app running
 */
@OptIn(Warning::class)
@Serializable
object PlaguBot : Plugin {
    /**
     * JSON format used for encoding/decoding PlaguBot-related configuration.
     *
     * Note:
     * - This property is not reactive. Set it only before starting the bot to affect configuration parsing/serialization.
     */
    var defaultJsonFormat = dev.inmo.plagubot.config.defaultJsonFormat
        @Warning("This is not reactive set. Use this only BEFORE starting of bot") set

    /**
     * Allows other plugins to customize the [KtorRequestsExecutorBuilder] used by the bot client.
     *
     * For each plugin in DI (excluding this object), calls its own setupBotClient, passing the same [scope] and [params].
     *
     * @param scope Koin scope where the bot is being created
     * @param params Raw JSON params of the bot part of the configuration
     */
    override fun KtorRequestsExecutorBuilder.setupBotClient(scope: Scope, params: JsonObject) {
        scope.plugins.filter { it !== this@PlaguBot }.forEach {
            with(it) {
                setupBotClient(scope, params)
            }
        }
    }

    /**
     * Sets up application DI for the bot.
     *
     * Registers:
     * - Decoded typed [Config] from provided [config] JSON
     * - Raw [JsonObject] config itself for low-level access
     * - Database-related config and database instance (if present in [Config])
     * - This [PlaguBot] plugin instance
     * - All plugins declared in launcher config as DI singletons
     * - Configured [TelegramBot] built via [telegramBot] and customized by all plugins' setupBotClient
     *
     * @param config Raw PlaguBot JSON configuration
     */
    override fun Module.setupDI(config: JsonObject) {
        single { get<Json>().decodeFromJsonElement(Config.serializer(), config) }
        single { config }
        single { get<Config>().databaseConfig }
        single { get<Config>().databaseConfig.database }
        single { this@PlaguBot }
        singlePlugins { get<dev.inmo.micro_utils.startup.launcher.Config>().plugins.filterIsInstance<Plugin>() }
        single {
            val config = get<Config>()
            telegramBot(
                token = config.botToken,
                testServer = config.testServer,
                apiUrl = config.botApiServer
            ) {
                setupBotClient(this@single, get<JsonObject>())
            }
        }
    }

    /**
     * Builds and starts the bot behaviour and FSM.
     *
     * Flow:
     * - Collects [OnStartContextsConflictResolver], [OnUpdateContextsConflictResolver], [StatesManager] and [DefaultStatesManagerRepo] from DI (or creates defaults)
     * - Builds BehaviourBuilder with FSM, logs errors, and calls [setupBotPlugin] for all plugins
     * - Ensures webhook is removed before long polling with [deleteWebhook]
     * - Starts long polling via [startGettingOfUpdatesByLongPolling] using the created behaviour context as scope and filter
     * - Awaits the long-polling [Job] to keep the app alive
     */
    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        val scope = koin.get<CoroutineScope>()

        lateinit var behaviourContext: BehaviourContext
        val onStartContextsConflictResolver by lazy { koin.getAllDistinct<OnStartContextsConflictResolver>() }
        val onUpdateContextsConflictResolver by lazy { koin.getAllDistinct<OnUpdateContextsConflictResolver>() }
        val bot = koin.get<TelegramBot>()
        bot.buildBehaviourWithFSM(
            scope = scope,
            defaultExceptionsHandler = {
                logger.e("Something went wrong", it)
            },
            statesManager = koin.getOrNull<StatesManager<State>>() ?: DefaultStatesManager(
                koin.getOrNull<DefaultStatesManagerRepo<State>>() ?: InMemoryDefaultStatesManagerRepo<State>(),
                onStartContextsConflictResolver = { old, new -> onStartContextsConflictResolver.firstNotNullOfOrNull { it(old, new) } ?: false },
                onUpdateContextsConflictResolver = { old, new, currentNew -> onUpdateContextsConflictResolver.firstNotNullOfOrNull { it(old, new, currentNew) } ?: false }
            ),
            onStateHandlingErrorHandler = koin.getOrNull<StateHandlingErrorHandler<State>>() ?: { state, e ->
                logger.eS(e) { "Unable to handle state $state" }
                null
            },
            subcontextInitialAction = CombinedSubcontextInitialAction(koin.getAllDistinct()).subcontextInitialAction
        ) {
            logger.i("Start setup of bot part")
            behaviourContext = this
            setupBotPlugin(koin)
            deleteWebhook()
        }.start()
        logger.i("Behaviour builder has been setup")

        bot.startGettingOfUpdatesByLongPolling(scope = behaviourContext, updatesFilter = behaviourContext).also {
            logger.i("Long polling has been started")
        }.join()
    }

    /**
     * Initializes and loads all other [Plugin]s from DI for the bot runtime.
     *
     * Each plugin is:
     * - Logged as loading started
     * - Executed via plugin.setupBotPlugin(...) inside a safe block
     * - Logged on failure (as warning) or success (as info)
     *
     * [PlaguBot] itself is excluded from this list.
     */
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        koin.plugins.filter { it !== this@PlaguBot }.forEach { plugin ->
            runCatchingLogging(logger = logger) {
                logger.i("Start loading of $plugin")
                with(plugin) {
                    setupBotPlugin(koin)
                }
            }.onFailure { e ->
                logger.w("Unable to load bot part of $plugin", e)
            }.onSuccess {
                logger.i("Complete loading of $plugin")
            }
        }
    }

    /**
     * Starts the application using raw JSON and a launcher config, optionally overriding PlaguBot config.
     *
     * Behavior:
     * - If [PlaguBot] is not present in [config.plugins], injects it into the plugins list
     * - Optionally merges [plaguBotConfig] into [json]
     * - Delegates to [StartLauncherPlugin.start] and returns the lifecycle [Job]
     *
     * @param json Raw JSON that may contain both launcher and PlaguBot configs
     * @param config Parsed launcher configuration
     * @param plaguBotConfig Optional typed PlaguBot config to merge/add
     * @return Job representing the bot lifecycle
     */
    suspend fun start(
        json: JsonObject,
        config: dev.inmo.micro_utils.startup.launcher.Config,
        plaguBotConfig: Config? = null
    ): Job {
        KSLog.i("Config has been read")

        // Adding of PlaguBot when it absent in config
        val (resultJson, resultConfig) = if (PlaguBot in config.plugins) {
            KSLog.i("Initial config contains PlaguBot, pass config as is to StartLauncherPlugin")
            json to config
        } else {
            KSLog.i("Start fixing of PlaguBot absence. If PlaguBot has been skipped by some reason, use dev.inmo.micro_utils.startup.launcher.main as startup point or StartLauncherPlugin directly")

            val encodedPlaguBotConfig = plaguBotConfig ?.let {
                defaultJsonFormat.encodeToJsonElement(Config.serializer(), it).jsonObject
            } ?: JsonObject(emptyMap())

            val resultJson = JsonObject(
                encodedPlaguBotConfig + JsonObject(
                    json + Pair(
                        "plugins",
                        JsonArray(
                            (json["plugins"] as? JsonArray ?: JsonArray(emptyList())) + JsonPrimitive(PlaguBot::class.qualifiedName!!)
                        )
                    )
                )
            )
            val resultConfig = defaultJsonFormat.decodeFromJsonElement(dev.inmo.micro_utils.startup.launcher.Config.serializer(), resultJson)
            resultJson to resultConfig
        }

        KSLog.i("Config initialization done. Passing config to StartLauncherPlugin")
        return StartLauncherPlugin.start(
            resultConfig,
            resultJson
        ).koin.get<CoroutineScope>().coroutineContext.job
    }

    /**
     * Starts the application using typed launcher config and typed PlaguBot config.
     *
     * Behavior:
     * - Merges both configs into a single JSON
     * - Ensures [PlaguBot] is present in plugins list
     * - Delegates to [start] that accepts JSON and launcher config
     *
     * @param pluginsConfig Launcher configuration
     * @param plaguBotConfig Typed PlaguBot configuration
     * @return Job representing the bot lifecycle
     */
    suspend fun start(
        pluginsConfig: dev.inmo.micro_utils.startup.launcher.Config,
        plaguBotConfig: Config
    ): Job {
        val json = JsonObject(
            defaultJsonFormat.encodeToJsonElement(
                dev.inmo.micro_utils.startup.launcher.Config.serializer(),
                pluginsConfig
            ).jsonObject + defaultJsonFormat.encodeToJsonElement(
                Config.serializer(),
                plaguBotConfig
            ).jsonObject
        )
        val pluginsConfig = if (pluginsConfig.plugins.contains(PlaguBot)) {
            pluginsConfig
        } else {
            pluginsConfig.copy(
                plugins = pluginsConfig.plugins + PlaguBot,
            )
        }

        return start(json, pluginsConfig)
    }

    /**
     * Starts the application using raw JSON and a typed PlaguBot config.
     *
     * Behavior:
     * - Encodes [plaguBotConfig] and merges it into [initialJson]
     * - Delegates to [start] that takes a JSON only
     *
     * @param initialJson Initial JSON to start from
     * @param plaguBotConfig Typed PlaguBot configuration that will be encoded and merged
     * @return Job representing the bot lifecycle
     */
    suspend fun start(
        initialJson: JsonObject,
        plaguBotConfig: Config
    ): Job {
        val encodedPlaguBotConfig = defaultJsonFormat.encodeToJsonElement(Config.serializer(), plaguBotConfig).jsonObject

        return start(
            JsonObject(initialJson + encodedPlaguBotConfig)
        )
    }

    /**
     * Starts the plugins system using [StartLauncherPlugin.start].
     *
     * Parsing notes:
     * - [initialJson] is decoded into launcher config
     * - If [PlaguBot] is missing from the plugins list, it will be added automatically
     *
     * By using this method it is guaranteed that [PlaguBot] will be included into the set of plugins to launch.
     *
     * @param initialJson Raw JSON that includes launcher configuration (and optionally PlaguBot config)
     * @return Job representing the bot lifecycle
     */
    suspend fun start(initialJson: JsonObject): Job {
        val initialConfig = defaultJsonFormat.decodeFromJsonElement(dev.inmo.micro_utils.startup.launcher.Config.serializer(), initialJson)

        return start(initialJson, initialConfig)
    }

    /**
     * Starts the application from CLI arguments.
     *
     * Expects:
     * - Exactly one argument: path to a file with JSON configuration
     *
     * The file is read, parsed as [JsonObject], and passed to [start(JsonObject)].
     *
     * @param args Command-line arguments; first element is a path to the config file
     * @return Job representing the bot lifecycle
     */
    suspend fun start(args: Array<String>): Job {
        KSLog.default = KSLog("PlaguBot")
        val (configPath) = args
        val file = File(configPath)
        KSLog.i("Start read config from ${file.absolutePath}")

        val initialJson = defaultJsonFormat.parseToJsonElement(file.readText()).jsonObject
        return start(initialJson)
    }
}
