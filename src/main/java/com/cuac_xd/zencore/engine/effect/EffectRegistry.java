package com.cuac_xd.zencore.engine.effect;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for ZenEffects.
 */
public class EffectRegistry {

    private final Map<String, ZenEffect> effects = new ConcurrentHashMap<>();

    public EffectRegistry() {}

    public void register(@NotNull String id, @NotNull ZenEffect effect) {
        effects.put(id.toUpperCase(Locale.ROOT), effect);
    }

    @Nullable
    public ZenEffect get(@Nullable String id) {
        if (id == null) return null;
        return effects.get(id.toUpperCase(Locale.ROOT));
    }

    public boolean has(@Nullable String id) {
        return id != null && effects.containsKey(id.toUpperCase(Locale.ROOT));
    }

    public Map<String, ZenEffect> getEffects() {
        return Collections.unmodifiableMap(effects);
    }
}
