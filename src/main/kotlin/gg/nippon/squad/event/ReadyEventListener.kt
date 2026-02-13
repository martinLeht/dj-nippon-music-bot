package gg.nippon.squad.event

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.NodeOptions
import dev.arbjerg.lavalink.client.event.TrackStartEvent
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup
import gg.nippon.squad.config.LavalinkClientConfiguration
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class ReadyEventListener(
    private val lavalinkClient: LavalinkClient,
    private val lavalinkClientConfiguration: LavalinkClientConfiguration,
) : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(ReadyEventListener::class.java)
    override fun onGenericEvent(event: GenericEvent) {
        if (event is ReadyEvent) {
            logger.info("Ready event recieved, discord API is ready!")
            registerLavalinkNode()
        }
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild
        logger.info("Guild ready: ${guild.name} (${guild.id}) with ${guild.memberCount} members")
    }

    private fun registerLavalinkNode() {
        logger.info("Registering Lavalink Node")
        listOf(
            lavalinkClient.addNode(
                NodeOptions.Builder()
                    .setName("Saitamas-PC")
                    .setServerUri(lavalinkClientConfiguration.uri)
                    .setPassword(lavalinkClientConfiguration.password)
                    .setRegionFilter(RegionGroup.EUROPE)
                    .build()
            )
        )
            .forEach { node ->
                node.on<TrackStartEvent>()
                    // .next() // Adding next turns this into a 'once' listener.
                    .subscribe { event ->
                        // A new track is started!
                        logger.info("${event.node.name}: track started: ${event.track.info}")
                    }
            }
    }
}