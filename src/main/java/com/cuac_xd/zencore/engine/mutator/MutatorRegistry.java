package com.cuac_xd.zencore.engine.mutator;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for ZenMutators.
 */
public class MutatorRegistry {

    private final Map<String, ZenMutator> mutators = new ConcurrentHashMap<>();

    public MutatorRegistry() {}

    public void register(@NotNull String id, @NotNull ZenMutator mutator) {
        mutators.put(id.toUpperCase(Locale.ROOT), mutator);
    }

    @Nullable
    public ZenMutator get(@Nullable String id) {
        if (id == null) return null;
        return mutators.get(id.toUpperCase(Locale.ROOT));
    }

    public boolean has(@Nullable String id) {
        return id != null && mutators.containsKey(id.toUpperCase(Locale.ROOT));
    }

    public Map<String, ZenMutator> getMutators() {
        return Collections.unmodifiableMap(mutators);
    }
}
