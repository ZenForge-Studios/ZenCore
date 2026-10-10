# Categoría: Jugador & Vida

Esta sección documenta los efectos correspondientes a **Jugador & Vida** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `add_potion_effect`

- **Identificador / Alias:** `ADD_POTION_EFFECT / POTION`
- **Descripción:** Aplica un efecto de poción de Minecraft con duración y amplificador.

### Ejemplo de Configuración
```yaml
effects:
 - type: ADD_POTION_EFFECT
    target: TARGET
    effect: SLOWNESS
    duration: 100 # ticks (5 segundos)
    amplifier: 1 # Lentitud II
```

---

## `clear_effect`

- **Identificador / Alias:** `CLEAR_EFFECT`
- **Descripción:** Elimina todos los efectos de poción activos en la entidad.

### Ejemplo de Configuración
```yaml
effects:
 - type: CLEAR_EFFECT
    target: SELF
```

---

## `damage`

- **Identificador / Alias:** `DAMAGE / DAMAGE_ENTITY`
- **Descripción:** Aplica daño a la entidad objetivo o al portador.

### Ejemplo de Configuración
```yaml
effects:
 - type: DAMAGE
    target: TARGET # TARGET o SELF
    amount: 6.0 # 3 corazones de daño
```

---

## `experience`

- **Identificador / Alias:** `EXPERIENCE`
- **Descripción:** Entrega puntos de experiencia planos al jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: EXPERIENCE
    amount: 50
```

---

## `food`

- **Identificador / Alias:** `FOOD`
- **Descripción:** Incrementa o disminuye el nivel de comida/hambre del jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: FOOD
    amount: 4
```

---

## `heal`

- **Identificador / Alias:** `HEAL`
- **Descripción:** Restaura puntos de salud a una entidad viva sin superar su salud máxima.

### Ejemplo de Configuración
```yaml
effects:
 - type: HEAL
    target: SELF
    amount: 4.0
```

---

## `level`

- **Identificador / Alias:** `LEVEL`
- **Descripción:** Otorga niveles completos de experiencia.

### Ejemplo de Configuración
```yaml
effects:
 - type: LEVEL
    amount: 1
```

---

## `remove_potion_effect`

- **Identificador / Alias:** `REMOVE_POTION_EFFECT`
- **Descripción:** Elimina un efecto de poción específico.

### Ejemplo de Configuración
```yaml
effects:
 - type: REMOVE_POTION_EFFECT
    target: SELF
    effect: POISON
```

---

## `saturation`

- **Identificador / Alias:** `SATURATION`
- **Descripción:** Añade puntos de saturación al jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: SATURATION
    amount: 3.0
```

---

## `set_health`

- **Identificador / Alias:** `SET_HEALTH`
- **Descripción:** Establece la salud de la entidad en un valor exacto.

### Ejemplo de Configuración
```yaml
effects:
 - type: SET_HEALTH
    target: SELF
    amount: 20.0
```

---

