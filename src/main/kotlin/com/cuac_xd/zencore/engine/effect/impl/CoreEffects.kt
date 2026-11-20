package com.cuac_xd.zencore.engine.effect.impl

import com.cuac_xd.zencore.booster.ZenBoosterEngine
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.effect.EffectRegistry
import com.cuac_xd.zencore.engine.effect.ZenEffect
import com.cuac_xd.zencore.hooks.ZenHooksHub
import com.cuac_xd.zencore.hooks.ZenPartyHook
import com.cuac_xd.zencore.stat.StatOperation
import com.cuac_xd.zencore.stat.ZenStatEngine
import com.cuac_xd.zencore.util.AdventureUtil
import com.cuac_xd.zencore.util.MathEvaluator
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
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

        // ==========================================
        // ECOSYSTEM EFFECTS (ZenEconomy, ZenCosmetics, ZenCustomItems, ZenEnchants)
        // ==========================================
        val giveCurrency = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val currency = (args["currency"] ?: args["type"] ?: "vault").toString()
            val amtStr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: "1", ctx)
            val amt = MathEvaluator.eval(amtStr)
            com.cuac_xd.zencore.hooks.ZenHooksHub.depositAny(p, currency, amt)
        }
        registry.register("GIVE_CURRENCY", giveCurrency)
        registry.register("DEPOSIT_CURRENCY", giveCurrency)

        val takeCurrency = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val currency = (args["currency"] ?: args["type"] ?: "vault").toString()
            val amtStr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: "1", ctx)
            val amt = MathEvaluator.eval(amtStr)
            com.cuac_xd.zencore.hooks.ZenHooksHub.withdrawAny(p, currency, amt)
        }
        registry.register("TAKE_CURRENCY", takeCurrency)
        registry.register("WITHDRAW_CURRENCY", takeCurrency)

        registry.register("SET_CURRENCY") { ctx, args ->
            val p = ctx.player ?: return@register
            val currency = (args["currency"] ?: args["type"] ?: "vault").toString()
            val amtStr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: "0", ctx)
            val amt = MathEvaluator.eval(amtStr)
            com.cuac_xd.zencore.hooks.ZenEconomyHook.setBalance(p, currency, amt)
        }

        registry.register("UNLOCK_COSMETIC") { ctx, args ->
            val p = ctx.player ?: return@register
            val id = (args["id"] ?: args["cosmetic"])?.toString() ?: return@register
            com.cuac_xd.zencore.hooks.ZenCosmeticsHook.unlockCosmetic(p, id)
        }

        registry.register("LOCK_COSMETIC") { ctx, args ->
            val p = ctx.player ?: return@register
            val id = (args["id"] ?: args["cosmetic"])?.toString() ?: return@register
            com.cuac_xd.zencore.hooks.ZenCosmeticsHook.lockCosmetic(p, id)
        }

        registry.register("EQUIP_COSMETIC") { ctx, args ->
            val p = ctx.player ?: return@register
            val id = (args["id"] ?: args["cosmetic"])?.toString() ?: return@register
            com.cuac_xd.zencore.hooks.ZenCosmeticsHook.equipCosmetic(p, id)
        }

        registry.register("UNEQUIP_COSMETIC") { ctx, args ->
            val p = ctx.player ?: return@register
            val category = (args["category"] ?: args["type"])?.toString() ?: return@register
            com.cuac_xd.zencore.hooks.ZenCosmeticsHook.unequipCategory(p, category)
        }

        val giveCustomItem = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val id = (args["id"] ?: args["item"])?.toString() ?: return@ZenEffect
            val amtStr = ContextResolver.resolvePlaceholders(args["amount"]?.toString() ?: "1", ctx)
            val amt = MathEvaluator.eval(amtStr).toInt().coerceAtLeast(1)
            com.cuac_xd.zencore.hooks.ZenCustomItemsHook.giveItem(p, id, amt)
        }
        registry.register("GIVE_CUSTOM_ITEM", giveCustomItem)
        registry.register("GIVE_ZEN_ITEM", giveCustomItem)

        registry.register("APPLY_ENCHANT") { ctx, args ->
            val item = ctx.item ?: return@register
            val id = (args["enchant"] ?: args["id"] ?: args["enchantment"])?.toString() ?: return@register
            val lvlStr = ContextResolver.resolvePlaceholders(args["level"]?.toString() ?: "1", ctx)
            val level = MathEvaluator.eval(lvlStr).toInt().coerceAtLeast(1)
            com.cuac_xd.zencore.hooks.ZenEnchantsHook.applyEnchantment(item, id, level, ctx.player)
        }

        // ==========================================
        // PARTY EFFECTS (ZenPartyHub / ZenPartyHook)
        // ==========================================
        val partyBroadcast = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val party = ZenPartyHook.getPartyByPlayer(p.uniqueId) ?: return@ZenEffect
            val rawMsg = (args["message"] ?: args["text"])?.toString() ?: return@ZenEffect
            val formatted = ContextResolver.resolvePlaceholders(rawMsg, ctx)
            ZenPartyHook.broadcastToParty(party.id, formatted)
        }
        registry.register("PARTY_BROADCAST", partyBroadcast)
        registry.register("PARTY_MESSAGE", partyBroadcast)

        registry.register("PARTY_WARP") { ctx, _ ->
            val p = ctx.player ?: return@register
            val party = ZenPartyHook.getPartyByPlayer(p.uniqueId) ?: return@register
            ZenPartyHook.warpPartyToLeader(party.id)
        }

        registry.register("PARTY_CONNECT_SERVER") { ctx, args ->
            val p = ctx.player ?: return@register
            val party = ZenPartyHook.getPartyByPlayer(p.uniqueId) ?: return@register
            val rawServer = (args["server"] ?: args["target_server"])?.toString() ?: return@register
            val server = ContextResolver.resolvePlaceholders(rawServer, ctx)
            if (server.isNotBlank()) {
                ZenPartyHook.connectPartyToServer(party.id, server)
            }
        }

        registry.register("PARTY_HEAL") { ctx, args ->
            val p = ctx.player ?: return@register
            val amtStr = ContextResolver.resolvePlaceholders((args["amount"] ?: "4").toString(), ctx)
            val heal = MathEvaluator.eval(amtStr)
            if (heal <= 0.0) return@register
            val radius = ContextResolver.getDouble(args, "radius", 20.0)
            for (member in ZenPartyHook.getNearbyOnlineMembers(p, radius)) {
                runCatching {
                    if (!member.isDead) {
                        val maxHp = runCatching { member.getAttribute(Attribute.MAX_HEALTH)?.value }.getOrNull()
                            ?: runCatching { member.maxHealth }.getOrDefault(20.0)
                        member.health = kotlin.math.min(maxHp, member.health + heal)
                    }
                }
            }
        }

        registry.register("PARTY_POTION_EFFECT") { ctx, args ->
            val p = ctx.player ?: return@register
            val effName = (args["effect"] ?: args["potion"])?.toString() ?: return@register
            val type = runCatching {
                PotionEffectType.getByName(effName.uppercase(Locale.ROOT).replace("MINECRAFT:", ""))
            }.getOrNull() ?: return@register
            val duration = ContextResolver.getInt(args, "duration", 100)
            val amplifier = ContextResolver.getInt(args, "amplifier", 0)
            val radius = ContextResolver.getDouble(args, "radius", 20.0)
            for (member in ZenPartyHook.getNearbyOnlineMembers(p, radius)) {
                runCatching { member.addPotionEffect(PotionEffect(type, duration, amplifier)) }
            }
        }

        registry.register("PARTY_GIVE_CURRENCY") { ctx, args ->
            val p = ctx.player ?: return@register
            val party = ZenPartyHook.getPartyByPlayer(p.uniqueId) ?: return@register
            val currency = (args["currency"] ?: args["type"] ?: "vault").toString()
            val amtStr = ContextResolver.resolvePlaceholders((args["amount"] ?: "1").toString(), ctx)
            val amt = MathEvaluator.eval(amtStr)
            val split = ContextResolver.getBoolean(args, "split", false)
            ZenHooksHub.depositPartyAny(party.id, currency, amt, split)
        }

        registry.register("PARTY_GIVE_XP") { ctx, args ->
            val p = ctx.player ?: return@register
            val amtStr = ContextResolver.resolvePlaceholders((args["amount"] ?: "10").toString(), ctx)
            val amt = kotlin.math.round(MathEvaluator.eval(amtStr)).toInt()
            if (amt == 0) return@register
            val radius = ContextResolver.getDouble(args, "radius", 20.0)
            for (member in ZenPartyHook.getNearbyOnlineMembers(p, radius)) {
                runCatching { member.giveExp(amt) }
            }
        }

        // ==========================================
        // BOOSTER, STAT & RESOURCE EFFECTS
        // ==========================================
        registry.register("ACTIVATE_BOOSTER") { ctx, args ->
            val explicitId = (args["id"] ?: args["booster"] ?: args["booster_id"])?.toString()
                ?.let { ContextResolver.resolvePlaceholders(it, ctx) }
            val bypassCooldown = ContextResolver.getBoolean(args, "bypass_cooldown", false)
            val hasInlineStats = args.containsKey("multiplier") || args.containsKey("duration")
            val p = ctx.player

            if (explicitId != null && !hasInlineStats && p != null && ZenBoosterEngine.isAvailable()) {
                if (ZenBoosterEngine.activateBooster(explicitId, p, bypassCooldown)) {
                    return@register
                }
            }

            val category = ContextResolver.resolvePlaceholders((args["category"] ?: "xp").toString(), ctx)
            val id = explicitId ?: "booster_$category"
            val mulStr = ContextResolver.resolvePlaceholders((args["multiplier"] ?: "1.5").toString(), ctx)
            val multiplier = MathEvaluator.eval(mulStr)
            val durStr = ContextResolver.resolvePlaceholders((args["duration"] ?: "60").toString(), ctx)
            val durationSeconds = MathEvaluator.eval(durStr).toLong()
            val global = ContextResolver.getBoolean(args, "global", false)
            val shareWithParty = ContextResolver.getBoolean(args, "share_with_party", false)

            if (global) {
                ZenBoosterEngine.addGlobalBooster(id, category, multiplier, durationSeconds)
            } else {
                val target = p ?: return@register
                ZenBoosterEngine.addPlayerBooster(target.uniqueId, id, category, multiplier, durationSeconds, shareWithParty)
            }
        }

        val giveBoosterHandler = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val idRaw = (args["id"] ?: args["booster"] ?: args["booster_id"])?.toString() ?: return@ZenEffect
            val boosterId = ContextResolver.resolvePlaceholders(idRaw, ctx)
            val amountStr = ContextResolver.resolvePlaceholders((args["amount"] ?: "1").toString(), ctx)
            val amount = MathEvaluator.eval(amountStr).toInt().coerceAtLeast(1)
            if (!ZenBoosterEngine.giveBooster(p.uniqueId, boosterId, amount)) {
                com.cuac_xd.zencore.hooks.ZenBoosterHook.giveBooster(p, boosterId, amount)
            }
        }
        registry.register("GIVE_BOOSTER", giveBoosterHandler)
        registry.register("GIVE_BOOSTER_VOUCHER", giveBoosterHandler)

        val stopBoosterHandler = ZenEffect { ctx, args ->
            val idRaw = (args["id"] ?: args["booster"] ?: args["booster_id"])?.toString() ?: return@ZenEffect
            val boosterId = ContextResolver.resolvePlaceholders(idRaw, ctx)
            val scope = args["scope"]?.toString()?.let { raw ->
                val resolved = ContextResolver.resolvePlaceholders(raw, ctx).trim().uppercase(Locale.ROOT)
                runCatching { com.cuac_xd.zencore.booster.ZenBoosterScope.valueOf(resolved) }.getOrNull()
            }
            val isGlobal = ContextResolver.getBoolean(args, "global", false)
            val targetUuid = if (isGlobal || scope == com.cuac_xd.zencore.booster.ZenBoosterScope.GLOBAL) {
                null
            } else {
                ctx.player?.uniqueId
            }
            ZenBoosterEngine.stopBooster(boosterId, scope, targetUuid)
        }
        registry.register("STOP_BOOSTER", stopBoosterHandler)
        registry.register("REMOVE_BOOSTER", stopBoosterHandler)

        registry.register("OPEN_BOOSTER_MENU") { ctx, _ ->
            val p = ctx.player ?: return@register
            ZenBoosterEngine.openMainMenu(p)
        }

        registry.register("OPEN_BOOSTER_VAULT") { ctx, _ ->
            val p = ctx.player ?: return@register
            ZenBoosterEngine.openVaultMenu(p)
        }

        registry.register("ADD_STAT_MODIFIER") { ctx, args ->
            val p = ctx.player ?: return@register
            val stat = args["stat"]?.toString() ?: return@register
            val key = ContextResolver.resolvePlaceholders((args["key"] ?: "effect_$stat").toString(), ctx)
            val amtStr = ContextResolver.resolvePlaceholders((args["amount"] ?: args["value"] ?: "1.0").toString(), ctx)
            val amount = MathEvaluator.eval(amtStr)
            val opStr = (args["operation"] ?: "ADDITIVE").toString().uppercase(Locale.ROOT)
            val operation = runCatching { StatOperation.valueOf(opStr) }.getOrDefault(StatOperation.ADDITIVE)
            val durStr = ContextResolver.resolvePlaceholders((args["duration"] ?: "0").toString(), ctx)
            val durationTicks = MathEvaluator.eval(durStr).toLong().coerceAtLeast(0L)

            ZenStatEngine.addModifier(p.uniqueId, key, stat, amount, operation, durationTicks)
            ZenStatEngine.syncBukkitAttribute(p, stat)
        }

        registry.register("REMOVE_STAT_MODIFIER") { ctx, args ->
            val p = ctx.player ?: return@register
            val keyRaw = args["key"]?.toString() ?: return@register
            val key = ContextResolver.resolvePlaceholders(keyRaw, ctx)
            val stat = args["stat"]?.toString()

            ZenStatEngine.removeModifier(p.uniqueId, key, stat)
            if (stat != null) {
                ZenStatEngine.syncBukkitAttribute(p, stat)
            }
        }

        val giveResource = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val pool = (args["pool"] ?: args["resource_pool"] ?: "mana").toString()
            val amtStr = ContextResolver.resolvePlaceholders((args["amount"] ?: args["value"] ?: "10").toString(), ctx)
            val amt = MathEvaluator.eval(amtStr)
            ZenStatEngine.giveResource(p.uniqueId, pool, amt)
        }
        registry.register("GIVE_RESOURCE", giveResource)
        registry.register("GIVE_MANA", giveResource)

        val takeResource = ZenEffect { ctx, args ->
            val p = ctx.player ?: return@ZenEffect
            val pool = (args["pool"] ?: args["resource_pool"] ?: "mana").toString()
            val amtStr = ContextResolver.resolvePlaceholders((args["amount"] ?: args["value"] ?: "10").toString(), ctx)
            val amt = MathEvaluator.eval(amtStr)
            ZenStatEngine.consumeResource(p.uniqueId, pool, amt)
        }
        registry.register("TAKE_RESOURCE", takeResource)
        registry.register("TAKE_MANA", takeResource)
    }
}
