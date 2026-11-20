package com.cuac_xd.zencore.engine.condition.impl

import com.cuac_xd.zencore.booster.ZenBoosterEngine
import com.cuac_xd.zencore.engine.condition.ConditionRegistry
import com.cuac_xd.zencore.engine.condition.ZenCondition
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.party.ZenPartyHub
import com.cuac_xd.zencore.stat.ZenStatEngine
import com.cuac_xd.zencore.util.MathEvaluator
import com.cuac_xd.zencore.world.ZenBlockTracker
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

        // ==========================================
        // ECOSYSTEM CONDITIONS (ZenEconomy, ZenCosmetics, ZenCustomItems, ZenEnchants)
        // ==========================================
        val hasCurrency = com.cuac_xd.zencore.engine.condition.ZenCondition { ctx, args ->
            val p = ctx.player ?: return@ZenCondition false
            val currency = (args["currency"] ?: args["type"] ?: "vault").toString()
            val amtStr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: "1", ctx)
            val amt = MathEvaluator.eval(amtStr)
            com.cuac_xd.zencore.hooks.ZenHooksHub.hasAny(p, currency, amt)
        }
        registry.register("HAS_CURRENCY", hasCurrency)
        registry.register("HAS_MONEY", hasCurrency)

        registry.register("HAS_COSMETIC") { ctx, args ->
            val p = ctx.player ?: return@register false
            val id = (args["id"] ?: args["cosmetic"])?.toString() ?: return@register false
            com.cuac_xd.zencore.hooks.ZenCosmeticsHook.hasCosmetic(p, id)
        }

        registry.register("HAS_CUSTOM_ITEM") { ctx, args ->
            val p = ctx.player ?: return@register false
            val id = (args["id"] ?: args["item"])?.toString() ?: return@register false
            val amtStr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: "1", ctx)
            val amt = MathEvaluator.eval(amtStr).toInt().coerceAtLeast(1)
            com.cuac_xd.zencore.hooks.ZenCustomItemsHook.hasItem(p, id, amt)
        }

        registry.register("HAS_ENCHANT") { ctx, args ->
            val item = ctx.item ?: return@register false
            val id = (args["enchant"] ?: args["id"] ?: args["enchantment"])?.toString() ?: return@register false
            val minLevel = ContextResolver.getInt(args, "level", 1)
            val actualLevel = com.cuac_xd.zencore.hooks.ZenEnchantsHook.getEnchantmentLevel(item, id)
            actualLevel >= minLevel
        }

        // ==========================================
        // PARTY CONDITIONS (ZenPartyHub)
        // ==========================================
        registry.register("IN_PARTY") { ctx, _ ->
            val p = ctx.player ?: return@register false
            ZenPartyHub.isInParty(p.uniqueId)
        }

        registry.register("IS_PARTY_LEADER") { ctx, _ ->
            val p = ctx.player ?: return@register false
            ZenPartyHub.isLeader(p.uniqueId)
        }

        registry.register("IS_PARTY_MODERATOR") { ctx, _ ->
            val p = ctx.player ?: return@register false
            ZenPartyHub.isModeratorOrLeader(p.uniqueId)
        }

        registry.register("SAME_PARTY") { ctx, _ ->
            val p = ctx.player ?: return@register false
            val targetPlayer = (ctx.targetEntity as? Player) ?: (ctx.target as? Player) ?: return@register false
            ZenPartyHub.areInSameParty(p.uniqueId, targetPlayer.uniqueId)
        }

        registry.register("PARTY_SIZE_ABOVE") { ctx, args ->
            val p = ctx.player ?: return@register false
            val party = ZenPartyHub.getPartyByPlayer(p.uniqueId) ?: return@register false
            val minSize = ContextResolver.getInt(args, "size", ContextResolver.getInt(args, "value", 1))
            party.size >= minSize
        }

        registry.register("PARTY_SIZE_BELOW") { ctx, args ->
            val p = ctx.player ?: return@register false
            val party = ZenPartyHub.getPartyByPlayer(p.uniqueId) ?: return@register false
            val maxSize = ContextResolver.getInt(args, "size", ContextResolver.getInt(args, "value", 2))
            party.size < maxSize
        }

        registry.register("PARTY_SIZE_EQUALS") { ctx, args ->
            val p = ctx.player ?: return@register false
            val party = ZenPartyHub.getPartyByPlayer(p.uniqueId) ?: return@register false
            val exactSize = ContextResolver.getInt(args, "size", ContextResolver.getInt(args, "value", 1))
            party.size == exactSize
        }

        registry.register("NEARBY_PARTY_MEMBERS") { ctx, args ->
            val p = ctx.player ?: return@register false
            val minCount = ContextResolver.getInt(args, "count", ContextResolver.getInt(args, "value", 1))
            val radius = ContextResolver.getDouble(args, "radius", 20.0)
            ZenPartyHub.getNearbyOnlineMembers(p, radius).count { it.uniqueId != p.uniqueId } >= minCount
        }

        registry.register("PARTY_FRIENDLY_FIRE") { ctx, _ ->
            val p = ctx.player ?: return@register false
            val party = ZenPartyHub.getPartyByPlayer(p.uniqueId) ?: return@register false
            party.friendlyFire
        }

        // ==========================================
        // BOOSTER, STAT, RESOURCE & WORLD CONDITIONS
        // ==========================================
        registry.register("HAS_ACTIVE_BOOSTER") { ctx, args ->
            val p = ctx.player ?: return@register false
            val category = (args["category"] ?: args["value"])?.toString()
            if (category.isNullOrBlank()) {
                ZenBoosterEngine.getActiveBoosters(p.uniqueId).isNotEmpty()
            } else {
                ZenBoosterEngine.hasActiveBooster(p.uniqueId, category)
            }
        }

        val statCheck = { ctx: com.cuac_xd.zencore.engine.context.ZenExecutionContext, args: Map<String, Any>, above: Boolean ->
            val p = ctx.player
            val stat = args["stat"]?.toString()
            if (p == null || stat == null) {
                false
            } else {
                val thresholdStr = ContextResolver.resolvePlaceholders((args["value"] ?: args["amount"] ?: "0").toString(), ctx)
                val threshold = MathEvaluator.eval(thresholdStr)
                val current = ZenStatEngine.getStat(p.uniqueId, stat)
                if (above) current > threshold else current < threshold
            }
        }

        registry.register("STAT_ABOVE") { ctx, args -> statCheck(ctx, args, true) }
        registry.register("STAT_BELOW") { ctx, args -> statCheck(ctx, args, false) }

        val hasResource = ZenCondition { ctx, args ->
            val p = ctx.player ?: return@ZenCondition false
            val pool = (args["pool"] ?: args["resource_pool"] ?: "mana").toString()
            val amtStr = ContextResolver.resolvePlaceholders((args["amount"] ?: args["value"] ?: "1").toString(), ctx)
            val amt = MathEvaluator.eval(amtStr)
            ZenStatEngine.hasResource(p.uniqueId, pool, amt)
        }
        registry.register("HAS_RESOURCE", hasResource)
        registry.register("HAS_MANA", hasResource)

        registry.register("IS_PLAYER_PLACED") { ctx, _ ->
            val b = ctx.block ?: return@register false
            ZenBlockTracker.isPlayerPlaced(b)
        }

        registry.register("NOT_PLAYER_PLACED") { ctx, _ ->
            val b = ctx.block ?: return@register false
            !ZenBlockTracker.isPlayerPlaced(b)
        }
    }
}
