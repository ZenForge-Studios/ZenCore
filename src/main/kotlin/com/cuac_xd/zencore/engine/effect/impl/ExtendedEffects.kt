package com.cuac_xd.zencore.engine.effect.impl

import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.effect.EffectRegistry
import com.cuac_xd.zencore.engine.effect.ZenEffect
import org.bukkit.*
import org.bukkit.block.data.Ageable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

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

        // AUTO REPLANT
        registry.register("AUTO_REPLANT") { ctx, _ ->
            val b = ctx.block ?: return@register
            val data = b.blockData
            if (data is Ageable && data.age >= data.maximumAge) {
                val plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects::class.java)
                val mat = b.type
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    b.type = mat
                    val newAge = b.blockData
                    if (newAge is Ageable) {
                        newAge.age = 0
                        b.blockData = newAge
                    }
                }, 2L)
            }
        }

        // AUTO SMELT
        registry.register("AUTO_SMELT") { ctx, _ ->
            val b = ctx.block ?: return@register
            val dropMat = when (b.type) {
                Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON_BLOCK -> Material.IRON_INGOT
                Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD_BLOCK -> Material.GOLD_INGOT
                Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER_BLOCK -> Material.COPPER_INGOT
                Material.ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP
                else -> null
            }
            if (dropMat != null) {
                b.type = Material.AIR
                b.world.dropItemNaturally(b.location.add(0.5, 0.5, 0.5), ItemStack(dropMat, 1))
                b.world.spawnParticle(Particle.FLAME, b.location.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.02)
            }
        }

        // TELEPATHY
        registry.register("TELEPATHY") { ctx, _ ->
            val p = ctx.player ?: return@register
            val b = ctx.block ?: return@register
            val drops = if (ctx.item != null) b.getDrops(ctx.item!!) else b.drops
            b.type = Material.AIR
            for (d in drops) {
                val leftovers = p.inventory.addItem(d)
                leftovers.values.forEach { rem -> p.world.dropItemNaturally(p.location, rem) }
            }
        }

        // TREE FELLER
        registry.register("TREE_FELLER") { ctx, args ->
            val start = ctx.block ?: return@register
            if (!Tag.LOGS.isTagged(start.type)) return@register
            val maxBlocks = ContextResolver.getInt(args, "max_blocks", 64)
            val queue: Queue<org.bukkit.block.Block> = LinkedList()
            val visited = mutableSetOf<org.bukkit.block.Block>()
            queue.add(start)
            visited.add(start)

            var broken = 0
            while (queue.isNotEmpty() && broken < maxBlocks) {
                val cur = queue.poll()
                if (Tag.LOGS.isTagged(cur.type)) {
                    if (ctx.item != null) cur.breakNaturally(ctx.item!!)
                    else cur.breakNaturally()
                    broken++
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            for (dz in -1..1) {
                                val next = cur.getRelative(dx, dy, dz)
                                if (visited.add(next) && Tag.LOGS.isTagged(next.type)) {
                                    queue.add(next)
                                }
                            }
                        }
                    }
                }
            }
        }

        // VEIN MINE
        registry.register("VEIN_MINE") { ctx, args ->
            val start = ctx.block ?: return@register
            val oreMat = start.type
            val maxBlocks = ContextResolver.getInt(args, "max_blocks", 16)
            val queue: Queue<org.bukkit.block.Block> = LinkedList()
            val visited = mutableSetOf<org.bukkit.block.Block>()
            queue.add(start)
            visited.add(start)

            var broken = 0
            while (queue.isNotEmpty() && broken < maxBlocks) {
                val cur = queue.poll()
                if (cur.type == oreMat) {
                    if (ctx.item != null) cur.breakNaturally(ctx.item!!)
                    else cur.breakNaturally()
                    broken++
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            for (dz in -1..1) {
                                val next = cur.getRelative(dx, dy, dz)
                                if (visited.add(next) && next.type == oreMat) {
                                    queue.add(next)
                                }
                            }
                        }
                    }
                }
            }
        }

        // MINE AREA
        registry.register("MINE_AREA") { ctx, args ->
            val start = ctx.block ?: return@register
            val radius = ContextResolver.getInt(args, "radius", 1)
            for (x in -radius..radius) {
                for (y in -radius..radius) {
                    for (z in -radius..radius) {
                        val b = start.getRelative(x, y, z)
                        if (b.type != Material.BEDROCK && !b.type.isAir) {
                            if (ctx.item != null) b.breakNaturally(ctx.item!!)
                            else b.breakNaturally()
                        }
                    }
                }
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
