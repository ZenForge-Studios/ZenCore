package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.awt.Color
import java.lang.reflect.Method
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Hook unificado y seguro para ZenDiscord dentro de ZenCore.
 * Permite a cualquier plugin del ecosistema Zen (ZenForjas, ZenAuctions, etc.)
 * verificar cuentas vinculadas y enviar DMs/Embeds a Discord sin acoplamiento duro.
 */
object ZenDiscordHook {

    private const val API_PROVIDER_CLASS = "com.cuac_xd.zendiscord.api.ZenDiscordAPIProvider"
    private const val API_CLASS = "com.cuac_xd.zendiscord.api.ZenDiscordAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenDiscord")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getApiInstance(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val providerClazz = Class.forName(API_PROVIDER_CLASS)
            val getApiMethod = providerClazz.getMethod("getAPI")
            getApiMethod.invoke(null)
        }.getOrNull()
    }

    @JvmStatic
    fun isLinked(uuid: UUID): Boolean {
        val api = getApiInstance() ?: return false
        return runCatching {
            val method = api.javaClass.getMethod("isLinked", UUID::class.java)
            method.invoke(api, uuid) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun isLinked(player: OfflinePlayer?): Boolean {
        if (player == null) return false
        return isLinked(player.uniqueId)
    }

    @JvmStatic
    fun getDiscordId(uuid: UUID): String? {
        val api = getApiInstance() ?: return null
        return runCatching {
            val method = api.javaClass.getMethod("getDiscordId", UUID::class.java)
            method.invoke(api, uuid) as? String
        }.getOrNull()
    }

    @JvmStatic
    fun getMinecraftUuid(discordId: String): UUID? {
        val api = getApiInstance() ?: return null
        return runCatching {
            val method = api.javaClass.getMethod("getMinecraftUuid", String::class.java)
            method.invoke(api, discordId) as? UUID
        }.getOrNull()
    }

    @JvmStatic
    fun sendDirectMessage(uuid: UUID, message: String): CompletableFuture<Boolean> {
        val api = getApiInstance() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = api.javaClass.getMethod("sendDirectMessage", UUID::class.java, String::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(api, uuid, message) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }

    @JvmStatic
    fun sendDirectEmbed(uuid: UUID, title: String, description: String, color: Color = Color.ORANGE): CompletableFuture<Boolean> {
        val api = getApiInstance() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = api.javaClass.getMethod(
                "sendDirectEmbed",
                UUID::class.java,
                String::class.java,
                String::class.java,
                Color::class.java
            )
            @Suppress("UNCHECKED_CAST")
            method.invoke(api, uuid, title, description, color) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }

    @JvmStatic
    fun sendChannelMessage(channelId: String, message: String): CompletableFuture<Boolean> {
        val api = getApiInstance() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val method = api.javaClass.getMethod("sendChannelMessage", String::class.java, String::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(api, channelId, message) as CompletableFuture<Boolean>
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }
}
