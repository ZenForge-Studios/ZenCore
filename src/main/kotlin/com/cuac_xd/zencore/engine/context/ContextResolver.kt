package com.cuac_xd.zencore.engine.context

import com.cuac_xd.zencore.integration.IntegrationHub
import com.cuac_xd.zencore.party.ZenPartyHub
import com.cuac_xd.zencore.stat.ZenStatEngine
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
            val playerName = runCatching { p.name }.getOrNull()
            if (playerName != null) {
                s = s.replace("%player%", playerName)
                s = s.replace("%player_name%", playerName)
            }
            if (s.contains("%player_health%")) {
                s = s.replace("%player_health%", runCatching { p.health.toString() }.getOrDefault("0"))
            }
            if (s.contains("%player_food%")) {
                s = s.replace("%player_food%", runCatching { p.foodLevel.toString() }.getOrDefault("0"))
            }
            if (s.contains("%player_x%") || s.contains("%player_y%") || s.contains("%player_z%")) {
                val loc = runCatching { p.location }.getOrNull()
                if (loc != null) {
                    s = s.replace("%player_x%", loc.x.toString())
                    s = s.replace("%player_y%", loc.y.toString())
                    s = s.replace("%player_z%", loc.z.toString())
                }
            }
            if (s.contains("%party_size%") || s.contains("%party_leader%") || s.contains("%in_party%")) {
                val party = runCatching { ZenPartyHub.getPartyByPlayer(p.uniqueId) }.getOrNull()
                s = s.replace("%party_size%", (party?.size ?: 0).toString())
                s = s.replace("%party_leader%", party?.leaderName ?: "")
                s = s.replace("%in_party%", (party != null).toString())
            }
            if (s.contains("%mana%") || s.contains("%max_mana%")) {
                val mana = runCatching { ZenStatEngine.getResource(p.uniqueId, "mana") }.getOrDefault(0.0)
                val maxMana = runCatching { ZenStatEngine.getMaxResource(p.uniqueId, "mana") }.getOrDefault(100.0)
                s = s.replace("%mana%", mana.toString())
                s = s.replace("%max_mana%", maxMana.toString())
            }
            if (s.contains("%booster_")) {
                if (s.contains("%booster_multiplier%")) {
                    val defaultCat = (ctx.data["booster_category"] ?: ctx.data["category"] ?: "xp").toString()
                    val mul = runCatching {
                        com.cuac_xd.zencore.booster.ZenBoosterEngine.getMultiplier(p.uniqueId, defaultCat)
                    }.getOrDefault(1.0)
                    s = s.replace("%booster_multiplier%", mul.toString())
                }
                if (s.contains("%booster_multiplier_")) {
                    val catRegex = Regex("%booster_multiplier_([a-zA-Z0-9_-]+)%")
                    s = catRegex.replace(s) { match ->
                        val cat = match.groupValues[1]
                        val mul = runCatching {
                            com.cuac_xd.zencore.booster.ZenBoosterEngine.getMultiplier(p.uniqueId, cat)
                        }.getOrDefault(1.0)
                        mul.toString()
                    }
                }
                if (s.contains("%booster_has_")) {
                    val hasRegex = Regex("%booster_has_([a-zA-Z0-9_-]+)%")
                    s = hasRegex.replace(s) { match ->
                        val cat = match.groupValues[1]
                        val active = runCatching {
                            com.cuac_xd.zencore.booster.ZenBoosterEngine.hasActiveBooster(p.uniqueId, cat)
                        }.getOrDefault(false)
                        active.toString()
                    }
                }
            }
        }
        val t = ctx.target
        if (t != null) {
            val victimName = runCatching { t.name }.getOrNull()
            if (victimName != null) {
                s = s.replace("%victim%", victimName)
                s = s.replace("%victim_name%", victimName)
                s = s.replace("%target%", victimName)
            }
            if (s.contains("%victim_health%")) {
                s = s.replace("%victim_health%", runCatching { t.health.toString() }.getOrDefault("0"))
            }
        }
        for ((k, v) in ctx.data) {
            s = s.replace("%$k%", v.toString())
            s = s.replace("{$k}", v.toString())
        }
        if (p != null && s.contains("%") && IntegrationHub.isPlaceholderApiAvailable()) {
            s = runCatching { IntegrationHub.setPlaceholders(p, s) }.getOrDefault(s)
        }
        return s
    }
}
