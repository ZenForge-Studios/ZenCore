package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenRankups (rangos por economía/estadísticas, prestigios y auto-rankup).
 */
object ZenRankupsHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenRankups")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getRanksManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenRankups") ?: return null
            val method = plugin.javaClass.getMethod("getRanksManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    private fun getPrestigeManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenRankups") ?: return null
            val method = plugin.javaClass.getMethod("getPrestigeManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun getPlayerRank(player: OfflinePlayer): String? = getPlayerRank(player.uniqueId)

    @JvmStatic
    fun getPlayerRank(playerUuid: UUID): String? {
        val rm = getRanksManager() ?: return null
        return runCatching {
            val method = rm.javaClass.getMethod("getPlayerRank", UUID::class.java)
            method.invoke(rm, playerUuid)?.toString()
        }.getOrNull()
    }

    @JvmStatic
    fun getPlayerPrestige(player: OfflinePlayer): String? = getPlayerPrestige(player.uniqueId)

    @JvmStatic
    fun getPlayerPrestige(playerUuid: UUID): String? {
        val pm = getPrestigeManager() ?: return null
        return runCatching {
            val method = pm.javaClass.getMethod("getPlayerPrestige", UUID::class.java)
            method.invoke(pm, playerUuid)?.toString()
        }.getOrNull()
    }
}
