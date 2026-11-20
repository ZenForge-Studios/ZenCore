package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenQuests (misiones diarias, hitos, etapas y recompensas).
 */
object ZenQuestsHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenQuests")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    @JvmStatic
    fun hasActiveProgress(player: Player): Boolean = hasActiveProgress(player.uniqueId)

    @JvmStatic
    fun hasActiveProgress(playerUuid: UUID): Boolean {
        if (!isInstalled()) return false
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenQuests") ?: return false
            val field = plugin.javaClass.getMethod("getActiveProgress")
            val progressMap = field.invoke(plugin) as? Map<*, *>
            progressMap?.containsKey(playerUuid) == true
        }.getOrDefault(false)
    }
}
