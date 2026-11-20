package com.cuac_xd.zencore.world

import com.cuac_xd.zencore.ZenCorePlugin
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks player-placed blocks in thread-safe per-chunk packed coordinate sets and persists
 * them to Chunk PersistentDataContainers whenever the ZenCore plugin instance is active.
 */
object ZenBlockTracker : Listener {

    private const val PDC_KEY_NAME = "zen_placed_blocks"

    private val placedBlocks = ConcurrentHashMap<String, MutableSet<Long>>()
    private val loadedChunks = ConcurrentHashMap.newKeySet<String>()

    /**
     * Marks [block] as player-placed in memory and persists the chunk state to PDC if available.
     */
    @JvmStatic
    fun markPlaced(block: Block) {
        val key = chunkKey(block)
        ensureChunkLoaded(block, key)
        val packed = packCoords(block.x, block.y, block.z)
        val set = placedBlocks.computeIfAbsent(key) { ConcurrentHashMap.newKeySet() }
        synchronized(set) {
            if (set.add(packed)) {
                saveToChunkPdc(block, set)
            }
        }
    }

    /**
     * Unmarks [block] as player-placed in memory and updates chunk PDC persistence if available.
     */
    @JvmStatic
    fun markBroken(block: Block) {
        val key = chunkKey(block)
        ensureChunkLoaded(block, key)
        val packed = packCoords(block.x, block.y, block.z)
        val set = placedBlocks[key] ?: return
        synchronized(set) {
            if (set.remove(packed)) {
                saveToChunkPdc(block, set)
            }
        }
    }

    /**
     * Returns `true` if [block] is currently tracked as player-placed.
     */
    @JvmStatic
    fun isPlayerPlaced(block: Block): Boolean {
        val key = chunkKey(block)
        ensureChunkLoaded(block, key)
        val packed = packCoords(block.x, block.y, block.z)
        return placedBlocks[key]?.contains(packed) == true
    }

    /**
     * Clears all in-memory tracked block positions and chunk load flags. Intended for tests and shutdown.
     */
    @JvmStatic
    fun clearAll() {
        placedBlocks.clear()
        loadedChunks.clear()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        markPlaced(event.block)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        markBroken(event.block)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        handlePistonMove(event.blocks, event.direction)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        handlePistonMove(event.blocks, event.direction)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChunkUnload(event: ChunkUnloadEvent) {
        if (ZenCorePlugin.getInstance() == null) return
        runCatching {
            val worldName = runCatching { event.world.name }.getOrNull() ?: "default"
            val key = formatChunkKey(worldName, event.chunk.x, event.chunk.z)
            placedBlocks.remove(key)
            loadedChunks.remove(key)
        }
    }

    private fun handlePistonMove(blocks: List<Block>, direction: BlockFace) {
        if (blocks.isEmpty()) return
        val placedToMove = blocks.filter { isPlayerPlaced(it) }
        if (placedToMove.isEmpty()) return

        for (block in placedToMove) {
            markBroken(block)
        }
        for (block in placedToMove) {
            runCatching { block.getRelative(direction) }.getOrNull()?.let { moved ->
                markPlaced(moved)
            }
        }
    }

    private fun ensureChunkLoaded(block: Block, key: String) {
        if (ZenCorePlugin.getInstance() == null) return
        if (!loadedChunks.add(key)) return

        runCatching {
            val pdcKey = getPdcKey() ?: return
            val pdc = block.chunk.persistentDataContainer
            val stored = pdc.get(pdcKey, PersistentDataType.LONG_ARRAY) ?: return
            if (stored.isNotEmpty()) {
                val set = placedBlocks.computeIfAbsent(key) { ConcurrentHashMap.newKeySet() }
                synchronized(set) {
                    for (coord in stored) {
                        set.add(coord)
                    }
                }
            }
        }
    }

    private fun saveToChunkPdc(block: Block, set: Set<Long>) {
        if (ZenCorePlugin.getInstance() == null) return
        runCatching {
            val pdcKey = getPdcKey() ?: return
            val pdc = block.chunk.persistentDataContainer
            if (set.isEmpty()) {
                pdc.remove(pdcKey)
            } else {
                pdc.set(pdcKey, PersistentDataType.LONG_ARRAY, set.toLongArray())
            }
        }
    }

    private fun getPdcKey(): NamespacedKey? {
        val plugin = ZenCorePlugin.getInstance() ?: return null
        return runCatching { NamespacedKey(plugin, PDC_KEY_NAME) }.getOrNull()
    }

    private fun chunkKey(block: Block): String {
        val worldName = runCatching { block.world.name }.getOrNull() ?: "default"
        return formatChunkKey(worldName, block.x shr 4, block.z shr 4)
    }

    private fun formatChunkKey(worldName: String, chunkX: Int, chunkZ: Int): String {
        return "$worldName:$chunkX:$chunkZ"
    }

    private fun packCoords(x: Int, y: Int, z: Int): Long {
        return ((x.toLong() and 0x3FFFFFFL) shl 38) or
            ((z.toLong() and 0x3FFFFFFL) shl 12) or
            (y.toLong() and 0xFFFL)
    }
}
