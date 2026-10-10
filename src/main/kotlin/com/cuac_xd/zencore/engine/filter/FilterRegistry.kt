package com.cuac_xd.zencore.engine.filter

import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

open class FilterRegistry {
    private val filters = ConcurrentHashMap<String, ZenFilter>()

    fun register(id: String, filter: ZenFilter) {
        filters[id.uppercase(Locale.ROOT)] = filter
    }

    fun get(id: String?): ZenFilter? {
        if (id == null) return null
        return filters[id.uppercase(Locale.ROOT)]
    }

    fun has(id: String?): Boolean {
        if (id == null) return false
        return filters.containsKey(id.uppercase(Locale.ROOT))
    }

    fun testAll(context: ZenExecutionContext, filterConfigs: List<Map<String, Any>>?): Boolean {
        if (filterConfigs.isNullOrEmpty()) return true
        for (map in filterConfigs) {
            val id = (map["id"] ?: map["type"]) as? String ?: continue
            val filter = get(id) ?: continue
            @Suppress("UNCHECKED_CAST")
            val args = (map["args"] ?: map["parameters"] ?: emptyMap<String, Any>()) as Map<String, Any>
            val pass = filter.test(context, args)
            val isNot = (map["not"] ?: map["inverted"] ?: false) as? Boolean ?: false
            if (if (isNot) pass else !pass) {
                return false
            }
        }
        return true
    }

    val all: Map<String, ZenFilter>
        get() = filters
}
