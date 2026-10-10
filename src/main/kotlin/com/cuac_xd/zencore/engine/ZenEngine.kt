package com.cuac_xd.zencore.engine

import com.cuac_xd.zencore.engine.condition.ConditionRegistry
import com.cuac_xd.zencore.engine.condition.impl.CoreConditions
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.engine.effect.EffectRegistry
import com.cuac_xd.zencore.engine.effect.impl.CoreEffects
import com.cuac_xd.zencore.engine.effect.impl.ExtendedEffects
import com.cuac_xd.zencore.engine.filter.FilterRegistry
import com.cuac_xd.zencore.engine.filter.impl.CoreFilters
import com.cuac_xd.zencore.engine.mutator.MutatorRegistry
import com.cuac_xd.zencore.engine.mutator.impl.CoreMutators
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

open class ZenEngine {
    val conditionRegistry = ConditionRegistry()
    val effectRegistry = EffectRegistry()
    val filterRegistry = FilterRegistry()
    val mutatorRegistry = MutatorRegistry()

    private val cooldowns = ConcurrentHashMap<String, Long>()
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
    }

    fun testConditions(context: ZenExecutionContext, conditionConfigs: List<Map<String, Any>>?): Boolean {
        if (conditionConfigs.isNullOrEmpty()) return true
        for (condMap in conditionConfigs) {
            val condId = (condMap["id"] ?: condMap["type"]) as? String ?: continue
            val cond = conditionRegistry.get(condId) ?: continue
            @Suppress("UNCHECKED_CAST")
            val args = (condMap["args"] ?: condMap["parameters"] ?: emptyMap<String, Any>()) as Map<String, Any>
            val result = cond.test(context, args)
            val isNot = (condMap["not"] ?: condMap["inverted"] ?: false) as? Boolean ?: false
            if (if (isNot) result else !result) {
                return false
            }
        }
        return true
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

    fun executeEffect(effectId: String, context: ZenExecutionContext, rawArgs: Map<String, Any>) {
        val effect = effectRegistry.get(effectId) ?: return
        val resolved = mutableMapOf<String, Any>()
        for ((k, v) in rawArgs) {
            if (v is String) {
                resolved[k] = ContextResolver.resolvePlaceholders(v, context)
            } else {
                resolved[k] = v
            }
        }
        effect.execute(context, resolved)
    }

    fun checkAndSetCooldown(key: String, cooldownSeconds: Double): Boolean {
        if (cooldownSeconds <= 0.0) return true
        val now = System.currentTimeMillis()
        val expire = cooldowns[key] ?: 0L
        if (now < expire) return false
        cooldowns[key] = now + (cooldownSeconds * 1000.0).toLong()
        return true
    }

    fun getRemainingCooldown(key: String): Double {
        val now = System.currentTimeMillis()
        val expire = cooldowns[key] ?: 0L
        if (now >= expire) return 0.0
        return (expire - now) / 1000.0
    }

    fun clearCooldowns(uuid: UUID) {
        val prefix = "$uuid:"
        cooldowns.entries.removeIf { it.key.startsWith(prefix) }
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
