package com.cuac_xd.zencore.engine.mutator.impl

import com.cuac_xd.zencore.booster.BoosterStackMode
import com.cuac_xd.zencore.booster.ZenBoosterEngine
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.engine.mutator.MutatorRegistry
import com.cuac_xd.zencore.party.ZenPartyHub
import com.cuac_xd.zencore.util.MathEvaluator
import java.util.Locale

object CoreMutators {
    private fun applyMultiplier(ctx: ZenExecutionContext, variableName: String?, factor: Double) {
        if (variableName != null) {
            val cur = (ctx.data[variableName] as? Number)?.toDouble() ?: 1.0
            ctx.data[variableName] = cur * factor
        } else {
            ctx.damage *= factor
        }
    }

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

        registry.register("PARTY_SIZE_MULTIPLIER") { ctx, args ->
            val p = ctx.player ?: return@register
            val perMember = ContextResolver.getDouble(args, "per_member", 0.1)
            val partySize = ZenPartyHub.getPartyByPlayer(p.uniqueId)?.size ?: 0
            applyMultiplier(ctx, args["variable"] as? String, 1.0 + (partySize * perMember))
        }

        registry.register("APPLY_BOOSTER_MULTIPLIER") { ctx, args ->
            val category = (args["category"] ?: "damage").toString()
            val modeStr = (args["mode"] ?: "SUM").toString().uppercase(Locale.ROOT)
            val mode = runCatching { BoosterStackMode.valueOf(modeStr) }.getOrDefault(BoosterStackMode.SUM)
            val mul = ZenBoosterEngine.getMultiplier(ctx.player?.uniqueId, category, mode)
            applyMultiplier(ctx, args["variable"] as? String, mul)
        }
    }
}
