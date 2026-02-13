package gg.nippon.squad.audio

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.client.player.TrackLoaded
import gg.nippon.squad.command.JDAEventExtensions.sendResponse
import gg.nippon.squad.track.UserTrackData
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class CustomAudioLoader(
    private val event: SlashCommandInteractionEvent,
    private val audioManager: GuildAudioManager,
) : AbstractAudioLoadResultHandler() {

    override fun ontrackLoaded(result: TrackLoaded) {
        val track: Track = result.track
        track.setUserData(UserTrackData(event.user.idLong))
        if (audioManager.scheduler.queue.size >= 50) {
            event.sendResponse("Too many tracks, can have max 50 tracks in a queue!")
        } else {
            audioManager.scheduler.enqueue(track)
            event.sendResponse("Added to queue: " + track.info.title + "\nRequested by: <@" + event.user.idLong + '>')
        }
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {
        val trackCount: Int = result.tracks.size
        if (audioManager.scheduler.queue.size + trackCount > 50) {
            event.sendResponse("Too many tracks, can have max 50 tracks in a queue!")
        } else {
            event.sendResponse("Added " + trackCount + " tracks to the queue from " + result.info.name + "!")
            audioManager.scheduler.enqueuePlaylist(result.tracks)
        }
    }

    override fun onSearchResultLoaded(result: SearchResult) {
        if (result.tracks.isEmpty()) {
            event.sendResponse("No tracks found!")
        } else {
            if (audioManager.scheduler.queue.size >= 50) {
                event.sendResponse("Too many tracks, can have max 50 tracks in a queue!")
            } else {
                val firstTrack: Track = result.tracks.first()
                event.sendResponse("Adding to queue: " + firstTrack.info.title)
                audioManager.scheduler.enqueue(firstTrack)
            }
        }

    }

    override fun noMatches() {
        event.sendResponse("No matches found for your input!")
    }

    override fun loadFailed(result: LoadFailed) {
        event.sendResponse("Failed to load track! " + result.exception.message)
    }
}