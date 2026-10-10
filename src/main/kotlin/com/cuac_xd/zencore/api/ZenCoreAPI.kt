package com.cuac_xd.zencore.api

import com.cuac_xd.zencore.ZenCorePlugin
import com.cuac_xd.zencore.aura.ZenAuraEngine
import com.cuac_xd.zencore.data.ZenPlayerDataService
import com.cuac_xd.zencore.engine.ZenEngine
import com.cuac_xd.zencore.engine.condition.ZenCondition
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.engine.context.ZenPDC
import com.cuac_xd.zencore.engine.effect.ZenEffect
import com.cuac_xd.zencore.engine.filter.ZenFilter
import com.cuac_xd.zencore.engine.mutator.ZenMutator
import com.cuac_xd.zencore.loot.ZenLootEngine
import com.cuac_xd.zencore.notify.ZenNotification
import com.cuac_xd.zencore.particle.ZenParticleShapes
import com.cuac_xd.zencore.profiler.ZenProfiler
import com.cuac_xd.zencore.projectile.ZenProjectileEngine
import com.cuac_xd.zencore.protection.ZenProtectionHub
import com.cuac_xd.zencore.scheduler.ZenScheduler
import com.cuac_xd.zencore.trigger.ZenTriggerEngine
import java.util.UUID

object ZenCoreAPI {
    private var plugin: ZenCorePlugin? = null
    private var fallbackEngine: ZenEngine? = null

    @JvmStatic
    fun init(instance: ZenCorePlugin) {
        plugin = instance
        ZenPDC.init(instance)
        ZenScheduler.init(instance)
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

    // Direct access to core subsystem singletons
    @JvmStatic val triggers get() = ZenTriggerEngine
    @JvmStatic val pdc get() = ZenPDC
    @JvmStatic val projectiles get() = ZenProjectileEngine
    @JvmStatic val playerData get() = ZenPlayerDataService
    @JvmStatic val notifications get() = ZenNotification
    @JvmStatic val auras get() = ZenAuraEngine
    @JvmStatic val protection get() = ZenProtectionHub
    @JvmStatic val loot get() = ZenLootEngine
    @JvmStatic val shapes get() = ZenParticleShapes
    @JvmStatic val profiler get() = ZenProfiler
    @JvmStatic val scheduler get() = ZenScheduler

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
        engine.executeEffect(id, context, args, plugin)
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
