package dev.inmo.plagubot.config

import dev.inmo.plagubot.HelloPlugin
import kotlinx.serialization.InternalSerializationApi
import org.junit.Test
import kotlin.test.assertEquals

class ConfigTest {
    @InternalSerializationApi
    @Test
    fun testThatPluginPassedToParamsWillBeCorrectlyUsedInPlugins() {
        val rawConfig = """
            {
              "database": {
              },
              "botToken": "",
              "plugins": [
                "helloPlugin"
              ],
              "params": {
                "helloPlugin": {"type": "dev.inmo.plagubot.HelloPlugin", "parameter": "Example"}
              }
            }
        """.trimIndent()
        val config = configAndPluginsConfigJsonFormat.decodeFromString(PluginsConfigurationSerializer, rawConfig) as Config

        assert(config.plugins.size == 1)
        assert(config.plugins.first() is HelloPlugin)
        assert((config.plugins.first() as HelloPlugin).parameter == "Example")

        val redecoded = configAndPluginsConfigJsonFormat.decodeFromString(
            PluginsConfigurationSerializer,
            configAndPluginsConfigJsonFormat.encodeToString(PluginsConfigurationSerializer, config)
        ) as Config
        assertEquals(config.databaseConfig, redecoded.databaseConfig)
        assertEquals(config.plugins, redecoded.plugins)
        assertEquals(config.botToken, redecoded.botToken)
        assertEquals(config.params ?.toMap(), redecoded.params ?.toMap())
    }
}
