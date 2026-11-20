package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Hook desacoplado para ZenBlockRegen dentro del ecosistema ZenCore.
 * Controla la regeneración de bloques minados, regiones protegidas y modo bypass.
 */
object ZenBlockRegenHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenBlockRegen")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getPluginInstance(): Any? {
        if (!isInstalled()) return null
        return Bukkit.getPluginManager().getPlugin("ZenBlockRegen")
    }

    @JvmStatic
    fun isPlayerInBypass(player: Player): Boolean {
        val plugin = getPluginInstance() ?: return false
        return runCatching {
            val method = plugin.javaClass.getMethod("isPlayerInBypass", Player::class.java)
            method.invoke(plugin, player) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun isRegenerating(location: Location): Boolean {
        val plugin = getPluginInstance() ?: return false
        return runCatching {
            val regenMgrMethod = plugin.javaClass.getMethod("getRegenManager")
            val regenMgr = regenMgrMethod.invoke(plugin) ?: return false
            val isRegenMethod = regenMgr.javaClass.getMethod("isRegenerating", Location::class.java)
            isRegenMethod.invoke(regenMgr, location) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun cancelRegen(location: Location): Boolean {
        val plugin = getPluginInstance() ?: return false
        return runCatching {
            val regenMgrMethod = plugin.javaClass.getMethod("getRegenManager")
            val regenMgr = regenMgrMethod.invoke(plugin) ?: return false
            val cancelMethod = regenMgr.javaClass.getMethod("cancelRegen", Location::class.java)
            cancelMethod.invoke(regenMgr, location) != null
        }.getOrDefault(false)
    }

    @JvmStatic
    fun isInRegion(location: Location): Boolean {
        val plugin = getPluginInstance() ?: return false
        return runCatching {
            val regionMgrMethod = plugin.javaClass.getMethod("getRegionManager")
            val regionMgr = regionMgrMethod.invoke(plugin) ?: return false
            val getRegionMethod = regionMgr.javaClass.getMethod("getRegionAt", Location::class.java)
            getRegionMethod.invoke(regionMgr, location) != null
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getRegionName(location: Location): String? {
        val plugin = getPluginInstance() ?: return null
        return runCatching {
            val regionMgrMethod = plugin.javaClass.getMethod("getRegionManager")
            val regionMgr = regionMgrMethod.invoke(plugin) ?: return null
            val getRegionMethod = regionMgr.javaClass.getMethod("getRegionAt", Location::class.java)
            val region = getRegionMethod.invoke(regionMgr, location) ?: return null
            val nameField = region.javaClass.getMethod("getName")
            nameField.invoke(region)?.toString()
        }.getOrNull()
    }

    @JvmStatic
    fun getMineName(location: Location): String? {
        val plugin = getPluginInstance() ?: return null
        return runCatching {
            val mineMgrMethod = plugin.javaClass.getMethod("getMineManager")
            val mineMgr = mineMgrMethod.invoke(plugin) ?: return null
            val getMineMethod = mineMgr.javaClass.getMethod("getMineAt", Location::class.java)
            val mine = getMineMethod.invoke(mineMgr, location) ?: return null
            val nameMethod = mine.javaClass.getMethod("getName")
            nameMethod.invoke(mine)?.toString()
        }.getOrNull()
    }
}
