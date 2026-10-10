package com.cuac_xd.zencore.api

import com.cuac_xd.zencore.ZenCorePlugin
import com.cuac_xd.zencore.engine.ZenEngine
import com.cuac_xd.zencore.engine.condition.ZenCondition
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.engine.effect.ZenEffect
import com.cuac_xd.zencore.engine.filter.ZenFilter
import com.cuac_xd.zencore.engine.mutator.ZenMutator
import java.util.UUID

object ZenCoreAPI {
    private var plugin: ZenCorePlugin? = null
    private var fallbackEngine: ZenEngine? = null

    @JvmStatic
    fun init(instance: ZenCorePlugin) {
        plugin = instance
    }

    @JvmStatic
    fun getPlugin(): ZenCorePlugin? = plugin

    @JvmStatic
    val engine: ZenEngine
        get() {
            val p = plugin
            if (p != null) {
                return p.engine
            }
            if (fallbackEngine == null) {
                fallbackEngine = ZenEngine()
            }
            return fallbackEngine!!
        }

    @JvmStatic
    fun registerEffect(id: String, effect: ZenEffect) {
        engine.effectRegistry.register(id, effect)
    }

    @JvmStatic
    fun registerCondition(id: String, condition: ZenCondition) {
        engine.conditionRegistry.register(id, condition)
    }

    @JvmStatic
    fun registerFilter(id: String, filter: ZenFilter) {
        engine.filterRegistry.register(id, filter)
    }

    @JvmStatic
    fun registerMutator(id: String, mutator: ZenMutator) {
        engine.mutatorRegistry.register(id, mutator)
    }

    @JvmStatic
    fun executeEffect(id: String, context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>): Boolean {
        if (!engine.effectRegistry.has(id)) return false
        engine.executeEffect(id, context, args)
        return true
    }

    @JvmStatic
    fun testCondition(id: String, context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>): Boolean {
        val condition = engine.conditionRegistry.get(id) ?: return false
        return condition.test(context, args)
    }

    @JvmStatic
    fun clearCooldowns(playerUUID: UUID) {
        engine.clearCooldowns(playerUUID)
    }
}
