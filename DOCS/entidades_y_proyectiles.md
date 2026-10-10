# Categoría: Entidades & Proyectiles

Esta sección documenta los efectos correspondientes a **Entidades & Proyectiles** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `launch_projectile`

- **Identificador / Alias:** `LAUNCH_PROJECTILE`
- **Descripción:** Dispara un proyectil (`SNOWBALL`, `FIREBALL`) desde el jugador.

### Ejemplo de Configuración
```yaml
effects:
 - type: LAUNCH_PROJECTILE
    type: FIREBALL
    speed: 1.8
```

---

## `remove_entity`

- **Identificador / Alias:** `REMOVE_ENTITY`
- **Descripción:** Elimina la entidad objetivo del mundo.

### Ejemplo de Configuración
```yaml
effects:
 - type: REMOVE_ENTITY
```

---

## `spawn_entity`

- **Identificador / Alias:** `SPAWN_ENTITY`
- **Descripción:** Spawnea una entidad en la ubicación del objetivo.

### Ejemplo de Configuración
```yaml
effects:
 - type: SPAWN_ENTITY
    type: LIGHTNING_BOLT
```

---

