package com.cuac_xd.zencore.engine.context;

import com.cuac_xd.zencore.util.MathEvaluator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Universal target and parameter resolver for execution contexts.
 */
public final class ContextResolver {

    private ContextResolver() {}

    @Nullable
    public static LivingEntity resolveLivingTarget(@NotNull ZenExecutionContext ctx, @Nullable String targetName) {
        if (targetName == null || targetName.isBlank() || targetName.equalsIgnoreCase("DEFAULT")) {
            return ctx.getTarget() != null ? ctx.getTarget() : (ctx.getPlayer() != null ? ctx.getPlayer() : null);
        }
        String lower = targetName.toLowerCase(Locale.ROOT).trim();
        String trig = ctx.getTrigger() != null ? ctx.getTrigger().toLowerCase(Locale.ROOT) : "";
        boolean isDefend = trig.contains("defend") || trig.contains("take_damage");

        return switch (lower) {
            case "victim", "target" -> ctx.getTarget();
            case "attacker" -> isDefend ? ctx.getTarget() : ctx.getPlayer();
            case "defender", "victim_defender" -> isDefend ? ctx.getPlayer() : ctx.getTarget();
            case "player", "self" -> ctx.getPlayer();
            default -> ctx.getTarget() != null ? ctx.getTarget() : ctx.getPlayer();
        };
    }

    @Nullable
    public static Entity resolveEntityTarget(@NotNull ZenExecutionContext ctx, @Nullable String targetName) {
        if (targetName == null || targetName.isBlank() || targetName.equalsIgnoreCase("DEFAULT")) {
            return ctx.getTargetEntity() != null ? ctx.getTargetEntity() : ctx.getPlayer();
        }
        String lower = targetName.toLowerCase(Locale.ROOT).trim();
        return switch (lower) {
            case "victim", "target", "entity" -> ctx.getTargetEntity();
            case "player", "self" -> ctx.getPlayer();
            default -> resolveLivingTarget(ctx, targetName);
        };
    }

    @Nullable
    public static Location resolveLocation(@NotNull ZenExecutionContext ctx, @Nullable String targetName) {
        if (targetName != null && !targetName.isBlank()) {
            String lower = targetName.toLowerCase(Locale.ROOT).trim();
            if (lower.equals("target_block") || lower.equals("block")) {
                Block b = ctx.getBlock();
                if (b != null) return b.getLocation().add(0.5, 0.5, 0.5);
                Player p = ctx.getPlayer();
                if (p != null) {
                    Block targetB = p.getTargetBlockExact(10);
                    if (targetB != null) return targetB.getLocation().add(0.5, 0.5, 0.5);
                }
            }
            if (lower.equals("entity_in_front")) {
                Player p = ctx.getPlayer();
                if (p != null) {
                    var trace = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getLocation().getDirection(), 10.0, e -> e != p);
                    if (trace != null && trace.getHitEntity() != null) {
                        return trace.getHitEntity().getLocation();
                    }
                    Block targetB = p.getTargetBlockExact(10);
                    if (targetB != null) return targetB.getLocation();
                    return p.getLocation().clone().add(p.getLocation().getDirection().multiply(3.0));
                }
            }
            Entity ent = resolveEntityTarget(ctx, targetName);
            if (ent != null) return ent.getLocation();
        }

        if (ctx.getLocation() != null) return ctx.getLocation();
        LivingEntity entity = resolveLivingTarget(ctx, targetName);
        if (entity != null) return entity.getLocation();
        if (ctx.getBlock() != null) return ctx.getBlock().getLocation().add(0.5, 0.5, 0.5);
        if (ctx.getPlayer() != null) return ctx.getPlayer().getLocation();
        return null;
    }

    public static double getDouble(@NotNull Map<String, Object> config, @NotNull String key, double fallback) {
        Object val = config.get(key);
        if (val == null) return fallback;
        if (val instanceof Number num) return num.doubleValue();
        try {
            return MathEvaluator.eval(String.valueOf(val));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static int getInt(@NotNull Map<String, Object> config, @NotNull String key, int fallback) {
        double d = getDouble(config, key, fallback);
        return (int) Math.round(d);
    }

    public static boolean getBoolean(@NotNull Map<String, Object> config, @NotNull String key, boolean fallback) {
        Object val = config.get(key);
        if (val == null) return fallback;
        if (val instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(val));
    }

    public static String getString(@NotNull Map<String, Object> config, @NotNull String key, @Nullable String fallback) {
        Object val = config.get(key);
        return val != null ? String.valueOf(val) : fallback;
    }

    public static String resolvePlaceholders(@Nullable String text, @NotNull ZenExecutionContext ctx) {
        if (text == null || text.isEmpty()) return "";
        String s = text;
        s = s.replace("%level%", String.valueOf(ctx.getLevel()));
        s = s.replace("%damage%", String.valueOf(ctx.getDamage()));
        if (ctx.getPlayer() != null) {
            Player p = ctx.getPlayer();
            s = s.replace("%player%", p.getName());
            s = s.replace("%player_name%", p.getName());
            s = s.replace("%player_health%", String.valueOf(p.getHealth()));
            s = s.replace("%player_max_health%", String.valueOf(p.getMaxHealth()));
            s = s.replace("%player_food%", String.valueOf(p.getFoodLevel()));
            s = s.replace("%player_x%", String.valueOf(p.getLocation().getX()));
            s = s.replace("%player_y%", String.valueOf(p.getLocation().getY()));
            s = s.replace("%player_z%", String.valueOf(p.getLocation().getZ()));
        }
        if (ctx.getTarget() != null) {
            LivingEntity t = ctx.getTarget();
            s = s.replace("%victim%", t.getName());
            s = s.replace("%victim_name%", t.getName());
            s = s.replace("%target%", t.getName());
            s = s.replace("%victim_health%", String.valueOf(t.getHealth()));
            s = s.replace("%victim_max_health%", String.valueOf(t.getMaxHealth()));
        }
        for (Map.Entry<String, Object> entry : ctx.getData().entrySet()) {
            s = s.replace("%" + entry.getKey() + "%", String.valueOf(entry.getValue()));
            s = s.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return s;
    }
}
