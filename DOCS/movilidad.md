# Categoría: Movilidad

Esta sección recopila todos los efectos pertenecientes a **movilidad** registrados en ZenCore.

---

## `blink`

Teletransporta al jugador una corta distancia en la dirección exacta hacia la que mira.

## Ejemplo de Configuración

```yaml
effects:
  - type: BLINK
    distance: 5.0
```

---

## `cloud_walker`

Genera nubes temporales bajo los pies permitiendo caminar en el aire brevemente.

## Ejemplo de Configuración

```yaml
effects:
  - type: CLOUD_WALKER
    duration: 100
```

---

## `ender_bow`

Teletransporta al tirador a la ubicación exacta donde impacta la flecha.

## Ejemplo de Configuración

```yaml
effects:
  - type: ENDER_BOW
    target: TARGET
```

---

## `endershift`

Teletransporte de emergencia a un punto seguro cercano con resistencia tras recibir daño.

## Ejemplo de Configuración

```yaml
effects:
  - type: ENDERSHIFT
    range: 6.0
```

---

## `harpoon`

Atrae a la entidad golpeada velozmente hacia el jugador cual arpón.

## Ejemplo de Configuración

```yaml
effects:
  - type: HARPOON
    strength: 1.5
```

---

## `knock_away`

Empuja violentamente a todas las entidades alejándolas del centro del impacto.

## Ejemplo de Configuración

```yaml
effects:
  - type: KNOCK_AWAY
    radius: 4.0
    strength: 1.5
```

---

## `levitate`

Hace levitar por el aire a la entidad objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: LEVITATE
    duration: 60
    amplifier: 1
```

---

## `pull_in`

Succiona y arrastra entidades hacia el centro del jugador o impacto.

## Ejemplo de Configuración

```yaml
effects:
  - type: PULL_IN
    strength: 1.2
```

---

## `random_teleport`

Teletransporta al objetivo a una ubicación segura aleatoria en un rango.

## Ejemplo de Configuración

```yaml
effects:
  - type: RANDOM_TELEPORT
    min_radius: 10
    max_radius: 50
```

---

## `rotate_victim`

Gira la orientación de la mirada de la víctima desorientándola.

## Ejemplo de Configuración

```yaml
effects:
  - type: ROTATE_VICTIM
    yaw: 180
```

---

## `set_velocity`

Aplica un vector tridimensional de velocidad exacto a la entidad.

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_VELOCITY
    x: 0.0
    y: 1.0
    z: 0.0
```

---

## `swap_positions`

Intercambia la posición física del atacante con la de su objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: SWAP_POSITIONS
    target: TARGET
```

---

## `teleport_behind`

Teletransporta al atacante inmediatamente detrás de la espalda del rival.

## Ejemplo de Configuración

```yaml
effects:
  - type: TELEPORT_BEHIND
    distance: 1.5
```

---

## `vortex`

Crea un vórtice rotatorio que aspira a los enemigos a su vórtice central.

## Ejemplo de Configuración

```yaml
effects:
  - type: VORTEX
    radius: 6.0
    strength: 1.0
```

---

## `wall_runner`

Permite correr horizontalmente a lo largo de paredes verticales.

## Ejemplo de Configuración

```yaml
effects:
  - type: WALL_RUNNER
    duration: 60
```

---

