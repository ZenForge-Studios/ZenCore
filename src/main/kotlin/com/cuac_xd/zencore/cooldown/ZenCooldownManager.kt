package com.cuac_xd.zencore.cooldown

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor de Cooldowns y Timers en memoria y persistentes (sobreviven a reinicios mediante PDC).
 */
class ZenCooldownManager(
    val plugin: Plugin? = null
) {
    private val memoryCooldowns = ConcurrentHashMap<String, Long>()

    /**
     * Aplica un cooldown en memoria.
     */
    fun set(key: String, duration: Duration) {
        val expireAt = System.currentTimeMillis() + duration.toMillis()
        memoryCooldowns[key] = expireAt
    }

    fun set(player: Player, actionId: String, duration: Duration) {
        set("${player.uniqueId}:$actionId", duration)
    }

    /**
     * Comprueba si el cooldown está activo.
     */
    fun has(key: String): Boolean {
        val expire = memoryCooldowns[key] ?: return false
        if (System.currentTimeMillis() >= expire) {
            memoryCooldowns.remove(key)
            return false
        }
        return true
    }

    fun has(player: Player, actionId: String): Boolean = has("${player.uniqueId}:$actionId")

    /**
     * Obtiene los milisegundos restantes del cooldown (0 si expiró).
     */
    fun getRemainingMillis(key: String): Long {
        val expire = memoryCooldowns[key] ?: return 0L
        val diff = expire - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    fun getRemainingMillis(player: Player, actionId: String): Long = getRemainingMillis("${player.uniqueId}:$actionId")

    /**
     * Formatea el tiempo restante en un formato legible (ej. "2h 15m 30s", "45s").
     */
    fun getRemainingFormatted(key: String): String {
        val millis = getRemainingMillis(key)
        if (millis <= 0) return "0s"
        val seconds = millis / 1000
        val s = seconds % 60
        val m = (seconds / 60) % 60
        val h = (seconds / 3600) % 24
        val d = seconds / 86400

        val sb = StringBuilder()
        if (d > 0) sb.append("${d}d ")
        if (h > 0) sb.append("${h}h ")
        if (m > 0) sb.append("${m}m ")
        if (s > 0 || sb.isEmpty()) sb.append("${s}s")
        return sb.toString().trim()
    }

    fun getRemainingFormatted(player: Player, actionId: String): String = getRemainingFormatted("${player.uniqueId}:$actionId")

    // ==========================================
    // COOLDOWNS PERSISTENTES (PDC de Jugador)
    // ==========================================

    fun setPersistent(player: Player, actionId: String, duration: Duration) {
        val p = plugin ?: return set(player, actionId, duration)
        val key = NamespacedKey(p, "cd_$actionId")
        val expireAt = System.currentTimeMillis() + duration.toMillis()
        player.persistentDataContainer.set(key, PersistentDataType.LONG, expireAt)
    }

    fun hasPersistent(player: Player, actionId: String): Boolean {
        val p = plugin ?: return has(player, actionId)
        val key = NamespacedKey(p, "cd_$actionId")
        val expire = player.persistentDataContainer.get(key, PersistentDataType.LONG) ?: return false
        if (System.currentTimeMillis() >= expire) {
            player.persistentDataContainer.remove(key)
            return false
        }
        return true
    }

    fun getRemainingPersistentMillis(player: Player, actionId: String): Long {
        val p = plugin ?: return getRemainingMillis(player, actionId)
        val key = NamespacedKey(p, "cd_$actionId")
        val expire = player.persistentDataContainer.get(key, PersistentDataType.LONG) ?: return 0L
        val diff = expire - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    fun clear(key: String) {
        memoryCooldowns.remove(key)
    }

    fun clear(player: Player, actionId: String) {
        clear("${player.uniqueId}:$actionId")
        plugin?.let {
            player.persistentDataContainer.remove(NamespacedKey(it, "cd_$actionId"))
        }
    }
}
