package com.cuac_xd.zencore.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Representa una ranura o elemento interactivo dentro de un [ZenMenu].
 * Soporta renderizado dinámico por jugador (para PAPI, condiciones o estados),
 * filtrado por ClickType (clic izquierdo, derecho, shift, etc.) y estados dinámicos.
 */
class MenuSlot(
    private var itemProvider: (Player) -> ItemStack?
) {

    constructor(staticItem: ItemStack) : this({ staticItem })

    private val clickHandlers = mutableListOf<(Player, InventoryClickEvent) -> Unit>()
    private val clickTypeHandlers = mutableMapOf<ClickType, (Player, InventoryClickEvent) -> Unit>()
    private var isCaptive: Boolean = false

    /**
     * Define si este slot es cautivo (permite que los jugadores coloquen o retiren sus propios items).
     */
    fun setCaptive(captive: Boolean): MenuSlot {
        this.isCaptive = captive
        return this
    }

    fun isCaptive(): Boolean = isCaptive

    /**
     * Renderiza el ItemStack que verá el jugador.
     */
    fun getItem(player: Player): ItemStack? = itemProvider(player)

    /**
     * Actualiza el proveedor de item.
     */
    fun setItem(provider: (Player) -> ItemStack?): MenuSlot {
        this.itemProvider = provider
        return this
    }

    fun setItem(staticItem: ItemStack): MenuSlot {
        this.itemProvider = { staticItem }
        return this
    }

    /**
     * Agrega un controlador de clic genérico.
     */
    fun onClick(handler: (Player, InventoryClickEvent) -> Unit): MenuSlot {
        clickHandlers.add(handler)
        return this
    }

    /**
     * Agrega un controlador de clic para un [ClickType] específico.
     */
    fun onClick(type: ClickType, handler: (Player, InventoryClickEvent) -> Unit): MenuSlot {
        clickTypeHandlers[type] = handler
        return this
    }

    /**
     * Clic izquierdo directo.
     */
    fun onLeftClick(handler: (Player, InventoryClickEvent) -> Unit): MenuSlot = onClick(ClickType.LEFT, handler)

    /**
     * Clic derecho directo.
     */
    fun onRightClick(handler: (Player, InventoryClickEvent) -> Unit): MenuSlot = onClick(ClickType.RIGHT, handler)

    /**
     * Maneja el evento de clic cuando un jugador interactúa con esta ranura.
     */
    fun handleClick(player: Player, event: InventoryClickEvent) {
        if (!isCaptive) {
            event.isCancelled = true
        }
        clickTypeHandlers[event.click]?.invoke(player, event)
        for (handler in clickHandlers) {
            handler(player, event)
        }
    }
}
