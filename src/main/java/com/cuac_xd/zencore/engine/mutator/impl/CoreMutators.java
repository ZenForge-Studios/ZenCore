package com.cuac_xd.zencore.engine.mutator.impl;

import com.cuac_xd.zencore.engine.context.ContextResolver;
import com.cuac_xd.zencore.engine.mutator.MutatorRegistry;
import com.cuac_xd.zencore.util.MathEvaluator;

public final class CoreMutators {

    private CoreMutators() {}

    public static void registerAll(MutatorRegistry registry) {
        registry.register("DAMAGE_ADDITION", (ctx, args) -> {
            String expr = String.valueOf(args.getOrDefault("amount", args.getOrDefault("value", "0")));
            expr = ContextResolver.resolvePlaceholders(expr, ctx);
            double add = MathEvaluator.eval(expr);
            ctx.setDamage(ctx.getDamage() + add);
        });

        registry.register("DAMAGE_MULTIPLIER", (ctx, args) -> {
            String expr = String.valueOf(args.getOrDefault("multiplier", args.getOrDefault("value", "1.0")));
            expr = ContextResolver.resolvePlaceholders(expr, ctx);
            double mul = MathEvaluator.eval(expr);
            ctx.setDamage(ctx.getDamage() * mul);
        });

        registry.register("SET_DAMAGE", (ctx, args) -> {
            String expr = String.valueOf(args.getOrDefault("amount", args.getOrDefault("value", "1.0")));
            expr = ContextResolver.resolvePlaceholders(expr, ctx);
            ctx.setDamage(MathEvaluator.eval(expr));
        });

        registry.register("LEVEL_MULTIPLIER", (ctx, args) -> {
            double perLevel = ContextResolver.getDouble(args, "per_level", 0.1);
            ctx.setDamage(ctx.getDamage() * (1.0 + (ctx.getLevel() * perLevel)));
        });

        registry.register("VARIABLE_ADDITION", (ctx, args) -> {
            String var = (String) args.get("variable");
            if (var != null) {
                double add = ContextResolver.getDouble(args, "amount", 1.0);
                Object cur = ctx.getData().get(var);
                double currentVal = (cur instanceof Number n) ? n.doubleValue() : 0.0;
                ctx.getData().put(var, currentVal + add);
            }
        });

        registry.register("VARIABLE_MULTIPLIER", (ctx, args) -> {
            String var = (String) args.get("variable");
            if (var != null) {
                double mul = ContextResolver.getDouble(args, "multiplier", 1.0);
                Object cur = ctx.getData().get(var);
                double currentVal = (cur instanceof Number n) ? n.doubleValue() : 1.0;
                ctx.getData().put(var, currentVal * mul);
            }
        });
    }
}
