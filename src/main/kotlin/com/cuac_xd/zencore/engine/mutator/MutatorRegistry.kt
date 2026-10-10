package com.cuac_xd.zencore.engine.mutator

import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

open class MutatorRegistry {
    private val mutators = ConcurrentHashMap<String, ZenMutator>()

    fun register(id: String, mutator: ZenMutator) {
        mutators[id.uppercase(Locale.ROOT)] = mutator
    }

    fun get(id: String?): ZenMutator? {
        if (id == null) return null
        return mutators[id.uppercase(Locale.ROOT)]
    }

    fun has(id: String?): Boolean {
        if (id == null) return false
        return mutators.containsKey(id.uppercase(Locale.ROOT))
    }

    val all: Map<String, ZenMutator>
        get() = mutators
}
