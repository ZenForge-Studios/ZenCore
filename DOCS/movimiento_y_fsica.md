# Categoría: Movimiento & Física

Esta sección documenta los efectos correspondientes a **Movimiento & Física** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `knockback`

- **Identificador / Alias:** `KNOCKBACK`
- **Descripción:** Aplica retroceso físico direccional a la entidad.

### Ejemplo de Configuración
```yaml
effects:
 - type: KNOCKBACK
    strength: 1.8
```

---

## `launch`

- **Identificador / Alias:** `LAUNCH`
- **Descripción:** Propulsa a la entidad en la dirección a la que está mirando.

### Ejemplo de Configuración
```yaml
effects:
 - type: LAUNCH
    power: 1.5
    y: 0.6
```

---

## `pull`

- **Identificador / Alias:** `PULL`
- **Descripción:** Atrae a la entidad objetivo hacia la posición del jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: PULL
    strength: 1.2
```

---

## `push`

- **Identificador / Alias:** `PUSH`
- **Descripción:** Empuja a la entidad objetivo lejos del jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: PUSH
    strength: 1.5
```

---

## `teleport`

- **Identificador / Alias:** `TELEPORT`
- **Descripción:** Teletransporta al jugador hacia la ubicación del objetivo.

### Ejemplo de Configuración
```yaml
effects:
 - type: TELEPORT
    target: TARGET
```

---

## `velocity`

- **Identificador / Alias:** `VELOCITY`
- **Descripción:** Aplica un vector de aceleración en los ejes X, Y, Z.

### Ejemplo de Configuración
```yaml
effects:
 - type: VELOCITY
    x: 0.0
    y: 1.0 # Impulso vertical alto
    z: 0.0
```

---

