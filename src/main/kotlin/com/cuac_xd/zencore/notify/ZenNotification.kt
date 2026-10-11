package com.cuac_xd.zencore.notify

import com.cuac_xd.zencore.util.AdventureUtil
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.Locale

/**
 * Synchronized player notification system (Chat + Title + Sound + Particle).
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
            val clean = soundName.trim().lowercase(Locale.ROOT).replace("minecraft:", "").replace("_", ".")
            val sound = runCatching { Registry.SOUNDS.get(NamespacedKey.minecraft(clean)) }.getOrNull()
            if (sound != null) {
                player.playSound(player.location, sound, volume, pitch)
            }
        }

        if (!particleName.isNullOrBlank()) {
            val clean = particleName.trim().uppercase(Locale.ROOT).replace("MINECRAFT:", "")
            val particle = runCatching { Particle.valueOf(clean) }.getOrNull()
            if (particle != null) {
                player.world.spawnParticle(particle, player.location.add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.05)
            }
        }
    }
}
