# Categoría: Mundo & Bloques

Esta sección documenta los efectos correspondientes a **Mundo & Bloques** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `break_block`

- **Identificador / Alias:** `BREAK_BLOCK`
- **Descripción:** Rompe el bloque involucrado.

### Ejemplo de Configuración
```yaml
effects:
 - type: BREAK_BLOCK
    drop: true # Si genera los drops naturales
```

---

## `explosion`

- **Identificador / Alias:** `EXPLOSION`
- **Descripción:** Genera una explosión en la ubicación.

### Ejemplo de Configuración
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
- **Descripción:** Invoca un relámpago en la ubicación del objetivo o bloque.

### Ejemplo de Configuración
```yaml
effects:
 - type: LIGHTNING
    target: TARGET
    strike: true # Si es true, inflige daño de rayo
```

---

## `set_block`

- **Identificador / Alias:** `SET_BLOCK / PLACE_BLOCK`
- **Descripción:** Cambia el bloque en la posición por otro material.

### Ejemplo de Configuración
```yaml
effects:
 - type: SET_BLOCK
    material: COBWEB
```

---

