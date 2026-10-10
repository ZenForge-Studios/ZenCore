# CategorÃ­a: Nuevos Efectos de Minería, Agricultura y Entorno

Esta secciÃ³n documenta los efectos correspondientes a **Nuevos Efectos de Minería, Agricultura y Entorno** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `bonemeal_radius`

- **Identificador / Alias:** `BONEMEAL_RADIUS`
- **DescripciÃ³n:** Aplica polvo de hueso y acelera la maduración de cultivos en área.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: BONEMEAL_RADIUS
    radius: 4
```

---

## `explosive_mining`

- **Identificador / Alias:** `EXPLOSIVE_MINING`
- **DescripciÃ³n:** Detona y mina en área esférica sin romper bloques irrompibles como la bedrock.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: EXPLOSIVE_MINING
    radius: 2
```

---

## `extinguish_fire_radius`

- **Identificador / Alias:** `EXTINGUISH_FIRE_RADIUS`
- **DescripciÃ³n:** Extingue fuego normal y del alma en área.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: EXTINGUISH_FIRE_RADIUS
    radius: 5
```

---

## `fill_liquid`

- **Identificador / Alias:** `FILL_LIQUID / DRAIN_LIQUID`
- **DescripciÃ³n:** Llena o drena fluidos (agua o lava) en un radio especificado.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: DRAIN_LIQUID
    radius: 4
```

---

## `freeze_water`

- **Identificador / Alias:** `FREEZE_WATER`
- **DescripciÃ³n:** Congela el agua circundante en hielo escarchado (Frosted Ice).

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: FREEZE_WATER
    radius: 3
```

---

## `line_mine`

- **Identificador / Alias:** `LINE_MINE`
- **DescripciÃ³n:** Mina un túnel o línea recta en la dirección a la que apunta el jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: LINE_MINE
    depth: 6
```

---

## `smelt_ore`

- **Identificador / Alias:** `SMELT_ORE`
- **DescripciÃ³n:** Funde menas de hierro, oro y cobre al instante en sus respectivos lingotes.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SMELT_ORE
```

---

## `sphere_mine`

- **Identificador / Alias:** `SPHERE_MINE`
- **DescripciÃ³n:** Mina una esfera perfecta alrededor del bloque roto.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SPHERE_MINE
    radius: 3
```

---

## `till_dirt`

- **Identificador / Alias:** `TILL_DIRT / PATH_DIRT`
- **DescripciÃ³n:** Crea arados o senderos de tierra en radio de forma instantánea.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: TILL_DIRT
    radius: 3
```

---

## `tree_feller`

- **Identificador / Alias:** `TREE_FELLER`
- **DescripciÃ³n:** Tala el árbol completo de un solo golpe recorriendo los troncos conectados.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: TREE_FELLER
    max_blocks: 128
```

---

