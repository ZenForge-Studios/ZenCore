package com.cuac_xd.zencore.engine.filter.impl

import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.filter.FilterRegistry
import org.bukkit.entity.Player

object CoreFilters {
    @JvmStatic
    fun registerAll(registry: FilterRegistry) {
        // ENTITY / VICTIM TYPE
        registry.register("VICTIM_TYPE") { ctx, args ->
            val ent = ContextResolver.resolveEntityTarget(ctx, args["target"] as? String ?: "victim") ?: return@register false
            val typeName = args["type"] as? String
            if (typeName == null) {
                @Suppress("UNCHECKED_CAST")
                val types = args["types"] as? List<String>
                return@register types?.any { it.equals(ent.type.name, ignoreCase = true) } ?: false
            }
            ent.type.name.equals(typeName, ignoreCase = true)
        }
        val victimFilter = registry.get("VICTIM_TYPE")
        if (victimFilter != null) registry.register("ENTITY_TYPE", victimFilter)

        // BLOCK TYPE
        registry.register("BLOCK_TYPE") { ctx, args ->
            val block = ctx.block ?: return@register false
            val matName = args["material"] as? String
            if (matName == null) {
                @Suppress("UNCHECKED_CAST")
                val mats = args["materials"] as? List<String>
                return@register mats?.any { it.equals(block.type.name, ignoreCase = true) } ?: false
            }
            block.type.name.equals(matName, ignoreCase = true)
        }

        // ITEM TYPE
        registry.register("ITEM_TYPE") { ctx, args ->
            val item = ctx.item ?: return@register false
            val matName = args["material"] as? String
            if (matName == null) {
                @Suppress("UNCHECKED_CAST")
                val mats = args["materials"] as? List<String>
                return@register mats?.any { it.equals(item.type.name, ignoreCase = true) } ?: false
            }
            item.type.name.equals(matName, ignoreCase = true)
        }

        // WORLD FILTER
        registry.register("WORLD") { ctx, args ->
            val w = ctx.location?.world ?: ctx.player?.world ?: return@register false
            val worldName = args["world"] as? String
            if (worldName == null) {
                @Suppress("UNCHECKED_CAST")
                val worlds = args["worlds"] as? List<String>
                return@register worlds?.any { it.equals(w.name, ignoreCase = true) } ?: false
            }
            w.name.equals(worldName, ignoreCase = true)
        }

        // PERMISSION FILTER
        registry.register("PERMISSION") { ctx, args ->
            val p = ctx.player ?: return@register false
            val perm = args["permission"] as? String
            perm != null && p.hasPermission(perm)
        }

        // GAMEMODE FILTER
        registry.register("GAMEMODE") { ctx, args ->
            val p = ctx.player ?: return@register false
            val gm = args["gamemode"] as? String
            gm != null && p.gameMode.name.equals(gm, ignoreCase = true)
        }
    }
}
