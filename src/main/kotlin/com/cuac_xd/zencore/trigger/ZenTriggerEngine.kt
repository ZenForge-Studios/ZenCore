package com.cuac_xd.zencore.trigger

import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Universal Trigger Engine that listens to core Paper events and dispatches unified ZenExecutionContexts.
 */
object ZenTriggerEngine : Listener {

    fun interface TriggerHandler {
        fun handle(triggerId: String, context: ZenExecutionContext)
    }

    private val handlers = CopyOnWriteArrayList<TriggerHandler>()

    @JvmStatic
    fun registerHandler(handler: TriggerHandler) {
        handlers.add(handler)
    }

    private fun dispatch(triggerId: String, context: ZenExecutionContext) {
        for (h in handlers) {
            h.handle(triggerId, context)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
        val damager = e.damager as? LivingEntity
        val victim = e.entity as? LivingEntity
        val p = (damager as? Player) ?: (victim as? Player)

        val ctx = ZenExecutionContext(
            player = p,
            targetEntity = if (p == damager) victim else damager,
            event = e,
            damage = e.finalDamage,
            location = e.entity.location
        )

        if (damager is Player) {
            dispatch("melee_attack", ctx)
            dispatch("attack", ctx)
        }
        if (victim is Player) {
            dispatch("take_damage", ctx)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            block = e.block,
            location = e.block.location,
            event = e,
            item = e.player.inventory.itemInMainHand
        )
        dispatch("mine_block", ctx)
        dispatch("break_block", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            block = e.clickedBlock,
            location = e.clickedBlock?.location ?: e.player.location,
            item = e.item,
            event = e
        )
        if (e.action.isRightClick) {
            dispatch("right_click", ctx)
        } else if (e.action.isLeftClick) {
            dispatch("left_click", ctx)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityDeath(e: EntityDeathEvent) {
        val killer = e.entity.killer
        val ctx = ZenExecutionContext(
            player = killer,
            targetEntity = e.entity,
            location = e.entity.location,
            event = e
        )
        if (killer != null) {
            dispatch("kill_entity", ctx)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onSneakToggle(e: PlayerToggleSneakEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            location = e.player.location,
            event = e
        )
        if (e.isSneaking) dispatch("sneak", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onSprintToggle(e: PlayerToggleSprintEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            location = e.player.location,
            event = e
        )
        if (e.isSprinting) dispatch("sprint", ctx)
    }
}
