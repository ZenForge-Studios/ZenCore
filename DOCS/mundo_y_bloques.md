# CategorÃ­a: Mundo & Bloques

Esta secciÃ³n documenta los efectos correspondientes a **Mundo & Bloques** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `break_block`

- **Identificador / Alias:** `BREAK_BLOCK`
- **DescripciÃ³n:** Rompe el bloque involucrado.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: BREAK_BLOCK
    drop: true # Si genera los drops naturales
```

---

## `explosion`

- **Identificador / Alias:** `EXPLOSION`
- **DescripciÃ³n:** Genera una explosión en la ubicación.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: EXPLOSION
    power: 3.0
    fire: false
    break_blocks: false
```

---

## `lightning`

- **Identificador / Alias:** `LIGHTNING`
- **DescripciÃ³n:** Invoca un relámpago en la ubicación del objetivo o bloque.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: LIGHTNING
    target: TARGET
    strike: true # Si es true, inflige daño de rayo
```

---

## `set_block`

- **Identificador / Alias:** `SET_BLOCK / PLACE_BLOCK`
- **DescripciÃ³n:** Cambia el bloque en la posición por otro material.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SET_BLOCK
    material: COBWEB
```

---

