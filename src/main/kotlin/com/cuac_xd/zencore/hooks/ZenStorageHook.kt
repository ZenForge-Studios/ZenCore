package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hook desacoplado para ZenStorage (bóvedas virtuales y almacenamiento compartido).
 */
object ZenStorageHook {

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenStorage")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getVaultManager(): Any? {
        if (!isInstalled()) return null
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenStorage") ?: return null
            val method = plugin.javaClass.getMethod("getVaultManager")
            method.invoke(plugin)
        }.getOrNull()
    }

    @JvmStatic
    fun getUnlockedVaults(player: Player): Int = getUnlockedVaults(player.uniqueId)

    @JvmStatic
    fun getUnlockedVaults(playerUuid: UUID): Int {
        val vm = getVaultManager() ?: return 0
        return runCatching {
            val method = vm.javaClass.getMethod("getUnlockedVaults", UUID::class.java)
            (method.invoke(vm, playerUuid) as? Number)?.toInt() ?: 0
        }.getOrDefault(0)
    }

    @JvmStatic
    fun getMaxVaults(player: Player): Int {
        val vm = getVaultManager() ?: return 1
        return runCatching {
            val method = vm.javaClass.getMethod("getMaxVaultsLimit", Player::class.java)
            (method.invoke(vm, player) as? Number)?.toInt() ?: 1
        }.getOrDefault(1)
    }

    @JvmStatic
    fun openVault(player: Player, vaultNumber: Int) {
        val vm = getVaultManager() ?: return
        runCatching {
            val method = vm.javaClass.getMethod("openVault", Player::class.java, Int::class.javaPrimitiveType)
            method.invoke(vm, player, vaultNumber)
        }
    }

    @JvmStatic
    fun openPartyVault(player: Player, partyId: UUID) {
        val vm = getVaultManager() ?: return
        runCatching {
            val method = vm.javaClass.getMethod("openPartyVault", Player::class.java, UUID::class.java)
            method.invoke(vm, player, partyId)
        }
    }
}
