package com.cuac_xd.zencore.trigger

import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.EntityTameEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLevelChangeEvent
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

    @JvmStatic
    fun trigger(triggerId: String, context: ZenExecutionContext) {
        dispatch(triggerId, context)
    }

    private fun dispatch(triggerId: String, context: ZenExecutionContext) {
        context.trigger = triggerId
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(e: BlockPlaceEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            block = e.blockPlaced,
            location = e.blockPlaced.location,
            item = e.itemInHand,
            event = e
        )
        dispatch("place_block", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerFish(e: PlayerFishEvent) {
        if (e.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val caught = e.caught
        val caughtItem = (caught as? Item)?.itemStack
        val ctx = ZenExecutionContext(
            player = e.player,
            target = caught as? LivingEntity,
            targetEntity = caught,
            item = caughtItem,
            location = caught?.location ?: e.hook.location,
            event = e
        )
        ctx.data["fish_state"] = e.state.name
        dispatch("fish", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onCraftItem(e: CraftItemEvent) {
        val player = e.whoClicked as? Player ?: return
        val resultItem = e.currentItem ?: e.recipe.result
        val ctx = ZenExecutionContext(
            player = player,
            item = resultItem,
            location = player.location,
            event = e
        )
        dispatch("craft_item", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerItemConsume(e: PlayerItemConsumeEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            item = e.item,
            location = e.player.location,
            event = e
        )
        dispatch("consume_item", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityShootBow(e: EntityShootBowEvent) {
        val shooter = e.entity as? Player ?: return
        val ctx = ZenExecutionContext(
            player = shooter,
            targetEntity = e.projectile,
            item = e.bow,
            location = shooter.location,
            event = e
        )
        ctx.data["force"] = e.force.toDouble()
        dispatch("shoot_bow", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onProjectileHit(e: ProjectileHitEvent) {
        val shooter = e.entity.shooter as? Player ?: return
        val hitEntity = e.hitEntity
        val hitBlock = e.hitBlock
        val ctx = ZenExecutionContext(
            player = shooter,
            target = hitEntity as? LivingEntity,
            targetEntity = hitEntity,
            block = hitBlock,
            location = hitEntity?.location ?: hitBlock?.location ?: e.entity.location,
            event = e
        )
        ctx.data["projectile"] = e.entity
        dispatch("projectile_hit", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerExpChange(e: PlayerExpChangeEvent) {
        if (e.amount <= 0) return
        val ctx = ZenExecutionContext(
            player = e.player,
            location = e.player.location,
            event = e
        )
        ctx.data["amount"] = e.amount
        dispatch("xp_gain", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerLevelChange(e: PlayerLevelChangeEvent) {
        if (e.newLevel <= e.oldLevel) return
        val ctx = ZenExecutionContext(
            player = e.player,
            location = e.player.location,
            event = e,
            level = e.newLevel
        )
        ctx.data["old_level"] = e.oldLevel
        ctx.data["new_level"] = e.newLevel
        dispatch("level_up", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            location = e.player.location,
            event = e
        )
        dispatch("player_join", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val ctx = ZenExecutionContext(
            player = e.player,
            location = e.player.location,
            event = e
        )
        dispatch("player_quit", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityBreed(e: EntityBreedEvent) {
        val breeder = e.breeder as? Player ?: return
        val ctx = ZenExecutionContext(
            player = breeder,
            target = e.entity,
            targetEntity = e.entity,
            item = e.bredWith,
            location = e.entity.location,
            event = e
        )
        dispatch("breed_entity", ctx)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityTame(e: EntityTameEvent) {
        val owner = e.owner as? Player ?: return
        val ctx = ZenExecutionContext(
            player = owner,
            target = e.entity,
            targetEntity = e.entity,
            location = e.entity.location,
            event = e
        )
        dispatch("tame_entity", ctx)
    }
}
