package com.cuac_xd.zencore.engine.context

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.UUID

/**
 * High-level PersistentDataContainer (PDC) helper for safe, typed metadata storage on
 * Items, Entities, Blocks, and Players without verbose boilerplate.
 */
object ZenPDC {
    private var plugin: Plugin? = null

    @JvmStatic
    fun init(pluginInstance: Plugin) {
        plugin = pluginInstance
    }

    private fun key(name: String): NamespacedKey {
        val p = plugin ?: throw IllegalStateException("ZenPDC is not initialized with a Plugin instance!")
        return NamespacedKey(p, name.lowercase())
    }

    @JvmStatic
    fun setString(holder: PersistentDataHolder, keyName: String, value: String) {
        holder.persistentDataContainer.set(key(keyName), PersistentDataType.STRING, value)
    }

    @JvmStatic
    fun getString(holder: PersistentDataHolder, keyName: String): String? {
        return holder.persistentDataContainer.get(key(keyName), PersistentDataType.STRING)
    }

    @JvmStatic
    fun setInt(holder: PersistentDataHolder, keyName: String, value: Int) {
        holder.persistentDataContainer.set(key(keyName), PersistentDataType.INTEGER, value)
    }

    @JvmStatic
    fun getInt(holder: PersistentDataHolder, keyName: String, default: Int = 0): Int {
        return holder.persistentDataContainer.get(key(keyName), PersistentDataType.INTEGER) ?: default
    }

    @JvmStatic
    fun setDouble(holder: PersistentDataHolder, keyName: String, value: Double) {
        holder.persistentDataContainer.set(key(keyName), PersistentDataType.DOUBLE, value)
    }

    @JvmStatic
    fun getDouble(holder: PersistentDataHolder, keyName: String, default: Double = 0.0): Double {
        return holder.persistentDataContainer.get(key(keyName), PersistentDataType.DOUBLE) ?: default
    }

    @JvmStatic
    fun setLong(holder: PersistentDataHolder, keyName: String, value: Long) {
        holder.persistentDataContainer.set(key(keyName), PersistentDataType.LONG, value)
    }

    @JvmStatic
    fun getLong(holder: PersistentDataHolder, keyName: String, default: Long = 0L): Long {
        return holder.persistentDataContainer.get(key(keyName), PersistentDataType.LONG) ?: default
    }

    @JvmStatic
    fun setBoolean(holder: PersistentDataHolder, keyName: String, value: Boolean) {
        holder.persistentDataContainer.set(key(keyName), PersistentDataType.BYTE, if (value) 1.toByte() else 0.toByte())
    }

    @JvmStatic
    fun getBoolean(holder: PersistentDataHolder, keyName: String, default: Boolean = false): Boolean {
        val b = holder.persistentDataContainer.get(key(keyName), PersistentDataType.BYTE) ?: return default
        return b == 1.toByte()
    }

    @JvmStatic
    fun has(holder: PersistentDataHolder, keyName: String): Boolean {
        return holder.persistentDataContainer.has(key(keyName))
    }

    @JvmStatic
    fun remove(holder: PersistentDataHolder, keyName: String) {
        holder.persistentDataContainer.remove(key(keyName))
    }
}
