package com.cuac_xd.zencore.engine.condition;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FunctionalInterface
public interface ZenCondition {
    boolean test(@NotNull ZenExecutionContext context, @NotNull Map<String, Object> args);
}
