package gg.nippon.squad.audio

import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.protocol.v4.Message
import org.slf4j.LoggerFactory
import java.util.LinkedList
import java.util.Queue


class TrackScheduler(private val guildAudioManager: GuildAudioManager) {
    private val logger = LoggerFactory.getLogger(GuildAudioManager::class.java)
    val queue: Queue<Track?> = LinkedList<Track?>()

    fun enqueue(track: Track?) {
        guildAudioManager.player
            ?.let { player ->
                if (player.track == null) {
                    startTrack(track)
                } else {
                    queue.offer(track)
                }
            } ?: startTrack(track)
    }

    fun enqueuePlaylist(tracks: List<Track?>) {
        queue.addAll(tracks)

        guildAudioManager.player
            ?.let{ player ->
                if (player.track == null) {
                    startTrack(queue.poll())
                }
            } ?: startTrack(queue.poll())
    }

    fun onTrackStart(track: Track) {
        logger.info("Track started: " + track.info.title)
    }

    fun onTrackEnd(lastTrack: Track?, endReason: Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            startNextTrack()
        }
    }

    fun startNextTrack() {
        val nextTrack: Track? = queue.poll()

        if (nextTrack != null) {
            startTrack(nextTrack)
        } else {
            guildAudioManager.player?.stopTrack()
        }
    }

    private fun startTrack(track: Track?) {
        guildAudioManager.link?.createOrUpdatePlayer()?.setTrack(track)?.setVolume(35)?.subscribe()
    }
}