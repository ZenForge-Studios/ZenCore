package com.cuac_xd.zencore.item

import com.cuac_xd.zencore.util.AdventureUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType
import java.net.URI
import java.util.UUID

/**
 * Constructor fluido y avanzado de ítems para todo el ecosistema Zen (al estilo ItemStackBuilder de Eco/Libreforge).
 * Proporciona soporte integral para MiniMessage, Lore dinámico, CustomModelData, Skins/Texturas Base64/Head,
 * Armaduras de cuero coloreadas, pociones, libros encantados, NBT/PDC seguro y banderas de ocultación.
 */
class ItemBuilder(
    private var itemStack: ItemStack
) {

    constructor(material: Material, amount: Int = 1) : this(ItemStack(material, amount))

    constructor(builder: ItemBuilder) : this(builder.build())

    private var customMeta: ItemMeta? = itemStack.itemMeta?.clone()

    /**
     * Modifica el tipo de material.
     */
    fun material(material: Material): ItemBuilder {
        itemStack.type = material
        return this
    }

    /**
     * Modifica la cantidad del item (1..99).
     */
    fun amount(amount: Int): ItemBuilder {
        itemStack.amount = amount.coerceIn(1, 99)
        return this
    }

    /**
     * Establece el nombre del item usando formato MiniMessage o Códigos de color (& / §).
     */
    fun name(name: String): ItemBuilder {
        editMeta { meta ->
            meta.displayName(AdventureUtil.parse(name))
        }
        return this
    }

    /**
     * Establece el nombre del item directamente como Kyori Component.
     */
    fun name(component: Component): ItemBuilder {
        editMeta { meta ->
            meta.displayName(component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
        }
        return this
    }

    /**
     * Reemplaza el lore completo del item mediante Strings formateados.
     */
    fun lore(lore: List<String>): ItemBuilder {
        editMeta { meta ->
            meta.lore(lore.map { AdventureUtil.parse(it) })
        }
        return this
    }

    /**
     * Reemplaza el lore completo del item mediante varargs.
     */
    fun lore(vararg lines: String): ItemBuilder = lore(lines.toList())

    /**
     * Reemplaza el lore usando directamente componentes Kyori Adventure.
     */
    fun loreComponents(components: List<Component>): ItemBuilder {
        editMeta { meta ->
            meta.lore(components.map { it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) })
        }
        return this
    }

    /**
     * Añade una o más líneas de lore al final de las existentes.
     */
    fun addLore(vararg lines: String): ItemBuilder {
        editMeta { meta ->
            val existing = meta.lore()?.toMutableList() ?: mutableListOf()
            for (line in lines) {
                existing.add(AdventureUtil.parse(line))
            }
            meta.lore(existing)
        }
        return this
    }

    /**
     * Añade una lista de líneas de lore al final de las existentes.
     */
    fun addLore(lines: Iterable<String>): ItemBuilder {
        editMeta { meta ->
            val existing = meta.lore()?.toMutableList() ?: mutableListOf()
            for (line in lines) {
                existing.add(AdventureUtil.parse(line))
            }
            meta.lore(existing)
        }
        return this
    }

    /**
     * Aplica CustomModelData (para resource packs, texturas custom y modelos 3D).
     */
    fun customModelData(data: Int?): ItemBuilder {
        editMeta { meta ->
            meta.setCustomModelData(data)
        }
        return this
    }

    /**
     * Hace el item irrompible.
     */
    fun unbreakable(unbreakable: Boolean = true): ItemBuilder {
        editMeta { meta ->
            meta.isUnbreakable = unbreakable
        }
        return this
    }

    /**
     * Aplica daño/durabilidad al item.
     */
    fun damage(damage: Int): ItemBuilder {
        editMeta { meta ->
            if (meta is Damageable) {
                meta.damage = damage
            }
        }
        return this
    }

    /**
     * Añade un encantamiento estándar de Bukkit.
     */
    fun enchant(enchantment: Enchantment, level: Int = 1, ignoreLevelRestriction: Boolean = true): ItemBuilder {
        editMeta { meta ->
            if (meta is EnchantmentStorageMeta) {
                meta.addStoredEnchant(enchantment, level, ignoreLevelRestriction)
            } else {
                meta.addEnchant(enchantment, level, ignoreLevelRestriction)
            }
        }
        return this
    }

    /**
     * Remueve un encantamiento del item.
     */
    fun removeEnchant(enchantment: Enchantment): ItemBuilder {
        editMeta { meta ->
            if (meta is EnchantmentStorageMeta) {
                meta.removeStoredEnchant(enchantment)
            } else {
                meta.removeEnchant(enchantment)
            }
        }
        return this
    }

    /**
     * Añade o quita el efecto de brillo (glow / enchanted glint) sin mostrar encantamientos molestos.
     * Utiliza el componente nativo de Paper 1.20.5+ (setEnchantmentGlintOverride) para evitar añadir Unbreaking I.
     */
    fun glow(glow: Boolean = true): ItemBuilder {
        editMeta { meta ->
            meta.setEnchantmentGlintOverride(glow)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        }
        return this
    }

    /**
     * Añade una o más ItemFlags.
     */
    fun flag(vararg flags: ItemFlag): ItemBuilder {
        editMeta { meta ->
            meta.addItemFlags(*flags)
        }
        return this
    }

    /**
     * Oculta todos los atributos, encantamientos, modificadores y efectos para un aspecto limpio.
     */
    fun hideAllFlags(): ItemBuilder {
        flag(*ItemFlag.entries.toTypedArray())
        return this
    }

    /**
     * Remueve una o más ItemFlags.
     */
    fun removeFlag(vararg flags: ItemFlag): ItemBuilder {
        editMeta { meta ->
            meta.removeItemFlags(*flags)
        }
        return this
    }

    /**
     * Configura el color de piezas de armadura de cuero.
     */
    fun color(color: Color): ItemBuilder {
        editMeta { meta ->
            if (meta is LeatherArmorMeta) {
                meta.setColor(color)
            }
        }
        return this
    }

    /**
     * Configura el color a partir de un valor RGB hexadecimal (#RRGGBB).
     */
    fun color(hex: String): ItemBuilder {
        val cleanHex = hex.removePrefix("#")
        return runCatching {
            val rgb = cleanHex.toInt(16)
            color(Color.fromRGB(rgb))
        }.getOrDefault(this)
    }

    /**
     * Establece el dueño de una cabeza (Player Head) a través del Player o nombre.
     */
    @Suppress("DEPRECATION")
    fun skullOwner(owner: String): ItemBuilder {
        editMeta { meta ->
            if (meta is SkullMeta) {
                meta.owner = owner
            }
        }
        return this
    }

    /**
     * Establece la textura de una cabeza mediante Base64 o URL de skin de Minecraft.
     */
    fun skullTexture(textureOrUrl: String): ItemBuilder {
        editMeta { meta ->
            if (meta is SkullMeta) {
                applySkullTexture(meta, textureOrUrl)
            }
        }
        return this
    }

    /**
     * Configura una poción base o añade efectos de poción custom.
     */
    fun potion(effect: PotionEffect): ItemBuilder {
        editMeta { meta ->
            if (meta is PotionMeta) {
                meta.addCustomEffect(effect, true)
            }
        }
        return this
    }

    /**
     * Guarda un String en el PersistentDataContainer (PDC) con una NamespacedKey.
     */
    fun pdc(key: NamespacedKey, value: String): ItemBuilder {
        editMeta { meta ->
            meta.persistentDataContainer.set(key, PersistentDataType.STRING, value)
        }
        return this
    }

    /**
     * Guarda un entero en el PersistentDataContainer (PDC).
     */
    fun pdc(key: NamespacedKey, value: Int): ItemBuilder {
        editMeta { meta ->
            meta.persistentDataContainer.set(key, PersistentDataType.INTEGER, value)
        }
        return this
    }

    /**
     * Guarda un booleano en el PersistentDataContainer (PDC).
     */
    fun pdc(key: NamespacedKey, value: Boolean): ItemBuilder {
        editMeta { meta ->
            meta.persistentDataContainer.set(key, PersistentDataType.BYTE, if (value) 1.toByte() else 0.toByte())
        }
        return this
    }

    /**
     * Guarda un Double en el PersistentDataContainer (PDC).
     */
    fun pdc(key: NamespacedKey, value: Double): ItemBuilder {
        editMeta { meta ->
            meta.persistentDataContainer.set(key, PersistentDataType.DOUBLE, value)
        }
        return this
    }

    /**
     * Guarda un Long en el PersistentDataContainer (PDC).
     */
    fun pdc(key: NamespacedKey, value: Long): ItemBuilder {
        editMeta { meta ->
            meta.persistentDataContainer.set(key, PersistentDataType.LONG, value)
        }
        return this
    }

    /**
     * Permite mutar la ItemMeta directamente con cualquier lógica de Bukkit/Paper.
     */
    fun editMeta(consumer: (ItemMeta) -> Unit): ItemBuilder {
        val meta = customMeta ?: itemStack.itemMeta ?: Bukkit.getItemFactory().getItemMeta(itemStack.type)
        if (meta != null) {
            consumer(meta)
            customMeta = meta
        }
        return this
    }

    /**
     * Permite mutar la ItemMeta con casteo seguro de tipo.
     */
    inline fun <reified T : ItemMeta> editMetaAs(crossinline consumer: (T) -> Unit): ItemBuilder {
        editMeta { meta ->
            if (meta is T) {
                consumer(meta)
            }
        }
        return this
    }

    /**
     * Construye y retorna la instancia final de ItemStack.
     */
    fun build(): ItemStack {
        val result = itemStack.clone()
        if (customMeta != null) {
            result.itemMeta = customMeta
        }
        return result
    }

    companion object {
        @JvmStatic
        fun of(material: Material, amount: Int = 1): ItemBuilder = ItemBuilder(material, amount)

        @JvmStatic
        fun of(itemStack: ItemStack): ItemBuilder = ItemBuilder(itemStack)

        /**
         * Aplica de forma segura una textura Base64 o URL a una SkullMeta mediante GameProfile / PlayerProfile de Paper.
         */
        private fun applySkullTexture(meta: SkullMeta, texture: String) {
            runCatching {
                // Paper PlayerProfile API moderna (1.18.2+)
                val profile = Bukkit.createProfile(UUID.randomUUID())
                val textures = profile.textures
                val cleanTexture = texture.trim()

                if (cleanTexture.startsWith("http://") || cleanTexture.startsWith("https://")) {
                    textures.skin = URI.create(cleanTexture).toURL()
                    profile.setTextures(textures)
                    meta.playerProfile = profile
                } else if (cleanTexture.length > 64) {
                    // Tratar como Base64 payload decodificable o Mojang texture hash
                    val decoded = runCatching {
                        String(java.util.Base64.getDecoder().decode(cleanTexture))
                    }.getOrNull()

                    val url = if (decoded != null && decoded.contains("\"url\":")) {
                        val part = decoded.substringAfter("\"url\":").substringAfter("\"").substringBefore("\"")
                        URI.create(part).toURL()
                    } else {
                        URI.create("https://textures.minecraft.net/texture/$cleanTexture").toURL()
                    }

                    textures.skin = url
                    profile.setTextures(textures)
                    meta.playerProfile = profile
                }
            }.onFailure {
                // Si la API moderna falla, intentar fallback silencioso por reflection de GameProfile
                runCatching {
                    val gameProfileClass = Class.forName("com.mojang.authlib.GameProfile")
                    val propertyClass = Class.forName("com.mojang.authlib.properties.Property")
                    val profile = gameProfileClass.getConstructor(UUID::class.java, String::class.java)
                        .newInstance(UUID.randomUUID(), "ZenHead")
                    val propertiesMethod = gameProfileClass.getMethod("getProperties")
                    val properties = propertiesMethod.invoke(profile)
                    val putMethod = properties.javaClass.getMethod("put", Any::class.java, Any::class.java)
                    val property = propertyClass.getConstructor(String::class.java, String::class.java)
                        .newInstance("textures", texture)
                    putMethod.invoke(properties, "textures", property)

                    val profileField = meta.javaClass.getDeclaredField("profile")
                    profileField.isAccessible = true
                    profileField.set(meta, profile)
                }
            }
        }
    }
}
