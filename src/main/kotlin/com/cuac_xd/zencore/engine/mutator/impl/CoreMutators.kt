package com.cuac_xd.zencore.engine.mutator.impl

import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.mutator.MutatorRegistry
import com.cuac_xd.zencore.util.MathEvaluator

object CoreMutators {
    @JvmStatic
    fun registerAll(registry: MutatorRegistry) {
        registry.register("DAMAGE_ADDITION") { ctx, args ->
            val expr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: args["value"]?.toString() ?: "0", ctx)
            val add = MathEvaluator.eval(expr)
            ctx.damage += add
        }

        registry.register("DAMAGE_MULTIPLIER") { ctx, args ->
            val expr = ContextResolver.resolvePlaceholders(args["multiplier"]?.toString() ?: args["value"]?.toString() ?: "1.0", ctx)
            val mul = MathEvaluator.eval(expr)
            ctx.damage *= mul
        }

        registry.register("SET_DAMAGE") { ctx, args ->
            val expr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: args["value"]?.toString() ?: "1.0", ctx)
            ctx.damage = MathEvaluator.eval(expr)
        }

        registry.register("LEVEL_MULTIPLIER") { ctx, args ->
            val perLevel = ContextResolver.getDouble(args, "per_level", 0.1)
            ctx.damage *= (1.0 + (ctx.level * perLevel))
        }

        registry.register("VARIABLE_ADDITION") { ctx, args ->
            val vName = args["variable"] as? String ?: return@register
            val add = ContextResolver.getDouble(args, "amount", 1.0)
            val cur = (ctx.data[vName] as? Number)?.toDouble() ?: 0.0
            ctx.data[vName] = cur + add
        }

        registry.register("VARIABLE_MULTIPLIER") { ctx, args ->
            val vName = args["variable"] as? String ?: return@register
            val mul = ContextResolver.getDouble(args, "multiplier", 1.0)
            val cur = (ctx.data[vName] as? Number)?.toDouble() ?: 1.0
            ctx.data[vName] = cur * mul
        }
    }
}
