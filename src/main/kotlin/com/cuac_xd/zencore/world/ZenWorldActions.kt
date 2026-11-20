package com.cuac_xd.zencore.world

import com.cuac_xd.zencore.drop.ZenDropEngine
import com.cuac_xd.zencore.protection.ZenProtectionHub
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * World-manipulation utilities for vein mining, tree felling, crop replanting, and auto-smelting.
 */
object ZenWorldActions {

    private data class BlockCoord(val x: Int, val y: Int, val z: Int)

    private val miningVein = ThreadLocal.withInitial { false }
    private val fellingTree = ThreadLocal.withInitial { false }

    private val smeltMappings = mapOf(
        // Ores & ancient debris
        Material.COAL_ORE to Material.COAL,
        Material.DEEPSLATE_COAL_ORE to Material.COAL,
        Material.IRON_ORE to Material.IRON_INGOT,
        Material.DEEPSLATE_IRON_ORE to Material.IRON_INGOT,
        Material.COPPER_ORE to Material.COPPER_INGOT,
        Material.DEEPSLATE_COPPER_ORE to Material.COPPER_INGOT,
        Material.GOLD_ORE to Material.GOLD_INGOT,
        Material.DEEPSLATE_GOLD_ORE to Material.GOLD_INGOT,
        Material.NETHER_GOLD_ORE to Material.GOLD_INGOT,
        Material.REDSTONE_ORE to Material.REDSTONE,
        Material.DEEPSLATE_REDSTONE_ORE to Material.REDSTONE,
        Material.EMERALD_ORE to Material.EMERALD,
        Material.DEEPSLATE_EMERALD_ORE to Material.EMERALD,
        Material.LAPIS_ORE to Material.LAPIS_LAZULI,
        Material.DEEPSLATE_LAPIS_ORE to Material.LAPIS_LAZULI,
        Material.DIAMOND_ORE to Material.DIAMOND,
        Material.DEEPSLATE_DIAMOND_ORE to Material.DIAMOND,
        Material.NETHER_QUARTZ_ORE to Material.QUARTZ,
        Material.ANCIENT_DEBRIS to Material.NETHERITE_SCRAP,

        // Raw metals
        Material.RAW_IRON to Material.IRON_INGOT,
        Material.RAW_GOLD to Material.GOLD_INGOT,
        Material.RAW_COPPER to Material.COPPER_INGOT,

        // Sand, stone, cobble, bricks, clay, netherrack & misc blocks
        Material.SAND to Material.GLASS,
        Material.RED_SAND to Material.GLASS,
        Material.COBBLESTONE to Material.STONE,
        Material.STONE to Material.SMOOTH_STONE,
        Material.COBBLED_DEEPSLATE to Material.DEEPSLATE,
        Material.SANDSTONE to Material.SMOOTH_SANDSTONE,
        Material.RED_SANDSTONE to Material.SMOOTH_RED_SANDSTONE,
        Material.QUARTZ_BLOCK to Material.SMOOTH_QUARTZ,
        Material.BASALT to Material.SMOOTH_BASALT,
        Material.STONE_BRICKS to Material.CRACKED_STONE_BRICKS,
        Material.NETHER_BRICKS to Material.CRACKED_NETHER_BRICKS,
        Material.POLISHED_BLACKSTONE_BRICKS to Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,
        Material.DEEPSLATE_BRICKS to Material.CRACKED_DEEPSLATE_BRICKS,
        Material.DEEPSLATE_TILES to Material.CRACKED_DEEPSLATE_TILES,
        Material.CLAY_BALL to Material.BRICK,
        Material.CLAY to Material.TERRACOTTA,
        Material.NETHERRACK to Material.NETHER_BRICK,
        Material.RESIN_CLUMP to Material.RESIN_BRICK,
        Material.WET_SPONGE to Material.SPONGE,
        Material.CACTUS to Material.GREEN_DYE,
        Material.CHORUS_FRUIT to Material.POPPED_CHORUS_FRUIT,

        // Glazed terracotta
        Material.WHITE_TERRACOTTA to Material.WHITE_GLAZED_TERRACOTTA,
        Material.ORANGE_TERRACOTTA to Material.ORANGE_GLAZED_TERRACOTTA,
        Material.MAGENTA_TERRACOTTA to Material.MAGENTA_GLAZED_TERRACOTTA,
        Material.LIGHT_BLUE_TERRACOTTA to Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
        Material.YELLOW_TERRACOTTA to Material.YELLOW_GLAZED_TERRACOTTA,
        Material.LIME_TERRACOTTA to Material.LIME_GLAZED_TERRACOTTA,
        Material.PINK_TERRACOTTA to Material.PINK_GLAZED_TERRACOTTA,
        Material.GRAY_TERRACOTTA to Material.GRAY_GLAZED_TERRACOTTA,
        Material.LIGHT_GRAY_TERRACOTTA to Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
        Material.CYAN_TERRACOTTA to Material.CYAN_GLAZED_TERRACOTTA,
        Material.PURPLE_TERRACOTTA to Material.PURPLE_GLAZED_TERRACOTTA,
        Material.BLUE_TERRACOTTA to Material.BLUE_GLAZED_TERRACOTTA,
        Material.BROWN_TERRACOTTA to Material.BROWN_GLAZED_TERRACOTTA,
        Material.GREEN_TERRACOTTA to Material.GREEN_GLAZED_TERRACOTTA,
        Material.RED_TERRACOTTA to Material.RED_GLAZED_TERRACOTTA,
        Material.BLACK_TERRACOTTA to Material.BLACK_GLAZED_TERRACOTTA,

        // Food & kelp
        Material.BEEF to Material.COOKED_BEEF,
        Material.PORKCHOP to Material.COOKED_PORKCHOP,
        Material.CHICKEN to Material.COOKED_CHICKEN,
        Material.MUTTON to Material.COOKED_MUTTON,
        Material.RABBIT to Material.COOKED_RABBIT,
        Material.COD to Material.COOKED_COD,
        Material.SALMON to Material.COOKED_SALMON,
        Material.POTATO to Material.BAKED_POTATO,
        Material.KELP to Material.DRIED_KELP
    )

    /**
     * Performs a breadth-first vein mine starting at [startBlock] for blocks matching its [Material].
     * Uses a [ThreadLocal] recursion guard and checks [ZenProtectionHub.canBreak] per block.
     *
     * @return Number of blocks broken.
     */
    @JvmStatic
    @JvmOverloads
    fun mineVein(
        player: Player,
        startBlock: Block,
        maxBlocks: Int = 32,
        dropToInventory: Boolean = false
    ): Int {
        val targetMaterial = runCatching { startBlock.type }.getOrNull() ?: return 0
        if (targetMaterial.isAir || targetMaterial == Material.BEDROCK) return 0

        return breakConnectedBlocks(
            player = player,
            startBlock = startBlock,
            maxBlocks = maxBlocks,
            dropToInventory = dropToInventory,
            guard = miningVein
        ) { material -> material == targetMaterial }
    }

    /**
     * Fells connected tree blocks (`_LOG`, `_STEM`, `_WOOD`, `_HYPHAE`) starting at [startBlock].
     * Uses a [ThreadLocal] recursion guard and checks [ZenProtectionHub.canBreak] per block.
     *
     * @return Number of tree blocks broken.
     */
    @JvmStatic
    @JvmOverloads
    fun fellTree(
        player: Player,
        startBlock: Block,
        maxLogs: Int = 64,
        dropToInventory: Boolean = false
    ): Int {
        val startMaterial = runCatching { startBlock.type }.getOrNull() ?: return 0
        if (!isTreeBlock(startMaterial)) return 0

        return breakConnectedBlocks(
            player = player,
            startBlock = startBlock,
            maxBlocks = maxLogs,
            dropToInventory = dropToInventory,
            guard = fellingTree,
            matcher = ::isTreeBlock
        )
    }

    /**
     * Resets a mature [Ageable] crop [block] back to age `0` and updates its `blockData`.
     *
     * @return `true` if [block] was a mature crop and was replanted, `false` otherwise.
     */
    @JvmStatic
    fun replantCrop(block: Block): Boolean {
        val ageable = runCatching { block.blockData }.getOrNull() as? Ageable ?: return false
        if (ageable.age < ageable.maximumAge) return false
        ageable.age = 0
        block.blockData = ageable
        return true
    }

    /**
     * Maps a raw [Material] (ores, raw metals, sand, cobble, logs/wood, food, kelp, netherrack, clay)
     * to its smelted output [Material], or `null` if it cannot be smelted.
     */
    @JvmStatic
    fun getSmeltedMaterial(raw: Material): Material? {
        smeltMappings[raw]?.let { return it }
        val name = raw.name
        if (name.endsWith("_LOG") || name.endsWith("_WOOD")) {
            return Material.CHARCOAL
        }
        return null
    }

    /**
     * Converts any smeltable [ItemStack] in [drops] into its smelted counterpart while preserving stack amounts.
     * Non-smeltable items are cloned and preserved as-is.
     */
    @JvmStatic
    fun autoSmeltDrops(drops: Collection<ItemStack>): List<ItemStack> {
        if (drops.isEmpty()) return emptyList()
        return drops.mapNotNull { item ->
            val type = runCatching { item.type }.getOrNull() ?: return@mapNotNull null
            val amount = runCatching { item.amount }.getOrDefault(0)
            if (type.isAir || amount <= 0) return@mapNotNull null

            val smelted = getSmeltedMaterial(type) ?: return@mapNotNull runCatching { item.clone() }.getOrDefault(item)
            createSmeltedItem(item, smelted, amount)
        }
    }

    private fun breakConnectedBlocks(
        player: Player,
        startBlock: Block,
        maxBlocks: Int,
        dropToInventory: Boolean,
        guard: ThreadLocal<Boolean>,
        matcher: (Material) -> Boolean
    ): Int {
        if (maxBlocks <= 0) return 0
        if (guard.get() || ZenProtectionHub.isSimulatingBreak()) return 0

        guard.set(true)
        try {
            val tool = runCatching { player.inventory.itemInMainHand }.getOrNull()
            val queue = ArrayDeque<Block>()
            val visited = HashSet<BlockCoord>()
            queue.addLast(startBlock)
            visited.add(coordOf(startBlock))

            var broken = 0
            while (queue.isNotEmpty() && broken < maxBlocks) {
                val current = queue.removeFirst()
                val currentType = runCatching { current.type }.getOrNull() ?: continue
                if (!matcher(currentType)) continue
                if (!canBreakSafely(player, current)) continue

                breakSingleBlock(player, current, tool, dropToInventory)
                ZenBlockTracker.markBroken(current)
                broken++

                if (broken >= maxBlocks) break

                for (dx in -1..1) {
                    for (dy in -1..1) {
                        for (dz in -1..1) {
                            if (dx == 0 && dy == 0 && dz == 0) continue
                            val neighbor = runCatching { current.getRelative(dx, dy, dz) }.getOrNull() ?: continue
                            if (visited.add(coordOf(neighbor))) {
                                val neighborType = runCatching { neighbor.type }.getOrNull()
                                if (neighborType != null && matcher(neighborType)) {
                                    queue.addLast(neighbor)
                                }
                            }
                        }
                    }
                }
            }
            return broken
        } finally {
            guard.set(false)
        }
    }

    private fun breakSingleBlock(player: Player, block: Block, tool: ItemStack?, dropToInventory: Boolean) {
        if (dropToInventory) {
            val drops = runCatching {
                val raw = if (tool != null && !tool.type.isAir) block.getDrops(tool, player) else block.drops
                raw.toList()
            }.getOrDefault(emptyList())

            runCatching { block.type = Material.AIR }
            if (drops.isNotEmpty()) {
                runCatching { ZenDropEngine.give(player, drops, dropToGround = true, notifyOverflow = false) }
            }
            return
        }

        val brokenNaturally = if (tool != null && !tool.type.isAir) {
            runCatching { block.breakNaturally(tool) }.getOrDefault(false)
        } else {
            runCatching { block.breakNaturally() }.getOrDefault(false)
        }
        if (!brokenNaturally) {
            runCatching {
                if (!block.type.isAir) {
                    block.type = Material.AIR
                }
            }
        }
    }

    private val bukkitGetServerMethod = runCatching { Bukkit::class.java.getMethod("getServer") }.getOrNull()

    private fun isServerAvailable(): Boolean {
        return runCatching { bukkitGetServerMethod?.invoke(null) != null }.getOrDefault(false)
    }

    private fun canBreakSafely(player: Player, block: Block): Boolean {
        return if (isServerAvailable()) {
            ZenProtectionHub.canBreak(player, block)
        } else {
            ZenProtectionHub.checkHooksOnly(player, block)
        }
    }

    private fun isTreeBlock(material: Material): Boolean {
        val name = material.name
        return name.endsWith("_LOG") ||
            name.endsWith("_STEM") ||
            name.endsWith("_WOOD") ||
            name.endsWith("_HYPHAE")
    }

    private fun coordOf(block: Block): BlockCoord = BlockCoord(block.x, block.y, block.z)

    private fun createSmeltedItem(original: ItemStack, smelted: Material, amount: Int): ItemStack {
        if (isServerAvailable()) {
            return ItemStack(smelted, amount)
        }
        return runCatching { original.withType(smelted) }
            .recoverCatching { ItemStack(smelted, amount) }
            .getOrElse { StubItemStack(smelted, amount) }
    }

    private class StubItemStack(
        private val mat: Material,
        private var amt: Int
    ) : ItemStack() {
        override fun getType(): Material = mat
        override fun getAmount(): Int = amt
        override fun setAmount(amount: Int) {
            amt = amount
        }
        override fun hasItemMeta(): Boolean = false
        override fun withType(type: Material): ItemStack = StubItemStack(type, amt)
        override fun clone(): ItemStack = StubItemStack(mat, amt)
        override fun toString(): String = "ItemStack{${mat.name} x $amt}"
        override fun equals(other: Any?): Boolean =
            other is ItemStack && other.type == mat && other.amount == amt
        override fun hashCode(): Int = 31 * mat.hashCode() + amt
    }
}
