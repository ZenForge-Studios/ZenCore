# 🔮 04. Hologramas Modernos (Display Entities)

El módulo `ZenHologramEngine` permite crear hologramas flotantes de alto rendimiento utilizando las entidades nativas **`TextDisplay`** introducidas en Minecraft 1.20+.

## 🚀 Ventajas sobre ArmorStands
- **Hasta un 80% menos de carga en el servidor y cliente**: no procesan colisiones, equipamiento, poses ni gravedad.
- **Renderizado nativo de texto enriquecido**: soporta gradientes de MiniMessage, fondos transparentes configurables y alineación automática centrada hacia la cámara del jugador (Billboard).

---

## 💻 Ejemplo de Uso

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import org.bukkit.Location

val spawnerLocation = block.location.add(0.5, 1.2, 0.5)

// 1. Crear y mostrar un holograma sobre un bloque
val hologram = ZenCoreAPI.holograms.create(
    spawnerLocation,
    "<gradient:#ffaa00:#ff5500><b>Spawner de Blaze</b></gradient>",
    "<yellow>Nivel: <white>3 <gray>| <yellow>Stack: <white>x5",
    "<aqua>Drops listos: <white>128"
)

// 2. Modificar el texto del holograma en tiempo real (ej. al acumular drops)
hologram.setLines(
    listOf(
        "<gradient:#ffaa00:#ff5500><b>Spawner de Blaze</b></gradient>",
        "<yellow>Nivel: <white>3 <gray>| <yellow>Stack: <white>x5",
        "<aqua>Drops listos: <gold><b>¡LLENO! (256)</b></gold>"
    )
)

// 3. Mover o teletransportar
hologram.teleport(newLocation)

// 4. Eliminar el holograma cuando se rompe el spawner
hologram.destroy()
```
