package gg.nippon.squad.command

import dev.arbjerg.lavalink.client.LavalinkClient
import gg.nippon.squad.audio.GuildAudioManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface SlashCommand {
    val commandType: CommandType
    val description: String
    val options: List<OptionData>
        get() = emptyList()

    val name: String
        get() = commandType.name.lowercase().replace("_", "-")

    val isGuildOnly: Boolean
        get() = false

    suspend fun execute(event: SlashCommandInteractionEvent, lavalinkClient: LavalinkClient)

    suspend fun execute(event: SlashCommandInteractionEvent, guild: Guild, lavalinkClient: LavalinkClient, audioManager: GuildAudioManager? = null)

    fun toJDACommandData(): SlashCommandData {
        return Commands.slash(name, description).addOptions(options)
    }
}