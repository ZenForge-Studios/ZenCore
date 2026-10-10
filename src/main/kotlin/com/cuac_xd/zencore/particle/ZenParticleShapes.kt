package com.cuac_xd.zencore.particle

import org.bukkit.Location
import org.bukkit.Particle
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mathematical geometric shapes for particle animations and magical effects.
 */
object ZenParticleShapes {

    @JvmStatic
    fun spawnCircle(
        center: Location,
        particle: Particle,
        radius: Double = 2.0,
        points: Int = 36,
        countPerPoint: Int = 1
    ) {
        val world = center.world ?: return
        val angleStep = (2 * Math.PI) / points
        for (i in 0 until points) {
            val angle = i * angleStep
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            world.spawnParticle(particle, center.x + x, center.y, center.z + z, countPerPoint, 0.0, 0.0, 0.0, 0.0)
        }
    }

    @JvmStatic
    fun spawnSphere(
        center: Location,
        particle: Particle,
        radius: Double = 2.0,
        rings: Int = 10,
        pointsPerRing: Int = 15
    ) {
        val world = center.world ?: return
        for (i in 0 until rings) {
            val phi = Math.PI * (i.toDouble() / rings)
            val y = radius * cos(phi)
            val ringRadius = radius * sin(phi)
            for (j in 0 until pointsPerRing) {
                val theta = 2 * Math.PI * (j.toDouble() / pointsPerRing)
                val x = ringRadius * cos(theta)
                val z = ringRadius * sin(theta)
                world.spawnParticle(particle, center.x + x, center.y + y, center.z + z, 1, 0.0, 0.0, 0.0, 0.0)
            }
        }
    }

    @JvmStatic
    fun spawnHelix(
        center: Location,
        particle: Particle,
        radius: Double = 1.5,
        height: Double = 3.0,
        coils: Int = 3,
        points: Int = 60
    ) {
        val world = center.world ?: return
        val stepY = height / points
        val stepAngle = (2 * Math.PI * coils) / points
        for (i in 0 until points) {
            val y = i * stepY
            val angle = i * stepAngle
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            world.spawnParticle(particle, center.x + x, center.y + y, center.z + z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }
}
