package com.cuac_xd.zencore.engine.condition;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for ZenConditions.
 */
public class ConditionRegistry {

    private final Map<String, ZenCondition> conditions = new ConcurrentHashMap<>();

    public ConditionRegistry() {}

    public void register(@NotNull String id, @NotNull ZenCondition condition) {
        conditions.put(id.toUpperCase(Locale.ROOT), condition);
    }

    @Nullable
    public ZenCondition get(@Nullable String id) {
        if (id == null) return null;
        return conditions.get(id.toUpperCase(Locale.ROOT));
    }

    public boolean has(@Nullable String id) {
        return id != null && conditions.containsKey(id.toUpperCase(Locale.ROOT));
    }

    public Map<String, ZenCondition> getConditions() {
        return Collections.unmodifiableMap(conditions);
    }
}
