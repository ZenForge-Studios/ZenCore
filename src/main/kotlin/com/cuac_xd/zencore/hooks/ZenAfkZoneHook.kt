package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenAfkZone (zonas AFK con recompensas y tracking de tiempo).
 */
object ZenAfkZoneHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenAfkZone")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getZoneManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenAfkZone") ?: return null
            val method = plugin.javaClass.getMethod("getZoneManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun isInAfkZone(player: Player): Boolean = isInAfkZone(player.uniqueId)

    @JvmStatic
    fun isInAfkZone(playerUuid: UUID): Boolean {
        val zm = getZoneManager() ?: return false
        return runCatching {
            val method = zm.javaClass.getMethod("getSession", UUID::class.java)
            method.invoke(zm, playerUuid) != null
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getZoneNameAt(location: Location): String? {
        val zm = getZoneManager() ?: return null
        return runCatching {
            val method = zm.javaClass.getMethod("getZoneAtLocation", Location::class.java)
            val zone = method.invoke(zm, location) ?: return null
            val nameMethod = zone.javaClass.getMethod("getName")
            nameMethod.invoke(zone)?.toString()
        }.getOrNull()
    }
}
