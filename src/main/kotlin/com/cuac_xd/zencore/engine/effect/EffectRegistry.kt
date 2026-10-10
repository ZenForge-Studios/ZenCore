package com.cuac_xd.zencore.engine.effect

import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

open class EffectRegistry {
    private val effects = ConcurrentHashMap<String, ZenEffect>()

    fun register(id: String, effect: ZenEffect) {
        effects[id.uppercase(Locale.ROOT)] = effect
    }

    fun get(id: String?): ZenEffect? {
        if (id == null) return null
        return effects[id.uppercase(Locale.ROOT)]
    }

    fun has(id: String?): Boolean {
        if (id == null) return false
        return effects.containsKey(id.uppercase(Locale.ROOT))
    }

    val all: Map<String, ZenEffect>
        get() = effects
}
