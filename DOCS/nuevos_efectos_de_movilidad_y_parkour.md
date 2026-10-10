# Categoría: Nuevos Efectos de Movilidad y Parkour

Esta sección documenta los efectos correspondientes a **Nuevos Efectos de Movilidad y Parkour** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `boost_elytra`

- **Identificador / Alias:** `BOOST_ELYTRA`
- **Descripción:** Otorga un cohete de propulsión instantáneo si el jugador está usando élitros en vuelo.

### Ejemplo de Configuración
```yaml
effects:
 - type: BOOST_ELYTRA
    multiplier: 1.8
```

---

## `dash`

- **Identificador / Alias:** `DASH`
- **Descripción:** Impulsa hacia adelante en la dirección de la mirada del jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: DASH
    strength: 1.6
```

---

## `double_jump`

- **Identificador / Alias:** `DOUBLE_JUMP`
- **Descripción:** Otorga un impulso en el aire estilo doble salto con partículas de nube.

### Ejemplo de Configuración
```yaml
effects:
 - type: DOUBLE_JUMP
    power: 0.85
```

---

## `float`

- **Identificador / Alias:** `FLOAT / LEVITATE`
- **Descripción:** Eleva por los aires a la entidad objetivo.

### Ejemplo de Configuración
```yaml
effects:
 - type: LEVITATE
    duration: 80
    amplifier: 1
```

---

## `grappling_hook`

- **Identificador / Alias:** `GRAPPLING_HOOK`
- **Descripción:** Propulsa al jugador a toda velocidad hacia la ubicación o bloque seleccionado.

### Ejemplo de Configuración
```yaml
effects:
 - type: GRAPPLING_HOOK
    speed: 2.0
```

---

## `safe_fall`

- **Identificador / Alias:** `SAFE_FALL`
- **Descripción:** Resetea la distancia de caída y otorga caída lenta temporal.

### Ejemplo de Configuración
```yaml
effects:
 - type: SAFE_FALL
```

---

## `set_glide`

- **Identificador / Alias:** `SET_GLIDE`
- **Descripción:** Activa o desactiva el planeo de élitros en el jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: SET_GLIDE
    glide: true
```

---

## `wall_climb`

- **Identificador / Alias:** `WALL_CLIMB`
- **Descripción:** Otorga impulso vertical continuo para trepar paredes lisas.

### Ejemplo de Configuración
```yaml
effects:
 - type: WALL_CLIMB
```

---

