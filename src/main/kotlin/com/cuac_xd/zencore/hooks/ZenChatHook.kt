package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Hook desacoplado para ZenChat (canales de chat, prefijos, menciones y chat bubbles).
 */
object ZenChatHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenChat")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getChannelManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenChat") ?: return null
            val method = plugin.javaClass.getMethod("getChannelManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun getActiveChannel(player: Player): String? {
        val cm = getChannelManager() ?: return null
        return runCatching {
            val method = cm.javaClass.getMethod("getActiveChannel", Player::class.java)
            val channel = method.invoke(cm, player) ?: return null
            val idMethod = channel.javaClass.getMethod("getId")
            idMethod.invoke(channel)?.toString()
        }.getOrNull()
    }

    @JvmStatic
    fun setActiveChannel(player: Player, channelId: String): Boolean {
        val cm = getChannelManager() ?: return false
        return runCatching {
            val getChanMethod = cm.javaClass.getMethod("getChannel", String::class.java)
            val channel = getChanMethod.invoke(cm, channelId) ?: return false
            val setMethod = cm.javaClass.getMethod("setActiveChannel", Player::class.java, channel.javaClass)
            setMethod.invoke(cm, player, channel)
            true
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getAllChannels(): List<String> {
        val cm = getChannelManager() ?: return emptyList()
        return runCatching {
            val method = cm.javaClass.getMethod("getAllChannels")
            val channels = method.invoke(cm) as? Collection<*>
            channels?.mapNotNull { chan ->
                chan?.javaClass?.getMethod("getId")?.invoke(chan)?.toString()
            } ?: emptyList()
        }.getOrDefault(emptyList())
    }
}
