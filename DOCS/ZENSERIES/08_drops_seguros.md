# 🎁 08. Sistema de Drops Seguros (Drop Engine)

El módulo `ZenDropEngine` (inspirado en `DropQueue` de Eco) resuelve el problema clásico en Minecraft de pérdida de ítems cuando el inventario del jugador está lleno al romper un bloque, recibir una recompensa o recolectar ganancias.

---

## 🚀 Modos de Operación

1. **Modo Suelo Natural (`dropToGround = true`):**
   - Intenta entregar los ítems al inventario.
   - Cualquier sobrante es arrojado suavemente a los pies del jugador (`dropItemNaturally`).
   - Notifica opcionalmente al jugador: *"¡Tu inventario estaba lleno! Algunos ítems cayeron al suelo."*

2. **Modo Buzón Virtual (`dropToGround = false`):**
   - Los ítems sobrantes se guardan en un buzón de desbordamiento en memoria por jugador.
   - Ideal para recompensas de paga real, ítems valiosos de crates o compras en tienda donde no se quiere que nadie robe el drop del suelo.
   - Los ítems pueden ser reclamados en cualquier momento con `ZenDropEngine.claimOverflow(player)`.

---

## 💻 Ejemplo de Uso

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val rewards: List<ItemStack> = listOf(spawnerItem, moneyWand, diamondBlocks)

// Entregar de forma segura (lo que no quepa caerá a sus pies sin perderse)
ZenCoreAPI.drops.give(
    player = player,
    items = rewards,
    dropToGround = true,
    notifyOverflow = true
)

// O modo buzón virtual para compras de tienda:
ZenCoreAPI.drops.give(
    player = player,
    items = rewards,
    dropToGround = false,
    notifyOverflow = true
)

// Reclamar buzón cuando el jugador libere espacio
val itemsEntregados = ZenCoreAPI.drops.claimOverflow(player)
if (itemsEntregados > 0) {
    player.sendMessage("§aHas reclamado $itemsEntregados ítems de tu buzón de almacenamiento.")
}
```
