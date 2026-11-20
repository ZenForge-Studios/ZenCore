# 📊 13. Estadísticas, Atributos y Recursos (`ZenStatEngine`)

El módulo `ZenStatEngine` (`com.cuac_xd.zencore.stat`) implementa un sistema unificado de estadísticas numéricas personalizadas, modificadores temporales/permanentes apilables, sincronización con atributos nativos de Paper y **pools de recursos** (`mana`, `energy`, `stamina`, etc.) con soporte directo de `mana_cost` en `ZenEngine`.

---

## 🧮 Operaciones y Fórmula de Cálculo (`StatOperation`)

Cada modificador (`StatModifier`) se identifica de forma única por la pareja `(key, stat)` (case-insensitive): si vuelves a aplicar un modificador con la misma `key` y `stat`, reemplaza al anterior sin acumular duplicados.

### Operaciones Disponibles
1. **`ADDITIVE`:** Suma plana al valor base (`+15` fuerza, `+40` maná máximo).
2. **`PERCENTAGE`:** Suma porcentual en puntos porcentuales (`25.0` equivale a `+25%`).
3. **`MULTIPLICATIVE`:** Factor multiplicativo directo (`1.2` equivale a `×1.20`).

### Fórmula Exacta de `getStat`
```text
valor_final = (base + sum(ADDITIVE)) * (1.0 + sum(PERCENTAGE) / 100.0) * product(MULTIPLICATIVE)
```
Si `durationTicks > 0L`, la duración se convierte a milisegundos mediante `durationTicks * 50L`; si `durationTicks <= 0L`, el modificador es permanente (`expiresAt = 0L`). Además, `ZenCorePlugin` ejecuta `ZenStatEngine.pruneExpired()` automáticamente cada 60 segundos para limpiar modificadores vencidos.

---

## 🧬 Puente con Atributos de Paper (`syncBukkitAttribute`)

Al llamar a `ZenStatEngine.syncBukkitAttribute(player, stat)` (o automáticamente al usar `ADD_STAT_MODIFIER` / `REMOVE_STAT_MODIFIER`), ZenCore actualiza el `Attribute` correspondiente del jugador de forma segura (`runCatching`):

| Stat en ZenCore | `Attribute` de Paper | Base por Defecto | Valor Mínimo |
| :--- | :--- | :--- | :--- |
| `max_health` / `health` | `Attribute.MAX_HEALTH` | `20.0` | `1.0` *(ajusta `player.health` si supera el nuevo máximo)* |
| `movement_speed` / `speed` | `Attribute.MOVEMENT_SPEED` | `0.1` | `0.0` |
| `attack_damage` / `damage` | `Attribute.ATTACK_DAMAGE` | `1.0` | `0.0` |
| `armor` / `defense` | `Attribute.ARMOR` | `0.0` | `0.0` |
| `attack_speed` | `Attribute.ATTACK_SPEED` | `4.0` | `0.0` |
| `luck` | `Attribute.LUCK` | `0.0` | `-1024.0` |
| `mining_efficiency` | `Attribute.MINING_EFFICIENCY` | `0.0` | `0.0` |
| `block_break_speed` | `Attribute.BLOCK_BREAK_SPEED` | `1.0` | `0.0` |

---

## 💧 Pools de Recursos (`mana`, `energy`, `stamina`)

Cualquier recurso se gestiona mediante pools identificados por nombre (por defecto `"mana"`):
- **Capacidad Máxima (`getMaxResource`):** se calcula dinámicamente como:
  ```kotlin
  (100.0 + getStat(playerUuid, "max_${pool.lowercase()}", 0.0)).coerceAtLeast(0.0)
  ```
  Por ejemplo, un jugador comienza con `100.0` de maná máximo, y añadir `+50` al stat `max_mana` eleva automáticamente su tope a `150.0`.
- **Lectura Inicial (`getResource`):** si el pool aún no tiene un valor guardado, comienza lleno igual a `getMaxResource`.
- **Consumo Seguro (`consumeResource`):** verifica y descuenta de manera sincronizada devolviendo `true` solo si había saldo suficiente.

---

## 💻 API en Código (Kotlin y Java)

Acceso mediante `ZenCoreAPI.stats` o `ZenStatEngine`:

| Método | Descripción |
| :--- | :--- |
| `setBaseStat(uuid, stat, baseValue)` / `getBaseStat(uuid, stat, default = 0.0)` | Establece o consulta el valor base de un stat. |
| `addModifier(uuid, key, stat, amount, operation = StatOperation.ADDITIVE, durationTicks = 0L)` | Añade o reemplaza un `StatModifier`. |
| `removeModifier(uuid, key, stat = null)` | Elimina un modificador por `key` (en un `stat` concreto o en todos si `stat == null`). |
| `getStat(uuid, stat, defaultBase = 0.0)` | Limpia expirados y calcula el valor efectivo del stat. |
| `getModifiers(uuid, stat = null)` | Devuelve la lista de `StatModifier` vigentes del jugador. |
| `getResource(uuid, pool = "mana")` / `getMaxResource(uuid, pool = "mana")` | Consulta el recurso actual o máximo del pool. |
| `setResource(uuid, pool, amount)` / `giveResource(uuid, pool, amount)` | Establece o añade recurso acotado entre `0.0` y el máximo. |
| `consumeResource(uuid, pool, amount)` / `hasResource(uuid, pool, amount)` | Comprueba o consume recurso del pool. |
| `syncBukkitAttribute(player, stat)` | Sincroniza un stat con el `Attribute` nativo de Paper. |
| `clearPlayer(uuid)` / `clearAll()` / `pruneExpired()` | Limpieza por jugador, global o de modificadores expirados. |

### Ejemplo en Kotlin
```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.stat.StatOperation

// 1. Subir el maná máximo (+60 plano) y el daño (+20%) con un anillo equipado
ZenCoreAPI.stats.addModifier(player.uniqueId, "arcane_ring", "max_mana", 60.0, StatOperation.ADDITIVE)
ZenCoreAPI.stats.addModifier(player.uniqueId, "rage_potion", "attack_damage", 20.0, StatOperation.PERCENTAGE, durationTicks = 600L)
ZenCoreAPI.stats.syncBukkitAttribute(player, "attack_damage")

// 2. Lanzar un hechizo consumiendo 35 de maná
if (ZenCoreAPI.stats.consumeResource(player.uniqueId, "mana", 35.0)) {
    val maxMana = ZenCoreAPI.stats.getMaxResource(player.uniqueId, "mana")
    val remaining = ZenCoreAPI.stats.getResource(player.uniqueId, "mana")
    player.sendRichMessage("<aqua>Hechizo lanzado ($remaining / $maxMana Maná).</aqua>")
} else {
    player.sendRichMessage("<red>No tienes suficiente maná.</red>")
}
```

### Ejemplo en Java
```java
import com.cuac_xd.zencore.api.ZenCoreAPI;
import com.cuac_xd.zencore.stat.StatOperation;
import com.cuac_xd.zencore.stat.ZenStatEngine;

ZenStatEngine.setBaseStat(player.getUniqueId(), "max_health", 24.0);
ZenStatEngine.addModifier(player.getUniqueId(), "titan_armor", "max_health", 25.0, StatOperation.PERCENTAGE, 0L);
ZenCoreAPI.getStats().syncBukkitAttribute(player, "max_health");

if (ZenStatEngine.hasResource(player.getUniqueId(), "stamina", 20.0)) {
    ZenStatEngine.consumeResource(player.getUniqueId(), "stamina", 20.0);
}
```

---

## ⚡ Integración en `ZenEngine`: `mana_cost`, Costes y Expresiones Dinámicas

En cualquier efecto ejecutado por `ZenEngine.executeEffect(...)` puedes usar:
- **`mana_cost` / `resource_cost`:** Coste de recurso evaluado con placeholders y `MathEvaluator` (ej. `"15 + (%level% * 3)"`).
- **`resource_pool` / `pool`:** Pool a consumir (por defecto `"mana"`).
- **`send_resource_message` / `send_mana_message`:** Activa o silencia el aviso por falta de recurso (por defecto `true`).
- **`resource_message` / `mana_message`:** Mensaje personalizado con `%cost%`, `%amount%` y `%pool%`.
- **Expresiones dinámicas en argumentos universales:** `cooldown`, `delay`, `every`, `repeat` (`times`, `start`, `increment`) y `cost` (junto con `cost_currency` / `currency` vía `ZenHooksHub`) resuelven placeholders antes de evaluarse matemáticamente. El motor pre-valida cooldown, dinero y maná antes de descontar recursos o iniciar el enfriamiento.

### Efectos y Condiciones YAML
| Tipo | ID (y Alias) | Parámetros | Descripción |
| :--- | :--- | :--- | :--- |
| **Efecto** | `ADD_STAT_MODIFIER` | `key` (def. `"effect_<stat>"`), `stat`, `amount` / `value` (def. `1.0`), `operation` (`ADDITIVE` \| `PERCENTAGE` \| `MULTIPLICATIVE`, def. `ADDITIVE`), `duration` (ticks, def. `0`) | Añade un modificador y sincroniza atributos de Paper si aplica. |
| **Efecto** | `REMOVE_STAT_MODIFIER` | `key`, `stat` (opcional) | Elimina el modificador indicado y re-sincroniza el atributo si se especificó `stat`. |
| **Efecto** | `GIVE_RESOURCE` (`GIVE_MANA`) | `pool` / `resource_pool` (def. `"mana"`), `amount` / `value` (def. `10`) | Regenera puntos en el pool indicado. |
| **Efecto** | `TAKE_RESOURCE` (`TAKE_MANA`) | `pool` / `resource_pool` (def. `"mana"`), `amount` / `value` (def. `10`) | Consume puntos del pool indicado. |
| **Condición** | `STAT_ABOVE` | `stat`, `value` / `amount` (def. `0`) | Verdadero si el stat efectivo es estrictamente mayor que el umbral (`>`). |
| **Condición** | `STAT_BELOW` | `stat`, `value` / `amount` (def. `0`) | Verdadero si el stat efectivo es estrictamente menor que el umbral (`<`). |
| **Condición** | `HAS_RESOURCE` (`HAS_MANA`) | `pool` / `resource_pool` (def. `"mana"`), `amount` / `value` (def. `1`) | Verdadero si el jugador tiene al menos `amount` en el pool. |

### Placeholders Contextuales (`ContextResolver`)
- `%mana%`: recurso actual del pool `"mana"`.
- `%max_mana%`: capacidad máxima del pool `"mana"`.
- Si `PlaceholderAPI` está disponible y quedan marcadores `%...%`, `ContextResolver` resuelve también placeholders de PAPI automáticamente.

### Ejemplo Completo YAML
```yaml
baculo_arcano:
  triggers:
    - right_click
  conditions:
    - type: HAS_MANA
      args:
        pool: "mana"
        amount: 25
    - type: STAT_ABOVE
      args:
        stat: "intelligence"
        value: 10
  effects:
    - type: PROJECTILE_BEAM
      distance: 18
      damage: "6 + (%level% * 1.5)"
      particle: "SOUL_FIRE_FLAME"
      mana_cost: "20 + (%level% * 2)"
      resource_pool: "mana"
      mana_message: "<red>Necesitas <aqua>%cost% de %pool%</aqua> para lanzar el rayo.</red>"
      cooldown: "8 - (%level% * 0.5)"
    - type: ADD_STAT_MODIFIER
      key: "arcane_surge"
      stat: "movement_speed"
      amount: 20
      operation: "PERCENTAGE"
      duration: 100
```
