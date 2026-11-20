package com.cuac_xd.zencore.gui

import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.item.ItemBuilder
import com.cuac_xd.zencore.item.ItemParser
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Parser de menús dinámicos a partir de archivos YAML (al estilo EcoMenus).
 * Permite cargar menús completos desde configuraciones YAML sin escribir código repetitivo.
 */
object MenuParser {

    @JvmStatic
    fun parse(
        section: ConfigurationSection,
        actionHandler: ((Player, String, ZenMenu) -> Unit)? = null
    ): ZenMenu = parse(section, emptyMap(), actionHandler)

    /**
     * Construye un [ZenMenu] a partir de una [ConfigurationSection] con reemplazo dinámico de placeholders.
     * Soporta tanto coordenadas (row, column) como índices directos (slot, slots) y máscaras visuales (mask).
     */
    @JvmStatic
    fun parse(
        section: ConfigurationSection,
        placeholders: Map<String, Any> = emptyMap(),
        actionHandler: ((Player, String, ZenMenu) -> Unit)? = null
    ): ZenMenu {
        val rawTitle = replacePlaceholders(section.getString("title") ?: "ZenMenu", placeholders)
        val rows = section.getInt("rows", 6).coerceIn(1, 6)
        val menu = ZenMenu(rows, rawTitle)

        // 1. Cargar Máscara si existe
        val maskSec = section.getConfigurationSection("mask")
        if (maskSec != null) {
            val pattern = maskSec.getStringList("pattern")
            val itemsSec = maskSec.getConfigurationSection("items")
            val maskItems = mutableMapOf<Char, ItemStack>()

            if (itemsSec != null) {
                for (key in itemsSec.getKeys(false)) {
                    val charKey = key.firstOrNull() ?: continue
                    val itemRaw = itemsSec.getString(key) ?: "GRAY_STAINED_GLASS_PANE"
                    val item = ItemBuilder(ItemParser.parseSimple(itemRaw))
                        .name(" ")
                        .hideAllFlags()
                        .build()
                    maskItems[charKey] = item
                }
            }
            if (pattern.isNotEmpty() && maskItems.isNotEmpty()) {
                menu.applyMask(MenuMask(pattern, maskItems))
            }
        }

        // 2. Cargar Items configurados
        val itemsSec = section.getConfigurationSection("items")
        if (itemsSec != null) {
            for (itemKey in itemsSec.getKeys(false)) {
                val itemConf = itemsSec.getConfigurationSection(itemKey) ?: continue
                val builder = ItemParser.parseSection(itemConf)

                val rawName = itemConf.getString("name") ?: itemConf.getString("display-name")
                if (rawName != null) {
                    builder.name(replacePlaceholders(rawName, placeholders))
                }

                val rawLore = itemConf.getStringList("lore")
                if (rawLore.isNotEmpty()) {
                    builder.lore(rawLore.map { replacePlaceholders(it, placeholders) })
                }

                val item = builder.hideAllFlags().build()
                val actionId = itemConf.getString("action") ?: itemKey
                val closeOnClick = itemConf.getBoolean("close-on-click", itemConf.getBoolean("close_on_click", false))
                val captive = itemConf.getBoolean("captive", false)
                val effectsList = parseEffectsList(itemConf)

                val targetSlots = mutableListOf<Int>()
                if (itemConf.contains("slot")) {
                    targetSlots.add(itemConf.getInt("slot"))
                }
                if (itemConf.contains("slots")) {
                    targetSlots.addAll(itemConf.getIntegerList("slots"))
                }
                if (targetSlots.isEmpty() && (itemConf.contains("row") || itemConf.contains("column"))) {
                    val row = itemConf.getInt("row", 1)
                    val column = itemConf.getInt("column", 1)
                    targetSlots.add((row - 1) * 9 + (column - 1))
                }

                for (rawSlot in targetSlots) {
                    val slot = MenuSlot(item)
                    if (captive) {
                        slot.setCaptive(true)
                    }
                    if (actionHandler != null || effectsList.isNotEmpty() || closeOnClick) {
                        slot.onClick { player, _ ->
                            if (effectsList.isNotEmpty()) {
                                val ctx = ZenExecutionContext.builder()
                                    .player(player)
                                    .location(player.location)
                                    .item(item)
                                    .trigger("menu_click")
                                    .build()
                                for ((k, v) in placeholders) {
                                    val cleanKey = k.removeSurrounding("{", "}").removeSurrounding("%", "%")
                                    ctx.setVariable(cleanKey, v)
                                }
                                val engine = ZenCoreAPI.engine
                                val plugin = ZenCoreAPI.getPlugin()
                                for ((effectId, effectArgs) in effectsList) {
                                    engine.executeEffect(effectId, ctx, effectArgs, plugin)
                                }
                            }
                            actionHandler?.invoke(player, actionId, menu)
                            if (closeOnClick) {
                                player.closeInventory()
                            }
                        }
                    }
                    menu.setSlot(rawSlot, slot)
                }
            }
        }

        return menu
    }

    private fun parseEffectsList(itemConf: ConfigurationSection): List<Pair<String, Map<String, Any>>> {
        val result = mutableListOf<Pair<String, Map<String, Any>>>()
        val rawList = itemConf.getMapList("effects")
        if (rawList.isNotEmpty()) {
            for (rawMap in rawList) {
                parseEffectMap(rawMap)?.let { result.add(it) }
            }
            return result
        }
        val sec = itemConf.getConfigurationSection("effects")
        if (sec != null) {
            for (key in sec.getKeys(false)) {
                val sub = sec.getConfigurationSection(key) ?: continue
                parseEffectMap(sub.getValues(false))?.let { result.add(it) }
            }
        }
        return result
    }

    private fun parseEffectMap(rawMap: Map<*, *>): Pair<String, Map<String, Any>>? {
        val normalized = mutableMapOf<String, Any>()
        for ((k, v) in rawMap) {
            if (k != null && v != null) {
                normalized[k.toString()] = v
            }
        }
        val id = (normalized["id"] ?: normalized["type"])?.toString()
            ?: (normalized["effect"] as? String)
            ?: return null

        val args = mutableMapOf<String, Any>()
        for ((k, v) in normalized) {
            if (k != "id" && k != "type" && k != "args" && k != "parameters") {
                args[k] = v
            }
        }
        val nested = (normalized["args"] as? Map<*, *>) ?: (normalized["parameters"] as? Map<*, *>)
        if (nested != null) {
            for ((k, v) in nested) {
                if (k != null && v != null) {
                    args[k.toString()] = v
                }
            }
        }
        return id to args
    }

    private fun replacePlaceholders(text: String, placeholders: Map<String, Any>): String {
        var formatted = text
        for ((k, v) in placeholders) {
            val cleanKey = k.removeSurrounding("{", "}").removeSurrounding("%", "%")
            formatted = formatted
                .replace("{$cleanKey}", v.toString())
                .replace("%$cleanKey%", v.toString())
        }
        return formatted
    }
}
