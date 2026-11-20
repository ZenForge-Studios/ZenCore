package com.cuac_xd.zencore.item

import com.cuac_xd.zencore.hooks.ZenHooksHub
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.Locale

/**
 * Parser de items altamente versátil al estilo Eco/Libreforge.
 * Permite instanciar un [ItemStack] o [ItemBuilder] desde:
 * 1. Strings: "DIAMOND", "minecraft:iron_sword", "head:TextureBase64", "custom:SPAWNER_PICKAXE"
 * 2. Secciones YAML completas (material, name, lore, custom-model-data, flags, enchants, skull, etc.)
 */
object ItemParser {

    /**
     * Resuelve un ItemStack a partir de una clave simple de texto:
     * - "DIAMOND" -> Material.DIAMOND
     * - "head:<texture_or_name>" -> Cabeza con skin
     * - "custom:<id>" / "zen:<id>" -> Ítem de ZenCustomItems
     */
    @JvmStatic
    fun parseSimple(input: String, amount: Int = 1): ItemStack {
        val trimmed = input.trim()
        if (trimmed.startsWith("head:", ignoreCase = true)) {
            val textureOrName = trimmed.substring(5).trim()
            val builder = ItemBuilder(Material.PLAYER_HEAD, amount)
            if (textureOrName.length > 25) {
                builder.skullTexture(textureOrName)
            } else {
                builder.skullOwner(textureOrName)
            }
            return builder.build()
        }

        if (trimmed.startsWith("custom:", ignoreCase = true) || trimmed.startsWith("zen:", ignoreCase = true)) {
            val id = trimmed.substringAfter(":")
            val custom = ZenHooksHub.customItems.createItem(id, amount)
            if (custom != null) return custom
        }

        val mat = matchMaterial(trimmed) ?: Material.STONE
        return ItemStack(mat, amount)
    }

    /**
     * Construye un [ItemBuilder] enriquecido a partir de una [ConfigurationSection] o mapa YAML.
     * Soporta claves comunes de plugins modernos:
     * - material / item / id
     * - amount
     * - name / display-name
     * - lore
     * - custom-model-data / model-data
     * - glow / enchanted
     * - unbreakable
     * - flags / item-flags
     * - enchants / enchantments
     * - skull / texture / head
     * - color / leather-color
     */
    @JvmStatic
    fun parseSection(section: ConfigurationSection): ItemBuilder {
        val rawMat = section.getString("material")
            ?: section.getString("item")
            ?: section.getString("id")
            ?: "STONE"
        val amount = section.getInt("amount", 1)

        val builder = if (rawMat.startsWith("head:", ignoreCase = true) || rawMat.startsWith("custom:", ignoreCase = true) || rawMat.startsWith("zen:", ignoreCase = true)) {
            ItemBuilder(parseSimple(rawMat, amount))
        } else {
            val mat = matchMaterial(rawMat) ?: Material.STONE
            ItemBuilder(mat, amount)
        }

        // Nombre
        val name = section.getString("name") ?: section.getString("display-name")
        if (name != null) {
            builder.name(name)
        }

        // Lore
        val loreList = section.getStringList("lore")
        if (loreList.isNotEmpty()) {
            builder.lore(loreList)
        }

        // Custom Model Data
        if (section.contains("custom-model-data")) {
            builder.customModelData(section.getInt("custom-model-data"))
        } else if (section.contains("model-data")) {
            builder.customModelData(section.getInt("model-data"))
        }

        // Unbreakable
        if (section.getBoolean("unbreakable", false)) {
            builder.unbreakable(true)
        }

        // Glow / Glint
        if (section.getBoolean("glow", false) || section.getBoolean("glint", false) || section.getBoolean("enchanted", false)) {
            builder.glow(true)
        }

        // Color para armaduras de cuero
        val colorHex = section.getString("color") ?: section.getString("leather-color")
        if (colorHex != null) {
            builder.color(colorHex)
        }

        // Textura de cabeza
        val skull = section.getString("skull") ?: section.getString("texture") ?: section.getString("head")
        if (skull != null) {
            if (skull.length > 25) {
                builder.skullTexture(skull)
            } else {
                builder.skullOwner(skull)
            }
        }

        // Flags
        val flags = section.getStringList("flags")
            .ifEmpty { section.getStringList("item-flags") }
        if (flags.contains("*") || flags.contains("ALL")) {
            builder.hideAllFlags()
        } else {
            for (fStr in flags) {
                runCatching {
                    val flag = ItemFlag.valueOf(fStr.uppercase(Locale.ROOT))
                    builder.flag(flag)
                }
            }
        }

        // Encantamientos
        val enchantsSec = section.getConfigurationSection("enchants")
            ?: section.getConfigurationSection("enchantments")
        if (enchantsSec != null) {
            for (key in enchantsSec.getKeys(false)) {
                val lvl = enchantsSec.getInt(key, 1)
                val ench = org.bukkit.NamespacedKey.minecraft(key.lowercase(Locale.ROOT))
                val bukkitEnch = org.bukkit.Registry.ENCHANTMENT.get(ench)
                if (bukkitEnch != null) {
                    builder.enchant(bukkitEnch, lvl)
                }
            }
        }

        return builder
    }

    /**
     * Parser para Map<String, Any?> (usado en JSON o configuraciones dinámicas).
     */
    @JvmStatic
    fun parseMap(map: Map<String, Any?>): ItemBuilder {
        val rawMat = (map["material"] ?: map["item"] ?: map["id"] ?: "STONE").toString()
        val amount = (map["amount"] as? Number)?.toInt() ?: 1

        val builder = if (rawMat.startsWith("head:", ignoreCase = true) || rawMat.startsWith("custom:", ignoreCase = true) || rawMat.startsWith("zen:", ignoreCase = true)) {
            ItemBuilder(parseSimple(rawMat, amount))
        } else {
            val mat = matchMaterial(rawMat) ?: Material.STONE
            ItemBuilder(mat, amount)
        }

        val name = (map["name"] ?: map["display-name"])?.toString()
        if (name != null) builder.name(name)

        @Suppress("UNCHECKED_CAST")
        val loreList = (map["lore"] as? List<*>)?.map { it.toString() }
        if (!loreList.isNullOrEmpty()) builder.lore(loreList)

        val cmdObj = map["custom-model-data"] ?: map["model-data"]
        val cmd = (cmdObj as? Number)?.toInt() ?: cmdObj?.toString()?.toIntOrNull()
        if (cmd != null) builder.customModelData(cmd)

        if ((map["glow"] as? Boolean) == true || (map["enchanted"] as? Boolean) == true) {
            builder.glow(true)
        }

        if ((map["unbreakable"] as? Boolean) == true) {
            builder.unbreakable(true)
        }

        val hex = (map["color"] ?: map["leather-color"])?.toString()
        if (hex != null) builder.color(hex)

        val skull = (map["skull"] ?: map["texture"] ?: map["head"])?.toString()
        if (skull != null) {
            if (skull.length > 25) builder.skullTexture(skull)
            else builder.skullOwner(skull)
        }

        return builder
    }

    private fun matchMaterial(name: String): Material? {
        val clean = name.uppercase(Locale.ROOT)
            .replace("MINECRAFT:", "")
            .replace(" ", "_")
        return Material.matchMaterial(clean)
    }
}
