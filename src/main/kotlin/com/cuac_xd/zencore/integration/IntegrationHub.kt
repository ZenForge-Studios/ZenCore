package com.cuac_xd.zencore.integration

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object IntegrationHub {
    private var vaultAvailable = false
    private var vaultEconomy: Economy? = null
    private var placeholderApiAvailable = false

    @JvmStatic
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            runCatching {
                val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java)
                if (rsp != null) {
                    vaultEconomy = rsp.provider
                    vaultAvailable = vaultEconomy != null
                }
            }
        }
        placeholderApiAvailable = (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
    }

    @JvmStatic
    fun isVaultAvailable(): Boolean = vaultAvailable && vaultEconomy != null

    @JvmStatic
    fun getEconomy(): Economy? = vaultEconomy

    @JvmStatic
    fun hasMoney(player: Player?, amount: Double): Boolean {
        if (!isVaultAvailable() || player == null) return false
        return vaultEconomy?.has(player, amount) == true
    }

    @JvmStatic
    fun withdrawMoney(player: Player?, amount: Double): Boolean {
        if (!isVaultAvailable() || player == null) return false
        return vaultEconomy?.withdrawPlayer(player, amount)?.transactionSuccess() == true
    }

    @JvmStatic
    fun depositMoney(player: Player?, amount: Double): Boolean {
        if (!isVaultAvailable() || player == null) return false
        return vaultEconomy?.depositPlayer(player, amount)?.transactionSuccess() == true
    }

    @JvmStatic
    fun isPlaceholderApiAvailable(): Boolean = placeholderApiAvailable

    @JvmStatic
    fun setPlaceholders(player: Player?, text: String?): String {
        if (text == null) return ""
        if (!placeholderApiAvailable || player == null) return text
        return runCatching { me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text) }.getOrDefault(text)
    }
}
