package com.cuac_xd.zencore.engine;

import com.cuac_xd.zencore.engine.condition.ConditionRegistry;
import com.cuac_xd.zencore.engine.condition.ZenCondition;
import com.cuac_xd.zencore.engine.condition.impl.CoreConditions;
import com.cuac_xd.zencore.engine.context.ContextResolver;
import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import com.cuac_xd.zencore.engine.effect.EffectRegistry;
import com.cuac_xd.zencore.engine.effect.ZenEffect;
import com.cuac_xd.zencore.engine.effect.impl.CoreEffects;
import com.cuac_xd.zencore.engine.effect.impl.ExtendedEffects;
import com.cuac_xd.zencore.engine.filter.FilterRegistry;
import com.cuac_xd.zencore.engine.filter.ZenFilter;
import com.cuac_xd.zencore.engine.filter.impl.CoreFilters;
import com.cuac_xd.zencore.engine.mutator.MutatorRegistry;
import com.cuac_xd.zencore.engine.mutator.ZenMutator;
import com.cuac_xd.zencore.engine.mutator.impl.CoreMutators;
import com.cuac_xd.zencore.util.AdventureUtil;
import com.cuac_xd.zencore.util.MathEvaluator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main Central Engine coordinating Condition, Effect, Filter, and Mutator execution,
 * cooldown tracking, and safe damage handling.
 */
public class ZenEngine {

    private final ConditionRegistry conditionRegistry = new ConditionRegistry();
    private final EffectRegistry effectRegistry = new EffectRegistry();
    private final FilterRegistry filterRegistry = new FilterRegistry();
    private final MutatorRegistry mutatorRegistry = new MutatorRegistry();

    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final ThreadLocal<Boolean> internalDamage = ThreadLocal.withInitial(() -> false);

    public ZenEngine() {
        registerDefaults();
    }

    private void registerDefaults() {
        CoreFilters.registerAll(filterRegistry);
        CoreMutators.registerAll(mutatorRegistry);
        CoreConditions.registerAll(conditionRegistry);
        CoreEffects.registerAll(effectRegistry);
        ExtendedEffects.registerAll(effectRegistry);
    }

    public boolean testConditions(@NotNull ZenExecutionContext context, @Nullable List<Map<String, Object>> conditionConfigs) {
        if (conditionConfigs == null || conditionConfigs.isEmpty()) return true;
        for (Map<String, Object> condMap : conditionConfigs) {
            String condId = (String) condMap.get("id");
            if (condId == null) condId = (String) condMap.get("type");
            if (condId == null) continue;

            ZenCondition cond = conditionRegistry.get(condId);
            if (cond != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> condArgs = (Map<String, Object>) condMap.getOrDefault("args", condMap.getOrDefault("parameters", Collections.emptyMap()));
                boolean result = cond.test(context, condArgs);
                boolean isNot = (boolean) condMap.getOrDefault("not", condMap.getOrDefault("inverted", false));
                if (isNot ? result : !result) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean testFilters(@NotNull ZenExecutionContext context, @Nullable List<Map<String, Object>> filterConfigs) {
        return filterRegistry.testAll(context, filterConfigs);
    }

    public void executeMutators(@NotNull ZenExecutionContext context, @Nullable List<Map<String, Object>> mutatorConfigs) {
        if (mutatorConfigs == null || mutatorConfigs.isEmpty()) return;
        for (Map<String, Object> mutMap : mutatorConfigs) {
            String mutId = (String) mutMap.get("id");
            if (mutId == null) mutId = (String) mutMap.get("type");
            if (mutId == null) continue;

            ZenMutator mutator = mutatorRegistry.get(mutId);
            if (mutator != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mutArgs = (Map<String, Object>) mutMap.getOrDefault("args", mutMap.getOrDefault("parameters", Collections.emptyMap()));
                mutator.mutate(context, mutArgs);
            }
        }
    }

    public void executeEffect(@NotNull String effectId, @NotNull ZenExecutionContext context, @NotNull Map<String, Object> rawArgs) {
        ZenEffect effect = effectRegistry.get(effectId);
        if (effect == null) return;

        Map<String, Object> resolved = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawArgs.entrySet()) {
            if (entry.getValue() instanceof String strVal) {
                resolved.put(entry.getKey(), ContextResolver.resolvePlaceholders(strVal, context));
            } else {
                resolved.put(entry.getKey(), entry.getValue());
            }
        }
        effect.execute(context, resolved);
    }

    public boolean checkAndSetCooldown(@NotNull String key, double cooldownSeconds) {
        if (cooldownSeconds <= 0.0) return true;
        long now = System.currentTimeMillis();
        long expire = cooldowns.getOrDefault(key, 0L);
        if (now < expire) {
            return false;
        }
        cooldowns.put(key, now + (long) (cooldownSeconds * 1000.0));
        return true;
    }

    public double getRemainingCooldown(@NotNull String key) {
        long now = System.currentTimeMillis();
        long expire = cooldowns.getOrDefault(key, 0L);
        if (now >= expire) return 0.0;
        return (expire - now) / 1000.0;
    }

    public void clearCooldowns(UUID uuid) {
        String prefix = uuid.toString() + ":";
        cooldowns.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
    }

    public void pruneExpiredCooldowns() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(e -> e.getValue() <= now);
    }

    public boolean isInternalDamage() {
        return internalDamage.get();
    }

    public void damageFromEffect(@NotNull LivingEntity target, double amount, @Nullable Entity source) {
        boolean prev = internalDamage.get();
        internalDamage.set(true);
        try {
            if (source != null) {
                target.damage(amount, source);
            } else {
                target.damage(amount);
            }
        } finally {
            internalDamage.set(prev);
        }
    }

    public ConditionRegistry getConditionRegistry() { return conditionRegistry; }
    public EffectRegistry getEffectRegistry() { return effectRegistry; }
    public FilterRegistry getFilterRegistry() { return filterRegistry; }
    public MutatorRegistry getMutatorRegistry() { return mutatorRegistry; }
}
