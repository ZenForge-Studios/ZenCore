package com.cuac_xd.zencore.engine.condition.impl;

import com.cuac_xd.zencore.engine.condition.ConditionRegistry;
import com.cuac_xd.zencore.engine.context.ContextResolver;
import com.cuac_xd.zencore.util.MathEvaluator;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.Random;

public final class CoreConditions {

    private static final Random RANDOM = new Random();

    private CoreConditions() {}

    public static void registerAll(ConditionRegistry registry) {
        // CHANCE
        registry.register("CHANCE", (ctx, args) -> {
            String expr = String.valueOf(args.getOrDefault("chance", args.getOrDefault("value", "100")));
            expr = ContextResolver.resolvePlaceholders(expr, ctx);
            double chance = MathEvaluator.eval(expr);
            if (chance <= 1.0 && chance > 0.0) chance *= 100.0;
            return RANDOM.nextDouble() * 100.0 <= chance;
        });

        // PLAYER CHECKS
        registry.register("IS_PLAYER", (ctx, args) -> ctx.getPlayer() != null);
        registry.register("PLAYER", registry.get("IS_PLAYER"));

        registry.register("IS_SNEAKING", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isSneaking());
        registry.register("SNEAKING", registry.get("IS_SNEAKING"));

        registry.register("IS_SPRINTING", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isSprinting());
        registry.register("SPRINTING", registry.get("IS_SPRINTING"));

        registry.register("IS_GLIDING", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isGliding());
        registry.register("GLIDING", registry.get("IS_GLIDING"));

        registry.register("IS_SWIMMING", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isSwimming());
        registry.register("SWIMMING", registry.get("IS_SWIMMING"));

        registry.register("IS_FLYING", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isFlying());
        registry.register("FLYING", registry.get("IS_FLYING"));

        registry.register("IS_BLOCKING", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isBlocking());
        registry.register("BLOCKING", registry.get("IS_BLOCKING"));

        registry.register("IS_BURNING", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().getFireTicks() > 0);
        registry.register("IS_ON_FIRE", registry.get("IS_BURNING"));
        registry.register("ON_FIRE", registry.get("IS_BURNING"));

        registry.register("IS_IN_WATER", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isInWater());
        registry.register("IN_WATER", registry.get("IS_IN_WATER"));

        registry.register("IS_IN_LAVA", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().getLocation().getBlock().getType() == Material.LAVA);
        registry.register("IN_LAVA", registry.get("IS_IN_LAVA"));

        registry.register("IS_IN_AIR", (ctx, args) -> ctx.getPlayer() != null && !ctx.getPlayer().isOnGround() && !ctx.getPlayer().isInWater());
        registry.register("IN_AIR", registry.get("IS_IN_AIR"));

        registry.register("IS_ON_GROUND", (ctx, args) -> ctx.getPlayer() != null && ctx.getPlayer().isOnGround());
        registry.register("ON_GROUND", registry.get("IS_ON_GROUND"));

        // TARGET / VICTIM CHECKS
        registry.register("VICTIM_IS_ALIVE", (ctx, args) -> ctx.getTarget() != null && !ctx.getTarget().isDead());
        registry.register("VICTIM_IS_PLAYER", (ctx, args) -> ctx.getTarget() instanceof Player);
        registry.register("VICTIM_IS_MONSTER", (ctx, args) -> ctx.getTarget() instanceof Monster);
        registry.register("VICTIM_IS_ANIMAL", (ctx, args) -> ctx.getTarget() instanceof Animals);
        registry.register("VICTIM_IS_BOSS", (ctx, args) -> ctx.getTarget() instanceof Boss);

        // HEALTH & FOOD
        registry.register("HEALTH_BELOW", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            double limit = ContextResolver.getDouble(args, "health", ContextResolver.getDouble(args, "amount", ContextResolver.getDouble(args, "value", 10.0)));
            return ctx.getPlayer().getHealth() < limit;
        });

        registry.register("HEALTH_ABOVE", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            double limit = ContextResolver.getDouble(args, "health", ContextResolver.getDouble(args, "amount", ContextResolver.getDouble(args, "value", 10.0)));
            return ctx.getPlayer().getHealth() > limit;
        });

        registry.register("HEALTH_PERCENT_BELOW", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            double percent = ContextResolver.getDouble(args, "percent", ContextResolver.getDouble(args, "value", 50.0));
            return (ctx.getPlayer().getHealth() / ctx.getPlayer().getMaxHealth()) * 100.0 < percent;
        });

        registry.register("VICTIM_HEALTH_BELOW", (ctx, args) -> {
            if (ctx.getTarget() == null) return false;
            double limit = ContextResolver.getDouble(args, "health", ContextResolver.getDouble(args, "amount", ContextResolver.getDouble(args, "value", 10.0)));
            return ctx.getTarget().getHealth() < limit;
        });

        registry.register("VICTIM_HEALTH_PERCENT_BELOW", (ctx, args) -> {
            if (ctx.getTarget() == null) return false;
            double percent = ContextResolver.getDouble(args, "percent", ContextResolver.getDouble(args, "value", 50.0));
            return (ctx.getTarget().getHealth() / ctx.getTarget().getMaxHealth()) * 100.0 < percent;
        });

        registry.register("FOOD_BELOW", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            int limit = ContextResolver.getInt(args, "food", ContextResolver.getInt(args, "amount", ContextResolver.getInt(args, "value", 6)));
            return ctx.getPlayer().getFoodLevel() < limit;
        });

        registry.register("FOOD_ABOVE", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            int limit = ContextResolver.getInt(args, "food", ContextResolver.getInt(args, "amount", ContextResolver.getInt(args, "value", 14)));
            return ctx.getPlayer().getFoodLevel() > limit;
        });

        // EXP & LEVEL
        registry.register("LEVEL_ABOVE", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            int limit = ContextResolver.getInt(args, "level", ContextResolver.getInt(args, "value", 30));
            return ctx.getPlayer().getLevel() >= limit;
        });

        registry.register("LEVEL_BELOW", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            int limit = ContextResolver.getInt(args, "level", ContextResolver.getInt(args, "value", 30));
            return ctx.getPlayer().getLevel() < limit;
        });

        // PERMISSION & POTIONS
        registry.register("HAS_PERMISSION", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            String perm = (String) args.getOrDefault("permission", args.get("value"));
            return perm != null && ctx.getPlayer().hasPermission(perm);
        });

        registry.register("HAS_POTION_EFFECT", (ctx, args) -> {
            if (ctx.getPlayer() == null) return false;
            String effectStr = (String) args.getOrDefault("effect", args.get("value"));
            if (effectStr == null) return false;
            PotionEffectType type = PotionEffectType.getByName(effectStr.toUpperCase(Locale.ROOT));
            return type != null && ctx.getPlayer().hasPotionEffect(type);
        });

        // ENVIRONMENT & WORLD
        registry.register("IS_DAY", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return false;
            long time = p.getWorld().getTime();
            return time < 12300 || time > 23850;
        });

        registry.register("IS_NIGHT", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return false;
            long time = p.getWorld().getTime();
            return time >= 12300 && time <= 23850;
        });

        registry.register("IS_RAINING", (ctx, args) -> {
            Player p = ctx.getPlayer();
            return p != null && (p.getWorld().hasStorm() || p.getWorld().isThundering());
        });

        registry.register("IS_THUNDERING", (ctx, args) -> {
            Player p = ctx.getPlayer();
            return p != null && p.getWorld().isThundering();
        });

        registry.register("IN_NETHER", (ctx, args) -> {
            Player p = ctx.getPlayer();
            return p != null && p.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER;
        });

        registry.register("IN_END", (ctx, args) -> {
            Player p = ctx.getPlayer();
            return p != null && p.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END;
        });

        registry.register("IN_OVERWORLD", (ctx, args) -> {
            Player p = ctx.getPlayer();
            return p != null && p.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL;
        });

        // LIGHT & ELEVATION
        registry.register("LIGHT_BELOW", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return false;
            int limit = ContextResolver.getInt(args, "light", ContextResolver.getInt(args, "value", 7));
            return p.getLocation().getBlock().getLightLevel() < limit;
        });

        registry.register("Y_BELOW", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return false;
            double limit = ContextResolver.getDouble(args, "y", ContextResolver.getDouble(args, "value", 0.0));
            return p.getLocation().getY() < limit;
        });

        registry.register("Y_ABOVE", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return false;
            double limit = ContextResolver.getDouble(args, "y", ContextResolver.getDouble(args, "value", 64.0));
            return p.getLocation().getY() > limit;
        });

        // VARIABLES
        registry.register("VARIABLE_EQUALS", (ctx, args) -> {
            String var = (String) args.get("variable");
            if (var == null) return false;
            Object exp = args.get("value");
            Object actual = ctx.getData().get(var);
            return String.valueOf(actual).equalsIgnoreCase(String.valueOf(exp));
        });

        registry.register("VARIABLE_ABOVE", (ctx, args) -> {
            String var = (String) args.get("variable");
            if (var == null) return false;
            double limit = ContextResolver.getDouble(args, "value", 0.0);
            Object actual = ctx.getData().get(var);
            double val = (actual instanceof Number n) ? n.doubleValue() : 0.0;
            return val > limit;
        });
    }
}
