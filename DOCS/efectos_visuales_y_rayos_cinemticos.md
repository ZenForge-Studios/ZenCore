# CategorÃ­a: Efectos Visuales & Rayos Cinemáticos

Esta secciÃ³n documenta los efectos correspondientes a **Efectos Visuales & Rayos Cinemáticos** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `particle`

- **Identificador / Alias:** `PARTICLE`
- **DescripciÃ³n:** Genera una nube estática de partículas.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PARTICLE
    particle: CRIT
    count: 25
    speed: 0.2
```

---

## `particle_circle`

- **Identificador / Alias:** `PARTICLE_CIRCLE`
- **DescripciÃ³n:** Dibuja un círculo horizontal de partículas alrededor de la ubicación.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PARTICLE_CIRCLE
    particle: FLAME
    radius: 3.0
```

---

## `particle_line`

- **Identificador / Alias:** `PARTICLE_LINE`
- **DescripciÃ³n:** Traza una línea geométrica de partículas desde los ojos del jugador hacia el objetivo.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PARTICLE_LINE
    particle: ENCHANT
```

---

## `particle_sphere`

- **Identificador / Alias:** `PARTICLE_SPHERE`
- **DescripciÃ³n:** Dibuja una esfera tridimensional de partículas.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PARTICLE_SPHERE
    particle: SOUL_FIRE_FLAME
    radius: 2.5
```

---

## `projectile_beam`

- **Identificador / Alias:** `PROJECTILE_BEAM (Novedad)`
- **DescripciÃ³n:** Emite un haz continuo de partículas que avanza en línea recta, atravesando el espacio e infligiendo daño con impacto ...

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PROJECTILE_BEAM
    particle: FLAME # FLAME, SOUL_FIRE_FLAME, DRAGON_BREATH, etc.
    distance: 15.0 # Bloques de alcance
    step: 0.5 # Precisión del rayo
    damage: 8.0 # Daño al colisionar
```

---

