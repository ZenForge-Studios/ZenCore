package com.cuac_xd.zencore.drop

import com.cuac_xd.zencore.util.AdventureUtil
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Sistema de entrega segura de drops y recompensas (al estilo DropQueue de Eco).
 * Gestiona inteligentemente la entrega de items:
 * 1. Intenta colocarlos en el inventario del jugador.
 * 2. Si el inventario está lleno, los arroja al suelo a sus pies de forma natural o los almacena en un buzón temporal.
 * 3. Notifica al jugador si hubo ítems que no cupieron.
 */
object ZenDropEngine {

    private val playerOverflowBoxes = ConcurrentHashMap<UUID, MutableList<ItemStack>>()

    /**
     * Entrega un conjunto de ítems de forma segura al jugador.
     * @param player Jugador receptor
     * @param items Ítems a entregar
     * @param dropToGround Si es true, lo que no quepa caerá a sus pies; si es false, se guardará en su buzón virtual
     * @param notifyOverflow Si debe avisarle que su inventario estaba lleno
     */
    @JvmStatic
    fun give(
        player: Player,
        items: List<ItemStack>,
        dropToGround: Boolean = true,
        notifyOverflow: Boolean = true
    ) {
        val leftovers = mutableListOf<ItemStack>()
        for (item in items) {
            val unadded = player.inventory.addItem(item)
            leftovers.addAll(unadded.values)
        }

        if (leftovers.isNotEmpty()) {
            if (dropToGround) {
                for (drop in leftovers) {
                    player.world.dropItemNaturally(player.location, drop)
                }
                if (notifyOverflow) {
                    player.sendMessage(AdventureUtil.parse("<yellow>¡Tu inventario estaba lleno! Algunos ítems cayeron al suelo."))
                }
            } else {
                val box = playerOverflowBoxes.computeIfAbsent(player.uniqueId) { mutableListOf() }
                box.addAll(leftovers)
                if (notifyOverflow) {
                    player.sendMessage(AdventureUtil.parse("<gold>¡Inventario lleno! <yellow>${leftovers.size} ítems fueron enviados a tu buzón virtual."))
                }
            }
        }
    }

    @JvmStatic
    fun give(player: Player, item: ItemStack, dropToGround: Boolean = true) {
        give(player, listOf(item), dropToGround)
    }

    /**
     * Reclama los ítems almacenados en el buzón virtual de overflow.
     */
    @JvmStatic
    fun claimOverflow(player: Player): Int {
        val box = playerOverflowBoxes[player.uniqueId] ?: return 0
        if (box.isEmpty()) return 0

        val iterator = box.iterator()
        var claimed = 0
        while (iterator.hasNext()) {
            val item = iterator.next()
            val left = player.inventory.addItem(item)
            if (left.isEmpty()) {
                iterator.remove()
                claimed++
            } else {
                // Parcialmente añadido o lleno de nuevo
                break
            }
        }
        return claimed
    }

    @JvmStatic
    fun getOverflowCount(player: Player): Int {
        return playerOverflowBoxes[player.uniqueId]?.sumOf { it.amount } ?: 0
    }
}
