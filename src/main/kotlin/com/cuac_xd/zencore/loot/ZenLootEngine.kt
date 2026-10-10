package com.cuac_xd.zencore.loot

import com.cuac_xd.zencore.util.MinecraftAdapter
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

/**
 * Dynamic loot table and weighted drop calculation engine.
 */
object ZenLootEngine {

    data class LootEntry(
        val item: ItemStack,
        val weight: Double,
        val minAmount: Int = 1,
        val maxAmount: Int = 1
    )

    data class LootTable(
        val id: String,
        val entries: MutableList<LootEntry> = mutableListOf()
    ) {
        fun roll(): List<ItemStack> {
            val results = mutableListOf<ItemStack>()
            val totalWeight = entries.sumOf { it.weight }
            if (totalWeight <= 0.0) return results

            val rand = Math.random() * totalWeight
            var current = 0.0
            for (entry in entries) {
                current += entry.weight
                if (rand <= current) {
                    val count = (entry.minAmount..entry.maxAmount).random()
                    val dropped = entry.item.clone()
                    dropped.amount = count
                    results.add(dropped)
                    break
                }
            }
            return results
        }
    }

    private val tables = ConcurrentHashMap<String, LootTable>()

    @JvmStatic
    fun registerTable(table: LootTable) {
        tables[table.id.lowercase()] = table
    }

    @JvmStatic
    fun getTable(id: String): LootTable? = tables[id.lowercase()]

    @JvmStatic
    fun dropLoot(tableId: String, location: Location): List<ItemStack> {
        val table = getTable(tableId) ?: return emptyList()
        val items = table.roll()
        for (item in items) {
            location.world?.dropItemNaturally(location, item)
        }
        return items
    }

    @JvmStatic
    fun giveLoot(tableId: String, player: Player): List<ItemStack> {
        val table = getTable(tableId) ?: return emptyList()
        val items = table.roll()
        for (item in items) {
            val leftover = player.inventory.addItem(item)
            for ((_, drop) in leftover) {
                player.world.dropItemNaturally(player.location, drop)
            }
        }
        return items
    }
}
