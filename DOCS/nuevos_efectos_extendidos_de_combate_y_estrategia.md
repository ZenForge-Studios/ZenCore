# CategorÃ­a: Nuevos Efectos Extendidos de Combate y Estrategia

Esta secciÃ³n documenta los efectos correspondientes a **Nuevos Efectos Extendidos de Combate y Estrategia** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `apply_vulnerability`

- **Identificador / Alias:** `APPLY_VULNERABILITY`
- **DescripciÃ³n:** Aplica vulnerabilidad y lentitud temporal al objetivo golpeado.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: APPLY_VULNERABILITY
    duration: 100
    amplifier: 1
```

---

## `bounce_attack`

- **Identificador / Alias:** `BOUNCE_ATTACK`
- **DescripciÃ³n:** El ataque rebota sucesivamente entre múltiples enemigos cercanos.

### Ejemplo de ConfiguraciÃ³n
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
- **DescripciÃ³n:** Inflige un porcentaje del daño del golpe principal a todas las criaturas cercanas.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: CLEAVE
    radius: 3.5
    ratio: 0.6
```

---

## `disarm`

- **Identificador / Alias:** `DISARM`
- **DescripciÃ³n:** Desarma al jugador oponente arrojando al suelo el ítem que sostiene en la mano principal.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: DISARM
```

---

## `execute`

- **Identificador / Alias:** `EXECUTE`
- **DescripciÃ³n:** Elimina instantáneamente al objetivo si su vida se encuentra por debajo del porcentaje configurado.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: EXECUTE
    threshold: 15.0
```

---

## `poison_cloud`

- **Identificador / Alias:** `POISON_CLOUD`
- **DescripciÃ³n:** Invoca una nube tóxica que envenena a todas las criaturas enemigas en el radio.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: POISON_CLOUD
    radius: 4.0
    duration: 120
```

---

## `reflect_projectile`

- **Identificador / Alias:** `REFLECT_PROJECTILE`
- **DescripciÃ³n:** Invierte la dirección y velocidad de un proyectil que se aproxima.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: REFLECT_PROJECTILE
```

---

## `shield_break`

- **Identificador / Alias:** `SHIELD_BREAK`
- **DescripciÃ³n:** Desactiva el escudo del oponente y lo deja en tiempo de recarga si estaba bloqueando.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SHIELD_BREAK
```

---

## `thorns_damage`

- **Identificador / Alias:** `THORNS_DAMAGE`
- **DescripciÃ³n:** Refleja daño directo de vuelta al atacante al recibir un impacto.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: THORNS_DAMAGE
    damage: 3.0
```

---

## `true_damage`

- **Identificador / Alias:** `TRUE_DAMAGE`
- **DescripciÃ³n:** Inflige daño puro directo que ignora la reducción de armadura, encantamientos de protección y escudos.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: TRUE_DAMAGE
    amount: 6.0
```

---

## `wither_damage`

- **Identificador / Alias:** `WITHER_DAMAGE`
- **DescripciÃ³n:** Aplica el efecto de daño degenerativo de Wither.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: WITHER_DAMAGE
    duration: 100
    amplifier: 1
```

---

