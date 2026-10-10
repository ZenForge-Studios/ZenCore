package com.cuac_xd.zencore.aura

import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Continuous radius and aura system for area of effect abilities.
 */
object ZenAuraEngine {

    class ActiveAura(
        val id: String,
        val centerProvider: () -> Location?,
        val radius: Double,
        val periodTicks: Long,
        val action: (LivingEntity) -> Unit
    ) {
        var taskId: Int = -1
    }

    private val auras = ConcurrentHashMap<String, ActiveAura>()

    @JvmStatic
    fun registerAura(
        plugin: Plugin,
        auraKey: String,
        radius: Double,
        periodTicks: Long,
        centerProvider: () -> Location?,
        action: (LivingEntity) -> Unit
    ) {
        stopAura(auraKey)

        val aura = ActiveAura(auraKey, centerProvider, radius, periodTicks, action)
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val loc = centerProvider() ?: return@Runnable
            val w = loc.world ?: return@Runnable
            for (e in w.getNearbyEntities(loc, radius, radius, radius)) {
                if (e is LivingEntity) {
                    action(e)
                }
            }
        }, periodTicks, periodTicks)

        aura.taskId = task.taskId
        auras[auraKey] = aura
    }

    @JvmStatic
    fun stopAura(auraKey: String) {
        val existing = auras.remove(auraKey)
        if (existing != null && existing.taskId != -1) {
            Bukkit.getScheduler().cancelTask(existing.taskId)
        }
    }

    @JvmStatic
    fun stopAll() {
        for ((_, aura) in auras) {
            if (aura.taskId != -1) Bukkit.getScheduler().cancelTask(aura.taskId)
        }
        auras.clear()
    }
}
