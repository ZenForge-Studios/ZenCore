package com.cuac_xd.zencore.engine.filter;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FunctionalInterface
public interface ZenFilter {
    boolean test(@NotNull ZenExecutionContext context, @NotNull Map<String, Object> args);
}
