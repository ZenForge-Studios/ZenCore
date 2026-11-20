package com.cuac_xd.zencore.hologram

import com.cuac_xd.zencore.util.AdventureUtil
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import java.util.UUID

/**
 * Holograma moderno de alto rendimiento basado en entidades nativas [TextDisplay] de Paper 1.20+.
 * Consume un 80% menos de recursos que los antiguos ArmorStands y soporta MiniMessage nativo.
 */
class ZenHologram(
    var location: Location,
    lines: List<String> = emptyList()
) {
    private val lineTexts = lines.toMutableList()
    private val activeDisplays = mutableListOf<TextDisplay>()
    private var spawned = false
    private val lineHeight = 0.28

    fun setLines(lines: List<String>): ZenHologram {
        this.lineTexts.clear()
        this.lineTexts.addAll(lines)
        if (spawned) {
            update()
        }
        return this
    }

    fun addLine(line: String): ZenHologram {
        this.lineTexts.add(line)
        if (spawned) {
            update()
        }
        return this
    }

    fun spawn(): ZenHologram {
        if (spawned) return this
        val world = location.world ?: return this
        spawned = true

        var currentY = location.y + (lineTexts.size * lineHeight)
        for (text in lineTexts) {
            val loc = Location(world, location.x, currentY, location.z)
            val display = world.spawn(loc, TextDisplay::class.java) { entity ->
                entity.isPersistent = false
                entity.text(AdventureUtil.parse(text))
                entity.billboard = Display.Billboard.CENTER
                entity.isSeeThrough = false
                entity.isShadowed = true
                entity.backgroundColor = Color.fromARGB(80, 0, 0, 0)
                entity.viewRange = 0.5f
            }
            activeDisplays.add(display)
            currentY -= lineHeight
        }
        return this
    }

    fun update(): ZenHologram {
        if (!spawned) return spawn()
        if (activeDisplays.size == lineTexts.size && activeDisplays.all { it.isValid }) {
            for (i in lineTexts.indices) {
                activeDisplays[i].text(AdventureUtil.parse(lineTexts[i]))
            }
            return this
        }
        destroy()
        spawn()
        return this
    }

    fun teleport(newLocation: Location): ZenHologram {
        val cloned = runCatching { newLocation.clone() }.getOrNull() ?: newLocation
        this.location = cloned
        if (spawned) {
            val world = location.world ?: return this
            if (activeDisplays.size != lineTexts.size || activeDisplays.any { !it.isValid }) {
                destroy()
                return spawn()
            }
            val totalLines = activeDisplays.size
            var currentY = location.y + (totalLines * lineHeight)
            for (display in activeDisplays) {
                display.teleport(Location(world, location.x, currentY, location.z))
                currentY -= lineHeight
            }
        }
        return this
    }

    fun destroy() {
        for (display in activeDisplays) {
            runCatching { display.remove() }
        }
        activeDisplays.clear()
        spawned = false
    }
}

/**
 * Gestor global de hologramas de ZenCore.
 */
object ZenHologramEngine {
    private val holograms = java.util.concurrent.ConcurrentHashMap<UUID, ZenHologram>()

    @JvmStatic
    fun create(location: Location, vararg lines: String): ZenHologram {
        val holo = ZenHologram(location, lines.toList())
        holo.spawn()
        holograms[UUID.randomUUID()] = holo
        return holo
    }

    @JvmStatic
    fun destroy(hologram: ZenHologram) {
        hologram.destroy()
        holograms.entries.removeIf { it.value === hologram }
    }

    @JvmStatic
    fun destroyAll() {
        for (holo in holograms.values) {
            holo.destroy()
        }
        holograms.clear()
    }
}
