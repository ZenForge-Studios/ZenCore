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
open class ZenExecutionContext @JvmOverloads constructor(
    var player: Player? = null,
    var target: LivingEntity? = null,
    var block: Block? = null,
    var item: ItemStack? = null,
    var targetEntity: Entity? = target,
    var location: Location? = null,
    var damage: Double = 0.0,
    var event: Event? = null,
    var level: Int = 1
) {
    var trigger: String? = null
    var isCancelled: Boolean = false
        set(value) {
            field = value
            val ev = event
            if (ev is Cancellable) {
                ev.isCancelled = value
            }
        }
    val data: MutableMap<String, Any> = ConcurrentHashMap()

    init {
        if (targetEntity == null) targetEntity = target
        if (location == null) {
            location = player?.location ?: target?.location ?: block?.location?.clone()?.add(0.5, 0.5, 0.5)
        }
    }

    fun cancelEvent() {
        isCancelled = true
    }

    fun hasPlayer(): Boolean = player != null
    fun hasTarget(): Boolean = target != null || targetEntity != null
    fun hasBlock(): Boolean = block != null
    fun hasItem(): Boolean = item != null

    fun getVariable(key: String): Any? = data[key]
    fun setVariable(key: String, value: Any) {
        data[key] = value
    }

    fun setCustomData(key: String, value: Any) {
        data[key] = value
    }

    fun getCustomData(key: String): Any? = data[key]

    class Builder {
        private var player: Player? = null
        private var target: LivingEntity? = null
        private var block: Block? = null
        private var item: ItemStack? = null
        private var targetEntity: Entity? = null
        private var location: Location? = null
        private var damage: Double = 0.0
        private var event: Event? = null
        private var level: Int = 1
        private var trigger: String? = null

        fun player(player: Player?) = apply { this.player = player }
        fun target(target: LivingEntity?) = apply { this.target = target }
        fun block(block: Block?) = apply { this.block = block }
        fun item(item: ItemStack?) = apply { this.item = item }
        fun targetEntity(targetEntity: Entity?) = apply { this.targetEntity = targetEntity }
        fun location(location: Location?) = apply { this.location = location }
        fun damage(damage: Double) = apply { this.damage = damage }
        fun event(event: Event?) = apply { this.event = event }
        fun level(level: Int) = apply { this.level = level }
        fun trigger(trigger: String?) = apply { this.trigger = trigger }

        fun build(): ZenExecutionContext {
            val ctx = ZenExecutionContext(
                player = player,
                target = target,
                block = block,
                item = item,
                targetEntity = targetEntity ?: target,
                location = location,
                damage = damage,
                event = event,
                level = level
            )
            ctx.trigger = trigger
            return ctx
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
