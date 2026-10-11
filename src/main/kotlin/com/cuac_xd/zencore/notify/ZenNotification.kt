package com.cuac_xd.zencore.notify

import com.cuac_xd.zencore.util.AdventureUtil
import com.cuac_xd.zencore.util.MinecraftAdapter
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * High-impact synchronized player notification system (Chat + Title + Sound + Particle).
 */
object ZenNotification {

    @JvmStatic
    fun send(
        player: Player,
        message: String? = null,
        title: String? = null,
        subtitle: String? = null,
        soundName: String? = "ENTITY_PLAYER_LEVELUP",
        particleName: String? = null,
        volume: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        if (!message.isNullOrBlank()) {
            AdventureUtil.sendMessage(player, message)
        }

        if (!title.isNullOrBlank() || !subtitle.isNullOrBlank()) {
            AdventureUtil.sendTitle(player, title ?: "", subtitle ?: "", 10, 40, 10)
        }

        if (!soundName.isNullOrBlank()) {
            val sound = MinecraftAdapter.resolveSound(soundName)
            if (sound != null) {
                player.playSound(player.location, sound, volume, pitch)
            }
        }

        if (!particleName.isNullOrBlank()) {
            val particle = MinecraftAdapter.resolveParticle(particleName)
            if (particle != null) {
                player.world.spawnParticle(particle, player.location.add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.05)
            }
        }
    }
}
