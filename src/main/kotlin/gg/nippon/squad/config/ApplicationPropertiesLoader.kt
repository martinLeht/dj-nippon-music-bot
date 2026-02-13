package gg.nippon.squad.config

import org.slf4j.LoggerFactory
import java.io.File
import java.net.URI
import java.util.Properties

private const val DEFAULT_APP_CONFIG = "application.properties"

object ApplicationPropertiesLoader {
    private val logger = LoggerFactory.getLogger(ApplicationPropertiesLoader::class.java)
    private val properties = Properties()

    init {
        val profile = System.getProperty("profile") ?: ""
        when (profile != "") {
            true -> logger.info("Running with $profile profile")
            false -> logger.info("Running without specific profile")
        }
        val propertiesFile = appendProfileToApplicationProperties(profile)
        val file = this::class.java.classLoader.getResourceAsStream(propertiesFile)
        properties.load(file)
    }

    fun getProperty(key: String, errorMessage: String = "$key properties were not found and initialized"): String =
        properties.getProperty(key) ?: throw IllegalStateException(errorMessage)

    fun getPropertyAsURI(key: String, errorMessage: String = "$key properties were not found and initialized"): URI =
        properties.getProperty(key)?.let { URI.create(it) } ?: throw IllegalStateException(errorMessage)

    fun getPropertyAsList(
        key: String,
        errorMessage: String = "$key list properties were not found and initialized",
    ): List<String> = getProperty(key, errorMessage).split(",")

    fun getDiscordBotConfiguration(): DiscordBotConfiguration =
        DiscordBotConfiguration(
            token = getProperty(DiscordBotConfiguration.TOKEN_CONFIG_PATH),
            enabledGuildsById = getPropertyAsList(DiscordBotConfiguration.ENABLED_GUILDS_CONFIG_PATH).toSet(),
        )

    fun getLavalinkClientConfiguration(): LavalinkClientConfiguration =
        LavalinkClientConfiguration(
            password = getProperty(LavalinkClientConfiguration.PASSWORD_CONFIG_PATH),
            uri = getPropertyAsURI(LavalinkClientConfiguration.URI_CONFIG_PATH),
        )

    private fun appendProfileToApplicationProperties(profile: String): String {
        val dotIndex = DEFAULT_APP_CONFIG.lastIndexOf(".")
        if (dotIndex == -1) return DEFAULT_APP_CONFIG

        val namePart = DEFAULT_APP_CONFIG.substring(0, dotIndex)
        val extension = DEFAULT_APP_CONFIG.substring(dotIndex)

        return "$namePart-$profile$extension"
    }
}