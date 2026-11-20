package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenJobs dentro del ecosistema ZenCore.
 * Interactúa de forma segura con `com.cuac_xd.zenjobs.api.ZenJobsAPI`.
 */
object ZenJobsHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zenjobs.api.ZenJobsAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenJobs")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getApiClass(): Class<*>? {
        if (!isInstalled()) return null
        return runCatching { Class.forName(API_CLASS_NAME) }.getOrNull()
    }

    @JvmStatic
    fun isPlayerInJob(player: Player, jobId: String): Boolean {
        val clazz = getApiClass() ?: return false
        return runCatching {
            val method = clazz.getMethod("isPlayerInJob", Player::class.java, String::class.java)
            method.invoke(null, player, jobId) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getPlayerJobLevel(player: Player, jobId: String): Int {
        val clazz = getApiClass() ?: return 0
        return runCatching {
            val method = clazz.getMethod("getPlayerJobLevel", Player::class.java, String::class.java)
            (method.invoke(null, player, jobId) as? Number)?.toInt() ?: 0
        }.getOrDefault(0)
    }

    @JvmStatic
    fun getPlayerJobXp(player: Player, jobId: String): Double {
        val clazz = getApiClass() ?: return 0.0
        return runCatching {
            val method = clazz.getMethod("getPlayerJobXp", Player::class.java, String::class.java)
            (method.invoke(null, player, jobId) as? Number)?.toDouble() ?: 0.0
        }.getOrDefault(0.0)
    }

    @JvmStatic
    fun getAllJobs(): List<String> {
        val clazz = getApiClass() ?: return emptyList()
        return runCatching {
            val method = clazz.getMethod("getAllJobs")
            val jobs = method.invoke(null) as? Collection<*>
            jobs?.mapNotNull { job ->
                job?.javaClass?.getMethod("getId")?.invoke(job)?.toString()
            } ?: emptyList()
        }.getOrDefault(emptyList())
    }
}
