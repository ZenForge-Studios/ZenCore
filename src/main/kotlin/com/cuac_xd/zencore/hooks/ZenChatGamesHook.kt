package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit

/**
 * Hook desacoplado para ZenChatGames (juegos en chat, trivias, matemáticas y recompensas).
 */
object ZenChatGamesHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenChatGames")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getGameManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenChatGames") ?: return null
            val method = plugin.javaClass.getMethod("getGameManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun hasActiveGame(): Boolean {
        val gm = getGameManager() ?: return false
        return runCatching {
            val method = gm.javaClass.getMethod("getActiveSession")
            method.invoke(gm) != null
        }.getOrDefault(false)
    }

    @JvmStatic
    fun startRandomGame(): Boolean {
        val gm = getGameManager() ?: return false
        return runCatching {
            val method = gm.javaClass.getMethod("startRandomGame")
            method.invoke(gm) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun stopActiveGame(): Boolean {
        val gm = getGameManager() ?: return false
        return runCatching {
            val method = gm.javaClass.getMethod("stopActiveGame")
            method.invoke(gm) as? Boolean ?: false
        }.getOrDefault(false)
    }
}
