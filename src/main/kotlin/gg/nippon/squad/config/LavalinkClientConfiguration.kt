package gg.nippon.squad.config

import java.net.URI

class LavalinkClientConfiguration(
    val password: String,
    val uri: URI
) {

    companion object {
        const val CONFIG_PATH = "lavalink.client"
        const val PASSWORD_CONFIG_PATH = "$CONFIG_PATH.password"
        const val URI_CONFIG_PATH = "$CONFIG_PATH.uri"
    }
}