package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit

/**
 * Hook desacoplado para ZenFairy (sistema de Fairy Souls coleccionables y stats pasivas).
 */
object ZenFairyHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenFairy")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getSoulManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenFairy") ?: return null
            val method = plugin.javaClass.getMethod("getSoulManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun getTotalSouls(): Int {
        val sm = getSoulManager() ?: return 0
        return runCatching {
            val method = sm.javaClass.getMethod("getTotalSouls")
            (method.invoke(sm) as? Number)?.toInt() ?: 0
        }.getOrDefault(0)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun getSoulTypeIds(): Set<String> {
        val sm = getSoulManager() ?: return emptySet()
        return runCatching {
            val method = sm.javaClass.getMethod("getSoulTypeIds")
            method.invoke(sm) as? Set<String> ?: emptySet()
        }.getOrDefault(emptySet())
    }
}
