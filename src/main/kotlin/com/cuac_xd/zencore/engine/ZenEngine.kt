package com.cuac_xd.zencore.engine

import com.cuac_xd.zencore.engine.condition.ConditionRegistry
import com.cuac_xd.zencore.engine.condition.impl.CoreConditions
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.engine.context.ZenPDC
import com.cuac_xd.zencore.engine.effect.EffectRegistry
import com.cuac_xd.zencore.engine.effect.impl.CoreEffects
import com.cuac_xd.zencore.engine.effect.impl.ExtendedEffects
import com.cuac_xd.zencore.engine.filter.FilterRegistry
import com.cuac_xd.zencore.engine.filter.impl.CoreFilters
import com.cuac_xd.zencore.engine.mutator.MutatorRegistry
import com.cuac_xd.zencore.engine.mutator.impl.CoreMutators
import com.cuac_xd.zencore.hooks.ZenHooksHub
import com.cuac_xd.zencore.stat.ZenStatEngine
import com.cuac_xd.zencore.util.AdventureUtil
import com.cuac_xd.zencore.util.MathEvaluator
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

open class ZenEngine {
    val conditionRegistry = ConditionRegistry()
    val effectRegistry = EffectRegistry()
    val filterRegistry = FilterRegistry()
    val mutatorRegistry = MutatorRegistry()

    private val cooldowns = ConcurrentHashMap<String, Long>()
    private val executionCounters = ConcurrentHashMap<String, AtomicInteger>()
    private val internalDamage = ThreadLocal.withInitial { false }

    init {
        registerDefaults()
    }

    private fun registerDefaults() {
        CoreFilters.registerAll(filterRegistry)
        CoreMutators.registerAll(mutatorRegistry)
        CoreConditions.registerAll(conditionRegistry)
        CoreEffects.registerAll(effectRegistry)
        ExtendedEffects.registerAll(effectRegistry)
        ZenTagsEngineSupport.registerConditions(conditionRegistry)
        ZenTagsEngineSupport.registerEffects(effectRegistry)
    }

    /**
     * Evaluates conditions supporting top-level and nested logical trees ('and', 'or', 'not', 'inverted').
     */
    fun testConditions(context: ZenExecutionContext, conditionConfigs: List<Map<String, Any>>?): Boolean {
        if (conditionConfigs.isNullOrEmpty()) return true
        for (condMap in conditionConfigs) {
            if (!testSingleCondition(context, condMap)) {
                return false
            }
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun testSingleCondition(context: ZenExecutionContext, condMap: Map<String, Any>): Boolean {
        // 1. Nested OR block
        val orBlock = condMap["or"] as? List<Map<String, Any>>
        if (orBlock != null) {
            val pass = orBlock.any { testSingleCondition(context, it) }
            val isNot = (condMap["not"] ?: condMap["inverted"] ?: false) as? Boolean ?: false
            return if (isNot) !pass else pass
        }

        // 2. Nested AND block
        val andBlock = condMap["and"] as? List<Map<String, Any>>
        if (andBlock != null) {
            val pass = andBlock.all { testSingleCondition(context, it) }
            val isNot = (condMap["not"] ?: condMap["inverted"] ?: false) as? Boolean ?: false
            return if (isNot) !pass else pass
        }

        // 3. Regular condition
        val condId = (condMap["id"] ?: condMap["type"]) as? String ?: return true
        val cond = conditionRegistry.get(condId) ?: return true
        val args = (condMap["args"] ?: condMap["parameters"] ?: emptyMap<String, Any>()) as Map<String, Any>
        val result = cond.test(context, args)
        val isNot = (condMap["not"] ?: condMap["inverted"] ?: false) as? Boolean ?: false
        return if (isNot) !result else result
    }

    fun testFilters(context: ZenExecutionContext, filterConfigs: List<Map<String, Any>>?): Boolean {
        return filterRegistry.testAll(context, filterConfigs)
    }

    fun executeMutators(context: ZenExecutionContext, mutatorConfigs: List<Map<String, Any>>?) {
        if (mutatorConfigs.isNullOrEmpty()) return
        for (mutMap in mutatorConfigs) {
            val mutId = (mutMap["id"] ?: mutMap["type"]) as? String ?: continue
            val mutator = mutatorRegistry.get(mutId) ?: continue
            @Suppress("UNCHECKED_CAST")
            val args = (mutMap["args"] ?: mutMap["parameters"] ?: emptyMap<String, Any>()) as Map<String, Any>
            mutator.mutate(context, args)
        }
    }

    private fun evalArgDouble(raw: Any?, context: ZenExecutionContext, fallback: Double = 0.0): Double {
        if (raw == null) return fallback
        if (raw is Number) return raw.toDouble()
        val resolved = ContextResolver.resolvePlaceholders(raw.toString(), context)
        return MathEvaluator.eval(resolved)
    }

    /**
     * Executes an effect supporting Libreforge optional arguments:
     * - chance: Percentage chance of activating (supports math expression)
     * - cost & cost_currency / currency: Monetary cost via ZenHooksHub (default "vault")
     * - mana_cost / resource_cost & resource_pool / pool: Resource cost via ZenStatEngine (default "mana")
     * - every: Activates only every X triggers (supports math expression)
     * - require: Logical/math expression that must evaluate > 0 to run
     * - delay: Ticks to wait before running (supports math expression)
     * - repeat: Repeated execution (times, start, increment)
     * - cooldown & cooldown_group & cooldown_message (supports math expression)
     */
    fun executeEffect(effectId: String, context: ZenExecutionContext, rawArgs: Map<String, Any>, plugin: Plugin? = null) {
        val effect = effectRegistry.get(effectId) ?: return

        val player = context.player

        // 1. CHANCE CHECK
        val chanceRaw = rawArgs["chance"]
        if (chanceRaw != null) {
            val chanceVal = evalArgDouble(chanceRaw, context, 100.0)
            if (chanceVal < 100.0) {
                val rolled = Math.random() * 100.0
                if (rolled >= chanceVal) return
            }
        }

        // 2. REQUIRE CHECK
        val requireRaw = rawArgs["require"]
        if (requireRaw != null) {
            val reqResult = evalArgDouble(requireRaw, context, 0.0)
            if (reqResult <= 0.0) return
        }

        // 3. EVERY X COUNTER
        val everyVal = evalArgDouble(rawArgs["every"], context, 0.0).toInt()
        if (everyVal > 1 && player != null) {
            val counterKey = "${player.uniqueId}:$effectId"
            val counter = executionCounters.computeIfAbsent(counterKey) { AtomicInteger(0) }
            val current = counter.incrementAndGet()
            if (current % everyVal != 0) return
        }

        // 4. COOLDOWN PRE-CHECK
        val cdRaw = evalArgDouble(rawArgs["cooldown"], context, 0.0)
        val cdGroup = (rawArgs["cooldown_group"] as? String) ?: effectId
        val cdKey = if (player != null) "${player.uniqueId}:$cdGroup" else ""
        if (cdRaw > 0.0 && player != null) {
            val remainingSecs = getRemainingCooldown(cdKey, player)
            if (remainingSecs > 0.0) {
                val sendMsg = (rawArgs["send_cooldown_message"] as? Boolean) ?: true
                if (sendMsg) {
                    val remaining = String.format(Locale.US, "%.1f", remainingSecs)
                    val defaultMsg = "<red>¡Esta habilidad está en enfriamiento! Espera <yellow>${remaining}s</yellow>.</red>"
                    val customMsg = (rawArgs["cooldown_message"] as? String)?.replace("%seconds%", remaining) ?: defaultMsg
                    runCatching { AdventureUtil.sendMessage(player, customMsg) }
                }
                return
            }
        }

        // 5. CURRENCY COST PRE-CHECK (Vault / ZenEconomy via ZenHooksHub)
        val costVal = evalArgDouble(rawArgs["cost"], context, 0.0)
        val costCurrency = (rawArgs["cost_currency"] ?: rawArgs["currency"] ?: "vault").toString()
        if (costVal > 0.0 && player != null) {
            if (!ZenHooksHub.hasAny(player, costCurrency, costVal)) {
                val sendCostMsg = (rawArgs["send_cost_message"] as? Boolean) ?: true
                if (sendCostMsg) {
                    val costFormatted = if (costVal % 1.0 == 0.0) costVal.toLong().toString() else String.format(Locale.US, "%.2f", costVal)
                    val customMsg = (rawArgs["cost_message"] as? String)
                        ?.replace("%cost%", costFormatted)
                        ?.replace("%currency%", costCurrency)
                        ?: "<red>No tienes suficiente dinero. Requiere <yellow>$costFormatted $costCurrency</yellow>.</red>"
                    runCatching { AdventureUtil.sendMessage(player, customMsg) }
                }
                return
            }
        }

        // 6. MANA / RESOURCE COST PRE-CHECK
        val resourceCostRaw = rawArgs["mana_cost"] ?: rawArgs["resource_cost"]
        val resourceCostVal = evalArgDouble(resourceCostRaw, context, 0.0)
        val resourcePool = (rawArgs["resource_pool"] ?: rawArgs["pool"] ?: "mana").toString()
        if (resourceCostVal > 0.0 && player != null) {
            if (!ZenStatEngine.hasResource(player.uniqueId, resourcePool, resourceCostVal)) {
                val sendResourceMsg = (rawArgs["send_resource_message"] as? Boolean)
                    ?: (rawArgs["send_mana_message"] as? Boolean)
                    ?: true
                if (sendResourceMsg) {
                    val costFormatted = if (resourceCostVal % 1.0 == 0.0) resourceCostVal.toLong().toString() else String.format(Locale.US, "%.1f", resourceCostVal)
                    val customMsg = ((rawArgs["resource_message"] ?: rawArgs["mana_message"]) as? String)
                        ?.replace("%cost%", costFormatted)
                        ?.replace("%amount%", costFormatted)
                        ?.replace("%pool%", resourcePool)
                        ?: "<red>No tienes suficiente <aqua>$resourcePool</aqua>. Requiere <yellow>$costFormatted</yellow>.</red>"
                    runCatching { AdventureUtil.sendMessage(player, customMsg) }
                }
                return
            }
        }

        // 7. COMMIT COOLDOWN & WITHDRAW COSTS
        if (cdRaw > 0.0 && player != null) {
            if (!checkAndSetCooldown(cdKey, cdRaw, player)) return
        }
        if (costVal > 0.0 && player != null) {
            if (!ZenHooksHub.withdrawAny(player, costCurrency, costVal)) return
        }
        if (resourceCostVal > 0.0 && player != null) {
            if (!ZenStatEngine.consumeResource(player.uniqueId, resourcePool, resourceCostVal)) return
        }

        // 8. RESOLVE RUNTIME ARGS
        val resolved = mutableMapOf<String, Any>()
        for ((k, v) in rawArgs) {
            if (v is String) {
                resolved[k] = ContextResolver.resolvePlaceholders(v, context)
            } else {
                resolved[k] = v
            }
        }

        // 9. EXECUTION DISPATCH (DELAY / REPEAT / DIRECT)
        val delayTicks = evalArgDouble(rawArgs["delay"], context, 0.0).toLong().coerceAtLeast(0L)
        @Suppress("UNCHECKED_CAST")
        val repeatCfg = rawArgs["repeat"] as? Map<String, Any>

        val pl = plugin ?: (runCatching { Bukkit.getPluginManager().getPlugin("ZenCore") }.getOrNull())

        if (repeatCfg != null && pl != null) {
            val times = evalArgDouble(repeatCfg["times"], context, 1.0).toInt().coerceAtLeast(1)
            val start = evalArgDouble(repeatCfg["start"], context, 0.0)
            val increment = evalArgDouble(repeatCfg["increment"], context, 1.0)

            var count = 0
            val period = (if (delayTicks > 0L) delayTicks else 20L)
            Bukkit.getScheduler().runTaskTimer(pl, { task ->
                if (count >= times || (player != null && !player.isOnline)) {
                    task.cancel()
                    return@runTaskTimer
                }
                val currentRepeatVal = start + (count * increment)
                val repeatContextMap = resolved.toMutableMap()
                repeatContextMap["repeat_count"] = currentRepeatVal
                context.data["repeat_count"] = currentRepeatVal
                effect.execute(context, repeatContextMap)
                count++
            }, delayTicks, period)
            return
        }

        if (delayTicks > 0L && pl != null) {
            Bukkit.getScheduler().runTaskLater(pl, Runnable {
                effect.execute(context, resolved)
            }, delayTicks)
        } else {
            effect.execute(context, resolved)
        }
    }

    /**
     * Checks cooldown using in-memory cache and PDC persistence for long cooldowns (> 30 seconds).
     */
    fun checkAndSetCooldown(key: String, cooldownSeconds: Double, holder: Player? = null): Boolean {
        if (cooldownSeconds <= 0.0) return true
        val now = System.currentTimeMillis()
        val pdcKey = "cd_${key.replace(':', '_')}"

        // Check PDC if available
        if (holder != null && cooldownSeconds >= 30.0) {
            val pdcExpire = runCatching { ZenPDC.getLong(holder, pdcKey, 0L) }.getOrDefault(0L)
            if (now < pdcExpire) return false
        }

        val expire = cooldowns[key] ?: 0L
        if (now < expire) return false

        val newExpire = now + (cooldownSeconds * 1000.0).toLong()
        cooldowns[key] = newExpire

        if (holder != null && cooldownSeconds >= 30.0) {
            runCatching { ZenPDC.setLong(holder, pdcKey, newExpire) }
        }
        return true
    }

    fun getRemainingCooldown(key: String, holder: Player? = null): Double {
        val now = System.currentTimeMillis()
        var expire = cooldowns[key] ?: 0L
        if (holder != null) {
            val pdcKey = "cd_${key.replace(':', '_')}"
            val pdcExpire = runCatching { ZenPDC.getLong(holder, pdcKey, 0L) }.getOrDefault(0L)
            if (pdcExpire > expire) expire = pdcExpire
        }
        if (now >= expire) return 0.0
        return (expire - now) / 1000.0
    }

    fun clearCooldowns(uuid: UUID) {
        val prefix = "$uuid:"
        cooldowns.entries.removeIf { it.key.startsWith(prefix) }
        executionCounters.entries.removeIf { it.key.startsWith(prefix) }
    }

    fun pruneExpiredCooldowns() {
        val now = System.currentTimeMillis()
        cooldowns.entries.removeIf { it.value <= now }
    }

    fun isInternalDamage(): Boolean = internalDamage.get()

    fun damageFromEffect(target: LivingEntity, amount: Double, source: Entity?) {
        val prev = internalDamage.get()
        internalDamage.set(true)
        try {
            if (source != null) target.damage(amount, source)
            else target.damage(amount)
        } finally {
            internalDamage.set(prev)
        }
    }
}
