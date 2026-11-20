package com.cuac_xd.zencore.gui

import com.cuac_xd.zencore.util.AdventureUtil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

/**
 * Fluent, event-driven GUI framework for interactive chest menus.
 */
class ZenGUI(
    val title: String,
    val rows: Int = 6
) : InventoryHolder {

    private val size = rows.coerceIn(1, 6) * 9
    private val inventory: Inventory = Bukkit.createInventory(this, size, AdventureUtil.parse(title))
    private val clickHandlers = ConcurrentHashMap<Int, (Player, InventoryClickEvent) -> Unit>()
    private var closeHandler: ((Player) -> Unit)? = null

    override fun getInventory(): Inventory = inventory

    @JvmOverloads
    fun setItem(slot: Int, item: ItemStack, onClick: ((Player, InventoryClickEvent) -> Unit)? = null): ZenGUI {
        if (slot in 0 until size) {
            inventory.setItem(slot, item)
            if (onClick != null) {
                clickHandlers[slot] = onClick
            } else {
                clickHandlers.remove(slot)
            }
        }
        return this
    }

    fun fill(item: ItemStack): ZenGUI {
        for (i in 0 until size) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item)
            }
        }
        return this
    }

    fun onClose(handler: (Player) -> Unit): ZenGUI {
        this.closeHandler = handler
        return this
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    companion object : Listener {
        @EventHandler
        fun onInventoryClick(e: InventoryClickEvent) {
            val holder = e.inventory.holder as? ZenGUI ?: return
            val player = e.whoClicked as? Player ?: return
            e.isCancelled = true

            val slot = e.rawSlot
            holder.clickHandlers[slot]?.invoke(player, e)
        }

        @EventHandler
        fun onInventoryClose(e: InventoryCloseEvent) {
            val holder = e.inventory.holder as? ZenGUI ?: return
            val player = e.player as? Player ?: return
            holder.closeHandler?.invoke(player)
        }
    }
}
