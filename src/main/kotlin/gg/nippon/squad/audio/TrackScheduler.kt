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
        this.guildAudioManager.player
            ?.let { player ->
                if (player.track == null) {
                    this.startTrack(track)
                } else {
                    this.queue.offer(track)
                }
            } ?: this.startTrack(track)
    }

    fun enqueuePlaylist(tracks: List<Track?>) {
        this.queue.addAll(tracks)

        this.guildAudioManager.player
            ?.let{ player ->
                if (player.track == null) {
                    this.startTrack(this.queue.poll())
                }
            } ?: this.startTrack(this.queue.poll())
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
        val nextTrack: Track? = this.queue.poll()

        if (nextTrack != null) {
            this.startTrack(nextTrack)
        }
    }

    private fun startTrack(track: Track?) {
        this.guildAudioManager.link?.createOrUpdatePlayer()?.setTrack(track)?.setVolume(35)?.subscribe()
    }
}