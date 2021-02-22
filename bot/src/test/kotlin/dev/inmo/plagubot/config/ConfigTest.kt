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
        val config = configJsonFormat.decodeFromString(ConfigSerializer, rawConfig)

        assert(config.plugins.size == 1)
        assert(config.plugins.first() is HelloPlugin)
        assert((config.plugins.first() as HelloPlugin).parameter == "Example")

        val redecoded = configJsonFormat.decodeFromString(ConfigSerializer, configJsonFormat.encodeToString(ConfigSerializer, config))
        assertEquals(config.database, redecoded.database)
        assertEquals(config.plugins, redecoded.plugins)
        assertEquals(config.botToken, redecoded.botToken)
        assertEquals(config.params ?.toMap(), redecoded.params ?.toMap())
    }
}