package com.cuac_xd.zencore.engine.filter;

import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for ZenFilters.
 */
public class FilterRegistry {

    private final Map<String, ZenFilter> filters = new ConcurrentHashMap<>();

    public FilterRegistry() {}

    public void register(@NotNull String id, @NotNull ZenFilter filter) {
        filters.put(id.toUpperCase(Locale.ROOT), filter);
    }

    @Nullable
    public ZenFilter get(@Nullable String id) {
        if (id == null) return null;
        return filters.get(id.toUpperCase(Locale.ROOT));
    }

    public boolean has(@Nullable String id) {
        return id != null && filters.containsKey(id.toUpperCase(Locale.ROOT));
    }

    public boolean testAll(@NotNull ZenExecutionContext context, @Nullable List<Map<String, Object>> filterConfigs) {
        if (filterConfigs == null || filterConfigs.isEmpty()) return true;
        for (Map<String, Object> map : filterConfigs) {
            String id = (String) map.get("id");
            if (id == null) continue;
            ZenFilter filter = get(id);
            if (filter != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> args = (Map<String, Object>) map.getOrDefault("args", Collections.emptyMap());
                boolean pass = filter.test(context, args);
                boolean isNot = (boolean) map.getOrDefault("not", false);
                if (isNot ? pass : !pass) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<String, ZenFilter> getFilters() {
        return Collections.unmodifiableMap(filters);
    }
}
