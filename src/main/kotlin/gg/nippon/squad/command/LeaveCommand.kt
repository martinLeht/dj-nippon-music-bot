package gg.nippon.squad.command

import dev.arbjerg.lavalink.client.LavalinkClient
import gg.nippon.squad.audio.GuildAudioManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

class LeaveCommand(
    override val description: String
) : SlashCommand {
    override val commandType: CommandType = CommandType.LEAVE
    private val logger = LoggerFactory.getLogger(LeaveCommand::class.java)

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        lavalinkClient: LavalinkClient
    ) = throw NotImplementedError("Leave command not implemented.")

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        guild: Guild,
        lavalinkClient: LavalinkClient,
        audioManager: GuildAudioManager?,
    ) {
        logger.info("Executing leave command...")

        // Disconnecting automatically destroys the player
        audioManager?.scheduler?.queue?.clear()
        event.jda.directAudioController.disconnect(guild)
        event.reply("Leaving your channel!").queue()
    }
}