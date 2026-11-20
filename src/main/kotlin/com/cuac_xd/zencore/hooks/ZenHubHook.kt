package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenHub (gestión de lobby, hotbar selector, parkour infinito y spawn).
 */
object ZenHubHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenHub")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getParkourManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenHub") ?: return null
            val method = plugin.javaClass.getMethod("getParkourManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun isParkourActive(player: Player): Boolean = isParkourActive(player.uniqueId)

    @JvmStatic
    fun isParkourActive(playerUuid: UUID): Boolean {
        val pm = getParkourManager() ?: return false
        return runCatching {
            val method = pm.javaClass.getMethod("isParkourPlayer", UUID::class.java)
            method.invoke(pm, playerUuid) as? Boolean ?: false
        }.getOrDefault(false)
    }
}
