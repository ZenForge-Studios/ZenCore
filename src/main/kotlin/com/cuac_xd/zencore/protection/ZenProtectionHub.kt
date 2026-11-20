package com.cuac_xd.zencore.protection

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent

/**
 * Unified region protection integration hub (WorldGuard, GriefPrevention, Towny, Residence fallback).
 */
object ZenProtectionHub {

    interface ProtectionHook {
        fun canBreak(player: Player, block: Block): Boolean
        fun canDamage(attacker: Player, victim: Entity): Boolean
    }

    private val hooks = mutableListOf<ProtectionHook>()
    private val checkingBreak = ThreadLocal.withInitial { false }

    @JvmStatic
    fun registerHook(hook: ProtectionHook) {
        hooks.add(hook)
    }

    @JvmStatic
    fun isSimulatingBreak(): Boolean = checkingBreak.get()

    /**
     * Checks only registered ProtectionHooks without firing a synthetic BlockBreakEvent.
     * Ideal for use inside an active BlockBreakEvent or BlockPlaceEvent listener.
     */
    @JvmStatic
    fun checkHooksOnly(player: Player, block: Block): Boolean {
        for (h in hooks) {
            if (!h.canBreak(player, block)) return false
        }
        return true
    }

    @JvmStatic
    fun canBreak(player: Player, block: Block): Boolean {
        if (!checkHooksOnly(player, block)) return false
        if (checkingBreak.get()) return true

        checkingBreak.set(true)
        return try {
            val testEvent = BlockBreakEvent(block, player)
            org.bukkit.Bukkit.getPluginManager().callEvent(testEvent)
            !testEvent.isCancelled
        } finally {
            checkingBreak.set(false)
        }
    }

    @JvmStatic
    fun canDamage(attacker: Player, victim: Entity): Boolean {
        for (h in hooks) {
            if (!h.canDamage(attacker, victim)) return false
        }
        @Suppress("DEPRECATION")
        val testEvent = org.bukkit.event.entity.EntityDamageByEntityEvent(
            attacker,
            victim,
            EntityDamageEvent.DamageCause.CUSTOM,
            1.0
        )
        org.bukkit.Bukkit.getPluginManager().callEvent(testEvent)
        return !testEvent.isCancelled
    }
}
