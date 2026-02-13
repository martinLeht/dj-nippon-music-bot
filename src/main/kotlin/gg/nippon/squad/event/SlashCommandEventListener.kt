package gg.nippon.squad.event

import gg.nippon.squad.command.CommandExecutionHandler
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SlashCommandEventListener(
    private val commandExecutionHandler: CommandExecutionHandler,
) : ListenerAdapter() {
    /**
     * Handle slash command interactions.
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        // Process slash commands using coroutines
        runBlocking {
            commandExecutionHandler.handleGuildCommand(event)
        }
    }
}