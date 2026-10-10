# CategorÃ­a: Entidades & Proyectiles

Esta secciÃ³n documenta los efectos correspondientes a **Entidades & Proyectiles** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `launch_projectile`

- **Identificador / Alias:** `LAUNCH_PROJECTILE`
- **DescripciÃ³n:** Dispara un proyectil (`SNOWBALL`, `FIREBALL`) desde el jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: LAUNCH_PROJECTILE
    type: FIREBALL
    speed: 1.8
```

---

## `remove_entity`

- **Identificador / Alias:** `REMOVE_ENTITY`
- **DescripciÃ³n:** Elimina la entidad objetivo del mundo.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: REMOVE_ENTITY
```

---

## `spawn_entity`

- **Identificador / Alias:** `SPAWN_ENTITY`
- **DescripciÃ³n:** Spawnea una entidad en la ubicación del objetivo.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SPAWN_ENTITY
    type: LIGHTNING_BOLT
```

---

