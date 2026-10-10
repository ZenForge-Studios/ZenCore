package com.cuac_xd.zencore.engine.effect.impl;

import com.cuac_xd.zencore.engine.context.ContextResolver;
import com.cuac_xd.zencore.engine.effect.EffectRegistry;
import com.cuac_xd.zencore.engine.effect.ZenEffect;
import com.cuac_xd.zencore.util.AdventureUtil;
import com.cuac_xd.zencore.util.MathEvaluator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Locale;

public final class CoreEffects {

    private CoreEffects() {}

    public static void registerAll(EffectRegistry registry) {
        // DAMAGE
        ZenEffect dealDamage = (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (target != null && !target.isDead()) {
                String dmgStr = String.valueOf(args.getOrDefault("damage", args.getOrDefault("amount", "2")));
                dmgStr = ContextResolver.resolvePlaceholders(dmgStr, ctx);
                double dmg = MathEvaluator.eval(dmgStr);
                if (ctx.getPlayer() != null && target != ctx.getPlayer()) {
                    target.damage(dmg, ctx.getPlayer());
                } else {
                    target.damage(dmg);
                }
            }
        };
        registry.register("DAMAGE", dealDamage);
        registry.register("DEAL_DAMAGE", dealDamage);
        registry.register("DAMAGE_ENTITY", dealDamage);

        // HEAL
        registry.register("HEAL", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null && !target.isDead()) {
                String amtStr = String.valueOf(args.getOrDefault("amount", "2"));
                amtStr = ContextResolver.resolvePlaceholders(amtStr, ctx);
                double heal = MathEvaluator.eval(amtStr);
                target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + heal));
            }
        });

        // SET_HEALTH
        registry.register("SET_HEALTH", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null && !target.isDead()) {
                double amt = ContextResolver.getDouble(args, "amount", target.getMaxHealth());
                target.setHealth(Math.max(0.0, Math.min(amt, target.getMaxHealth())));
            }
        });

        // LIFESTEAL
        registry.register("LIFESTEAL", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null && ctx.getDamage() > 0) {
                double pct = ContextResolver.getDouble(args, "percent", ContextResolver.getDouble(args, "percentage", 20.0));
                if (pct > 1.0) pct /= 100.0;
                double heal = ctx.getDamage() * pct;
                p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + Math.max(0.5, heal)));
                p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 1.5, 0), 3, 0.3, 0.3, 0.3, 0.0);
            }
        });

        // FOOD & SATURATION
        registry.register("FOOD", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                int amt = ContextResolver.getInt(args, "amount", 1);
                p.setFoodLevel(Math.max(0, Math.min(20, p.getFoodLevel() + amt)));
            }
        });
        registry.register("FEED", registry.get("FOOD"));

        registry.register("SATURATION", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                float amt = (float) ContextResolver.getDouble(args, "amount", 1.0);
                p.setSaturation(Math.max(0f, p.getSaturation() + amt));
            }
        });

        // EXP & LEVEL
        registry.register("EXPERIENCE", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                int amt = ContextResolver.getInt(args, "amount", 10);
                p.giveExp(amt);
            }
        });
        registry.register("GIVE_EXP", registry.get("EXPERIENCE"));

        registry.register("LEVEL", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                int amt = ContextResolver.getInt(args, "amount", 1);
                p.giveExpLevels(amt);
            }
        });

        // POTION EFFECTS
        ZenEffect addPotion = (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null) {
                String effName = (String) args.getOrDefault("effect", args.get("potion"));
                if (effName == null) return;
                int duration = ContextResolver.getInt(args, "duration", 100);
                int amplifier = ContextResolver.getInt(args, "amplifier", 0);
                try {
                    PotionEffectType type = PotionEffectType.getByName(effName.toUpperCase(Locale.ROOT).replace("MINECRAFT:", ""));
                    if (type != null) {
                        target.addPotionEffect(new PotionEffect(type, duration, amplifier));
                    }
                } catch (Exception ignored) {}
            }
        };
        registry.register("ADD_POTION_EFFECT", addPotion);
        registry.register("POTION", addPotion);
        registry.register("POTION_EFFECT", addPotion);

        registry.register("REMOVE_POTION_EFFECT", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null) {
                String effName = (String) args.getOrDefault("effect", args.get("potion"));
                if (effName == null) return;
                try {
                    PotionEffectType type = PotionEffectType.getByName(effName.toUpperCase(Locale.ROOT).replace("MINECRAFT:", ""));
                    if (type != null) target.removePotionEffect(type);
                } catch (Exception ignored) {}
            }
        });

        registry.register("CLEAR_EFFECT", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null) {
                for (PotionEffect pe : target.getActivePotionEffects()) {
                    target.removePotionEffect(pe.getType());
                }
            }
        });
        registry.register("CLEAR_NEGATIVE_EFFECTS", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null) {
                target.removePotionEffect(PotionEffectType.POISON);
                target.removePotionEffect(PotionEffectType.WITHER);
                target.removePotionEffect(PotionEffectType.SLOWNESS);
                target.removePotionEffect(PotionEffectType.WEAKNESS);
                target.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                target.removePotionEffect(PotionEffectType.BLINDNESS);
                target.removePotionEffect(PotionEffectType.NAUSEA);
            }
        });

        // IGNITE
        registry.register("IGNITE", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (target != null) {
                int duration = ContextResolver.getInt(args, "duration", 4);
                target.setFireTicks(duration * 20);
            }
        });

        // LIGHTNING
        ZenEffect lightning = (ctx, args) -> {
            Location loc = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "victim"));
            if (loc != null && loc.getWorld() != null) {
                boolean visual = ContextResolver.getBoolean(args, "visual", ContextResolver.getBoolean(args, "effect_only", false));
                if (visual) loc.getWorld().strikeLightningEffect(loc);
                else loc.getWorld().strikeLightning(loc);
            }
        };
        registry.register("LIGHTNING", lightning);
        registry.register("STRIKE_LIGHTNING", lightning);

        // EXPLOSION
        registry.register("EXPLOSION", (ctx, args) -> {
            Location loc = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "victim"));
            if (loc != null && loc.getWorld() != null) {
                float power = (float) Math.max(0.1, Math.min(10.0, ContextResolver.getDouble(args, "power", 2.0)));
                boolean fire = ContextResolver.getBoolean(args, "fire", false);
                boolean breakBlocks = ContextResolver.getBoolean(args, "break_blocks", false);
                boolean damage = ContextResolver.getBoolean(args, "damage", true);
                if (damage) {
                    loc.getWorld().createExplosion(loc, power, fire, breakBlocks);
                } else {
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
                    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                }
            }
        });

        // MOVEMENT & VELOCITY
        registry.register("TELEPORT", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                Location loc = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "victim"));
                if (loc != null) p.teleport(loc);
            }
        });

        registry.register("LAUNCH", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null) {
                double power = ContextResolver.getDouble(args, "power", 1.0);
                double y = ContextResolver.getDouble(args, "y", 0.5);
                target.setVelocity(target.getLocation().getDirection().multiply(power).setY(y));
            }
        });

        registry.register("KNOCKBACK", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (target != null) {
                double str = ContextResolver.getDouble(args, "strength", 1.0);
                Location from = ctx.getPlayer() != null ? ctx.getPlayer().getLocation() : ctx.getLocation();
                if (from != null) {
                    Vector v = target.getLocation().toVector().subtract(from.toVector()).setY(0.2).normalize().multiply(str);
                    target.setVelocity(v);
                }
            }
        });

        registry.register("VELOCITY", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "player"));
            if (target != null) {
                double x = ContextResolver.getDouble(args, "x", 0.0);
                double y = ContextResolver.getDouble(args, "y", 0.5);
                double z = ContextResolver.getDouble(args, "z", 0.0);
                target.setVelocity(new Vector(x, y, z));
            }
        });
        registry.register("SET_VELOCITY", registry.get("VELOCITY"));

        // PARTICLES & SOUNDS
        ZenEffect particle = (ctx, args) -> {
            Location loc = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "victim"));
            if (loc != null && loc.getWorld() != null) {
                String pName = (String) args.getOrDefault("particle", "FLAME");
                int count = ContextResolver.getInt(args, "count", ContextResolver.getInt(args, "amount", 10));
                double speed = ContextResolver.getDouble(args, "speed", 0.05);
                try {
                    Particle p = Particle.valueOf(pName.toUpperCase(Locale.ROOT).replace("MINECRAFT:", ""));
                    loc.getWorld().spawnParticle(p, loc.clone().add(0, 1, 0), count, 0.2, 0.2, 0.2, speed);
                } catch (Exception ignored) {}
            }
        };
        registry.register("PARTICLE", particle);
        registry.register("SPAWN_PARTICLE", particle);

        ZenEffect sound = (ctx, args) -> {
            Location loc = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "player"));
            if (loc != null && loc.getWorld() != null) {
                String sName = (String) args.getOrDefault("sound", args.get("name"));
                if (sName == null) return;
                float vol = (float) ContextResolver.getDouble(args, "volume", 1.0);
                float pitch = (float) ContextResolver.getDouble(args, "pitch", 1.0);
                try {
                    Sound s = Sound.valueOf(sName.toUpperCase(Locale.ROOT).replace("MINECRAFT:", ""));
                    loc.getWorld().playSound(loc, s, vol, pitch);
                } catch (Exception ignored) {
                    loc.getWorld().playSound(loc, sName.toLowerCase(Locale.ROOT), vol, pitch);
                }
            }
        };
        registry.register("PLAY_SOUND", sound);
        registry.register("SOUND", sound);

        // MESSAGES & UI
        registry.register("MESSAGE", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                String msg = (String) args.getOrDefault("message", args.get("text"));
                if (msg != null) {
                    msg = ContextResolver.resolvePlaceholders(msg, ctx);
                    p.sendMessage(AdventureUtil.parse(msg));
                }
            }
        });
        registry.register("SEND_MESSAGE", registry.get("MESSAGE"));

        registry.register("ACTIONBAR", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                String msg = (String) args.getOrDefault("message", args.get("text"));
                if (msg != null) {
                    msg = ContextResolver.resolvePlaceholders(msg, ctx);
                    p.sendActionBar(AdventureUtil.parse(msg));
                }
            }
        });

        registry.register("TITLE", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                String title = ContextResolver.resolvePlaceholders((String) args.getOrDefault("title", ""), ctx);
                String sub = ContextResolver.resolvePlaceholders((String) args.getOrDefault("subtitle", ""), ctx);
                p.showTitle(Title.title(AdventureUtil.parse(title), AdventureUtil.parse(sub),
                        Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(1500), Duration.ofMillis(300))));
            }
        });

        registry.register("BOSSBAR", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                String title = ContextResolver.resolvePlaceholders((String) args.getOrDefault("title", "ZenCore"), ctx);
                float progress = (float) ContextResolver.getDouble(args, "progress", 1.0);
                String colorStr = (String) args.getOrDefault("color", "PURPLE");
                BossBar.Color c = BossBar.Color.PURPLE;
                try { c = BossBar.Color.valueOf(colorStr.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
                p.showBossBar(BossBar.bossBar(AdventureUtil.parse(title), Math.max(0f, Math.min(1f, progress)), c, BossBar.Overlay.PROGRESS));
            }
        });

        // BLOCKS
        registry.register("SET_BLOCK", (ctx, args) -> {
            Location loc = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "target_block"));
            if (loc != null) {
                String matName = (String) args.getOrDefault("material", args.get("block"));
                if (matName != null) {
                    Material mat = Material.matchMaterial(matName.toUpperCase(Locale.ROOT).replace("MINECRAFT:", ""));
                    if (mat != null) loc.getBlock().setType(mat);
                }
            }
        });

        registry.register("BREAK_BLOCK", (ctx, args) -> {
            Block b = ctx.getBlock();
            if (b == null) {
                Location loc = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "target_block"));
                if (loc != null) b = loc.getBlock();
            }
            if (b != null) {
                boolean drop = ContextResolver.getBoolean(args, "drop", ContextResolver.getBoolean(args, "drop_items", true));
                if (ctx.getItem() != null) b.breakNaturally(ctx.getItem());
                else b.breakNaturally(drop);
            }
        });

        // CANCEL EVENT
        registry.register("CANCEL_EVENT", (ctx, args) -> ctx.cancelEvent());

        // COMMANDS
        registry.register("CONSOLE_COMMAND", (ctx, args) -> {
            String cmd = (String) args.get("command");
            if (cmd != null) {
                cmd = ContextResolver.resolvePlaceholders(cmd, ctx);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        });

        registry.register("PLAYER_COMMAND", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p != null) {
                String cmd = (String) args.get("command");
                if (cmd != null) {
                    cmd = ContextResolver.resolvePlaceholders(cmd, ctx);
                    p.performCommand(cmd);
                }
            }
        });
    }
}
