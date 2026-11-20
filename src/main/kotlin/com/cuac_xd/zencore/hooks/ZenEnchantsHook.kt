package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Hook para interactuar con ZenEnchants de forma desacoplada y segura.
 * Permite consultar, aplicar y remover encantamientos custom sobre cualquier ItemStack.
 */
object ZenEnchantsHook {

    fun isInstalled(): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin("ZenEnchants")
        return plugin != null && plugin.isEnabled
    }

    /**
     * Comprueba si un ItemStack tiene un encantamiento custom aplicado.
     */
    fun hasEnchantment(item: ItemStack?, enchantId: String): Boolean {
        if (!isInstalled() || item == null) return false
        return runCatching {
            val clazz = Class.forName("com.cuac_xd.zenenchants.api.ZenEnchantsAPI")
            val method = clazz.getMethod("hasCustomEnchantment", ItemStack::class.java, String::class.java)
            method.invoke(null, item, enchantId) as? Boolean ?: false
        }.getOrDefault(false)
    }

    /**
     * Obtiene el nivel de un encantamiento custom en un ítem (0 si no lo tiene).
     */
    fun getEnchantmentLevel(item: ItemStack?, enchantId: String): Int {
        if (!isInstalled() || item == null) return 0
        return runCatching {
            val clazz = Class.forName("com.cuac_xd.zenenchants.api.ZenEnchantsAPI")
            val method = clazz.getMethod("getEnchantmentLevel", ItemStack::class.java, String::class.java)
            (method.invoke(null, item, enchantId) as? Number)?.toInt() ?: 0
        }.getOrDefault(0)
    }

    /**
     * Aplica un encantamiento custom a un ItemStack.
     */
    fun applyEnchantment(item: ItemStack, enchantId: String, level: Int, player: Player? = null): Boolean {
        if (!isInstalled()) return false
        return runCatching {
            val apiClazz = Class.forName("com.cuac_xd.zenenchants.api.ZenEnchantsAPI")
            val getEnchantMethod = apiClazz.getMethod("getEnchantment", String::class.java)
            val optionalEnchant = getEnchantMethod.invoke(null, enchantId) as? java.util.Optional<*>
            if (optionalEnchant == null || !optionalEnchant.isPresent) return false

            val enchantmentObj = optionalEnchant.get()
            val enchantClazz = Class.forName("com.cuac_xd.zenenchants.model.ZenEnchantment")

            if (player != null) {
                val applyMethod = apiClazz.getMethod(
                    "applyEnchantment",
                    Player::class.java,
                    ItemStack::class.java,
                    enchantClazz,
                    Int::class.javaPrimitiveType
                )
                applyMethod.invoke(null, player, item, enchantmentObj, level)
            } else {
                val applyMethod = apiClazz.getMethod(
                    "applyEnchantment",
                    ItemStack::class.java,
                    enchantClazz,
                    Int::class.javaPrimitiveType
                )
                applyMethod.invoke(null, item, enchantmentObj, level)
            }
            true
        }.getOrDefault(false)
    }
}
