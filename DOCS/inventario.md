# Categoría: Inventario

Esta sección recopila todos los efectos pertenecientes a **inventario** registrados en ZenCore.

---

## `auto_sell`

Vende automáticamente los ítems extraídos al inventario si Vault está activo.

## Ejemplo de Configuración

```yaml
effects:
  - type: AUTO_SELL
    notify: true
```

---

## `clear_inventory`

Limpia el inventario completo del jugador o de la entidad afectada.

## Ejemplo de Configuración

```yaml
effects:
  - type: CLEAR_INVENTORY
    clear_armor: false
```

---

## `close_inventory`

Fuerza el cierre del inventario abierto del jugador de forma inmediata.

## Ejemplo de Configuración

```yaml
effects:
  - type: CLOSE_INVENTORY
    target: SELF
```

---

## `condense_items`

Compacta ítems y minerales sueltos en bloques dentro del inventario del jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: CONDENSE_ITEMS
    materials:
      - IRON_INGOT
      - GOLD_INGOT
```

---

## `drop_skull`

Genera y arroja la cabeza de la entidad o jugador derrotado.

## Ejemplo de Configuración

```yaml
effects:
  - type: DROP_SKULL
    chance: 100
```

---

## `item_magnet`

Atrae magnéticamente todos los ítems del suelo en un radio hacia el jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: ITEM_MAGNET
    radius: 6.0
```

---

## `neptune_bounty`

Multiplica las recompensas y tesoros obtenidos al pescar.

## Ejemplo de Configuración

```yaml
effects:
  - type: NEPTUNE_BOUNTY
    multiplier: 2
```

---

## `open_anvil`

Abre remotamente la interfaz de un yunque para el jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_ANVIL
    target: SELF
```

---

## `open_crafting`

Abre remotamente la mesa de crafteo 3x3 para el jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_CRAFTING
    target: SELF
```

---

## `open_disposal`

Abre un menú de basurero para eliminar ítems descartados.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_DISPOSAL
    title: '<red>Papelera</red>'
```

---

## `open_ender_chest`

Abre el cofre del End del jugador desde cualquier lugar.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_ENDER_CHEST
    target: SELF
```

---

## `open_grindstone`

Abre remotamente la interfaz de la afiladora para desencantar y reparar.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_GRINDSTONE
    target: SELF
```

---

## `open_loom`

Abre la interfaz de telar para estandartes remotamente.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_LOOM
    target: SELF
```

---

## `open_smithing_table`

Abre la mesa de herrería para mejoras de netherita o plantillas.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_SMITHING_TABLE
    target: SELF
```

---

## `open_stonecutter`

Abre el cortapiedras remotamente.

## Ejemplo de Configuración

```yaml
effects:
  - type: OPEN_STONECUTTER
    target: SELF
```

---

## `pull_items`

Atrae los drops generados directamente al jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: PULL_ITEMS
    radius: 5.0
```

---

## `shuffle_hotbar`

Desordena aleatoriamente los ítems de la barra de acceso rápido del oponente.

## Ejemplo de Configuración

```yaml
effects:
  - type: SHUFFLE_HOTBAR
    target: TARGET
```

---

## `telekinesis`

Envía los bloques minados o drops directamente al inventario del jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: TELEKINESIS
    target: SELF
```

---

## `void_storage`

Envía automáticamente los drops del jugador al vacío para evitar saturación.

## Ejemplo de Configuración

```yaml
effects:
  - type: VOID_STORAGE
    enabled: true
```

---

