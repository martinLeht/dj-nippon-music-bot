package gg.nippon.squad.bot

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.event.EmittedEvent
import dev.arbjerg.lavalink.client.event.ReadyEvent
import dev.arbjerg.lavalink.client.event.StatsEvent
import dev.arbjerg.lavalink.client.event.TrackStartEvent
import dev.arbjerg.lavalink.client.getUserIdFromToken
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener
import gg.nippon.squad.command.CommandExecutionHandler
import gg.nippon.squad.command.SlashCommand
import gg.nippon.squad.config.DiscordBotConfiguration
import gg.nippon.squad.config.LavalinkClientConfiguration
import gg.nippon.squad.event.ReadyEventListener
import gg.nippon.squad.event.SlashCommandEventListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory


class DiscordBot(
    val discordBotConfig: DiscordBotConfiguration,
    lavalinkClientConfig: LavalinkClientConfiguration,
) {
    private val logger = LoggerFactory.getLogger(DiscordBot::class.java)
    private val jda: JDA
    private val lavalinkClient: LavalinkClient = LavalinkClient(
        getUserIdFromToken(discordBotConfig.token)
    )
    private val connectedGuilds: List<Guild>
    private val commandExecutionHandler: CommandExecutionHandler

    init {
        lavalinkClient.loadBalancer.addPenaltyProvider(VoiceRegionPenaltyProvider())
        lavalinkClient.on<ReadyEvent>()
            .subscribe { event ->
                logger.info("Node '${event.node.name}' is ready, session id is '${event.sessionId}'!")
            }
        lavalinkClient.on<StatsEvent>()
            .subscribe({ event ->
                logger.info("Node '${event.node.name}' has stats, current players: ${event.playingPlayers}/${event.players}")
            }) { err ->
                err.printStackTrace()
                logger.error("Error occurred while loading nodes.", err)
            }

        logger.info("Starting DJ Nippon bot initialization...")
        commandExecutionHandler = CommandExecutionHandler(
            enabledGuildIds = discordBotConfig.enabledGuildsById,
            lavalinkClient = lavalinkClient,
        )
        jda = JDABuilder.createDefault(discordBotConfig.token)
            .enableIntents(
                GatewayIntent.GUILD_VOICE_STATES,
            //     GatewayIntent.GUILD_MESSAGES,
            //     GatewayIntent.GUILD_MEMBERS,
            //     GatewayIntent.MESSAGE_CONTENT,
            //     GatewayIntent.GUILD_PRESENCES
            )
            .enableCache(CacheFlag.VOICE_STATE)
            //.setMemberCachePolicy(MemberCachePolicy.ALL)
            .setActivity(Activity.playing("Type / for commands"))
            .setStatus(OnlineStatus.ONLINE)
            .addEventListeners(
                ReadyEventListener(lavalinkClient, lavalinkClientConfig),
                SlashCommandEventListener(commandExecutionHandler)
            )
            .setVoiceDispatchInterceptor(JDAVoiceUpdateListener(lavalinkClient))
            .build()
        jda.awaitReady()
        connectedGuilds = jda.guilds
    }

    fun registerGuildCommands() {
        logger.info(
            "Registering ${commandExecutionHandler.guildCommandsByType.size} slash commands for guilds: " +
                    discordBotConfig.enabledGuildsById.joinToString(",")
        )

        val commands = commandExecutionHandler.guildCommandsByType.values.toList()
        addCommandsToGuilds(
            guildIds = discordBotConfig.enabledGuildsById,
            commands = commands,
            fetchGuildByIdFn = jda::getGuildById,
        )
        deleteCommandsFromGuilds(
            commands = commands,
            guilds = connectedGuilds.filter { !discordBotConfig.enabledGuildsById.contains(it.id) },
        )
    }

    /**
     * Enable commands for configured guilds
     */
    private fun addCommandsToGuilds(
        guildIds: Set<String>,
        commands: List<SlashCommand>,
        fetchGuildByIdFn: (String) -> Guild?
    ) {
        val jdaCommands = commands.map { it.toJDACommandData() }
        guildIds.forEach { guildId ->
            val guild = fetchGuildByIdFn(guildId)
                ?: throw IllegalStateException("Configured guild to enable does not exist!")

            guild.updateCommands().addCommands(jdaCommands).queue {
                logger.info("Successfully registered ${jdaCommands.size} guild commands to ${guild.name}")
            }
        }
    }

    /**
     * Delete registered commands from guilds that are not enabled in configuration
     */
    private fun deleteCommandsFromGuilds(
        commands: List<SlashCommand>,
        guilds: List<Guild>
    ) {
        guilds.forEach { guild ->
            guild.retrieveCommands().queue { existingCommands ->
                existingCommands.forEach { existingCommand ->
                    existingCommand
                        .takeIf { commands.any { it.name == existingCommand.name } }?.delete()
                }
            }
        }
    }
}