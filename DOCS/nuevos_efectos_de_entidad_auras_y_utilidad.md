# CategorÃ­a: Nuevos Efectos de Entidad, Auras y Utilidad

Esta secciÃ³n documenta los efectos correspondientes a **Nuevos Efectos de Entidad, Auras y Utilidad** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `charm`

- **Identificador / Alias:** `CHARM`
- **DescripciÃ³n:** Enamora instantáneamente a los animales objetivos listos para reproducirse.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: CHARM
```

---

## `compact_blocks`

- **Identificador / Alias:** `COMPACT_BLOCKS`
- **DescripciÃ³n:** Compacta materiales sueltos en el inventario (ej. 9 de carbón en 1 bloque de carbón).

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: COMPACT_BLOCKS
```

---

## `drop_head`

- **Identificador / Alias:** `DROP_HEAD`
- **DescripciÃ³n:** Hace que la criatura derrotada suelte su cabeza.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: DROP_HEAD
```

---

## `fear`

- **Identificador / Alias:** `FEAR`
- **DescripciÃ³n:** Repele y asusta a los monstruos cercanos empujándolos lejos.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: FEAR
    radius: 8.0
```

---

## `feed_radius`

- **Identificador / Alias:** `FEED_RADIUS / HEAL_RADIUS`
- **DescripciÃ³n:** Restaura comida o vida en área a los jugadores aliados cercanos.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: HEAL_RADIUS
    radius: 6.0
    amount: 5.0
```

---

## `grant_night_vision`

- **Identificador / Alias:** `GRANT_NIGHT_VISION / GRANT_INVISIBILITY / GRANT_STRENGTH / GRANT_RESISTANCE / GRANT_SPEED`
- **DescripciÃ³n:** Efectos de buff directo configurables.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: GRANT_STRENGTH
    duration: 120
    amplifier: 1
```

---

## `gravity_well`

- **Identificador / Alias:** `GRAVITY_WELL`
- **DescripciÃ³n:** Genera un pozo de gravedad que succiona a todas las entidades hacia el centro.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: GRAVITY_WELL
    radius: 7.0
```

---

## `meteor_shower`

- **Identificador / Alias:** `METEOR_SHOWER`
- **DescripciÃ³n:** Bombardea el área con una lluvia de pequeñas bolas de fuego.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: METEOR_SHOWER
    count: 6
```

---

## `piglin_pacify`

- **Identificador / Alias:** `PIGLIN_PACIFY`
- **DescripciÃ³n:** Pacifica a los Piglins cercanos que tengan como objetivo al jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PIGLIN_PACIFY
    radius: 12.0
```

---

## `remove_all_negative_effects`

- **Identificador / Alias:** `REMOVE_ALL_NEGATIVE_EFFECTS`
- **DescripciÃ³n:** Limpia todos los efectos negativos activos (veneno, lentitud, wither, fatiga, etc.).

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: REMOVE_ALL_NEGATIVE_EFFECTS
```

---

## `repair_hand`

- **Identificador / Alias:** `REPAIR_HAND / REPAIR_ARMOR`
- **DescripciÃ³n:** Restaura la durabilidad del ítem en mano o de todas las piezas de armadura equipadas.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: REPAIR_ARMOR
    amount: 40
```

---

## `repulse_all`

- **Identificador / Alias:** `REPULSE_ALL`
- **DescripciÃ³n:** Onda expansiva que empuja a todos los enemigos alejándolos con fuerza sónica.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: REPULSE_ALL
    radius: 6.0
    force: 1.8
```

---

## `spawn_particle_ring`

- **Identificador / Alias:** `SPAWN_PARTICLE_RING`
- **DescripciÃ³n:** Genera un anillo circular de partículas decorativas o de advertencia.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SPAWN_PARTICLE_RING
    radius: 3.5
    count: 32
```

---

## `steal_exp`

- **Identificador / Alias:** `STEAL_EXP / DEPOSIT_EXP`
- **DescripciÃ³n:** Permite absorber o almacenar niveles/experiencia en el ítem.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: STEAL_EXP
    amount: 25
```

---

## `sun_strike`

- **Identificador / Alias:** `SUN_STRIKE`
- **DescripciÃ³n:** Impacto de fuego celestial concentrado desde el cielo en la ubicación objetivo.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SUN_STRIKE
    damage: 12.0
```

---

## `taunt`

- **Identificador / Alias:** `TAUNT`
- **DescripciÃ³n:** Provoca a todos los monstruos en el radio para que ataquen al jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: TAUNT
    radius: 15.0
```

---

## `time_warp`

- **Identificador / Alias:** `TIME_WARP`
- **DescripciÃ³n:** Intercambia instantáneamente las posiciones físicas del jugador y la víctima objetivo.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: TIME_WARP
```

---

