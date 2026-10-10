# CategorÃ­a: Movimiento & Física

Esta secciÃ³n documenta los efectos correspondientes a **Movimiento & Física** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `knockback`

- **Identificador / Alias:** `KNOCKBACK`
- **DescripciÃ³n:** Aplica retroceso físico direccional a la entidad.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: KNOCKBACK
    strength: 1.8
```

---

## `launch`

- **Identificador / Alias:** `LAUNCH`
- **DescripciÃ³n:** Propulsa a la entidad en la dirección a la que está mirando.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: LAUNCH
    power: 1.5
    y: 0.6
```

---

## `pull`

- **Identificador / Alias:** `PULL`
- **DescripciÃ³n:** Atrae a la entidad objetivo hacia la posición del jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PULL
    strength: 1.2
```

---

## `push`

- **Identificador / Alias:** `PUSH`
- **DescripciÃ³n:** Empuja a la entidad objetivo lejos del jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PUSH
    strength: 1.5
```

---

## `teleport`

- **Identificador / Alias:** `TELEPORT`
- **DescripciÃ³n:** Teletransporta al jugador hacia la ubicación del objetivo.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: TELEPORT
    target: TARGET
```

---

## `velocity`

- **Identificador / Alias:** `VELOCITY`
- **DescripciÃ³n:** Aplica un vector de aceleración en los ejes X, Y, Z.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: VELOCITY
    x: 0.0
    y: 1.0 # Impulso vertical alto
    z: 0.0
```

---

