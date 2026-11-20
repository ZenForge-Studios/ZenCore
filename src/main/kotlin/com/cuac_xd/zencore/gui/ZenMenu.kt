package com.cuac_xd.zencore.gui

import com.cuac_xd.zencore.integration.IntegrationHub
import com.cuac_xd.zencore.util.AdventureUtil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Motor de menús avanzado, declarativo e interactivo para el ecosistema Zen (al estilo EcoMenus / Libreforge Menu).
 * Características:
 * - Soporte para coordenadas de filas y columnas naturales (row 1..6, column 1..9).
 * - Máscaras y patrones visuales (FillerMask).
 * - Paginación nativa integrada y botones de avance/retroceso de página.
 * - Renderizado dinámico y reactivo por jugador con soporte de PlaceholderAPI y estados locales.
 * - Soporte para ranuras cautivas (slots donde el jugador puede colocar/sacar items).
 * - Soporte para recarga y refresco dinámico sin cerrar el inventario.
 */
class ZenMenu(
    val rows: Int = 6,
    var titleProvider: (Player) -> Component
) {

    constructor(rows: Int = 6, title: String) : this(
        rows,
        { player -> AdventureUtil.parse(IntegrationHub.setPlaceholders(player, title)) }
    )

    val size: Int = rows.coerceIn(1, 6) * 9
    private val staticSlots = ConcurrentHashMap<Int, MenuSlot>()
    private val dynamicSlots = mutableListOf<(Player, Int) -> MenuSlot?>()
    private val playerStates = ConcurrentHashMap<UUID, MutableMap<String, Any>>()
    private val openHolders = ConcurrentHashMap<UUID, ZenMenuHolder>()

    private var onOpenHandler: ((Player, ZenMenu) -> Unit)? = null
    private var onCloseHandler: ((Player, ZenMenu) -> Unit)? = null
    private var onRenderHandler: ((Player, ZenMenu) -> Unit)? = null
    private var clearStateOnClose: Boolean = true

    // Paginación
    private var pagedItemsProvider: ((Player) -> List<ItemStack>)? = null
    private var pagedSlots: List<Int> = emptyList()
    private var pagedClickHandler: ((Player, ItemStack, Int) -> Unit)? = null
    private var pagedClickEventHandler: ((Player, ItemStack, Int, InventoryClickEvent) -> Unit)? = null

    /**
     * Establece una ranura por su índice plano (0..size-1).
     */
    fun setSlot(rawSlot: Int, slot: MenuSlot): ZenMenu {
        if (rawSlot in 0 until size) {
            staticSlots[rawSlot] = slot
        }
        return this
    }

    /**
     * Establece una ranura usando coordenadas amigables de fila (1..6) y columna (1..9).
     */
    fun setSlot(row: Int, column: Int, slot: MenuSlot): ZenMenu {
        val rawSlot = (row - 1) * 9 + (column - 1)
        return setSlot(rawSlot, slot)
    }

    /**
     * Establece un item estático en una ranura específica.
     */
    fun setItem(row: Int, column: Int, item: ItemStack, onClick: ((Player, InventoryClickEvent) -> Unit)? = null): ZenMenu {
        val slot = MenuSlot(item)
        if (onClick != null) slot.onClick(onClick)
        return setSlot(row, column, slot)
    }

    fun setItem(rawSlot: Int, item: ItemStack, onClick: ((Player, InventoryClickEvent) -> Unit)? = null): ZenMenu {
        val slot = MenuSlot(item)
        if (onClick != null) slot.onClick(onClick)
        return setSlot(rawSlot, slot)
    }

    /**
     * Aplica una máscara o patrón de relleno a la interfaz.
     */
    fun applyMask(mask: MenuMask): ZenMenu {
        mask.apply(this)
        return this
    }

    /**
     * Registra un callback que se ejecuta antes de renderizar la interfaz para un jugador.
     */
    fun onRender(handler: (Player, ZenMenu) -> Unit): ZenMenu {
        this.onRenderHandler = handler
        return this
    }

    fun onOpen(handler: (Player, ZenMenu) -> Unit): ZenMenu {
        this.onOpenHandler = handler
        return this
    }

    fun onClose(handler: (Player, ZenMenu) -> Unit): ZenMenu {
        this.onCloseHandler = handler
        return this
    }

    fun setClearStateOnClose(clear: Boolean): ZenMenu {
        this.clearStateOnClose = clear
        return this
    }

    // ==========================================
    // SISTEMA DE PAGINACIÓN INTEGRADO
    // ==========================================

    /**
     * Configura el área de items paginados y la lista de items a mostrar.
     */
    fun setPagedItems(
        slots: List<Int>,
        itemsProvider: (Player) -> List<ItemStack>,
        onClick: ((Player, ItemStack, Int) -> Unit)? = null
    ): ZenMenu {
        this.pagedSlots = slots
        this.pagedItemsProvider = itemsProvider
        this.pagedClickHandler = onClick
        this.pagedClickEventHandler = null
        return this
    }

    /**
     * Configura el área de items paginados recibiendo también el [InventoryClickEvent]
     * (útil para distinguir clic izquierdo, derecho y shift-clic).
     */
    fun setPagedItemsWithEvent(
        slots: List<Int>,
        itemsProvider: (Player) -> List<ItemStack>,
        onClick: ((Player, ItemStack, Int, InventoryClickEvent) -> Unit)? = null
    ): ZenMenu {
        this.pagedSlots = slots
        this.pagedItemsProvider = itemsProvider
        this.pagedClickHandler = null
        this.pagedClickEventHandler = onClick
        return this
    }

    /**
     * Configura el área de items paginados por filas y columnas rectangulares.
     */
    fun setPagedArea(
        startRow: Int,
        startColumn: Int,
        width: Int,
        height: Int,
        itemsProvider: (Player) -> List<ItemStack>,
        onClick: ((Player, ItemStack, Int) -> Unit)? = null
    ): ZenMenu {
        val slots = mutableListOf<Int>()
        for (r in startRow until startRow + height) {
            for (c in startColumn until startColumn + width) {
                slots.add((r - 1) * 9 + (c - 1))
            }
        }
        return setPagedItems(slots, itemsProvider, onClick)
    }

    /**
     * Botón para ir a la página anterior.
     */
    fun setPreviousPageButton(row: Int, column: Int, itemProvider: (Player, Boolean) -> ItemStack): ZenMenu {
        val rawSlot = (row - 1) * 9 + (column - 1)
        val slot = MenuSlot { player ->
            val page = getPage(player)
            val hasPrevious = page > 1
            itemProvider(player, hasPrevious)
        }.onClick { player, _ ->
            val page = getPage(player)
            if (page > 1) {
                setPage(player, page - 1)
                refresh(player)
            }
        }
        return setSlot(rawSlot, slot)
    }

    /**
     * Botón para ir a la página siguiente.
     */
    fun setNextPageButton(row: Int, column: Int, itemProvider: (Player, Boolean) -> ItemStack): ZenMenu {
        val rawSlot = (row - 1) * 9 + (column - 1)
        val slot = MenuSlot { player ->
            val page = getPage(player)
            val maxPage = getMaxPage(player)
            val hasNext = page < maxPage
            itemProvider(player, hasNext)
        }.onClick { player, _ ->
            val page = getPage(player)
            val maxPage = getMaxPage(player)
            if (page < maxPage) {
                setPage(player, page + 1)
                refresh(player)
            }
        }
        return setSlot(rawSlot, slot)
    }

    fun getPage(player: Player): Int {
        return getState<Int>(player, "zen_page") ?: 1
    }

    fun setPage(player: Player, page: Int) {
        setState(player, "zen_page", page.coerceAtLeast(1))
    }

    fun getMaxPage(player: Player): Int {
        val items = pagedItemsProvider?.invoke(player) ?: return 1
        if (pagedSlots.isEmpty() || items.isEmpty()) return 1
        return kotlin.math.ceil(items.size.toDouble() / pagedSlots.size).toInt().coerceAtLeast(1)
    }

    // ==========================================
    // GESTIÓN DE ESTADOS LOCALES (Player State)
    // ==========================================

    fun setState(player: Player, key: String, value: Any) {
        playerStates.computeIfAbsent(player.uniqueId) { ConcurrentHashMap() }[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getState(player: Player, key: String): T? {
        return playerStates[player.uniqueId]?.get(key) as? T
    }

    fun clearState(player: Player) {
        playerStates.remove(player.uniqueId)
    }

    // ==========================================
    // APERTURA Y RENDERIZADO
    // ==========================================

    /**
     * Abre el menú interactivo para un jugador.
     */
    fun open(player: Player) {
        onRenderHandler?.invoke(player, this)
        val title = titleProvider(player)
        val holder = ZenMenuHolder(this, player, size, title)
        openHolders[player.uniqueId] = holder
        renderInventory(player, holder.inventory, isRefresh = false)
        player.openInventory(holder.inventory)
        onOpenHandler?.invoke(player, this)
    }

    /**
     * Actualiza y re-renderiza el inventario en tiempo real sin cerrarlo.
     */
    fun refresh(player: Player) {
        val holder = openHolders[player.uniqueId] ?: return
        onRenderHandler?.invoke(player, this)
        renderInventory(player, holder.inventory, isRefresh = true)
    }

    fun getOpenInventory(player: Player): Inventory? = openHolders[player.uniqueId]?.inventory

    private fun renderInventory(player: Player, inventory: Inventory, isRefresh: Boolean) {
        if (!isRefresh) {
            inventory.clear()
        }

        // 1. Renderizar ranuras estáticas (preservando ranuras cautivas en refresh)
        for (idx in 0 until size) {
            val slot = staticSlots[idx]
            if (slot != null) {
                if (isRefresh && slot.isCaptive()) {
                    continue
                }
                inventory.setItem(idx, slot.getItem(player))
            } else if (isRefresh && idx !in pagedSlots) {
                inventory.setItem(idx, null)
            }
        }

        // 2. Renderizar items paginados si existen (solo sobre las ranuras de paginación)
        val pagedProvider = pagedItemsProvider
        if (pagedProvider != null && pagedSlots.isNotEmpty()) {
            val allItems = pagedProvider(player)
            val page = getPage(player)
            val startIndex = (page - 1) * pagedSlots.size
            for (i in pagedSlots.indices) {
                val targetSlot = pagedSlots[i]
                if (targetSlot !in 0 until size) continue
                val itemIndex = startIndex + i
                if (itemIndex in allItems.indices) {
                    val pagedItem = allItems[itemIndex]
                    inventory.setItem(targetSlot, pagedItem)
                } else {
                    // Limpiar el slot paginado vacío si no tiene un slot estático explícito
                    if (!staticSlots.containsKey(targetSlot)) {
                        inventory.setItem(targetSlot, null)
                    }
                }
            }
        }
    }

    internal fun handleRawClick(player: Player, event: InventoryClickEvent) {
        val hasCaptive = staticSlots.values.any { it.isCaptive() }

        // Evitar recolección por doble clic sobre ranuras no cautivas del menú
        if (event.action == InventoryAction.COLLECT_TO_CURSOR) {
            event.isCancelled = true
            return
        }

        val rawSlot = event.rawSlot
        if (rawSlot < 0) {
            if (!hasCaptive) {
                event.isCancelled = true
            }
            return
        }

        if (rawSlot >= size) {
            // Clic en el inventario del propio jugador
            if (!hasCaptive || event.isShiftClick) {
                event.isCancelled = true
            }
            return
        }

        // 1. Comprobar slot paginado primero (tiene prioridad sobre máscaras de fondo)
        if (rawSlot in pagedSlots) {
            event.isCancelled = true
            val page = getPage(player)
            val slotIndex = pagedSlots.indexOf(rawSlot)
            val pagedProvider = pagedItemsProvider
            if (pagedProvider != null) {
                val allItems = pagedProvider(player)
                val itemIndex = (page - 1) * pagedSlots.size + slotIndex
                if (itemIndex in allItems.indices) {
                    val item = allItems[itemIndex]
                    pagedClickEventHandler?.invoke(player, item, itemIndex, event)
                        ?: pagedClickHandler?.invoke(player, item, itemIndex)
                    return
                }
            }
        }

        // 2. Comprobar slot estático
        val staticSlot = staticSlots[rawSlot]
        if (staticSlot != null) {
            staticSlot.handleClick(player, event)
            return
        }

        // Por defecto, cancelar interacción con ranuras vacías no cautivas
        event.isCancelled = true
    }

    private fun handleClose(player: Player, closingHolder: ZenMenuHolder) {
        val removed = openHolders.remove(player.uniqueId, closingHolder)
        if (removed) {
            onCloseHandler?.invoke(player, this)
            if (clearStateOnClose && !openHolders.containsKey(player.uniqueId)) {
                clearState(player)
            }
        }
    }

    private class ZenMenuHolder(
        val menu: ZenMenu,
        val player: Player,
        val size: Int,
        title: Component
    ) : InventoryHolder {
        private val inv: Inventory = Bukkit.createInventory(this, size, title)
        override fun getInventory(): Inventory = inv
    }

    companion object : Listener {
        private var lastProcessedEventRef: WeakReference<InventoryClickEvent>? = null

        @EventHandler(priority = EventPriority.HIGHEST)
        fun onInventoryClick(e: InventoryClickEvent) {
            val holder = e.inventory.holder as? ZenMenuHolder ?: return
            if (lastProcessedEventRef?.get() === e) return
            lastProcessedEventRef = WeakReference(e)
            val player = e.whoClicked as? Player ?: return
            holder.menu.handleRawClick(player, e)
        }

        @EventHandler
        fun onInventoryDrag(e: InventoryDragEvent) {
            val holder = e.inventory.holder as? ZenMenuHolder ?: return
            for (rawSlot in e.rawSlots) {
                if (rawSlot in 0 until holder.size) {
                    val slot = holder.menu.staticSlots[rawSlot]
                    if (rawSlot in holder.menu.pagedSlots || slot == null || !slot.isCaptive()) {
                        e.isCancelled = true
                        return
                    }
                }
            }
        }

        @EventHandler
        fun onInventoryClose(e: InventoryCloseEvent) {
            val holder = e.inventory.holder as? ZenMenuHolder ?: return
            val player = e.player as? Player ?: return
            holder.menu.handleClose(player, holder)
        }
    }
}
