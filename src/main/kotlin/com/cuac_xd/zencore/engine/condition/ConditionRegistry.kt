package com.cuac_xd.zencore.engine.condition

import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

open class ConditionRegistry {
    private val conditions = ConcurrentHashMap<String, ZenCondition>()

    fun register(id: String, condition: ZenCondition) {
        conditions[id.uppercase(Locale.ROOT)] = condition
    }

    fun get(id: String?): ZenCondition? {
        if (id == null) return null
        return conditions[id.uppercase(Locale.ROOT)]
    }

    fun has(id: String?): Boolean {
        if (id == null) return false
        return conditions.containsKey(id.uppercase(Locale.ROOT))
    }

    val all: Map<String, ZenCondition>
        get() = conditions
}
