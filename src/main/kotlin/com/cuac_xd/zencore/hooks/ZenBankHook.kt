package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.UUID

/**
 * Hook desacoplado para ZenBank (cuentas bancarias, depósitos a plazo fijo e intereses).
 */
object ZenBankHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenBank")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getBankManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenBank") ?: return null
            val method = plugin.javaClass.getMethod("getBankManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun getBankBalance(player: OfflinePlayer): Double = getBankBalance(player.uniqueId)

    @JvmStatic
    fun getBankBalance(playerUuid: UUID): Double {
        val bm = getBankManager() ?: return 0.0
        return runCatching {
            val method = bm.javaClass.getMethod("getCachedAccount", UUID::class.java)
            val account = method.invoke(bm, playerUuid) ?: return 0.0
            val balMethod = account.javaClass.getMethod("getBalance")
            (balMethod.invoke(account) as? Number)?.toDouble() ?: 0.0
        }.getOrDefault(0.0)
    }

    @JvmStatic
    fun deposit(playerUuid: UUID, amount: Double): Boolean {
        if (amount <= 0.0) return false
        val bm = getBankManager() ?: return false
        return runCatching {
            val method = bm.javaClass.getMethod("deposit", UUID::class.java, Double::class.javaPrimitiveType)
            method.invoke(bm, playerUuid, amount) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun withdraw(playerUuid: UUID, amount: Double): Boolean {
        if (amount <= 0.0) return false
        val bm = getBankManager() ?: return false
        return runCatching {
            val method = bm.javaClass.getMethod("withdraw", UUID::class.java, Double::class.javaPrimitiveType)
            method.invoke(bm, playerUuid, amount) as? Boolean ?: false
        }.getOrDefault(false)
    }
}
