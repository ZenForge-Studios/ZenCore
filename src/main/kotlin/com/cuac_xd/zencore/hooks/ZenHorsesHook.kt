package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Hook desacoplado para ZenHorses dentro del ecosistema ZenCore.
 * Interactúa de forma segura con `com.cuac_xd.zenhorses.api.ZenHorsesAPI`.
 */
object ZenHorsesHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zenhorses.api.ZenHorsesAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenHorses")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getApiClass(): Class<*>? {
        if (!isInstalled()) return null
        return runCatching { Class.forName(API_CLASS_NAME) }.getOrNull()
    }

    @JvmStatic
    fun summonHorse(player: Player, horseUuid: UUID): CompletableFuture<Boolean> {
        val clazz = getApiClass() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = clazz.getMethod("summonHorse", Player::class.java, UUID::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, player, horseUuid) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }

    @JvmStatic
    fun storeHorse(player: Player, horseUuid: UUID): CompletableFuture<Boolean> {
        val clazz = getApiClass() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = clazz.getMethod("storeHorse", Player::class.java, UUID::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, player, horseUuid) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }

    @JvmStatic
    fun renameHorse(player: Player, horseUuid: UUID, newName: String): CompletableFuture<Boolean> {
        val clazz = getApiClass() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = clazz.getMethod("renameHorse", Player::class.java, UUID::class.java, String::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, player, horseUuid, newName) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }
}
