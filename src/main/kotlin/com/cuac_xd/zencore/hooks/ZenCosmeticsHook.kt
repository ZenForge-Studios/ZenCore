package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Hook para interactuar con ZenCosmetics de forma desacoplada y segura.
 * Prioriza el proveedor tipado [ZenCosmeticsProvider] registrado al habilitarse ZenCosmetics,
 * con fallback automático a reflexión para versiones legacy.
 */
object ZenCosmeticsHook {

    @Volatile
    private var provider: ZenCosmeticsProvider? = null

    @JvmStatic
    fun registerProvider(cosmeticsProvider: ZenCosmeticsProvider) {
        provider = cosmeticsProvider
    }

    @JvmStatic
    fun unregisterProvider(cosmeticsProvider: ZenCosmeticsProvider? = null) {
        if (cosmeticsProvider == null || provider === cosmeticsProvider) {
            provider = null
        }
    }

    @JvmStatic
    fun getProvider(): ZenCosmeticsProvider? = provider

    @JvmStatic
    fun isInstalled(): Boolean {
        if (provider != null) return true
        val plugin = Bukkit.getPluginManager().getPlugin("ZenCosmetics")
        return plugin != null && plugin.isEnabled
    }

    /**
     * Comprueba si el jugador posee un cosmético específico o tiene permiso para él.
     */
    @JvmStatic
    fun hasCosmetic(player: Player, cosmeticId: String): Boolean {
        provider?.let { return it.hasCosmetic(player, cosmeticId) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("hasCosmetic", Player::class.java, String::class.java)
            method.invoke(api, player, cosmeticId) as? Boolean ?: false
        }.getOrDefault(false)
    }

    /**
     * Desbloquea un cosmético permanente para un jugador.
     */
    @JvmStatic
    fun unlockCosmetic(player: Player, cosmeticId: String): Boolean {
        provider?.let { return it.unlockCosmetic(player, cosmeticId) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("unlockCosmetic", Player::class.java, String::class.java)
            method.invoke(api, player, cosmeticId)
            true
        }.getOrDefault(false)
    }

    /**
     * Desbloquea un cosmético temporal (o permanente si durationMillis <= 0) para un jugador.
     */
    @JvmStatic
    fun unlockCosmetic(player: Player, cosmeticId: String, durationMillis: Long): Boolean {
        provider?.let { return it.unlockCosmetic(player, cosmeticId, durationMillis) }
        return unlockCosmetic(player, cosmeticId)
    }

    /**
     * Bloquea/remueve un cosmético desbloqueado para un jugador.
     */
    @JvmStatic
    fun lockCosmetic(player: Player, cosmeticId: String): Boolean {
        provider?.let { return it.lockCosmetic(player, cosmeticId) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("lockCosmetic", Player::class.java, String::class.java)
            method.invoke(api, player, cosmeticId)
            true
        }.getOrDefault(false)
    }

    /**
     * Equipa un cosmético al jugador.
     */
    @JvmStatic
    fun equipCosmetic(player: Player, cosmeticId: String): Boolean {
        provider?.let { return it.equipCosmetic(player, cosmeticId) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("equipCosmetic", Player::class.java, String::class.java)
            method.invoke(api, player, cosmeticId) as? Boolean ?: true
        }.getOrDefault(false)
    }

    /**
     * Desequipa cualquier cosmético que el jugador tenga en una categoría dada (o "all").
     */
    @JvmStatic
    fun unequipCategory(player: Player, categoryName: String): Boolean {
        provider?.let { return it.unequipCategory(player, categoryName) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val catClass = runCatching { Class.forName("com.cuac_xd.zencosmetics.cosmetics.CosmeticCategory") }.getOrNull()
            if (catClass != null) {
                val enumVal = catClass.enumConstants.firstOrNull { (it as Enum<*>).name.equals(categoryName, ignoreCase = true) }
                if (enumVal != null) {
                    val method = api.javaClass.getMethod("unequipCategory", Player::class.java, catClass)
                    method.invoke(api, player, enumVal)
                    return true
                }
            }
            false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getActiveCosmetic(player: Player, categoryName: String): String? {
        return provider?.getActiveCosmetic(player, categoryName)
    }

    private fun getAPI(): Any? {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenCosmetics") ?: return null
            val getApiMethod = plugin.javaClass.getMethod("getApi")
            getApiMethod.invoke(plugin)
        }.getOrNull()
    }
}
