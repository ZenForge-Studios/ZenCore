package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Hook unificado y desacoplado para ZenAuctions dentro del ecosistema ZenCore.
 * Utiliza reflexión sobre `com.cuac_xd.zenauctions.api.ZenAuctionsAPI` para evitar acoplamiento duro.
 */
object ZenAuctionsHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zenauctions.api.ZenAuctionsAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenAuctions")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getApiInstance(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val clazz = Class.forName(API_CLASS_NAME)
            val method = clazz.getMethod("getInstance")
            method.invoke(null)
        }.getOrNull()
    }

    @JvmStatic
    fun getActiveAuctionCount(): Int {
        val api = getApiInstance() ?: return 0
        return runCatching {
            val method = api.javaClass.getMethod("getActiveAuctions")
            val auctions = method.invoke(api) as? Collection<*>
            auctions?.size ?: 0
        }.getOrDefault(0)
    }

    @JvmStatic
    fun cancelAuction(auctionId: String): Boolean {
        val api = getApiInstance() ?: return false
        return runCatching {
            val method = api.javaClass.getMethod("cancelAuction", String::class.java)
            method.invoke(api, auctionId) as? Boolean ?: false
        }.getOrDefault(false)
    }
}
