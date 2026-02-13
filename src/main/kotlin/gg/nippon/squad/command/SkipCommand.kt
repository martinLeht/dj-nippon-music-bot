package gg.nippon.squad.command

import dev.arbjerg.lavalink.client.LavalinkClient
import gg.nippon.squad.audio.CustomAudioLoader
import gg.nippon.squad.audio.GuildAudioManager
import gg.nippon.squad.command.JDAEventExtensions.replyResponse
import gg.nippon.squad.command.JDAEventExtensions.sendResponse
import gg.nippon.squad.error.CommandExecutionException
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

class SkipCommand(
    override val description: String
) : SlashCommand {
    override val commandType: CommandType = CommandType.SKIP
    private val logger = LoggerFactory.getLogger(SkipCommand::class.java)

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        lavalinkClient: LavalinkClient
    ) = throw NotImplementedError("Skip command without guild not implemented.")

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        guild: Guild,
        lavalinkClient: LavalinkClient,
        audioManager: GuildAudioManager?,
    ) {
        logger.info("Executing skip command...")
        if (audioManager == null) throw CommandExecutionException("No audio manager available.")
        audioManager.scheduler.startNextTrack()
        event.replyResponse("Skipping current track...")
    }
}