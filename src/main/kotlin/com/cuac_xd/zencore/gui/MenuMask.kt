package com.cuac_xd.zencore.gui

import org.bukkit.inventory.ItemStack

/**
 * Sistema de máscaras y patrones visuales para rellenar fondos y bordes de menús rápidamente.
 * Ejemplo de patrón:
 * ```yaml
 * mask:
 *   pattern:
 *     - "xxxxxxxxx"
 *     - "x.......x"
 *     - "xxxxxxxxx"
 *   items:
 *     x: GRAY_STAINED_GLASS_PANE
 * ```
 */
class MenuMask(
    val pattern: List<String>,
    val items: Map<Char, ItemStack>
) {

    constructor(item: ItemStack, vararg patternLines: String) : this(
        patternLines.toList(),
        mapOf('x' to item, 'X' to item)
    )

    /**
     * Aplica la máscara directamente a un [ZenMenu].
     */
    fun apply(menu: ZenMenu) {
        val rows = menu.rows
        for (r in 0 until rows.coerceAtMost(pattern.size)) {
            val line = pattern[r]
            for (c in 0 until 9.coerceAtMost(line.length)) {
                val char = line[c]
                val item = items[char]
                if (item != null) {
                    menu.setSlot(r + 1, c + 1, MenuSlot(item))
                }
            }
        }
    }
}
