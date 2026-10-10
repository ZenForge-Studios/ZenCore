# Categoría: Nuevos Efectos de Minería, Agricultura y Entorno

Esta sección documenta los efectos correspondientes a **Nuevos Efectos de Minería, Agricultura y Entorno** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `bonemeal_radius`

- **Identificador / Alias:** `BONEMEAL_RADIUS`
- **Descripción:** Aplica polvo de hueso y acelera la maduración de cultivos en área.

### Ejemplo de Configuración
```yaml
effects:
 - type: BONEMEAL_RADIUS
    radius: 4
```

---

## `explosive_mining`

- **Identificador / Alias:** `EXPLOSIVE_MINING`
- **Descripción:** Detona y mina en área esférica sin romper bloques irrompibles como la bedrock.

### Ejemplo de Configuración
```yaml
effects:
 - type: EXPLOSIVE_MINING
    radius: 2
```

---

## `extinguish_fire_radius`

- **Identificador / Alias:** `EXTINGUISH_FIRE_RADIUS`
- **Descripción:** Extingue fuego normal y del alma en área.

### Ejemplo de Configuración
```yaml
effects:
 - type: EXTINGUISH_FIRE_RADIUS
    radius: 5
```

---

## `fill_liquid`

- **Identificador / Alias:** `FILL_LIQUID / DRAIN_LIQUID`
- **Descripción:** Llena o drena fluidos (agua o lava) en un radio especificado.

### Ejemplo de Configuración
```yaml
effects:
 - type: DRAIN_LIQUID
    radius: 4
```

---

## `freeze_water`

- **Identificador / Alias:** `FREEZE_WATER`
- **Descripción:** Congela el agua circundante en hielo escarchado (Frosted Ice).

### Ejemplo de Configuración
```yaml
effects:
 - type: FREEZE_WATER
    radius: 3
```

---

## `line_mine`

- **Identificador / Alias:** `LINE_MINE`
- **Descripción:** Mina un túnel o línea recta en la dirección a la que apunta el jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: LINE_MINE
    depth: 6
```

---

## `smelt_ore`

- **Identificador / Alias:** `SMELT_ORE`
- **Descripción:** Funde menas de hierro, oro y cobre al instante en sus respectivos lingotes.

### Ejemplo de Configuración
```yaml
effects:
 - type: SMELT_ORE
```

---

## `sphere_mine`

- **Identificador / Alias:** `SPHERE_MINE`
- **Descripción:** Mina una esfera perfecta alrededor del bloque roto.

### Ejemplo de Configuración
```yaml
effects:
 - type: SPHERE_MINE
    radius: 3
```

---

## `till_dirt`

- **Identificador / Alias:** `TILL_DIRT / PATH_DIRT`
- **Descripción:** Crea arados o senderos de tierra en radio de forma instantánea.

### Ejemplo de Configuración
```yaml
effects:
 - type: TILL_DIRT
    radius: 3
```

---

## `tree_feller`

- **Identificador / Alias:** `TREE_FELLER`
- **Descripción:** Tala el árbol completo de un solo golpe recorriendo los troncos conectados.

### Ejemplo de Configuración
```yaml
effects:
 - type: TREE_FELLER
    max_blocks: 128
```

---

