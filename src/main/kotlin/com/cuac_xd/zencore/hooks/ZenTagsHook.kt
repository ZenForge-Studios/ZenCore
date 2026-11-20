package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.lang.reflect.Modifier
import java.util.UUID

/**
 * Hook desacoplado y seguro para interactuar con ZenTags desde ZenCore y cualquier plugin del ecosistema Zen.
 * Utiliza reflexión segura sobre `com.cuac_xd.zentags.api.ZenTagsAPI` para evitar acoplamiento duro.
 */
object ZenTagsHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zentags.api.ZenTagsAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val server = Bukkit.getServer() as Any? ?: return@runCatching false
            val plugin = server.let { Bukkit.getPluginManager().getPlugin("ZenTags") }
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    // ==========================================
    // CONSULTAS
    // ==========================================

    @JvmStatic
    fun hasTag(player: Player, tagId: String): Boolean {
        if (!isInstalled()) return false
        return invokeStatic(
            "hasTag",
            arrayOf(Player::class.java, String::class.java),
            arrayOf(player, tagId)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun hasEquippedTag(player: Player, tagId: String): Boolean =
        hasEquippedTag(player.uniqueId, tagId)

    @JvmStatic
    fun hasEquippedTag(playerUuid: UUID, tagId: String): Boolean {
        if (!isInstalled()) return false
        return invokeStatic(
            "hasEquippedTag",
            arrayOf(UUID::class.java, String::class.java),
            arrayOf(playerUuid, tagId)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun getActiveTagIds(playerUuid: UUID): List<String> {
        if (!isInstalled()) return emptyList()
        return invokeStatic(
            "getActiveTagIds",
            arrayOf(UUID::class.java),
            arrayOf(playerUuid)
        ) as? List<String> ?: emptyList()
    }

    @JvmStatic
    fun getActiveTagIds(player: Player): List<String> = getActiveTagIds(player.uniqueId)

    @JvmStatic
    fun getActiveTagCount(playerUuid: UUID): Int = getActiveTagIds(playerUuid).size

    @JvmStatic
    fun getActiveTagCount(player: Player): Int = getActiveTagIds(player.uniqueId).size

    @JvmStatic
    fun getMaxTags(player: Player): Int {
        if (!isInstalled()) return 1
        return (invokeStatic(
            "getMaxTags",
            arrayOf(Player::class.java),
            arrayOf(player)
        ) as? Number)?.toInt() ?: 1
    }

    @JvmStatic
    fun getFormattedTagsLegacy(player: OfflinePlayer): String {
        if (!isInstalled()) return ""
        return invokeStatic(
            "getFormattedTagsLegacy",
            arrayOf(OfflinePlayer::class.java),
            arrayOf(player)
        ) as? String ?: ""
    }

    @JvmStatic
    fun getFormattedTagsRaw(player: OfflinePlayer): String {
        if (!isInstalled()) return ""
        return invokeStatic(
            "getFormattedTagsRaw",
            arrayOf(OfflinePlayer::class.java),
            arrayOf(player)
        ) as? String ?: ""
    }

    // ==========================================
    // ACCIONES
    // ==========================================

    @JvmStatic
    fun giveTag(player: Player, tagId: String): Boolean = giveTag(player.uniqueId, tagId)

    @JvmStatic
    fun giveTag(playerUuid: UUID, tagId: String): Boolean {
        if (!isInstalled()) return false
        return invokeStatic(
            "grantTag",
            arrayOf(UUID::class.java, String::class.java),
            arrayOf(playerUuid, tagId)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun takeTag(player: Player, tagId: String): Boolean = takeTag(player.uniqueId, tagId)

    @JvmStatic
    fun takeTag(playerUuid: UUID, tagId: String): Boolean {
        if (!isInstalled()) return false
        return invokeStatic(
            "revokeTag",
            arrayOf(UUID::class.java, String::class.java),
            arrayOf(playerUuid, tagId)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun equipTag(player: Player, tagId: String, bypassAccess: Boolean = false): Boolean {
        if (!isInstalled()) return false
        return invokeStatic(
            "tryEquipTag",
            arrayOf(Player::class.java, String::class.java, Boolean::class.javaPrimitiveType!!),
            arrayOf(player, tagId, bypassAccess)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun unequipTag(player: Player, tagId: String): Boolean {
        if (!isInstalled()) return false
        return invokeStatic(
            "tryUnequipTag",
            arrayOf(Player::class.java, String::class.java),
            arrayOf(player, tagId)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun clearTags(player: Player): Boolean {
        if (!isInstalled()) return false
        return invokeStatic(
            "clearTags",
            arrayOf(Player::class.java),
            arrayOf(player)
        ) as? Boolean ?: false
    }

    // ==========================================
    // REFLEXIÓN SEGURA
    // ==========================================

    private fun invokeStatic(methodName: String, paramTypes: Array<Class<*>>, args: Array<Any?>): Any? {
        return runCatching {
            val clazz = Class.forName(API_CLASS_NAME)
            val method = clazz.getMethod(methodName, *paramTypes)
            if (Modifier.isStatic(method.modifiers)) {
                method.invoke(null, *args)
            } else {
                val instance = clazz.getDeclaredField("INSTANCE").get(null)
                method.invoke(instance, *args)
            }
        }.getOrNull()
    }
}
