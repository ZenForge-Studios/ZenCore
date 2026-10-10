package com.cuac_xd.zencore.engine.effect.impl;

import com.cuac_xd.zencore.engine.context.ContextResolver;
import com.cuac_xd.zencore.engine.effect.EffectRegistry;
import com.cuac_xd.zencore.util.MathEvaluator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public final class ExtendedEffects {

    private static final Random RANDOM = new Random();

    private ExtendedEffects() {}

    public static void registerAll(EffectRegistry registry) {
        // BLEED
        registry.register("BLEED", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (target == null) return;
            int ticks = ContextResolver.getInt(args, "duration", 60);
            double dmgPerSec = ContextResolver.getDouble(args, "damage", 1.0);
            Player p = ctx.getPlayer();

            var plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects.class);
            var task = new Runnable() {
                int count = 0;
                final int maxRuns = Math.max(1, ticks / 20);
                @Override
                public void run() {
                    if (!target.isValid() || target.isDead() || count >= maxRuns) return;
                    if (p != null) target.damage(dmgPerSec, p);
                    else target.damage(dmgPerSec);
                    target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1.0, 0), 15, 0.2, 0.4, 0.2, Material.REDSTONE_BLOCK.createBlockData());
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 0.8f, 1.2f);
                    count++;
                }
            };
            Bukkit.getScheduler().runTaskTimer(plugin, task, 20L, 20L);
        });

        // AOE DAMAGE / SHOCKWAVE
        registry.register("AOE_DAMAGE", (ctx, args) -> {
            Location center = ContextResolver.resolveLocation(ctx, (String) args.getOrDefault("target", "victim"));
            if (center == null || center.getWorld() == null) return;
            double radius = ContextResolver.getDouble(args, "radius", 5.0);
            double dmg = ContextResolver.getDouble(args, "damage", ContextResolver.getDouble(args, "amount", 4.0));
            Player p = ctx.getPlayer();

            for (Entity ent : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (ent instanceof LivingEntity living && ent != p) {
                    if (p != null) living.damage(dmg, p);
                    else living.damage(dmg);
                }
            }
            center.getWorld().spawnParticle(Particle.EXPLOSION, center, 3, 1.0, 0.5, 1.0, 0.0);
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
        });
        registry.register("SHOCKWAVE", registry.get("AOE_DAMAGE"));

        // FREEZE / ARCTIC FREEZE
        registry.register("FREEZE", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (target != null) {
                int duration = ContextResolver.getInt(args, "duration", 100);
                target.setFreezeTicks(Math.max(target.getFreezeTicks(), duration));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 2));
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 1.0f, 0.8f);
            }
        });
        registry.register("ARCTIC_FREEZE", registry.get("FREEZE"));
        registry.register("FREEZE_TARGET", registry.get("FREEZE"));

        // AUTO REPLANT
        registry.register("AUTO_REPLANT", (ctx, args) -> {
            Block b = ctx.getBlock();
            if (b != null && b.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() >= ageable.getMaximumAge()) {
                    var plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ExtendedEffects.class);
                    Material mat = b.getType();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        b.setType(mat);
                        if (b.getBlockData() instanceof Ageable newAge) {
                            newAge.setAge(0);
                            b.setBlockData(newAge);
                        }
                    }, 2L);
                }
            }
        });

        // AUTO SMELT
        registry.register("AUTO_SMELT", (ctx, args) -> {
            Block b = ctx.getBlock();
            if (b != null) {
                Material type = b.getType();
                Material dropMat = switch (type) {
                    case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON_BLOCK -> Material.IRON_INGOT;
                    case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD_BLOCK -> Material.GOLD_INGOT;
                    case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER_BLOCK -> Material.COPPER_INGOT;
                    case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
                    default -> null;
                };
                if (dropMat != null) {
                    b.setType(Material.AIR);
                    b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), new ItemStack(dropMat, 1));
                    b.getWorld().spawnParticle(Particle.FLAME, b.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.02);
                }
            }
        });

        // TELEPATHY
        registry.register("TELEPATHY", (ctx, args) -> {
            Player p = ctx.getPlayer();
            Block b = ctx.getBlock();
            if (p != null && b != null) {
                Collection<ItemStack> drops = b.getDrops(ctx.getItem());
                b.setType(Material.AIR);
                for (ItemStack d : drops) {
                    Map<Integer, ItemStack> leftover = p.getInventory().addItem(d);
                    leftover.values().forEach(rem -> p.getWorld().dropItemNaturally(p.getLocation(), rem));
                }
            }
        });

        // TREE FELLER
        registry.register("TREE_FELLER", (ctx, args) -> {
            Block start = ctx.getBlock();
            if (start == null || !Tag.LOGS.isTagged(start.getType())) return;
            Player p = ctx.getPlayer();
            int maxBlocks = ContextResolver.getInt(args, "max_blocks", 64);
            Queue<Block> queue = new LinkedList<>();
            Set<Block> visited = new HashSet<>();
            queue.add(start);
            visited.add(start);

            int broken = 0;
            while (!queue.isEmpty() && broken < maxBlocks) {
                Block cur = queue.poll();
                if (Tag.LOGS.isTagged(cur.getType())) {
                    cur.breakNaturally(ctx.getItem());
                    broken++;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                Block next = cur.getRelative(dx, dy, dz);
                                if (!visited.contains(next) && Tag.LOGS.isTagged(next.getType())) {
                                    visited.add(next);
                                    queue.add(next);
                                }
                            }
                        }
                    }
                }
            }
        });

        // VEIN MINE / MINE AREA
        registry.register("VEIN_MINE", (ctx, args) -> {
            Block start = ctx.getBlock();
            if (start == null) return;
            Material oreMat = start.getType();
            int maxBlocks = ContextResolver.getInt(args, "max_blocks", 16);
            Queue<Block> queue = new LinkedList<>();
            Set<Block> visited = new HashSet<>();
            queue.add(start);
            visited.add(start);

            int broken = 0;
            while (!queue.isEmpty() && broken < maxBlocks) {
                Block cur = queue.poll();
                if (cur.getType() == oreMat) {
                    cur.breakNaturally(ctx.getItem());
                    broken++;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                Block next = cur.getRelative(dx, dy, dz);
                                if (!visited.contains(next) && next.getType() == oreMat) {
                                    visited.add(next);
                                    queue.add(next);
                                }
                            }
                        }
                    }
                }
            }
        });
        registry.register("MINE_AREA", (ctx, args) -> {
            Block start = ctx.getBlock();
            Player p = ctx.getPlayer();
            if (start == null || p == null) return;
            int radius = ContextResolver.getInt(args, "radius", 1);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block b = start.getRelative(x, y, z);
                        if (b.getType() != Material.BEDROCK && !b.getType().isAir()) {
                            b.breakNaturally(ctx.getItem());
                        }
                    }
                }
            }
        });

        // PROJECTILE BEAM
        registry.register("PROJECTILE_BEAM", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return;
            Location start = p.getEyeLocation();
            Vector dir = start.getDirection().normalize();
            double maxDist = ContextResolver.getDouble(args, "distance", 15.0);
            double step = ContextResolver.getDouble(args, "step", 0.5);
            double damage = ContextResolver.getDouble(args, "damage", 5.0);
            String pStr = (String) args.getOrDefault("particle", "FLAME");
            Particle pType = Particle.FLAME;
            try { pType = Particle.valueOf(pStr.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}

            double cur = 0.5;
            while (cur <= maxDist) {
                Location pt = start.clone().add(dir.clone().multiply(cur));
                pt.getWorld().spawnParticle(pType, pt, 1, 0, 0, 0, 0);
                var nearby = pt.getWorld().getNearbyEntities(pt, 0.8, 0.8, 0.8);
                for (Entity e : nearby) {
                    if (e != p && e instanceof LivingEntity living) {
                        living.damage(damage, p);
                        pt.getWorld().spawnParticle(Particle.EXPLOSION, pt, 1);
                        return;
                    }
                }
                cur += step;
            }
        });

        // SET GLOWING
        registry.register("SET_GLOWING", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (target != null) {
                boolean glowing = ContextResolver.getBoolean(args, "glowing", true);
                target.setGlowing(glowing);
            }
        });

        // DISARM
        registry.register("DISARM", (ctx, args) -> {
            LivingEntity target = ContextResolver.resolveLivingTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (target instanceof Player victimPlayer) {
                ItemStack mainHand = victimPlayer.getInventory().getItemInMainHand();
                if (mainHand != null && !mainHand.getType().isAir()) {
                    victimPlayer.getInventory().setItemInMainHand(null);
                    victimPlayer.getWorld().dropItemNaturally(victimPlayer.getLocation(), mainHand);
                    victimPlayer.playSound(victimPlayer.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                }
            }
        });
    }
}
