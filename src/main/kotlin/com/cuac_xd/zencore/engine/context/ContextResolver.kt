package com.cuac_xd.zencore.engine.context

import com.cuac_xd.zencore.util.MathEvaluator
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.Locale

object ContextResolver {

    @JvmStatic
    fun resolveLivingTarget(ctx: ZenExecutionContext, targetName: String?): LivingEntity? {
        if (targetName.isNullOrBlank() || targetName.equals("DEFAULT", ignoreCase = true)) {
            return ctx.target ?: ctx.player
        }
        val lower = targetName.lowercase(Locale.ROOT).trim()
        val trig = ctx.trigger?.lowercase(Locale.ROOT) ?: ""
        val isDefend = trig.contains("defend") || trig.contains("take_damage")

        return when (lower) {
            "victim", "target" -> ctx.target
            "attacker" -> if (isDefend) ctx.target else ctx.player
            "defender", "victim_defender" -> if (isDefend) ctx.player else ctx.target
            "player", "self" -> ctx.player
            else -> ctx.target ?: ctx.player
        }
    }

    @JvmStatic
    fun resolveEntityTarget(ctx: ZenExecutionContext, targetName: String?): Entity? {
        if (targetName.isNullOrBlank() || targetName.equals("DEFAULT", ignoreCase = true)) {
            return ctx.targetEntity ?: ctx.player
        }
        val lower = targetName.lowercase(Locale.ROOT).trim()
        return when (lower) {
            "victim", "target", "entity" -> ctx.targetEntity
            "player", "self" -> ctx.player
            else -> resolveLivingTarget(ctx, targetName)
        }
    }

    @JvmStatic
    fun resolveLocation(ctx: ZenExecutionContext, targetName: String?): Location? {
        if (!targetName.isNullOrBlank()) {
            val lower = targetName.lowercase(Locale.ROOT).trim()
            if (lower == "target_block" || lower == "block") {
                val b = ctx.block
                if (b != null) return b.location.clone().add(0.5, 0.5, 0.5)
                val p = ctx.player
                if (p != null) {
                    val tb = p.getTargetBlockExact(10)
                    if (tb != null) return tb.location.clone().add(0.5, 0.5, 0.5)
                }
            }
            if (lower == "entity_in_front") {
                val p = ctx.player
                if (p != null) {
                    val trace = p.world.rayTraceEntities(p.eyeLocation, p.location.direction, 10.0) { it != p }
                    if (trace?.hitEntity != null) return trace.hitEntity?.location
                    val tb = p.getTargetBlockExact(10)
                    if (tb != null) return tb.location
                    return p.location.clone().add(p.location.direction.multiply(3.0))
                }
            }
            val ent = resolveEntityTarget(ctx, targetName)
            if (ent != null) return ent.location
        }

        if (ctx.location != null) return ctx.location
        val ent = resolveLivingTarget(ctx, targetName)
        if (ent != null) return ent.location
        if (ctx.block != null) return ctx.block?.location?.clone()?.add(0.5, 0.5, 0.5)
        if (ctx.player != null) return ctx.player?.location
        return null
    }

    @JvmStatic
    fun getDouble(config: Map<String, Any>, key: String, fallback: Double): Double {
        val v = config[key] ?: return fallback
        if (v is Number) return v.toDouble()
        return try {
            MathEvaluator.eval(v.toString())
        } catch (_: Exception) {
            fallback
        }
    }

    @JvmStatic
    fun getInt(config: Map<String, Any>, key: String, fallback: Int): Int {
        val d = getDouble(config, key, fallback.toDouble())
        return kotlin.math.round(d).toInt()
    }

    @JvmStatic
    fun getBoolean(config: Map<String, Any>, key: String, fallback: Boolean): Boolean {
        val v = config[key] ?: return fallback
        if (v is Boolean) return v
        return v.toString().toBoolean()
    }

    @JvmStatic
    fun getString(config: Map<String, Any>, key: String, fallback: String?): String? {
        val v = config[key]
        return v?.toString() ?: fallback
    }

    @JvmStatic
    fun resolvePlaceholders(text: String?, ctx: ZenExecutionContext): String {
        if (text.isNullOrEmpty()) return ""
        var s: String = text
        s = s.replace("%level%", ctx.level.toString())
        s = s.replace("%damage%", ctx.damage.toString())
        val p = ctx.player
        if (p != null) {
            s = s.replace("%player%", p.name)
            s = s.replace("%player_name%", p.name)
            s = s.replace("%player_health%", p.health.toString())
            s = s.replace("%player_food%", p.foodLevel.toString())
            s = s.replace("%player_x%", p.location.x.toString())
            s = s.replace("%player_y%", p.location.y.toString())
            s = s.replace("%player_z%", p.location.z.toString())
        }
        val t = ctx.target
        if (t != null) {
            s = s.replace("%victim%", t.name)
            s = s.replace("%victim_name%", t.name)
            s = s.replace("%target%", t.name)
            s = s.replace("%victim_health%", t.health.toString())
        }
        for ((k, v) in ctx.data) {
            s = s.replace("%$k%", v.toString())
            s = s.replace("{$k}", v.toString())
        }
        return s
    }
}
