package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenContest dentro del ecosistema ZenCore.
 * Permite interactuar con concursos agrícolas activos (estilo Jacob Skyblock),
 * consultar puntuaciones y disparar o cancelar concursos.
 */
object ZenContestHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenContest")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getContestManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenContest") ?: return null
            val method = plugin.javaClass.getMethod("getContestManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun isContestActive(contestId: String): Boolean {
        val cm = getContestManager() ?: return false
        return runCatching {
            val method = cm.javaClass.getMethod("isContestActive", String::class.java)
            method.invoke(cm, contestId) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun hasActiveContests(): Boolean {
        val cm = getContestManager() ?: return false
        return runCatching {
            val field = cm.javaClass.getMethod("getActiveContests")
            val activeMap = field.invoke(cm) as? Map<*, *>
            activeMap?.isNotEmpty() == true
        }.getOrDefault(false)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun getActiveContestIds(): List<String> {
        val cm = getContestManager() ?: return emptyList()
        return runCatching {
            val field = cm.javaClass.getMethod("getActiveContests")
            val activeMap = field.invoke(cm) as? Map<String, *> ?: return emptyList()
            activeMap.keys.toList()
        }.getOrDefault(emptyList())
    }

    @JvmStatic
    fun getPlayerScore(player: Player, contestId: String): Long = getPlayerScore(player.uniqueId, contestId)

    @JvmStatic
    fun getPlayerScore(playerUuid: UUID, contestId: String): Long {
        val cm = getContestManager() ?: return 0L
        return runCatching {
            val getActiveMethod = cm.javaClass.getMethod("getActiveContest", String::class.java)
            val contest = getActiveMethod.invoke(cm, contestId) ?: return 0L
            val getPartsMethod = contest.javaClass.getMethod("getParticipants")
            val participants = getPartsMethod.invoke(contest) as? Map<*, *> ?: return 0L
            val participant = participants[playerUuid] ?: return 0L
            val getScoreMethod = participant.javaClass.getMethod("getScore")
            (getScoreMethod.invoke(participant) as? Number)?.toLong() ?: 0L
        }.getOrDefault(0L)
    }

    @JvmStatic
    fun startContest(contestId: String): Boolean {
        val cm = getContestManager() ?: return false
        return runCatching {
            val method = cm.javaClass.getMethod("startContest", String::class.java)
            method.invoke(cm, contestId) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun stopContest(contestId: String): Boolean {
        val cm = getContestManager() ?: return false
        return runCatching {
            val method = cm.javaClass.getMethod("stopContest", String::class.java)
            method.invoke(cm, contestId) as? Boolean ?: false
        }.getOrDefault(false)
    }
}
