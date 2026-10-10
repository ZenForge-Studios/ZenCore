package com.cuac_xd.zencore.data

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Lightweight asynchronous player data cache service.
 */
object ZenPlayerDataService {
    private val dataCache = ConcurrentHashMap<UUID, ConcurrentHashMap<String, Any>>()

    @JvmStatic
    fun set(uuid: UUID, key: String, value: Any) {
        dataCache.computeIfAbsent(uuid) { ConcurrentHashMap() }[key] = value
    }

    @JvmStatic
    fun set(player: Player, key: String, value: Any) {
        set(player.uniqueId, key, value)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> get(uuid: UUID, key: String, default: T? = null): T? {
        val userMap = dataCache[uuid] ?: return default
        return (userMap[key] as? T) ?: default
    }

    @JvmStatic
    fun <T> get(player: Player, key: String, default: T? = null): T? {
        return get(player.uniqueId, key, default)
    }

    @JvmStatic
    fun remove(uuid: UUID, key: String) {
        dataCache[uuid]?.remove(key)
    }

    @JvmStatic
    fun clear(uuid: UUID) {
        dataCache.remove(uuid)
    }
}
