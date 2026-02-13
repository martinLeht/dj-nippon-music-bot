package gg.nippon.squad.command

import dev.arbjerg.lavalink.client.LavalinkClient
import gg.nippon.squad.audio.CustomAudioLoader
import gg.nippon.squad.audio.GuildAudioManager
import gg.nippon.squad.command.JDAEventExtensions.joinHelper
import gg.nippon.squad.error.CommandExecutionException
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.LoggerFactory

class PlayCommand(
    override val description: String
) : SlashCommand {

    enum class Option(val description: String) {
        IDENTIFIER("Option to get track by URL (e.g. SoundCloud or Youtube) or youtube video ID."),
        SEARCH("Option to search track from youtube.");

        fun getName(): String = this.name.lowercase()

        fun toOptionData(): OptionData = OptionData(OptionType.STRING, getName(), description)
    }

    override val commandType: CommandType = CommandType.PLAY
    override val options: List<OptionData> = listOf(
        Option.IDENTIFIER.toOptionData(),
        Option.SEARCH.toOptionData(),
    )
    private val logger = LoggerFactory.getLogger(PlayCommand::class.java)

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        lavalinkClient: LavalinkClient,
    ) = throw NotImplementedError("Play command without guild not implemented.")

    override suspend fun execute(
        event: SlashCommandInteractionEvent,
        guild: Guild,
        lavalinkClient: LavalinkClient,
        audioManager: GuildAudioManager?,
    ) {
        logger.info("Executing play command...")
        if (audioManager == null) throw CommandExecutionException("No audio manager available.")
        if (!guild.selfMember.voiceState!!.inAudioChannel()) {
            event.joinHelper()
        } else {
            event.deferReply(false).queue()
        }

        val (option, value) = event.resolveOptionAndValue()
        val parsedValue = when(option) {
            Option.IDENTIFIER -> value
            Option.SEARCH -> "ytmsearch:$value"
        }
        val guildId = guild.idLong
        val link = lavalinkClient.getOrCreateLink(guildId)

        link.loadItem(parsedValue).subscribe(CustomAudioLoader(event, audioManager))
    }

    private fun SlashCommandInteractionEvent.resolveOptionAndValue(): Pair<Option, String> =
        try {
            Option.entries.firstNotNullOf { option ->
                getOption(option.getName())?.let { option to it.asString }
            }
        } catch (ex: Exception) {
            throw CommandExecutionException("Could not resolve command option.", ex)
        }
}