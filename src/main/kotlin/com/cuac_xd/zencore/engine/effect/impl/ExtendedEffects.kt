package com.cuac_xd.zencore.engine.effect.impl

import com.cuac_xd.zencore.ZenCorePlugin
import com.cuac_xd.zencore.drop.ZenDropEngine
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.effect.EffectRegistry
import com.cuac_xd.zencore.engine.effect.ZenEffect
import com.cuac_xd.zencore.world.ZenWorldActions
import org.bukkit.*
import org.bukkit.block.data.Ageable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.Locale

object ExtendedEffects {
    @JvmStatic
    fun registerAll(registry: EffectRegistry) {
        // BLEED
        registry.register("BLEED") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim") ?: return@register
            val ticks = ContextResolver.getInt(args, "duration", 60)
            val dmgPerSec = ContextResolver.getDouble(args, "damage", 1.0)
            val p = ctx.player
            val plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects::class.java)

            var count = 0
            val maxRuns = (ticks / 20).coerceAtLeast(1)
            Bukkit.getScheduler().runTaskTimer(plugin, { task ->
                if (!target.isValid || target.isDead || count >= maxRuns) {
                    task.cancel()
                    return@runTaskTimer
                }
                if (p != null) target.damage(dmgPerSec, p)
                else target.damage(dmgPerSec)
                target.world.spawnParticle(Particle.BLOCK, target.location.add(0.0, 1.0, 0.0), 15, 0.2, 0.4, 0.2, Material.REDSTONE_BLOCK.createBlockData())
                target.world.playSound(target.location, Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 0.8f, 1.2f)
                count++
            }, 20L, 20L)
        }

        // AOE DAMAGE / SHOCKWAVE
        val aoeDamage = ZenEffect { ctx, args ->
            val center = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "victim") ?: return@ZenEffect
            val world = center.world ?: return@ZenEffect
            val radius = ContextResolver.getDouble(args, "radius", 5.0)
            val dmg = ContextResolver.getDouble(args, "damage", ContextResolver.getDouble(args, "amount", 4.0))
            val p = ctx.player

            for (ent in world.getNearbyEntities(center, radius, radius, radius)) {
                if (ent is LivingEntity && ent != p) {
                    if (p != null) ent.damage(dmg, p)
                    else ent.damage(dmg)
                }
            }
            world.spawnParticle(Particle.EXPLOSION, center, 3, 1.0, 0.5, 1.0, 0.0)
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f)
        }
        registry.register("AOE_DAMAGE", aoeDamage)
        registry.register("SHOCKWAVE", aoeDamage)

        // FREEZE
        val freeze = ZenEffect { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim") ?: return@ZenEffect
            val duration = ContextResolver.getInt(args, "duration", 100)
            target.freezeTicks = kotlin.math.max(target.freezeTicks, duration)
            target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, duration, 2))
            target.world.spawnParticle(Particle.SNOWFLAKE, target.location.add(0.0, 1.0, 0.0), 20, 0.3, 0.5, 0.3, 0.05)
            target.world.playSound(target.location, Sound.BLOCK_POWDER_SNOW_STEP, 1.0f, 0.8f)
        }
        registry.register("FREEZE", freeze)
        registry.register("ARCTIC_FREEZE", freeze)
        registry.register("FREEZE_TARGET", freeze)

        // AUTO REPLANT / REPLANT CROPS
        val replantCrops = ZenEffect { ctx, _ ->
            val b = ctx.block ?: return@ZenEffect
            val data = runCatching { b.blockData }.getOrNull() as? Ageable ?: return@ZenEffect
            if (data.age < data.maximumAge) return@ZenEffect
            val mat = b.type
            val plugin = ZenCorePlugin.getInstance()
                ?: runCatching { org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects::class.java) }.getOrNull()
            if (ctx.event is BlockBreakEvent && plugin != null && Bukkit.getServer() != null) {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    b.type = mat
                    val newAge = b.blockData
                    if (newAge is Ageable) {
                        newAge.age = 0
                        b.blockData = newAge
                    }
                }, 2L)
            } else {
                ZenWorldActions.replantCrop(b)
            }
        }
        registry.register("REPLANT_CROPS", replantCrops)
        registry.register("AUTO_REPLANT", replantCrops)

        // AUTO SMELT / AUTOSMELT / SMELT_ORE
        val autoSmelt = ZenEffect { ctx, args ->
            val b = ctx.block ?: return@ZenEffect
            val rawDrops = runCatching {
                val p = ctx.player
                val tool = ctx.item
                when {
                    tool != null && p != null -> b.getDrops(tool, p)
                    tool != null -> b.getDrops(tool)
                    else -> b.drops
                }
            }.getOrDefault(emptyList())

            val directSmelted = runCatching { ZenWorldActions.getSmeltedMaterial(b.type) }.getOrNull()
            val hasDropSmelt = rawDrops.any { ZenWorldActions.getSmeltedMaterial(it.type) != null }
            if (directSmelted == null && !hasDropSmelt) return@ZenEffect

            val smeltedDrops = if (rawDrops.isNotEmpty()) {
                ZenWorldActions.autoSmeltDrops(rawDrops)
            } else {
                listOf(ItemStack(directSmelted!!, 1))
            }

            b.type = Material.AIR
            val telekinesis = ContextResolver.getBoolean(args, "telekinesis", false)
            val p = ctx.player
            if (telekinesis && p != null) {
                ZenDropEngine.give(p, smeltedDrops, dropToGround = true, notifyOverflow = false)
            } else {
                val dropLoc = b.location.add(0.5, 0.5, 0.5)
                for (stack in smeltedDrops) {
                    b.world.dropItemNaturally(dropLoc, stack)
                }
            }
            runCatching {
                b.world.spawnParticle(Particle.FLAME, b.location.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.02)
            }
        }
        registry.register("AUTOSMELT", autoSmelt)
        registry.register("AUTO_SMELT", autoSmelt)
        registry.register("SMELT_ORE", autoSmelt)

        // TELEPATHY
        registry.register("TELEPATHY") { ctx, _ ->
            val p = ctx.player ?: return@register
            val b = ctx.block ?: return@register
            val drops = if (ctx.item != null) b.getDrops(ctx.item!!) else b.drops
            b.type = Material.AIR
            ZenDropEngine.give(p, drops.toList(), dropToGround = true, notifyOverflow = false)
        }

        // FELL TREE / TREE FELLER
        val fellTree = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val start = ctx.block ?: return@ZenEffect
            val maxLogs = ContextResolver.getInt(args, "max_logs", ContextResolver.getInt(args, "max_blocks", 64))
            val telekinesis = ContextResolver.getBoolean(args, "telekinesis", ContextResolver.getBoolean(args, "drop_to_inventory", false))
            ZenWorldActions.fellTree(p, start, maxLogs, telekinesis)
        }
        registry.register("FELL_TREE", fellTree)
        registry.register("TREE_FELLER", fellTree)

        // MINE VEIN / VEIN MINE
        val mineVein = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val start = ctx.block ?: return@ZenEffect
            val maxBlocks = ContextResolver.getInt(args, "max_blocks", 32)
            val telekinesis = ContextResolver.getBoolean(args, "telekinesis", ContextResolver.getBoolean(args, "drop_to_inventory", false))
            ZenWorldActions.mineVein(p, start, maxBlocks, telekinesis)
        }
        registry.register("MINE_VEIN", mineVein)
        registry.register("VEIN_MINE", mineVein)

        // MINE AREA
        val mineAreaRunning = ThreadLocal.withInitial { false }
        registry.register("MINE_AREA") { ctx, args ->
            if (mineAreaRunning.get()) return@register
            val start = ctx.block ?: return@register
            val radius = ContextResolver.getInt(args, "radius", 1)
            mineAreaRunning.set(true)
            try {
                for (x in -radius..radius) {
                    for (y in -radius..radius) {
                        for (z in -radius..radius) {
                            if (x == 0 && y == 0 && z == 0) continue
                            val b = start.getRelative(x, y, z)
                            if (b.type != Material.BEDROCK && !b.type.isAir) {
                                if (ctx.item != null) b.breakNaturally(ctx.item!!)
                                else b.breakNaturally()
                            }
                        }
                    }
                }
            } finally {
                mineAreaRunning.set(false)
            }
        }

        // PROJECTILE BEAM
        registry.register("PROJECTILE_BEAM") { ctx, args ->
            val p = ctx.player ?: return@register
            val start = p.eyeLocation
            val dir = start.direction.normalize()
            val maxDist = ContextResolver.getDouble(args, "distance", 15.0)
            val step = ContextResolver.getDouble(args, "step", 0.5)
            val damage = ContextResolver.getDouble(args, "damage", 5.0)
            val pStr = (args["particle"] as? String)?.uppercase(Locale.ROOT) ?: "FLAME"
            val pType = runCatching { Particle.valueOf(pStr) }.getOrDefault(Particle.FLAME)

            var cur = 0.5
            while (cur <= maxDist) {
                val pt = start.clone().add(dir.clone().multiply(cur))
                pt.world.spawnParticle(pType, pt, 1, 0.0, 0.0, 0.0, 0.0)
                val nearby = pt.world.getNearbyEntities(pt, 0.8, 0.8, 0.8)
                for (e in nearby) {
                    if (e != p && e is LivingEntity) {
                        e.damage(damage, p)
                        pt.world.spawnParticle(Particle.EXPLOSION, pt, 1)
                        return@register
                    }
                }
                cur += step
            }
        }

        // SET GLOWING
        registry.register("SET_GLOWING") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim") ?: return@register
            target.isGlowing = ContextResolver.getBoolean(args, "glowing", true)
        }

        // DISARM
        registry.register("DISARM") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim")
            if (target is Player) {
                val hand = target.inventory.itemInMainHand
                if (!hand.type.isAir) {
                    target.inventory.setItemInMainHand(null)
                    target.world.dropItemNaturally(target.location, hand)
                    target.playSound(target.location, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f)
                }
            }
        }

        // BLACK_HOLE
        registry.register("BLACK_HOLE") { ctx, args ->
            val center = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "victim") ?: return@register
            val world = center.world ?: return@register
            val p = ctx.player
            val radius = ContextResolver.getDouble(args, "radius", 8.0)
            val pullPower = ContextResolver.getDouble(args, "pull_power", 1.2)
            world.spawnParticle(Particle.PORTAL, center.clone().add(0.0, 1.0, 0.0), 80, 0.8, 0.8, 0.8, 0.2)
            world.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f)
            for (nearby in world.getNearbyLivingEntities(center, radius)) {
                if (nearby == p) continue
                val pull = center.toVector().subtract(nearby.location.toVector()).normalize().multiply(pullPower)
                pull.setY(0.25)
                nearby.velocity = pull
            }
        }

        // WEB_SHOT
        registry.register("WEB_SHOT") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim") ?: return@register
            val feet = target.location.block
            if (feet.type.isAir || feet.isPassable) {
                feet.type = Material.COBWEB
                val ticks = ContextResolver.getInt(args, "ticks", ContextResolver.getInt(args, "duration", 5) * 20)
                val plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects::class.java)
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    if (feet.type == Material.COBWEB) feet.type = Material.AIR
                }, ticks.toLong())
            }
            target.world.spawnParticle(Particle.ITEM_COBWEB, target.location.add(0.0, 0.5, 0.0), 20, 0.3, 0.3, 0.3, 0.05)
            target.world.playSound(target.location, Sound.BLOCK_COBWEB_PLACE, 1.0f, 1.0f)
        }

        // ENDER_BOW
        registry.register("ENDER_BOW") { ctx, args ->
            val shooter = ctx.player ?: return@register
            val dest = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "victim") ?: return@register
            shooter.teleport(dest.clone().add(0.0, 0.2, 0.0))
            shooter.fallDistance = 0f
            shooter.world.playSound(shooter.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
            shooter.world.spawnParticle(Particle.PORTAL, shooter.location.add(0.0, 1.0, 0.0), 25, 0.5, 0.5, 0.5, 0.1)
        }

        // ENDERSHIFT
        registry.register("ENDERSHIFT") { ctx, args ->
            val player = ctx.player ?: return@register
            val range = ContextResolver.getDouble(args, "range", 6.0)
            val angle = Math.random() * 2 * Math.PI
            val world = player.world
            val safeLoc = player.location.clone().add(Math.cos(angle) * range, 0.0, Math.sin(angle) * range)
            safeLoc.y = world.getHighestBlockYAt(safeLoc) + 1.0
            player.teleport(safeLoc)
            player.fallDistance = 0f
            player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 60, 2))
            player.world.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f)
            player.world.spawnParticle(Particle.PORTAL, player.location.add(0.0, 1.0, 0.0), 30, 0.4, 0.6, 0.4, 0.1)
        }

        // BLOOD_PACT
        registry.register("BLOOD_PACT") { ctx, args ->
            val player = ctx.player ?: return@register
            val cost = ContextResolver.getInt(args, "exp_cost", 50)
            if (player.totalExperience >= cost) {
                player.giveExp(-cost)
                ctx.damage = 0.0
                ctx.cancelEvent()
                player.health = kotlin.math.max(1.0, player.health)
                player.world.playSound(player.location, Sound.ITEM_TOTEM_USE, 0.8f, 1.5f)
                player.world.spawnParticle(Particle.DAMAGE_INDICATOR, player.location.add(0.0, 1.0, 0.0), 15, 0.3, 0.3, 0.3, 0.1)
            }
        }

        // CURSE_CLUMSINESS
        registry.register("CURSE_CLUMSINESS") { ctx, args ->
            val player = ctx.player ?: return@register
            ctx.damage = 0.0
            ctx.cancelEvent()
            val dropWeapon = ContextResolver.getBoolean(args, "drop_weapon", false)
            val hand = player.inventory.itemInMainHand
            if (dropWeapon && !hand.type.isAir) {
                player.inventory.setItemInMainHand(null)
                player.world.dropItemNaturally(player.location, hand)
            }
            player.world.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f)
        }

        // CURSE_DECAY
        registry.register("CURSE_DECAY") { ctx, args ->
            val player = ctx.player ?: return@register
            val duration = ContextResolver.getInt(args, "duration", 60)
            val amplifier = ContextResolver.getInt(args, "amplifier", 0)
            player.addPotionEffect(PotionEffect(PotionEffectType.WITHER, duration, amplifier))
            player.world.spawnParticle(Particle.SMOKE, player.location.add(0.0, 1.0, 0.0), 10, 0.2, 0.4, 0.2, 0.02)
        }

        // BREAKING_POWER (Hypixel Skyblock Mining Stat modifier / metadata)
        val breakingPowerEffect = ZenEffect { ctx, args ->
            val player = ctx.player ?: return@ZenEffect
            val amount = ContextResolver.getInt(args, "amount", ContextResolver.getInt(args, "value", 1))
            val mode = (args["mode"] as? String)?.uppercase(Locale.ROOT) ?: "ADD" // SET or ADD
            val current = (player.getMetadata("zen_breaking_power").firstOrNull()?.asInt()) ?: 0
            val finalVal = if (mode == "SET") amount else current + amount
            val plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects::class.java)
            player.setMetadata("zen_breaking_power", org.bukkit.metadata.FixedMetadataValue(plugin, finalVal))
            ctx.setVariable("breaking_power", finalVal)

            val durationTicks = ContextResolver.getInt(args, "duration", 0)
            if (durationTicks > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    if (player.isOnline) {
                        val now = (player.getMetadata("zen_breaking_power").firstOrNull()?.asInt()) ?: 0
                        player.setMetadata("zen_breaking_power", org.bukkit.metadata.FixedMetadataValue(plugin, kotlin.math.max(0, now - amount)))
                    }
                }, durationTicks.toLong())
            }
        }
        registry.register("BREAKING_POWER", breakingPowerEffect)
        registry.register("GIVE_BREAKING_POWER", breakingPowerEffect)

        // MINING_SPEED (Hypixel Skyblock Mining Stat modifier / metadata)
        val miningSpeedEffect = ZenEffect { ctx, args ->
            val player = ctx.player ?: return@ZenEffect
            val amount = ContextResolver.getInt(args, "amount", ContextResolver.getInt(args, "value", 50))
            val mode = (args["mode"] as? String)?.uppercase(Locale.ROOT) ?: "ADD"
            val current = (player.getMetadata("zen_mining_speed").firstOrNull()?.asInt()) ?: 0
            val finalVal = if (mode == "SET") amount else current + amount
            val plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects::class.java)
            player.setMetadata("zen_mining_speed", org.bukkit.metadata.FixedMetadataValue(plugin, finalVal))
            ctx.setVariable("mining_speed", finalVal)

            val durationTicks = ContextResolver.getInt(args, "duration", 0)
            if (durationTicks > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    if (player.isOnline) {
                        val now = (player.getMetadata("zen_mining_speed").firstOrNull()?.asInt()) ?: 0
                        player.setMetadata("zen_mining_speed", org.bukkit.metadata.FixedMetadataValue(plugin, kotlin.math.max(0, now - amount)))
                    }
                }, durationTicks.toLong())
            }
        }
        registry.register("MINING_SPEED", miningSpeedEffect)
        registry.register("GIVE_MINING_SPEED", miningSpeedEffect)
    }
}
