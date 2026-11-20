package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenForges (forja personalizada, hornos avanzados y progresión de herrería).
 */
object ZenForgesHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenForges")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getForgeManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenForges") ?: return null
            val method = plugin.javaClass.getMethod("getForgeManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun getActiveProcessesCount(playerUuid: UUID): Int {
        val fm = getForgeManager() ?: return 0
        return runCatching {
            val method = fm.javaClass.getMethod("getActiveProcessesCount", UUID::class.java)
            (method.invoke(fm, playerUuid) as? Number)?.toInt() ?: 0
        }.getOrDefault(0)
    }

    @JvmStatic
    fun awardXp(player: Player, xp: Double): Boolean {
        val fm = getForgeManager() ?: return false
        return runCatching {
            val method = fm.javaClass.getMethod("awardXp", Player::class.java, Double::class.javaPrimitiveType)
            method.invoke(fm, player, xp)
            true
        }.getOrDefault(false)
    }
}
