package com.cuac_xd.zencore.engine.effect;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FunctionalInterface
public interface ZenEffect {
    void execute(@NotNull ZenExecutionContext context, @NotNull Map<String, Object> args);
}
