package com.cuac_xd.zencore.engine.condition.impl

import com.cuac_xd.zencore.engine.condition.ConditionRegistry
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.util.MathEvaluator
import org.bukkit.Material
import org.bukkit.entity.Animals
import org.bukkit.entity.Boss
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import java.util.Locale
import java.util.Random

object CoreConditions {
    private val random = Random()

    @JvmStatic
    fun registerAll(registry: ConditionRegistry) {
        // CHANCE
        registry.register("CHANCE") { ctx, args ->
            val expr = ContextResolver.resolvePlaceholders(args["chance"]?.toString() ?: args["value"]?.toString() ?: "100", ctx)
            var chance = MathEvaluator.eval(expr)
            if (chance in 0.0..1.0) chance *= 100.0
            random.nextDouble() * 100.0 <= chance
        }

        // PLAYER CHECKS
        registry.register("IS_PLAYER") { ctx, _ -> ctx.player != null }
        registry.register("PLAYER") { ctx, _ -> ctx.player != null }

        registry.register("IS_SNEAKING") { ctx, _ -> ctx.player?.isSneaking == true }
        registry.register("SNEAKING") { ctx, _ -> ctx.player?.isSneaking == true }

        registry.register("IS_SPRINTING") { ctx, _ -> ctx.player?.isSprinting == true }
        registry.register("SPRINTING") { ctx, _ -> ctx.player?.isSprinting == true }

        registry.register("IS_GLIDING") { ctx, _ -> ctx.player?.isGliding == true }
        registry.register("GLIDING") { ctx, _ -> ctx.player?.isGliding == true }

        registry.register("IS_SWIMMING") { ctx, _ -> ctx.player?.isSwimming == true }
        registry.register("SWIMMING") { ctx, _ -> ctx.player?.isSwimming == true }

        registry.register("IS_FLYING") { ctx, _ -> ctx.player?.isFlying == true }
        registry.register("FLYING") { ctx, _ -> ctx.player?.isFlying == true }

        registry.register("IS_BLOCKING") { ctx, _ -> ctx.player?.isBlocking == true }
        registry.register("BLOCKING") { ctx, _ -> ctx.player?.isBlocking == true }

        registry.register("IS_BURNING") { ctx, _ -> (ctx.player?.fireTicks ?: 0) > 0 }
        registry.register("IS_ON_FIRE") { ctx, _ -> (ctx.player?.fireTicks ?: 0) > 0 }
        registry.register("ON_FIRE") { ctx, _ -> (ctx.player?.fireTicks ?: 0) > 0 }

        registry.register("IS_IN_WATER") { ctx, _ -> ctx.player?.isInWater == true }
        registry.register("IN_WATER") { ctx, _ -> ctx.player?.isInWater == true }

        registry.register("IS_IN_LAVA") { ctx, _ -> ctx.player?.location?.block?.type == Material.LAVA }
        registry.register("IN_LAVA") { ctx, _ -> ctx.player?.location?.block?.type == Material.LAVA }

        registry.register("IS_IN_AIR") { ctx, _ ->
            val p = ctx.player ?: return@register false
            !p.isOnGround && !p.isInWater
        }
        registry.register("IN_AIR") { ctx, args -> registry.get("IS_IN_AIR")?.test(ctx, args) == true }

        registry.register("IS_ON_GROUND") { ctx, _ -> ctx.player?.isOnGround == true }
        registry.register("ON_GROUND") { ctx, _ -> ctx.player?.isOnGround == true }

        // TARGET / VICTIM CHECKS
        registry.register("VICTIM_IS_ALIVE") { ctx, _ -> ctx.target != null && !ctx.target!!.isDead }
        registry.register("VICTIM_IS_PLAYER") { ctx, _ -> ctx.target is Player }
        registry.register("VICTIM_IS_MONSTER") { ctx, _ -> ctx.target is Monster }
        registry.register("VICTIM_IS_ANIMAL") { ctx, _ -> ctx.target is Animals }
        registry.register("VICTIM_IS_BOSS") { ctx, _ -> ctx.target is Boss }

        // HEALTH & FOOD
        registry.register("HEALTH_BELOW") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getDouble(args, "health", ContextResolver.getDouble(args, "amount", ContextResolver.getDouble(args, "value", 10.0)))
            p.health < limit
        }

        registry.register("HEALTH_ABOVE") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getDouble(args, "health", ContextResolver.getDouble(args, "amount", ContextResolver.getDouble(args, "value", 10.0)))
            p.health > limit
        }

        registry.register("HEALTH_PERCENT_BELOW") { ctx, args ->
            val p = ctx.player ?: return@register false
            val percent = ContextResolver.getDouble(args, "percent", ContextResolver.getDouble(args, "value", 50.0))
            (p.health / p.maxHealth) * 100.0 < percent
        }

        registry.register("VICTIM_HEALTH_BELOW") { ctx, args ->
            val t = ctx.target ?: return@register false
            val limit = ContextResolver.getDouble(args, "health", ContextResolver.getDouble(args, "amount", ContextResolver.getDouble(args, "value", 10.0)))
            t.health < limit
        }

        registry.register("VICTIM_HEALTH_PERCENT_BELOW") { ctx, args ->
            val t = ctx.target ?: return@register false
            val percent = ContextResolver.getDouble(args, "percent", ContextResolver.getDouble(args, "value", 50.0))
            (t.health / t.maxHealth) * 100.0 < percent
        }

        registry.register("FOOD_BELOW") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getInt(args, "food", ContextResolver.getInt(args, "amount", ContextResolver.getInt(args, "value", 6)))
            p.foodLevel < limit
        }

        registry.register("FOOD_ABOVE") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getInt(args, "food", ContextResolver.getInt(args, "amount", ContextResolver.getInt(args, "value", 14)))
            p.foodLevel > limit
        }

        // EXP & LEVEL
        registry.register("LEVEL_ABOVE") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getInt(args, "level", ContextResolver.getInt(args, "value", 30))
            p.level >= limit
        }

        registry.register("LEVEL_BELOW") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getInt(args, "level", ContextResolver.getInt(args, "value", 30))
            p.level < limit
        }

        // PERMISSION & POTIONS
        registry.register("HAS_PERMISSION") { ctx, args ->
            val p = ctx.player ?: return@register false
            val perm = (args["permission"] ?: args["value"]) as? String
            perm != null && p.hasPermission(perm)
        }

        registry.register("HAS_POTION_EFFECT") { ctx, args ->
            val p = ctx.player ?: return@register false
            val effectStr = (args["effect"] ?: args["value"]) as? String ?: return@register false
            val type = PotionEffectType.getByName(effectStr.uppercase(Locale.ROOT))
            type != null && p.hasPotionEffect(type)
        }

        // ENVIRONMENT & WORLD
        registry.register("IS_DAY") { ctx, _ ->
            val p = ctx.player ?: return@register false
            val time = p.world.time
            time < 12300 || time > 23850
        }

        registry.register("IS_NIGHT") { ctx, _ ->
            val p = ctx.player ?: return@register false
            val time = p.world.time
            time in 12300..23850
        }

        registry.register("IS_RAINING") { ctx, _ ->
            val p = ctx.player ?: return@register false
            p.world.hasStorm() || p.world.isThundering
        }

        registry.register("IS_THUNDERING") { ctx, _ ->
            val p = ctx.player ?: return@register false
            p.world.isThundering
        }

        registry.register("IN_NETHER") { ctx, _ ->
            ctx.player?.world?.environment == org.bukkit.World.Environment.NETHER
        }

        registry.register("IN_END") { ctx, _ ->
            ctx.player?.world?.environment == org.bukkit.World.Environment.THE_END
        }

        registry.register("IN_OVERWORLD") { ctx, _ ->
            ctx.player?.world?.environment == org.bukkit.World.Environment.NORMAL
        }

        // LIGHT & ELEVATION
        registry.register("LIGHT_BELOW") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getInt(args, "light", ContextResolver.getInt(args, "value", 7))
            p.location.block.lightLevel < limit
        }

        registry.register("Y_BELOW") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getDouble(args, "y", ContextResolver.getDouble(args, "value", 0.0))
            p.location.y < limit
        }

        registry.register("Y_ABOVE") { ctx, args ->
            val p = ctx.player ?: return@register false
            val limit = ContextResolver.getDouble(args, "y", ContextResolver.getDouble(args, "value", 64.0))
            p.location.y > limit
        }

        // VARIABLES
        registry.register("VARIABLE_EQUALS") { ctx, args ->
            val vName = args["variable"] as? String ?: return@register false
            val exp = args["value"]?.toString() ?: ""
            val actual = ctx.data[vName]?.toString() ?: ""
            actual.equals(exp, ignoreCase = true)
        }

        registry.register("VARIABLE_ABOVE") { ctx, args ->
            val vName = args["variable"] as? String ?: return@register false
            val limit = ContextResolver.getDouble(args, "value", 0.0)
            val actual = (ctx.data[vName] as? Number)?.toDouble() ?: 0.0
            actual > limit
        }
    }
}
