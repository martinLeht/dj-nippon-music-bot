package gg.nippon.squad.config

class DiscordBotConfiguration(
    val token: String,
    val enabledGuildsById: Set<String>
) {
    companion object {
        const val CONFIG_PATH = "discord.bot"
        const val TOKEN_CONFIG_PATH = "$CONFIG_PATH.token"
        const val ENABLED_GUILDS_CONFIG_PATH = "$CONFIG_PATH.enabledGuildsById"
    }
}