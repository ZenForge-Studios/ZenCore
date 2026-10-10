package com.cuac_xd.zencore.projectile

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

/**
 * Custom RayTrace and Particle Projectile Engine for advanced spells and abilities.
 */
object ZenProjectileEngine {

    fun interface HitCallback {
        fun onHit(hitEntity: LivingEntity?, hitLocation: Location)
    }

    @JvmStatic
    fun launchRayTraceProjectile(
        plugin: Plugin,
        source: Player,
        speed: Double = 1.5,
        maxDistance: Double = 30.0,
        particle: Particle = Particle.FLAME,
        stepSize: Double = 0.5,
        hitRadius: Double = 0.8,
        onHit: HitCallback
    ) {
        val start = source.eyeLocation
        val direction = start.direction.normalize().multiply(stepSize)
        var current = start.clone()
        var traveled = 0.0

        val task = Bukkit.getScheduler().runTaskTimer(plugin, { t ->
            val stepsPerTick = (speed / stepSize).toInt().coerceAtLeast(1)
            for (i in 0 until stepsPerTick) {
                current.add(direction)
                traveled += stepSize

                // Spawn beam particle
                current.world.spawnParticle(particle, current, 1, 0.0, 0.0, 0.0, 0.0)

                // Check block collision
                if (current.block.type.isSolid) {
                    onHit.onHit(null, current)
                    t.cancel()
                    return@runTaskTimer
                }

                // Check entity collision
                val nearby = current.world.getNearbyEntities(current, hitRadius, hitRadius, hitRadius)
                for (e in nearby) {
                    if (e is LivingEntity && e != source) {
                        onHit.onHit(e, current)
                        t.cancel()
                        return@runTaskTimer
                    }
                }

                if (traveled >= maxDistance) {
                    t.cancel()
                    return@runTaskTimer
                }
            }
        }, 1L, 1L)
    }
}
