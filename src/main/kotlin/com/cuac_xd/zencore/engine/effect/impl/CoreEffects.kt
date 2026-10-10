package com.cuac_xd.zencore.engine.effect.impl

import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.effect.EffectRegistry
import com.cuac_xd.zencore.engine.effect.ZenEffect
import com.cuac_xd.zencore.util.AdventureUtil
import com.cuac_xd.zencore.util.MathEvaluator
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Damageable
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.time.Duration
import java.util.Locale

object CoreEffects {
    @JvmStatic
    fun registerAll(registry: EffectRegistry) {
        val dealDamage = ZenEffect { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim")
            if (target != null && !target.isDead) {
                val dmgStr = ContextResolver.resolvePlaceholders(args["damage"]?.toString() ?: args["amount"]?.toString() ?: "2", ctx)
                val dmg = MathEvaluator.eval(dmgStr)
                if (ctx.player != null && target != ctx.player) {
                    target.damage(dmg, ctx.player)
                } else {
                    target.damage(dmg)
                }
            }
        }
        registry.register("DAMAGE", dealDamage)
        registry.register("DEAL_DAMAGE", dealDamage)
        registry.register("DAMAGE_ENTITY", dealDamage)

        registry.register("HEAL") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player")
            if (target != null && !target.isDead) {
                val amtStr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: "2", ctx)
                val heal = MathEvaluator.eval(amtStr)
                target.health = kotlin.math.min(target.maxHealth, target.health + heal)
            }
        }

        registry.register("SET_HEALTH") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player")
            if (target != null && !target.isDead) {
                val amt = ContextResolver.getDouble(args, "amount", target.maxHealth)
                target.health = amt.coerceIn(0.0, target.maxHealth)
            }
        }

        registry.register("LIFESTEAL") { ctx, args ->
            val p = ctx.player
            if (p != null && ctx.damage > 0) {
                var pct = ContextResolver.getDouble(args, "percent", ContextResolver.getDouble(args, "percentage", 20.0))
                if (pct > 1.0) pct /= 100.0
                val heal = ctx.damage * pct
                p.health = kotlin.math.min(p.maxHealth, p.health + kotlin.math.max(0.5, heal))
                p.world.spawnParticle(Particle.HEART, p.location.add(0.0, 1.5, 0.0), 3, 0.3, 0.3, 0.3, 0.0)
            }
        }

        val feedEffect = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val amt = ContextResolver.getInt(args, "amount", 1)
            p.foodLevel = (p.foodLevel + amt).coerceIn(0, 20)
        }
        registry.register("FOOD", feedEffect)
        registry.register("FEED", feedEffect)

        registry.register("SATURATION") { ctx, args ->
            val p = ctx.player ?: return@register
            val amt = ContextResolver.getDouble(args, "amount", 1.0).toFloat()
            p.saturation = kotlin.math.max(0f, p.saturation + amt)
        }

        val expEffect = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val amt = ContextResolver.getInt(args, "amount", 10)
            p.giveExp(amt)
        }
        registry.register("EXPERIENCE", expEffect)
        registry.register("GIVE_EXP", expEffect)

        registry.register("LEVEL") { ctx, args ->
            val p = ctx.player ?: return@register
            val amt = ContextResolver.getInt(args, "amount", 1)
            p.giveExpLevels(amt)
        }

        val addPotion = ZenEffect { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player") ?: return@ZenEffect
            val effName = (args["effect"] ?: args["potion"]) as? String ?: return@ZenEffect
            val duration = ContextResolver.getInt(args, "duration", 100)
            val amplifier = ContextResolver.getInt(args, "amplifier", 0)
            try {
                val type = PotionEffectType.getByName(effName.uppercase(Locale.ROOT).replace("MINECRAFT:", ""))
                if (type != null) target.addPotionEffect(PotionEffect(type, duration, amplifier))
            } catch (_: Exception) {}
        }
        registry.register("ADD_POTION_EFFECT", addPotion)
        registry.register("POTION", addPotion)
        registry.register("POTION_EFFECT", addPotion)

        registry.register("REMOVE_POTION_EFFECT") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player") ?: return@register
            val effName = (args["effect"] ?: args["potion"]) as? String ?: return@register
            try {
                val type = PotionEffectType.getByName(effName.uppercase(Locale.ROOT).replace("MINECRAFT:", ""))
                if (type != null) target.removePotionEffect(type)
            } catch (_: Exception) {}
        }

        registry.register("CLEAR_EFFECT") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player") ?: return@register
            for (pe in target.activePotionEffects) target.removePotionEffect(pe.type)
        }

        registry.register("CLEAR_NEGATIVE_EFFECTS") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player") ?: return@register
            target.removePotionEffect(PotionEffectType.POISON)
            target.removePotionEffect(PotionEffectType.WITHER)
            target.removePotionEffect(PotionEffectType.SLOWNESS)
            target.removePotionEffect(PotionEffectType.WEAKNESS)
            target.removePotionEffect(PotionEffectType.MINING_FATIGUE)
            target.removePotionEffect(PotionEffectType.BLINDNESS)
            target.removePotionEffect(PotionEffectType.NAUSEA)
        }

        registry.register("IGNITE") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim") ?: return@register
            val duration = ContextResolver.getInt(args, "duration", 4)
            target.fireTicks = duration * 20
        }

        val lightning = ZenEffect { ctx, args ->
            val loc = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "victim") ?: return@ZenEffect
            val visual = ContextResolver.getBoolean(args, "visual", ContextResolver.getBoolean(args, "effect_only", false))
            if (visual) loc.world?.strikeLightningEffect(loc)
            else loc.world?.strikeLightning(loc)
        }
        registry.register("LIGHTNING", lightning)
        registry.register("STRIKE_LIGHTNING", lightning)

        registry.register("EXPLOSION") { ctx, args ->
            val loc = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "victim") ?: return@register
            val power = ContextResolver.getDouble(args, "power", 2.0).toFloat().coerceIn(0.1f, 10f)
            val fire = ContextResolver.getBoolean(args, "fire", false)
            val breakBlocks = ContextResolver.getBoolean(args, "break_blocks", false)
            val damage = ContextResolver.getBoolean(args, "damage", true)
            if (damage) {
                loc.world?.createExplosion(loc, power, fire, breakBlocks)
            } else {
                loc.world?.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1)
                loc.world?.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f)
            }
        }

        registry.register("TELEPORT") { ctx, args ->
            val p = ctx.player ?: return@register
            val loc = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "victim") ?: return@register
            p.teleport(loc)
        }

        registry.register("LAUNCH") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player") ?: return@register
            val power = ContextResolver.getDouble(args, "power", 1.0)
            val y = ContextResolver.getDouble(args, "y", 0.5)
            target.velocity = target.location.direction.multiply(power).setY(y)
        }

        registry.register("KNOCKBACK") { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "victim") ?: return@register
            val str = ContextResolver.getDouble(args, "strength", 1.0)
            val from = ctx.player?.location ?: ctx.location ?: return@register
            val v = target.location.toVector().subtract(from.toVector()).setY(0.2).normalize().multiply(str)
            target.velocity = v
        }

        val velocity = ZenEffect { ctx, args ->
            val target = ContextResolver.resolveLivingTarget(ctx, args["target"] as? String ?: "player") ?: return@ZenEffect
            val x = ContextResolver.getDouble(args, "x", 0.0)
            val y = ContextResolver.getDouble(args, "y", 0.5)
            val z = ContextResolver.getDouble(args, "z", 0.0)
            target.velocity = Vector(x, y, z)
        }
        registry.register("VELOCITY", velocity)
        registry.register("SET_VELOCITY", velocity)

        val particle = ZenEffect { ctx, args ->
            val loc = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "victim") ?: return@ZenEffect
            val pName = (args["particle"] as? String) ?: "FLAME"
            val count = ContextResolver.getInt(args, "count", ContextResolver.getInt(args, "amount", 10))
            val speed = ContextResolver.getDouble(args, "speed", 0.05)
            try {
                val p = Particle.valueOf(pName.uppercase(Locale.ROOT).replace("MINECRAFT:", ""))
                loc.world?.spawnParticle(p, loc.clone().add(0.0, 1.0, 0.0), count, 0.2, 0.2, 0.2, speed)
            } catch (_: Exception) {}
        }
        registry.register("PARTICLE", particle)
        registry.register("SPAWN_PARTICLE", particle)

        val sound = ZenEffect { ctx, args ->
            val loc = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "player") ?: return@ZenEffect
            val sName = (args["sound"] ?: args["name"]) as? String ?: return@ZenEffect
            val vol = ContextResolver.getDouble(args, "volume", 1.0).toFloat()
            val pitch = ContextResolver.getDouble(args, "pitch", 1.0).toFloat()
            try {
                val s = Sound.valueOf(sName.uppercase(Locale.ROOT).replace("MINECRAFT:", ""))
                loc.world?.playSound(loc, s, vol, pitch)
            } catch (_: Exception) {
                try {
                    loc.world?.playSound(loc, sName.lowercase(Locale.ROOT), vol, pitch)
                } catch (_: Exception) {}
            }
        }
        registry.register("PLAY_SOUND", sound)
        registry.register("SOUND", sound)

        val message = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val msg = (args["message"] ?: args["text"]) as? String ?: return@ZenEffect
            p.sendMessage(AdventureUtil.parse(ContextResolver.resolvePlaceholders(msg, ctx)))
        }
        registry.register("MESSAGE", message)
        registry.register("SEND_MESSAGE", message)

        registry.register("ACTIONBAR") { ctx, args ->
            val p = ctx.player ?: return@register
            val msg = (args["message"] ?: args["text"]) as? String ?: return@register
            p.sendActionBar(AdventureUtil.parse(ContextResolver.resolvePlaceholders(msg, ctx)))
        }

        registry.register("TITLE") { ctx, args ->
            val p = ctx.player ?: return@register
            val title = ContextResolver.resolvePlaceholders((args["title"] ?: "") as? String, ctx)
            val sub = ContextResolver.resolvePlaceholders((args["subtitle"] ?: "") as? String, ctx)
            p.showTitle(Title.title(AdventureUtil.parse(title), AdventureUtil.parse(sub),
                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(1500), Duration.ofMillis(300))))
        }

        registry.register("BOSSBAR") { ctx, args ->
            val p = ctx.player ?: return@register
            val title = ContextResolver.resolvePlaceholders((args["title"] ?: "ZenCore") as? String, ctx)
            val progress = ContextResolver.getDouble(args, "progress", 1.0).toFloat().coerceIn(0f, 1f)
            val colorStr = (args["color"] as? String)?.uppercase(Locale.ROOT) ?: "PURPLE"
            val c = runCatching { BossBar.Color.valueOf(colorStr) }.getOrDefault(BossBar.Color.PURPLE)
            p.showBossBar(BossBar.bossBar(AdventureUtil.parse(title), progress, c, BossBar.Overlay.PROGRESS))
        }

        registry.register("SET_BLOCK") { ctx, args ->
            val loc = ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "target_block") ?: return@register
            val matName = (args["material"] ?: args["block"]) as? String ?: return@register
            val mat = Material.matchMaterial(matName.uppercase(Locale.ROOT).replace("MINECRAFT:", "")) ?: return@register
            loc.block.type = mat
        }

        registry.register("BREAK_BLOCK") { ctx, args ->
            val b = ctx.block ?: ContextResolver.resolveLocation(ctx, args["target"] as? String ?: "target_block")?.block ?: return@register
            val drop = ContextResolver.getBoolean(args, "drop", ContextResolver.getBoolean(args, "drop_items", true))
            if (ctx.item != null) b.breakNaturally(ctx.item!!)
            else b.breakNaturally(drop)
        }

        registry.register("CANCEL_EVENT") { ctx, _ -> ctx.cancelEvent() }

        registry.register("CONSOLE_COMMAND") { ctx, args ->
            val cmd = args["command"] as? String ?: return@register
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ContextResolver.resolvePlaceholders(cmd, ctx))
        }

        registry.register("PLAYER_COMMAND") { ctx, args ->
            val p = ctx.player ?: return@register
            val cmd = args["command"] as? String ?: return@register
            p.performCommand(ContextResolver.resolvePlaceholders(cmd, ctx))
        }
    }
}
