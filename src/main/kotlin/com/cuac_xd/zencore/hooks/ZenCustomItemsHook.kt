package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Hook para interactuar con ZenCustomItems de forma desacoplada y segura.
 * Permite crear items personalizados, verificar si un ItemStack es un ZenItem y obtener/modificar variables.
 */
object ZenCustomItemsHook {

    fun isInstalled(): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin("ZenCustomItems")
        return plugin != null && plugin.isEnabled
    }

    /**
     * Comprueba si un ItemStack es un item personalizado de ZenCustomItems.
     */
    fun isZenItem(item: ItemStack?): Boolean {
        if (!isInstalled() || item == null) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("isZenItem", ItemStack::class.java)
            method.invoke(api, item) as? Boolean ?: false
        }.getOrDefault(false)
    }

    /**
     * Obtiene el ID del item personalizado, o null si no lo es.
     */
    fun getItemId(item: ItemStack?): String? {
        if (!isInstalled() || item == null) return null
        return runCatching {
            val api = getAPI() ?: return null
            val method = api.javaClass.getMethod("getItemId", ItemStack::class.java)
            method.invoke(api, item) as? String
        }.getOrNull()
    }

    /**
     * Comprueba si un ItemStack coincide con un ID de item custom esperado.
     */
    fun matches(item: ItemStack?, expectedId: String): Boolean {
        if (!isInstalled() || item == null) return false
        val currentId = getItemId(item) ?: return false
        return currentId.equals(expectedId, ignoreCase = true)
    }

    /**
     * Crea un nuevo ItemStack a partir de la definición en ZenCustomItems.
     */
    fun createItem(id: String, amount: Int = 1): ItemStack? {
        if (!isInstalled()) return null
        return runCatching {
            val api = getAPI() ?: return null
            val method = api.javaClass.getMethod("createItem", String::class.java, Int::class.javaPrimitiveType)
            method.invoke(api, id, amount) as? ItemStack
        }.getOrNull()
    }

    /**
     * Entrega un item custom al inventario del jugador o lo deja caer al suelo si está lleno.
     */
    fun giveItem(player: Player, id: String, amount: Int = 1): Boolean {
        val item = createItem(id, amount) ?: return false
        val leftover = player.inventory.addItem(item)
        for (drop in leftover.values) {
            player.world.dropItemNaturally(player.location, drop)
        }
        return true
    }

    /**
     * Comprueba si el jugador posee al menos `amount` items con el ID especificado en su inventario.
     */
    fun hasItem(player: Player, id: String, amount: Int = 1): Boolean {
        if (!isInstalled()) return false
        var count = 0
        for (item in player.inventory.contents) {
            if (item != null && matches(item, id)) {
                count += item.amount
                if (count >= amount) return true
            }
        }
        return false
    }

    private fun getAPI(): Any? {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenCustomItems") ?: return null
            val getApiMethod = plugin.javaClass.getMethod("getApi")
            getApiMethod.invoke(plugin)
        }.getOrNull()
    }
}
