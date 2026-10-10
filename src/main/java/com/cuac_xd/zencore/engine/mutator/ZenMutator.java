package com.cuac_xd.zencore.engine.mutator;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FunctionalInterface
public interface ZenMutator {
    void mutate(@NotNull ZenExecutionContext context, @NotNull Map<String, Object> args);
}
