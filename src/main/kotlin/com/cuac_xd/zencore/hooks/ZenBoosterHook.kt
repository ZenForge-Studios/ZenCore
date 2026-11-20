package com.cuac_xd.zencore.hooks

import com.cuac_xd.zencore.booster.ActiveBooster
import com.cuac_xd.zencore.booster.BoosterStackMode
import com.cuac_xd.zencore.booster.ZenBoosterDefinition
import com.cuac_xd.zencore.booster.ZenBoosterEngine
import com.cuac_xd.zencore.booster.ZenBoosterListener
import com.cuac_xd.zencore.booster.ZenBoosterScope
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Modifier
import java.util.UUID

/**
 * Hook unificado para interactuar con el sistema de Boosters del ecosistema Zen.
 * Delega prioritariamente en [ZenBoosterEngine] (donde se registra ZenBoosters como proveedor)
 * y ofrece fallback reflexivo seguro sobre `com.cuac_xd.zenboosters.api.ZenBoostersAPI` para evitar acoplamiento duro.
 */
object ZenBoosterHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zenboosters.api.ZenBoostersAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        if (ZenBoosterEngine.isAvailable()) return true
        return runCatching {
            val server = Bukkit.getServer() as Any? ?: return@runCatching false
            val plugin = server.let { Bukkit.getPluginManager().getPlugin("ZenBoosters") }
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    @JvmStatic
    fun isProviderRegistered(): Boolean = ZenBoosterEngine.isAvailable()

    // ==========================================
    // CONSULTAS
    // ==========================================

    @JvmStatic
    fun hasActiveBooster(player: Player, category: String): Boolean =
        hasActiveBooster(player.uniqueId, category)

    @JvmStatic
    fun hasActiveBooster(playerUuid: UUID, category: String): Boolean {
        if (ZenBoosterEngine.hasActiveBooster(playerUuid, category)) return true
        if (!ZenBoosterEngine.isAvailable()) {
            val player = runCatching { Bukkit.getPlayer(playerUuid) }.getOrNull()
            if (player != null) {
                val fb = invokeFallback(
                    "hasActiveBooster",
                    arrayOf(Player::class.java),
                    arrayOf(player)
                ) as? Boolean
                if (fb == true) return true
            }
        }
        return false
    }

    @JvmStatic
    @JvmOverloads
    fun getMultiplier(
        player: Player,
        category: String,
        mode: BoosterStackMode = BoosterStackMode.SUM
    ): Double = getMultiplier(player.uniqueId, category, mode)

    @JvmStatic
    @JvmOverloads
    fun getMultiplier(
        playerUuid: UUID?,
        category: String,
        mode: BoosterStackMode = BoosterStackMode.SUM
    ): Double {
        val engineMul = ZenBoosterEngine.getMultiplier(playerUuid, category, mode)
        if (ZenBoosterEngine.isAvailable() || engineMul != 1.0) {
            return engineMul
        }
        val player = playerUuid?.let { uuid -> runCatching { Bukkit.getPlayer(uuid) }.getOrNull() }
        if (player != null) {
            val fb = invokeFallback(
                "getConsolidatedMultiplier",
                arrayOf(Player::class.java, String::class.java),
                arrayOf(player, category)
            ) as? Number
            if (fb != null) return fb.toDouble()
        }
        return engineMul
    }

    @JvmStatic
    @JvmOverloads
    fun getActiveBoosters(player: Player, category: String? = null): List<ActiveBooster> =
        getActiveBoosters(player.uniqueId, category)

    @JvmStatic
    @JvmOverloads
    fun getActiveBoosters(playerUuid: UUID?, category: String? = null): List<ActiveBooster> =
        ZenBoosterEngine.getAllActiveBoosters(playerUuid, category)

    @JvmStatic
    fun getGlobalBoosters(): List<ActiveBooster> = ZenBoosterEngine.getGlobalBoosters()

    @JvmStatic
    fun getTeamBoosters(player: Player): List<ActiveBooster> = ZenBoosterEngine.getTeamBoosters(player)

    @JvmStatic
    fun getPersonalBoosters(playerUuid: UUID): List<ActiveBooster> = ZenBoosterEngine.getPersonalBoosters(playerUuid)

    @JvmStatic
    fun getPrimaryGlobalBooster(): ActiveBooster? = ZenBoosterEngine.getPrimaryGlobalBooster()

    @JvmStatic
    fun getBoosterById(boosterId: String): ZenBoosterDefinition? = ZenBoosterEngine.getBoosterById(boosterId)

    @JvmStatic
    fun getAllBoosterDefinitions(): Collection<ZenBoosterDefinition> = ZenBoosterEngine.getAllBoosterDefinitions()

    // ==========================================
    // ACCIONES
    // ==========================================

    @JvmStatic
    @JvmOverloads
    fun activateBooster(
        boosterId: String,
        activator: Player,
        bypassCooldown: Boolean = false
    ): Boolean {
        if (ZenBoosterEngine.isAvailable()) {
            return ZenBoosterEngine.activateBooster(boosterId, activator, bypassCooldown)
        }
        return invokeFallback(
            "activateBooster",
            arrayOf(String::class.java, Player::class.java, Boolean::class.javaPrimitiveType!!),
            arrayOf(boosterId, activator, bypassCooldown)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun giveBooster(target: Player, boosterId: String, amount: Int = 1): Boolean =
        giveBooster(target.uniqueId, boosterId, amount)

    @JvmStatic
    @JvmOverloads
    fun giveBooster(targetUuid: UUID, boosterId: String, amount: Int = 1): Boolean {
        if (ZenBoosterEngine.isAvailable()) {
            return ZenBoosterEngine.giveBooster(targetUuid, boosterId, amount)
        }
        val player = runCatching { Bukkit.getPlayer(targetUuid) }.getOrNull() ?: return false
        return invokeFallback(
            "giveBooster",
            arrayOf(Player::class.java, String::class.java, Int::class.javaPrimitiveType!!),
            arrayOf(player, boosterId, amount)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun stopBooster(
        boosterId: String,
        scope: ZenBoosterScope? = null,
        targetUuid: UUID? = null
    ): Int = ZenBoosterEngine.stopBooster(boosterId, scope, targetUuid)

    @JvmStatic
    fun openMainMenu(player: Player) {
        ZenBoosterEngine.openMainMenu(player)
    }

    @JvmStatic
    fun openVaultMenu(player: Player) {
        ZenBoosterEngine.openVaultMenu(player)
    }

    @JvmStatic
    fun addListener(listener: ZenBoosterListener) {
        ZenBoosterEngine.addListener(listener)
    }

    @JvmStatic
    fun removeListener(listener: ZenBoosterListener) {
        ZenBoosterEngine.removeListener(listener)
    }

    // ==========================================
    // REFLEXIÓN SEGURA SOBRE ZenBoostersAPI
    // ==========================================

    private fun getApiInstance(clazz: Class<*>): Any? {
        for (methodName in arrayOf("getInstance", "get")) {
            val res = runCatching {
                val m = clazz.getMethod(methodName)
                if (Modifier.isStatic(m.modifiers)) m.invoke(null) else null
            }.getOrNull()
            if (res != null) return res
        }
        return runCatching {
            clazz.getDeclaredField("instance").apply { isAccessible = true }.get(null)
        }.getOrNull()
    }

    private fun invokeFallback(methodName: String, paramTypes: Array<Class<*>>, args: Array<Any?>): Any? {
        return runCatching {
            val clazz = Class.forName(API_CLASS_NAME)
            val method = runCatching { clazz.getMethod(methodName, *paramTypes) }
                .getOrElse { clazz.getDeclaredMethod(methodName, *paramTypes).apply { isAccessible = true } }
            if (Modifier.isStatic(method.modifiers)) {
                method.invoke(null, *args)
            } else {
                val api = getApiInstance(clazz) ?: return null
                method.invoke(api, *args)
            }
        }.getOrNull()
    }
}
