package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * Hook desacoplado para ZenCrates dentro del ecosistema ZenCore.
 * Interactúa de forma segura con `com.cuac_xd.zencrates.api.ZenCratesAPI`.
 */
object ZenCratesHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zencrates.api.ZenCratesAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        return runCatching {
            val plugin = Bukkit.getPluginManager().getPlugin("ZenCrates")
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    private fun getApiClass(): Class<*>? {
        if (!isInstalled()) return null
        return runCatching { Class.forName(API_CLASS_NAME) }.getOrNull()
    }

    @JvmStatic
    fun getCrateIds(): List<String> {
        val clazz = getApiClass() ?: return emptyList()
        return runCatching {
            val method = clazz.getMethod("getCrates")
            val crates = method.invoke(null) as? Collection<*> ?: return emptyList()
            crates.mapNotNull { crate ->
                crate?.javaClass?.getMethod("getId")?.invoke(crate)?.toString()
            }
        }.getOrDefault(emptyList())
    }

    @JvmStatic
    fun getKeyIds(): List<String> {
        val clazz = getApiClass() ?: return emptyList()
        return runCatching {
            val method = clazz.getMethod("getKeys")
            val keys = method.invoke(null) as? Collection<*> ?: return emptyList()
            keys.mapNotNull { key ->
                key?.javaClass?.getMethod("getId")?.invoke(key)?.toString()
            }
        }.getOrDefault(emptyList())
    }

    @JvmStatic
    @JvmOverloads
    fun openCrate(player: Player, crateId: String, forceInstant: Boolean = false): Boolean {
        val clazz = getApiClass() ?: return false
        return runCatching {
            val getCrateMethod = clazz.getMethod("getCrate", String::class.java)
            val crate = getCrateMethod.invoke(null, crateId) ?: return false
            val openMethod = clazz.getMethod("openCrate", Player::class.java, crate.javaClass, Boolean::class.javaPrimitiveType)
            openMethod.invoke(null, player, crate, forceInstant)
            true
        }.getOrDefault(false)
    }

    @JvmStatic
    @JvmOverloads
    fun givePhysicalKey(player: Player, keyId: String, amount: Int = 1): Boolean {
        if (amount <= 0) return false
        val clazz = getApiClass() ?: return false
        return runCatching {
            val getKeyMethod = clazz.getMethod("getKey", String::class.java)
            val key = getKeyMethod.invoke(null, keyId) ?: return false
            val getKeyMgrMethod = clazz.getMethod("getKeyManager")
            val keyMgr = getKeyMgrMethod.invoke(null) ?: return false
            val giveMethod = keyMgr.javaClass.getMethod("givePhysicalKey", Player::class.java, key.javaClass, Int::class.javaPrimitiveType)
            giveMethod.invoke(keyMgr, player, key, amount)
            true
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getPhysicalKeyCount(player: Player, keyId: String): Int {
        val clazz = getApiClass() ?: return 0
        return runCatching {
            val getKeyMethod = clazz.getMethod("getKey", String::class.java)
            val key = getKeyMethod.invoke(null, keyId) ?: return 0
            val getKeyMgrMethod = clazz.getMethod("getKeyManager")
            val keyMgr = getKeyMgrMethod.invoke(null) ?: return 0
            val countMethod = keyMgr.javaClass.getMethod("getPhysicalKeyCount", Player::class.java, key.javaClass)
            (countMethod.invoke(keyMgr, player, key) as? Number)?.toInt() ?: 0
        }.getOrDefault(0)
    }

    @JvmStatic
    fun getVirtualKeys(player: Player, keyId: String): CompletableFuture<Int> {
        val clazz = getApiClass() ?: return CompletableFuture.completedFuture(0)
        return runCatching {
            val getKeyMethod = clazz.getMethod("getKey", String::class.java)
            val key = getKeyMethod.invoke(null, keyId) ?: return CompletableFuture.completedFuture(0)
            val getVirtMethod = clazz.getMethod("getVirtualKeys", Player::class.java, key.javaClass)
            @Suppress("UNCHECKED_CAST")
            getVirtMethod.invoke(null, player, key) as? CompletableFuture<Int> ?: CompletableFuture.completedFuture(0)
        }.getOrDefault(CompletableFuture.completedFuture(0))
    }

    @JvmStatic
    fun addVirtualKeys(player: Player, keyId: String, amount: Int): CompletableFuture<Boolean> {
        if (amount <= 0) return CompletableFuture.completedFuture(false)
        val clazz = getApiClass() ?: return CompletableFuture.completedFuture(false)
        return runCatching {
            val getKeyMethod = clazz.getMethod("getKey", String::class.java)
            val key = getKeyMethod.invoke(null, keyId) ?: return CompletableFuture.completedFuture(false)
            val addVirtMethod = clazz.getMethod("addVirtualKeys", Player::class.java, key.javaClass, Int::class.javaPrimitiveType)
            @Suppress("UNCHECKED_CAST")
            val future = addVirtMethod.invoke(null, player, key, amount) as? CompletableFuture<*>
            future?.thenApply { true } ?: CompletableFuture.completedFuture(false)
        }.getOrDefault(CompletableFuture.completedFuture(false))
    }
}
