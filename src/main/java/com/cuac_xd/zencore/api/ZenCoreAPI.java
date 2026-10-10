package com.cuac_xd.zencore.api;

import com.cuac_xd.zencore.ZenCorePlugin;
import com.cuac_xd.zencore.engine.ZenEngine;
import com.cuac_xd.zencore.engine.condition.ZenCondition;
import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import com.cuac_xd.zencore.engine.effect.ZenEffect;
import com.cuac_xd.zencore.engine.filter.ZenFilter;
import com.cuac_xd.zencore.engine.mutator.ZenMutator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Public API for ZenCore.
 */
public final class ZenCoreAPI {

    private static ZenCorePlugin plugin;

    private ZenCoreAPI() {}

    private static ZenEngine fallbackEngine;

    public static void init(@NotNull ZenCorePlugin instance) {
        plugin = instance;
    }

    @Nullable
    public static ZenCorePlugin getPlugin() {
        return plugin;
    }

    @NotNull
    public static ZenEngine getEngine() {
        if (plugin != null && plugin.getEngine() != null) {
            return plugin.getEngine();
        }
        if (fallbackEngine == null) {
            fallbackEngine = new ZenEngine();
        }
        return fallbackEngine;
    }

    public static void registerEffect(@NotNull String id, @NotNull ZenEffect effect) {
        getEngine().getEffectRegistry().register(id, effect);
    }

    public static void registerCondition(@NotNull String id, @NotNull ZenCondition condition) {
        getEngine().getConditionRegistry().register(id, condition);
    }

    public static void registerFilter(@NotNull String id, @NotNull ZenFilter filter) {
        getEngine().getFilterRegistry().register(id, filter);
    }

    public static void registerMutator(@NotNull String id, @NotNull ZenMutator mutator) {
        getEngine().getMutatorRegistry().register(id, mutator);
    }

    public static boolean executeEffect(@NotNull String id, @NotNull ZenExecutionContext context, @NotNull Map<String, Object> args) {
        ZenEffect effect = getEngine().getEffectRegistry().get(id);
        if (effect == null) return false;
        getEngine().executeEffect(id, context, args);
        return true;
    }

    public static boolean testCondition(@NotNull String id, @NotNull ZenExecutionContext context, @NotNull Map<String, Object> args) {
        ZenCondition condition = getEngine().getConditionRegistry().get(id);
        if (condition == null) return false;
        return condition.test(context, args);
    }

    public static void clearCooldowns(@NotNull UUID playerUUID) {
        getEngine().clearCooldowns(playerUUID);
    }
}
