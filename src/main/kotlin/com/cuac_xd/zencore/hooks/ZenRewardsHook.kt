package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenRewards (recompensas diarias, rachas, calendario y recompensas por tiempo).
 */
object ZenRewardsHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenRewards")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getPlayerDataManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenRewards") ?: return null
            val method = plugin.javaClass.getMethod("getPlayerDataManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun getStreak(player: OfflinePlayer): Int = getStreak(player.uniqueId)

    @JvmStatic
    fun getStreak(playerUuid: UUID): Int {
        val pdm = getPlayerDataManager() ?: return 0
        return runCatching {
            val method = pdm.javaClass.getMethod("getPlayerDataIfPresent", UUID::class.java)
            val data = method.invoke(pdm, playerUuid) ?: return 0
            val streakMethod = data.javaClass.getMethod("getCurrentStreak")
            (streakMethod.invoke(data) as? Number)?.toInt() ?: 0
        }.getOrDefault(0)
    }
}
