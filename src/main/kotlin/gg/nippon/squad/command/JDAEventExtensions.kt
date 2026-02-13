package gg.nippon.squad.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object JDAEventExtensions {

    fun SlashCommandInteractionEvent.joinHelper() {
        val member = member!!
        val memberVoice = member.voiceState!!

        if (memberVoice.inAudioChannel()) {
            jda.directAudioController.connect(memberVoice.channel!!)
        }

        reply("Joining channel!").queue()
    }

    fun SlashCommandInteractionEvent.sendEphemeralErrorResponse(message: String) =
        when (isAcknowledged) {
            true -> hook.sendMessage(message).setEphemeral(true).queue()
            false -> reply(message).setEphemeral(true).queue()
        }

    fun SlashCommandInteractionEvent.replyEphemeralResponse(message: String) =
        reply(message).setEphemeral(true).queue()

    fun SlashCommandInteractionEvent.replyResponse(message: String) =
        reply(message).setEphemeral(false).queue()

    fun SlashCommandInteractionEvent.sendResponse(message: String) =
        hook.sendMessage(message).queue()
}