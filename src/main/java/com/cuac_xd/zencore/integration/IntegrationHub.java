package com.cuac_xd.zencore.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Universal Integration Hub for economy, placeholders, and custom systems.
 */
public final class IntegrationHub {

    private static boolean vaultAvailable = false;
    private static Economy vaultEconomy = null;
    private static boolean placeholderApiAvailable = false;

    private IntegrationHub() {}

    public static void init() {
        // Vault check
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    vaultEconomy = rsp.getProvider();
                    vaultAvailable = (vaultEconomy != null);
                }
            } catch (Throwable ignored) {}
        }

        // PlaceholderAPI check
        placeholderApiAvailable = (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
    }

    public static boolean isVaultAvailable() {
        return vaultAvailable && vaultEconomy != null;
    }

    public static Economy getEconomy() {
        return vaultEconomy;
    }

    public static boolean hasMoney(Player player, double amount) {
        if (!isVaultAvailable() || player == null) return false;
        return vaultEconomy.has(player, amount);
    }

    public static boolean withdrawMoney(Player player, double amount) {
        if (!isVaultAvailable() || player == null) return false;
        return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public static boolean depositMoney(Player player, double amount) {
        if (!isVaultAvailable() || player == null) return false;
        return vaultEconomy.depositPlayer(player, amount).transactionSuccess();
    }

    public static boolean isPlaceholderApiAvailable() {
        return placeholderApiAvailable;
    }

    public static String setPlaceholders(Player player, String text) {
        if (text == null) return "";
        if (!placeholderApiAvailable || player == null) return text;
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable ignored) {
            return text;
        }
    }
}
