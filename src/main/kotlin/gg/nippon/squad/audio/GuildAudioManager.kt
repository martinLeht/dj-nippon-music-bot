package gg.nippon.squad.audio

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.LavalinkPlayer

class GuildAudioManager(private val guildId: Long, private val lavalink: LavalinkClient) {
    val scheduler: TrackScheduler = TrackScheduler(this)

    fun stop() {
        this.scheduler.queue.clear()
        this.player?.setPaused(false)?.setTrack(null)?.subscribe()
    }

    val link: Link?
        get() = lavalink.getLinkIfCached(this.guildId)

    val player: LavalinkPlayer?
        get() = this.link?.cachedPlayer
}