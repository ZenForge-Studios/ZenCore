# ⛏️ 14. Mundo, Bloques y Triggers (`ZenBlockTracker`, `ZenWorldActions` & `ZenTriggerEngine`)

Este módulo reúne la infraestructura de manipulación segura del mundo en **ZenCore**: detección anti-exploit de bloques colocados por jugadores (`ZenBlockTracker`), utilidades avanzadas de minería/tala/agricultura/fundición (`ZenWorldActions`) y el motor universal de eventos (`ZenTriggerEngine`).

---

## 🧱 1. Rastreo de Bloques Colocados (`ZenBlockTracker`)

`ZenBlockTracker` (`com.cuac_xd.zencore.world.ZenBlockTracker`, accesible vía `ZenCoreAPI.blockTracker`) evita abusos clásicos en plugins de Jobs, Skills o Minería (como colocar y volver a romper menas para farmear experiencia o drops).

### Arquitectura
- **Memoria ultra rápida:** almacena coordenadas empaquetadas en `Long` por chunk dentro de un `ConcurrentHashMap<String, MutableSet<Long>>`.
- **Persistencia automática por chunk:** cuando `ZenCorePlugin` está activo, guarda y carga las coordenadas en el `PersistentDataContainer` del chunk bajo la clave `zen_placed_blocks` (`PersistentDataType.LONG_ARRAY`) y libera la caché de memoria en `ChunkUnloadEvent`.
- **Eventos monitoreados (`EventPriority.MONITOR`, `ignoreCancelled = true`):** `BlockPlaceEvent`, `BlockBreakEvent`, `BlockPistonExtendEvent` y `BlockPistonRetractEvent` (actualizando la posición si un pistón empuja o retrae un bloque colocado por un jugador).

### API y Condiciones YAML
| Método Kotlin/Java | Descripción |
| :--- | :--- |
| `markPlaced(block: Block)` | Marca manualmente un bloque como colocado por jugador. |
| `markBroken(block: Block)` | Elimina la marca de bloque colocado por jugador. |
| `isPlayerPlaced(block: Block): Boolean` | Comprueba si el bloque fue colocado por un jugador. |
| `clearAll()` | Limpia los registros en memoria (útil en tests). |

- **Condiciones (`CoreConditions`) y Filtros (`CoreFilters`):** `IS_PLAYER_PLACED` y `NOT_PLAYER_PLACED`.

---

## 🌲 2. Acciones Avanzadas de Mundo (`ZenWorldActions`)

`ZenWorldActions` (`com.cuac_xd.zencore.world.ZenWorldActions`, accesible vía `ZenCoreAPI.worldActions`) centraliza operaciones de bloques con protección contra recursión (`ThreadLocal`) y validación por bloque mediante `ZenProtectionHub.canBreak(...)`:

| Método | Firma y Comportamiento |
| :--- | :--- |
| **`mineVein`** | `mineVein(player: Player, startBlock: Block, maxBlocks: Int = 32, dropToInventory: Boolean = false): Int`<br>Búsqueda en anchura (BFS) sobre bloques contiguos del mismo `Material` (ignorando aire y `BEDROCK`). Desmarca `ZenBlockTracker.markBroken` y, si `dropToInventory = true`, entrega drops vía `ZenDropEngine.give`. |
| **`fellTree`** | `fellTree(player: Player, startBlock: Block, maxLogs: Int = 64, dropToInventory: Boolean = false): Int`<br>Tala troncos y madera conectados cuyos nombres terminan en `_LOG`, `_STEM`, `_WOOD` o `_HYPHAE`. |
| **`replantCrop`** | `replantCrop(block: Block): Boolean`<br>Comprueba `Ageable`; si `age >= maximumAge`, reinicia `age = 0` y actualiza `blockData`. |
| **`getSmeltedMaterial`** | `getSmeltedMaterial(raw: Material): Material?`<br>Mapea minerales (incluyendo variantes *Deepslate* y *Nether*), metales crudos (`RAW_IRON`, `RAW_GOLD`, `RAW_COPPER`), arena, piedra/adoquín, ladrillos, arcilla, netherrack, cactus, kelp, terracota, comida cruda y troncos (`*_LOG` / `*_WOOD` → `CHARCOAL`). |
| **`autoSmeltDrops`** | `autoSmeltDrops(drops: Collection<ItemStack>): List<ItemStack>`<br>Convierte todos los ítems fundibles de una lista de drops a su versión fundida conservando cantidades y clonando los demás. |

### Efectos YAML Consolidados (`ExtendedEffects`)
| ID (y Alias) | Parámetros | Descripción |
| :--- | :--- | :--- |
| `MINE_VEIN` (`VEIN_MINE`) | `max_blocks` (def. `32`), `telekinesis` / `drop_to_inventory` (def. `false`) | Ejecuta `ZenWorldActions.mineVein(...)`. |
| `FELL_TREE` (`TREE_FELLER`) | `max_logs` / `max_blocks` (def. `64`), `telekinesis` / `drop_to_inventory` (def. `false`) | Ejecuta `ZenWorldActions.fellTree(...)`. |
| `REPLANT_CROPS` (`AUTO_REPLANT`) | *(ninguno)* | Replanta cultivos maduros (programa la restauración a 2 ticks si viene de `BlockBreakEvent`, o directa vía `ZenWorldActions.replantCrop`). |
| `AUTOSMELT` (`AUTO_SMELT`, `SMELT_ORE`) | `telekinesis` (def. `false`) | Funde el bloque/drops con `ZenWorldActions`, opcionalmente los envía directo al inventario y genera partículas de llama. |

---

## ⚡ 3. Motor Universal de Triggers (`ZenTriggerEngine`)

`ZenTriggerEngine` (`ZenCoreAPI.triggers`) escucha eventos clave de Paper y construye un `ZenExecutionContext` unificado. Además, expone **`ZenTriggerEngine.trigger(triggerId: String, context: ZenExecutionContext)`** para que plugins externos disparen sus propios triggers personalizados.

### Triggers Nativos Disponibles
| Categoría | Trigger ID | Evento Paper y Datos Extra en `ctx` |
| :--- | :--- | :--- |
| **Combate** | `melee_attack`, `attack` | `EntityDamageByEntityEvent` (atacante jugador, `damage`, `targetEntity`). |
| **Combate** | `take_damage` | `EntityDamageByEntityEvent` (víctima jugador, `damage`, `targetEntity`). |
| **Combate** | `kill_entity` | `EntityDeathEvent` (cuando `killer != null`). |
| **Proyectiles** | `shoot_bow` | `EntityShootBowEvent` (`item = bow`, `targetEntity = projectile`, `data["force"]`). |
| **Proyectiles** | `projectile_hit` | `ProjectileHitEvent` (`targetEntity`, `block`, `data["projectile"]`). |
| **Bloques** | `mine_block`, `break_block` | `BlockBreakEvent` (`block`, `item = mainHand`). |
| **Bloques** | `place_block` | `BlockPlaceEvent` (`block = blockPlaced`, `item = itemInHand`). |
| **Interacción** | `right_click`, `left_click` | `PlayerInteractEvent` (`block`, `item`, `location`). |
| **Movimiento** | `sneak`, `sprint` | `PlayerToggleSneakEvent` (`isSneaking`), `PlayerToggleSprintEvent` (`isSprinting`). |
| **Actividad** | `fish` | `PlayerFishEvent` cuando `state == CAUGHT_FISH` (`item`, `data["fish_state"]`). |
| **Actividad** | `craft_item` | `CraftItemEvent` (`item = currentItem ?: recipe.result`). |
| **Actividad** | `consume_item` | `PlayerItemConsumeEvent` (`item`). |
| **Entidades** | `breed_entity` | `EntityBreedEvent` (`target`, `item = bredWith`). |
| **Entidades** | `tame_entity` | `EntityTameEvent` (`target`). |
| **Progresión** | `xp_gain` | `PlayerExpChangeEvent` cuando `amount > 0` (`data["amount"]`). |
| **Progresión** | `level_up` | `PlayerLevelChangeEvent` cuando `newLevel > oldLevel` (`level`, `data["old_level"]`, `data["new_level"]`). |
| **Sesión** | `player_join`, `player_quit` | `PlayerJoinEvent`, `PlayerQuitEvent`. |

---

## 💻 Ejemplos de Uso (Kotlin, Java y YAML)

### Kotlin
```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.engine.context.ZenExecutionContext

// 1. Verificar bloques naturales antes de dar recompensas y aplicar Vein Mine
if (!ZenCoreAPI.blockTracker.isPlayerPlaced(block)) {
    val broken = ZenCoreAPI.worldActions.mineVein(
        player = player,
        startBlock = block,
        maxBlocks = 24,
        dropToInventory = true
    )
    player.giveExp(broken * 2)
}

// 2. Disparar un trigger personalizado desde otro plugin (ej. ZenBosses)
val customCtx = ZenExecutionContext.builder()
    .player(player)
    .target(bossEntity)
    .location(bossEntity.location)
    .level(3)
    .build()
ZenCoreAPI.triggers.trigger("zenboss_phase_change", customCtx)
```

### Java
```java
import com.cuac_xd.zencore.api.ZenCoreAPI;
import com.cuac_xd.zencore.engine.context.ZenExecutionContext;
import com.cuac_xd.zencore.world.ZenBlockTracker;
import com.cuac_xd.zencore.world.ZenWorldActions;
import org.bukkit.inventory.ItemStack;
import java.util.List;

if (!ZenBlockTracker.isPlayerPlaced(block)) {
    int logs = ZenWorldActions.fellTree(player, block, 48, true);
    List<ItemStack> smelted = ZenCoreAPI.getWorldActions().autoSmeltDrops(rawDrops);
}

ZenExecutionContext ctx = ZenExecutionContext.builder()
    .player(player)
    .block(block)
    .build();
ZenCoreAPI.getTriggers().trigger("custom_miner_milestone", ctx);
```

### YAML
```yaml
pico_minero_maestro:
  triggers:
    - mine_block
  conditions:
    - type: NOT_PLAYER_PLACED
  filters:
    - type: BLOCK_TYPE
      args:
        materials:
          - IRON_ORE
          - DEEPSLATE_IRON_ORE
          - GOLD_ORE
          - DEEPSLATE_GOLD_ORE
          - DIAMOND_ORE
          - DEEPSLATE_DIAMOND_ORE
  effects:
    - type: MINE_VEIN
      max_blocks: 32
      telekinesis: true
    - type: AUTOSMELT
      telekinesis: true

hacha_lenador_natural:
  triggers:
    - mine_block
  conditions:
    - type: NOT_PLAYER_PLACED
  effects:
    - type: FELL_TREE
      max_logs: 64
      telekinesis: false
```
