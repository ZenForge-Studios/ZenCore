package com.cuac_xd.zencore.util

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.Sound
import java.util.Locale

/**
 * Normalizes and safely maps sounds, particles, and materials across Paper 1.21+ subversions
 * avoiding exceptions when legacy or alternative names are provided in YAML configs.
 */
object MinecraftAdapter {

    @JvmStatic
    fun resolveSound(raw: String?): Sound? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.trim().lowercase(Locale.ROOT).replace("minecraft:", "")

        // 1. Exact uppercase match
        val upper = clean.uppercase(Locale.ROOT)
        val direct = runCatching { Sound.valueOf(upper) }.getOrNull()
        if (direct != null) return direct

        // 2. Paper Registry lookup with dots
        val dotKey = clean.replace("_", ".")
        val reg = runCatching { Registry.SOUNDS.get(NamespacedKey.minecraft(dotKey)) }.getOrNull()
        if (reg != null) return reg

        // 3. Fallback common aliases
        val alias = when (upper) {
            "LEVEL_UP", "PLAYER_LEVELUP" -> Sound.ENTITY_PLAYER_LEVELUP
            "ORB_PICKUP", "EXP_PICKUP" -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP
            "ITEM_BREAK" -> Sound.ENTITY_ITEM_BREAK
            "EXPLODE", "GENERIC_EXPLODE" -> Sound.ENTITY_GENERIC_EXPLODE
            "HURT" -> Sound.ENTITY_PLAYER_HURT
            "BOW_HIT" -> Sound.ENTITY_ARROW_HIT_PLAYER
            "SUCCESS", "DING" -> Sound.BLOCK_NOTE_BLOCK_PLING
            else -> null
        }
        return alias
    }

    @JvmStatic
    fun resolveParticle(raw: String?): Particle? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.trim().uppercase(Locale.ROOT).replace("MINECRAFT:", "")
        val direct = runCatching { Particle.valueOf(clean) }.getOrNull()
        if (direct != null) return direct

        return when (clean) {
            "REDSTONE", "DUST" -> Particle.DUST
            "SMOKE", "SMOKE_NORMAL" -> Particle.SMOKE
            "CRIT_MAGIC", "ENCHANTED_HIT" -> Particle.ENCHANTED_HIT
            "EXPLOSION_NORMAL", "POOF" -> Particle.POOF
            "MAGIC_CRIT" -> Particle.ENCHANTED_HIT
            "FIREWORKS_SPARK" -> Particle.FIREWORK
            "VILLAGER_HAPPY", "HAPPY_VILLAGER" -> Particle.HAPPY_VILLAGER
            "VILLAGER_ANGRY", "ANGRY_VILLAGER" -> Particle.ANGRY_VILLAGER
            "HEART" -> Particle.HEART
            "FLAME" -> Particle.FLAME
            "SOUL_FIRE_FLAME" -> Particle.SOUL_FIRE_FLAME
            else -> null
        }
    }

    @JvmStatic
    fun resolveMaterial(raw: String?): Material? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.trim().uppercase(Locale.ROOT).replace("MINECRAFT:", "")
        val direct = Material.matchMaterial(clean)
        if (direct != null) return direct

        return when (clean) {
            "HEAD", "PLAYER_HEAD" -> Material.PLAYER_HEAD
            "SKULL" -> Material.SKELETON_SKULL
            "WORKBENCH", "CRAFTING" -> Material.CRAFTING_TABLE
            "ANVIL" -> Material.ANVIL
            "CHEST" -> Material.CHEST
            "ENDERCHEST" -> Material.ENDER_CHEST
            else -> null
        }
    }
}
