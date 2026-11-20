package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.UUID

/**
 * Hook desacoplado para ZenSpawner (spawners apilables, drop engine y boosters de spawner).
 */
object ZenSpawnerHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenSpawner")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getSpawnerService(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenSpawner") ?: return null
            val method = plugin.javaClass.getMethod("getSpawnerService")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun hasSpawnerAt(location: Location): Boolean {
        val ss = getSpawnerService() ?: return false
        return runCatching {
            val method = ss.javaClass.getMethod("getSpawnerAt", Location::class.java)
            method.invoke(ss, location) != null
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getSpawnersCount(ownerUuid: UUID): Int {
        val ss = getSpawnerService() ?: return 0
        return runCatching {
            val method = ss.javaClass.getMethod("getSpawnersByOwner", UUID::class.java)
            val list = method.invoke(ss, ownerUuid) as? Collection<*>
            list?.size ?: 0
        }.getOrDefault(0)
    }

    @JvmStatic
    fun getTotalSpawnersCount(): Int {
        val ss = getSpawnerService() ?: return 0
        return runCatching {
            val method = ss.javaClass.getMethod("getAllSpawners")
            val list = method.invoke(ss) as? Collection<*>
            list?.size ?: 0
        }.getOrDefault(0)
    }
}
