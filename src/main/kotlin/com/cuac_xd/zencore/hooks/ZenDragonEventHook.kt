package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity

/**
 * Hook desacoplado para ZenDragonEvent (evento comunitario del Ender Dragón custom).
 */
object ZenDragonEventHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenDragonEvent")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getDragonManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenDragonEvent") ?: return null
            val method = plugin.javaClass.getMethod("getDragonManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun isCustomDragon(entity: Entity): Boolean {
        val dm = getDragonManager() ?: return false
        return runCatching {
            val method = dm.javaClass.getMethod("isCustomDragon", Entity::class.java)
            method.invoke(dm, entity) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun spawnDragon(location: Location): Boolean {
        val dm = getDragonManager() ?: return false
        return runCatching {
            val method = dm.javaClass.getMethod("spawnDragon", Location::class.java)
            method.invoke(dm, location) != null
        }.getOrDefault(false)
    }

    @JvmStatic
    fun forceStop(): Boolean {
        val dm = getDragonManager() ?: return false
        return runCatching {
            val method = dm.javaClass.getMethod("forceStop")
            method.invoke(dm)
            true
        }.getOrDefault(false)
    }
}
