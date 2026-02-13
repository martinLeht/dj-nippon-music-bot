package gg.nippon.squad

import gg.nippon.squad.bot.BotInitializationException
import gg.nippon.squad.bot.DiscordBot
import gg.nippon.squad.config.ApplicationPropertiesLoader.getDiscordBotConfiguration
import gg.nippon.squad.config.ApplicationPropertiesLoader.getLavalinkClientConfiguration
import org.slf4j.LoggerFactory

fun main() {
    val logger =  LoggerFactory.getLogger("MainClass")
    try {
        val discordBotConfig = getDiscordBotConfiguration()
        val lavalinkClientConfig = getLavalinkClientConfiguration()
        val discordBot = DiscordBot(discordBotConfig, lavalinkClientConfig)
        discordBot.registerGuildCommands()
    } catch (botInitEx: BotInitializationException) {
        logger.error("Failed to initialize bot")
        logger.error(botInitEx.message)
    } catch (ex: Exception) {
        logger.error("Unexpected error occurred")
        logger.error(ex.message)
    }
}