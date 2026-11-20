# ⏳ 06. Cooldowns y Timers

El módulo `ZenCooldownManager` elimina la necesidad de mantener mapas manuales `HashMap<UUID, Long>` en cada plugin y resuelve el problema de la pérdida de cooldowns al reiniciar el servidor.

---

## 🚀 Modos de Cooldown

1. **Cooldowns en Memoria:** Rápidos y livianos. Ideales para habilidades, clics repetitivos en menús o tiempos de espera cortos (segundos a minutos).
2. **Cooldowns Persistentes (PDC):** Se almacenan en el contenedor de datos persistentes del jugador (`Player PDC`). **Sobreviven a reinicios del servidor y desconexiones.** Ideales para recompensas diarias, límites diarios de venta o habilidades con enfriamiento de varias horas.

---

## 💻 Ejemplo de Uso

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import java.time.Duration

// 1. Comprobar Cooldown en Memoria (ej. usar vara mágica cada 10 segundos)
if (ZenCoreAPI.cooldowns.has(player, "wand_use")) {
    val remaining = ZenCoreAPI.cooldowns.getRemainingFormatted(player, "wand_use")
    player.sendMessage("§cDebes esperar $remaining antes de volver a usar la vara.")
    return
}

// Aplicar cooldown en memoria
ZenCoreAPI.cooldowns.set(player, "wand_use", Duration.ofSeconds(10))

// 2. Cooldown Persistente (ej. Reclamo de Booster diario cada 24 horas)
if (ZenCoreAPI.cooldowns.hasPersistent(player, "daily_booster")) {
    val remaining = ZenCoreAPI.cooldowns.getRemainingFormatted(player, "daily_booster")
    player.sendMessage("§eYa reclamaste tu booster hoy. Tiempo restante: §f$remaining")
    return
}

// Aplicar cooldown persistente (no se perderá si el servidor se reinicia)
ZenCoreAPI.cooldowns.setPersistent(player, "daily_booster", Duration.ofHours(24))
player.sendMessage("§a¡Has reclamado tu Booster Diario de Spawners!")
```

### Formato Automático
El método `getRemainingFormatted(...)` convierte automáticamente los milisegundos en cadenas limpias para los jugadores:
- `45s`
- `12m 30s`
- `2h 15m 10s`
- `1d 4h 0m`
