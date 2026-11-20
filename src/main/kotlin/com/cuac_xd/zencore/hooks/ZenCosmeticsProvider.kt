package com.cuac_xd.zencore.hooks

import org.bukkit.entity.Player

/**
 * Contrato tipado del proveedor de cosméticos para el ecosistema ZenCore.
 * ZenCosmetics implementa esta interfaz y se registra en [ZenCosmeticsHook.registerProvider]
 * al habilitarse, eliminando la reflexión frágil en runtime.
 */
interface ZenCosmeticsProvider {

    fun hasCosmetic(player: Player, cosmeticId: String): Boolean

    fun unlockCosmetic(player: Player, cosmeticId: String): Boolean

    fun unlockCosmetic(player: Player, cosmeticId: String, durationMillis: Long): Boolean {
        return unlockCosmetic(player, cosmeticId)
    }

    fun lockCosmetic(player: Player, cosmeticId: String): Boolean

    fun equipCosmetic(player: Player, cosmeticId: String): Boolean

    fun unequipCategory(player: Player, categoryName: String): Boolean

    fun getActiveCosmetic(player: Player, categoryName: String): String? = null

    fun isCategoryEnabled(categoryName: String): Boolean = true

    fun isCosmeticEnabled(cosmeticId: String): Boolean = true
}
