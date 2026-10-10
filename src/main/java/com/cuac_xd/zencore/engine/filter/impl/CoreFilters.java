package com.cuac_xd.zencore.engine.filter.impl;

import com.cuac_xd.zencore.engine.context.ContextResolver;
import com.cuac_xd.zencore.engine.filter.FilterRegistry;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

public final class CoreFilters {

    private CoreFilters() {}

    public static void registerAll(FilterRegistry registry) {
        // ENTITY / VICTIM TYPE
        registry.register("VICTIM_TYPE", (ctx, args) -> {
            Entity ent = ContextResolver.resolveEntityTarget(ctx, (String) args.getOrDefault("target", "victim"));
            if (ent == null) return false;
            String typeName = (String) args.get("type");
            if (typeName == null) {
                @SuppressWarnings("unchecked")
                List<String> types = (List<String>) args.get("types");
                if (types != null) {
                    return types.stream().anyMatch(t -> t.equalsIgnoreCase(ent.getType().name()));
                }
                return false;
            }
            return ent.getType().name().equalsIgnoreCase(typeName);
        });
        registry.register("ENTITY_TYPE", registry.get("VICTIM_TYPE"));

        // BLOCK TYPE
        registry.register("BLOCK_TYPE", (ctx, args) -> {
            if (ctx.getBlock() == null) return false;
            String matName = (String) args.get("material");
            if (matName == null) {
                @SuppressWarnings("unchecked")
                List<String> mats = (List<String>) args.get("materials");
                if (mats != null) {
                    return mats.stream().anyMatch(m -> m.equalsIgnoreCase(ctx.getBlock().getType().name()));
                }
                return false;
            }
            return ctx.getBlock().getType().name().equalsIgnoreCase(matName);
        });

        // ITEM TYPE / MATERIAL
        registry.register("ITEM_TYPE", (ctx, args) -> {
            ItemStack item = ctx.getItem();
            if (item == null) return false;
            String matName = (String) args.get("material");
            if (matName == null) {
                @SuppressWarnings("unchecked")
                List<String> mats = (List<String>) args.get("materials");
                if (mats != null) {
                    return mats.stream().anyMatch(m -> m.equalsIgnoreCase(item.getType().name()));
                }
                return false;
            }
            return item.getType().name().equalsIgnoreCase(matName);
        });

        // WORLD FILTER
        registry.register("WORLD", (ctx, args) -> {
            World w = ctx.getLocation() != null ? ctx.getLocation().getWorld() : (ctx.getPlayer() != null ? ctx.getPlayer().getWorld() : null);
            if (w == null) return false;
            String worldName = (String) args.get("world");
            if (worldName == null) {
                @SuppressWarnings("unchecked")
                List<String> worlds = (List<String>) args.get("worlds");
                if (worlds != null) {
                    return worlds.stream().anyMatch(n -> n.equalsIgnoreCase(w.getName()));
                }
                return false;
            }
            return w.getName().equalsIgnoreCase(worldName);
        });

        // PERMISSION FILTER
        registry.register("PERMISSION", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return false;
            String perm = (String) args.get("permission");
            return perm != null && p.hasPermission(perm);
        });

        // GAMEMODE FILTER
        registry.register("GAMEMODE", (ctx, args) -> {
            Player p = ctx.getPlayer();
            if (p == null) return false;
            String gm = (String) args.get("gamemode");
            return gm != null && p.getGameMode().name().equalsIgnoreCase(gm);
        });
    }
}
