package com.cuac_xd.zencore.engine

import com.cuac_xd.zencore.engine.condition.ConditionRegistry
import com.cuac_xd.zencore.engine.effect.EffectRegistry
import com.cuac_xd.zencore.hooks.ZenTagsHook

/**
 * Registra condiciones y efectos nativos para ZenTags en el motor de ZenCore.
 *
 * Condiciones:
 * - `has_tag`           (args: `tag` o `id`)
 * - `has_equipped_tag`  (args: `tag` o `id`)
 * - `active_tags_count` (args: `min`, `max`, o `amount` + `operator` [= , !=, >=, <=, >, <])
 *
 * Efectos:
 * - `give_tag`          (args: `tag` o `id`)
 * - `take_tag`          (args: `tag` o `id`)
 * - `equip_tag`         (args: `tag` o `id`, opcional `bypass` / `force`: Boolean)
 * - `unequip_tag`       (args: `tag` o `id`)
 * - `clear_tags`        (sin argumentos requeridos)
 */
object ZenTagsEngineSupport {

    fun registerConditions(registry: ConditionRegistry) {
        registry.register("has_tag") { ctx, args ->
            val player = ctx.player ?: return@register false
            val tagId = resolveTagId(args) ?: return@register false
            ZenTagsHook.hasTag(player, tagId)
        }

        registry.register("has_equipped_tag") { ctx, args ->
            val player = ctx.player ?: return@register false
            val tagId = resolveTagId(args) ?: return@register false
            ZenTagsHook.hasEquippedTag(player, tagId)
        }

        registry.register("active_tags_count") { ctx, args ->
            val player = ctx.player ?: return@register false
            val count = ZenTagsHook.getActiveTagCount(player)

            val min = (args["min"] as? Number)?.toInt() ?: args["min"]?.toString()?.toIntOrNull()
            val max = (args["max"] as? Number)?.toInt() ?: args["max"]?.toString()?.toIntOrNull()
            if (min != null || max != null) {
                val passMin = min == null || count >= min
                val passMax = max == null || count <= max
                return@register passMin && passMax
            }

            val amount = (args["amount"] ?: args["value"] ?: args["count"])
                ?.let { (it as? Number)?.toInt() ?: it.toString().toIntOrNull() }
                ?: 1
            val op = (args["operator"] ?: args["op"] ?: ">=").toString().trim()
            when (op) {
                "=", "==" -> count == amount
                "!=" -> count != amount
                ">" -> count > amount
                "<" -> count < amount
                "<=" -> count <= amount
                else -> count >= amount
            }
        }
    }

    fun registerEffects(registry: EffectRegistry) {
        registry.register("give_tag") { ctx, args ->
            val player = ctx.player ?: return@register
            val tagId = resolveTagId(args) ?: return@register
            ZenTagsHook.giveTag(player, tagId)
        }

        registry.register("take_tag") { ctx, args ->
            val player = ctx.player ?: return@register
            val tagId = resolveTagId(args) ?: return@register
            ZenTagsHook.takeTag(player, tagId)
        }

        registry.register("equip_tag") { ctx, args ->
            val player = ctx.player ?: return@register
            val tagId = resolveTagId(args) ?: return@register
            val bypass = (args["bypass"] ?: args["force"])?.toString()?.toBooleanStrictOrNull() ?: false
            ZenTagsHook.equipTag(player, tagId, bypass)
        }

        registry.register("unequip_tag") { ctx, args ->
            val player = ctx.player ?: return@register
            val tagId = resolveTagId(args) ?: return@register
            ZenTagsHook.unequipTag(player, tagId)
        }

        registry.register("clear_tags") { ctx, _ ->
            val player = ctx.player ?: return@register
            ZenTagsHook.clearTags(player)
        }
    }

    private fun resolveTagId(args: Map<String, Any>): String? {
        val raw = (args["tag"] ?: args["tag_id"] ?: args["id"])?.toString()?.trim()
        return raw?.takeIf { it.isNotEmpty() }
    }
}
