package com.cuac_xd.zencore.engine.context

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

/**
 * Universal execution context for triggers, conditions, mutators, filters, and effects.
 */
open class ZenExecutionContext(
    var player: Player? = null,
    var target: LivingEntity? = null,
    var block: Block? = null,
    var item: ItemStack? = null
) {
    var trigger: String? = null
    var targetEntity: Entity? = target
    var location: Location? = player?.location ?: target?.location ?: block?.location?.clone()?.add(0.5, 0.5, 0.5)
    var level: Int = 1
    var damage: Double = 0.0
    var isCancelled: Boolean = false
        set(value) {
            field = value
            val ev = event
            if (ev is Cancellable) {
                ev.isCancelled = value
            }
        }
    var event: Event? = null
    val data: MutableMap<String, Any> = ConcurrentHashMap()

    fun cancelEvent() {
        isCancelled = true
    }

    fun getCustomData(key: String): Any? = data[key]

    fun setCustomData(key: String, value: Any) {
        data[key] = value
    }

    class Builder {
        private val ctx = ZenExecutionContext()

        fun trigger(trigger: String?) = apply { ctx.trigger = trigger }
        fun player(player: Player?) = apply { ctx.player = player }
        fun target(target: LivingEntity?) = apply {
            ctx.target = target
            ctx.targetEntity = target
        }
        fun targetEntity(targetEntity: Entity?) = apply {
            ctx.targetEntity = targetEntity
            if (targetEntity is LivingEntity) ctx.target = targetEntity
        }
        fun block(block: Block?) = apply { ctx.block = block }
        fun item(item: ItemStack?) = apply { ctx.item = item }
        fun location(location: Location?) = apply { ctx.location = location }
        fun level(level: Int) = apply { ctx.level = level }
        fun damage(damage: Double) = apply { ctx.damage = damage }
        fun event(event: Event?) = apply { ctx.event = event }
        fun data(key: String, value: Any) = apply { ctx.data[key] = value }

        fun build(): ZenExecutionContext = ctx
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
