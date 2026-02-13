package gg.nippon.squad.command

import dev.arbjerg.lavalink.client.LavalinkClient
import gg.nippon.squad.audio.GuildAudioManager
import gg.nippon.squad.command.JDAEventExtensions.replyResponse
import gg.nippon.squad.error.CommandExecutionException
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

class QueueComand(
    override val description: String
) : SlashCommand {
    override val commandType: CommandType = CommandType.QUEUE
    private val logger = LoggerFactory.getLogger(SkipCommand::class.java)

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        lavalinkClient: LavalinkClient
    ) = throw NotImplementedError("Queue command without guild not implemented.")

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        guild: Guild,
        lavalinkClient: LavalinkClient,
        audioManager: GuildAudioManager?,
    ) {
        logger.info("Executing queue command...")
        if (audioManager == null) throw CommandExecutionException("No audio manager available.")
        if (audioManager.scheduler.queue.isEmpty()) {
            event.replyResponse("Queue is empty.")
        } else {
            val queueSb = StringBuilder()
            audioManager.scheduler.queue.forEach { track ->
                queueSb.append("${track?.info?.title}\n")
            }
            event.replyResponse("Current queue:\n$queueSb")
        }
    }
}