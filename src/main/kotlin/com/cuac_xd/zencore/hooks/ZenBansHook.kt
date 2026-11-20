package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Hook desacoplado para ZenBans (sanciones, moderación y silencio de chat).
 */
object ZenBansHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenBans")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getPunishmentManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenBans") ?: return null
            val method = plugin.javaClass.getMethod("getPunishmentManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun isChatMuted(): Boolean {
        val pm = getPunishmentManager() ?: return false
        return runCatching {
            val method = pm.javaClass.getMethod("isChatMuted")
            method.invoke(pm) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun setChatMuted(muted: Boolean) {
        val pm = getPunishmentManager() ?: return
        runCatching {
            val method = pm.javaClass.getMethod("setChatMuted", Boolean::class.javaPrimitiveType)
            method.invoke(pm, muted)
        }
    }

    @JvmStatic
    fun unban(playerUuid: UUID): CompletableFuture<Boolean> {
        val pm = getPunishmentManager() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = pm.javaClass.getMethod("unban", UUID::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(pm, playerUuid) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }

    @JvmStatic
    fun unmute(playerUuid: UUID): CompletableFuture<Boolean> {
        val pm = getPunishmentManager() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = pm.javaClass.getMethod("unmute", UUID::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(pm, playerUuid) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }
}
