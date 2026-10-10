# Categoría: Mineria_agricultura

Esta sección recopila todos los efectos pertenecientes a **mineria_agricultura** registrados en ZenCore.

---

## `age_crop`

Acelera el crecimiento y la edad de los cultivos en el bloque objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: AGE_CROP
    radius: 3
    age: 2
```

---

## `autosmelt`

Funde automáticamente los minerales y bloques al ser minados.

## Ejemplo de Configuración

```yaml
effects:
  - type: AUTOSMELT
    fortune: true
```

---

## `drill`

Mina un túnel recto en profundidad en la dirección hacia donde apunta el minero.

## Ejemplo de Configuración

```yaml
effects:
  - type: DRILL
    depth: 3
```

---

## `drop_ores`

Hace que los bloques de mineral suelten gotas directas concentradas de mineral.

## Ejemplo de Configuración

```yaml
effects:
  - type: DROP_ORES
    multiplier: 2
```

---

## `mine_radius`

Mina todos los bloques en un radio cúbico alrededor del bloque roto.

## Ejemplo de Configuración

```yaml
effects:
  - type: MINE_RADIUS
    radius: 1
```

---

## `mine_vein`

Mina toda la veta contigua del mismo tipo de mineral.

## Ejemplo de Configuración

```yaml
effects:
  - type: MINE_VEIN
    max_blocks: 32
```

---

## `path_dirt`

Convierte la tierra o césped en caminos de tierra en un radio.

## Ejemplo de Configuración

```yaml
effects:
  - type: PATH_DIRT
    radius: 2
```

---

## `replant_crops`

Replanta automáticamente los cultivos maduros recolectados.

## Ejemplo de Configuración

```yaml
effects:
  - type: REPLANT_CROPS
    replant: true
```

---

## `strip_logs`

Descorteza troncos en un radio alrededor del bloque talado.

## Ejemplo de Configuración

```yaml
effects:
  - type: STRIP_LOGS
    radius: 2
```

---

