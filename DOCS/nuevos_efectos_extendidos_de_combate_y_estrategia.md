# Categoría: Nuevos Efectos Extendidos de Combate y Estrategia

Esta sección documenta los efectos correspondientes a **Nuevos Efectos Extendidos de Combate y Estrategia** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `apply_vulnerability`

- **Identificador / Alias:** `APPLY_VULNERABILITY`
- **Descripción:** Aplica vulnerabilidad y lentitud temporal al objetivo golpeado.

### Ejemplo de Configuración
```yaml
effects:
 - type: APPLY_VULNERABILITY
    duration: 100
    amplifier: 1
```

---

## `bounce_attack`

- **Identificador / Alias:** `BOUNCE_ATTACK`
- **Descripción:** El ataque rebota sucesivamente entre múltiples enemigos cercanos.

### Ejemplo de Configuración
```yaml
effects:
 - type: BOUNCE_ATTACK
    bounces: 4
    radius: 6.0
    damage: 5.0
```

---

## `cleave`

- **Identificador / Alias:** `CLEAVE`
- **Descripción:** Inflige un porcentaje del daño del golpe principal a todas las criaturas cercanas.

### Ejemplo de Configuración
```yaml
effects:
 - type: CLEAVE
    radius: 3.5
    ratio: 0.6
```

---

## `disarm`

- **Identificador / Alias:** `DISARM`
- **Descripción:** Desarma al jugador oponente arrojando al suelo el ítem que sostiene en la mano principal.

### Ejemplo de Configuración
```yaml
effects:
 - type: DISARM
```

---

## `execute`

- **Identificador / Alias:** `EXECUTE`
- **Descripción:** Elimina instantáneamente al objetivo si su vida se encuentra por debajo del porcentaje configurado.

### Ejemplo de Configuración
```yaml
effects:
 - type: EXECUTE
    threshold: 15.0
```

---

## `poison_cloud`

- **Identificador / Alias:** `POISON_CLOUD`
- **Descripción:** Invoca una nube tóxica que envenena a todas las criaturas enemigas en el radio.

### Ejemplo de Configuración
```yaml
effects:
 - type: POISON_CLOUD
    radius: 4.0
    duration: 120
```

---

## `reflect_projectile`

- **Identificador / Alias:** `REFLECT_PROJECTILE`
- **Descripción:** Invierte la dirección y velocidad de un proyectil que se aproxima.

### Ejemplo de Configuración
```yaml
effects:
 - type: REFLECT_PROJECTILE
```

---

## `shield_break`

- **Identificador / Alias:** `SHIELD_BREAK`
- **Descripción:** Desactiva el escudo del oponente y lo deja en tiempo de recarga si estaba bloqueando.

### Ejemplo de Configuración
```yaml
effects:
 - type: SHIELD_BREAK
```

---

## `thorns_damage`

- **Identificador / Alias:** `THORNS_DAMAGE`
- **Descripción:** Refleja daño directo de vuelta al atacante al recibir un impacto.

### Ejemplo de Configuración
```yaml
effects:
 - type: THORNS_DAMAGE
    damage: 3.0
```

---

## `true_damage`

- **Identificador / Alias:** `TRUE_DAMAGE`
- **Descripción:** Inflige daño puro directo que ignora la reducción de armadura, encantamientos de protección y escudos.

### Ejemplo de Configuración
```yaml
effects:
 - type: TRUE_DAMAGE
    amount: 6.0
```

---

## `wither_damage`

- **Identificador / Alias:** `WITHER_DAMAGE`
- **Descripción:** Aplica el efecto de daño degenerativo de Wither.

### Ejemplo de Configuración
```yaml
effects:
 - type: WITHER_DAMAGE
    duration: 100
    amplifier: 1
```

---

