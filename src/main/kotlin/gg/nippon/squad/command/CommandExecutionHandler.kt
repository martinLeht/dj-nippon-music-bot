package gg.nippon.squad.command

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.event.EmittedEvent
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.event.TrackStartEvent
import gg.nippon.squad.audio.GuildAudioManager
import gg.nippon.squad.command.JDAEventExtensions.sendEphemeralErrorResponse
import gg.nippon.squad.command.JDAEventExtensions.replyEphemeralResponse
import gg.nippon.squad.error.BotException
import gg.nippon.squad.error.CommandExecutionException
import gg.nippon.squad.error.DiscordApiException
import gg.nippon.squad.error.InvalidInputException
import gg.nippon.squad.error.PermissionException
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory
import java.util.Collections

class CommandExecutionHandler(
    private val enabledGuildIds: Set<String>,
    private val lavalinkClient: LavalinkClient,
) {
    private val logger = LoggerFactory.getLogger(CommandExecutionHandler::class.java)
    val guildCommandsByType: Map<CommandType, SlashCommand> = mapOf(
        CommandType.PLAY to PlayCommand("Play a music track!"),
        CommandType.LEAVE to LeaveCommand("Leave voice channel."),
        CommandType.SKIP to SkipCommand("Skip a track from current queue."),
        CommandType.QUEUE to QueueComand("List current queue."),
    )
    val audioManagersByGuildId: MutableMap<Long, GuildAudioManager> = mutableMapOf()
    init {
        lavalinkClient.on<TrackStartEvent>()
            .subscribe { event ->
                audioManagersByGuildId[event.guildId]?.scheduler?.onTrackStart(event.track)
            }
        lavalinkClient.on<TrackEndEvent>()
            .subscribe { event ->
                audioManagersByGuildId[event.guildId]?.scheduler?.onTrackEnd(event.track, event.endReason)
            }
    }

    suspend fun handleGuildCommand(
        event: SlashCommandInteractionEvent,
    ) {
        val guild = event.guild
        val guildId = guild?.id
        if (event.isGuildCommand && guildId != null && enabledGuildIds.any { it == guildId }) {
            val commandType = CommandType.valueOf(event.name.uppercase())
            val command = guildCommandsByType[commandType]

            if (command != null) {
                logger.info("Executing slash command: ${commandType.name}, User: ${event.user.name}")
                try {
                    val audioManager = getOrCreateAudioManager(guild.idLong)
                    command.execute(event, guild, lavalinkClient, audioManager)
                } catch (e: InvalidInputException) {
                    logger.info("Invalid input for slash command $commandType: ${e.message}")
                    event.sendEphemeralErrorResponse(e.getUserFriendlyMessage())
                } catch (e: PermissionException) {
                    logger.info("Permission denied for user ${event.user.id} on slash command $commandType: ${e.message}")
                    event.sendEphemeralErrorResponse(e.getUserFriendlyMessage())
                } catch (e: CommandExecutionException) {
                    logger.error("Error executing slash command $commandType", e)
                    event.sendEphemeralErrorResponse(e.getUserFriendlyMessage())
                } catch (e: DiscordApiException) {
                    logger.error("Discord API error in slash command $commandType", e)
                    event.sendEphemeralErrorResponse(e.getUserFriendlyMessage())
                } catch (e: BotException) {
                    logger.error("Bot exception in slash command $commandType", e)
                    event.sendEphemeralErrorResponse(e.getUserFriendlyMessage())
                } catch (e: Exception) {
                    logger.error("Unexpected error executing slash command $commandType", e)
                    val exception = CommandExecutionException("An unexpected error occurred", e, commandType)
                    event.sendEphemeralErrorResponse(exception.getUserFriendlyMessage())
                }
            } else {
                logger.warn("Received unknown slash command: $commandType")
                if (!event.isAcknowledged) {
                    event.replyEphemeralResponse("Unknown command: $commandType")
                }
            }
        } else {
            logger.warn("Guild validation failed!")
            event.sendEphemeralErrorResponse("Guild validation failed!")
        }
    }

    private fun getOrCreateAudioManager(guildId: Long): GuildAudioManager =
        audioManagersByGuildId.getOrPut(guildId) { GuildAudioManager(guildId, lavalinkClient) }
}