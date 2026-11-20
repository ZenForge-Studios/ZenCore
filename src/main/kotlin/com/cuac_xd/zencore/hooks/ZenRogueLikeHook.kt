package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenRogueLike (mazmorras procedurales, runs, clases y progresión roguelike).
 */
object ZenRogueLikeHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zenroguelike.api.ZenRogueLikeAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenRogueLike")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getRunManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val clazz = Class.forName(API_CLASS_NAME)
            val method = clazz.getMethod("getRunManager")
            method.invoke(null)
        }.getOrNull()
    }

    @JvmStatic
    fun isInRun(player: Player): Boolean = isInRun(player.uniqueId)

    @JvmStatic
    fun isInRun(playerUuid: UUID): Boolean {
        val rm = getRunManager() ?: return false
        return runCatching {
            val method = rm.javaClass.getMethod("isPlayerInRun", UUID::class.java)
            method.invoke(rm, playerUuid) as? Boolean ?: false
        }.getOrDefault(false)
    }
}
