# Categoría: Jugador_vida

Esta sección recopila todos los efectos pertenecientes a **jugador_vida** registrados en ZenCore.

---

## `air_refill`

Restaura el nivel de aire (oxígeno) bajo el agua del jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: AIR_REFILL
    amount: 100
```

---

## `blood_pact`

Pacto de sangre que previene daño mortal consumiendo experiencia en su lugar.

## Ejemplo de Configuración

```yaml
effects:
  - type: BLOOD_PACT
    exp_cost: 50
```

---

## `clear_negative_effects`

Elimina todos los efectos de poción negativos y perjudiciales activos en el jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: CLEAR_NEGATIVE_EFFECTS
    target: SELF
```

---

## `deposit_exp`

Deposita o almacena experiencia del jugador en un contenedor o variable.

## Ejemplo de Configuración

```yaml
effects:
  - type: DEPOSIT_EXP
    amount: 20
```

---

## `extinguish`

Apaga inmediatamente las llamas del cuerpo del jugador o entidad objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: EXTINGUISH
    target: SELF
```

---

## `feed`

Restaura puntos de alimento y saturación al jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: FEED
    amount: 4
```

---

## `give_absorption`

Otorga corazones amarillos de absorción adicionales temporales.

## Ejemplo de Configuración

```yaml
effects:
  - type: GIVE_ABSORPTION
    amount: 4.0
    duration: 200
```

---

## `give_exp`

Otorga una cantidad de puntos de experiencia al jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: GIVE_EXP
    amount: 50
```

---

## `give_levels`

Incrementa los niveles enteros de experiencia del jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: GIVE_LEVELS
    levels: 2
```

---

## `give_oxygen`

Aumenta el oxígeno restante bajo el agua.

## Ejemplo de Configuración

```yaml
effects:
  - type: GIVE_OXYGEN
    amount: 60
```

---

## `grant_invisibility`

Otorga invisibilidad completa al jugador durante un tiempo determinado.

## Ejemplo de Configuración

```yaml
effects:
  - type: GRANT_INVISIBILITY
    duration: 100
```

---

## `grant_resistance`

Otorga efecto de resistencia a daños al portador.

## Ejemplo de Configuración

```yaml
effects:
  - type: GRANT_RESISTANCE
    duration: 100
    amplifier: 1
```

---

## `grant_speed`

Otorga velocidad de movimiento aumentada al jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: GRANT_SPEED
    duration: 100
    amplifier: 1
```

---

## `grant_strength`

Aumenta la fuerza y potencia de ataque del portador.

## Ejemplo de Configuración

```yaml
effects:
  - type: GRANT_STRENGTH
    duration: 100
    amplifier: 1
```

---

## `heal_radius`

Restaura salud a todos los aliados en un radio cercano.

## Ejemplo de Configuración

```yaml
effects:
  - type: HEAL_RADIUS
    radius: 5.0
    amount: 4.0
```

---

## `modify_attribute`

Modifica temporalmente atributos de entidad como velocidad o daño.

## Ejemplo de Configuración

```yaml
effects:
  - type: MODIFY_ATTRIBUTE
    attribute: GENERIC_MOVEMENT_SPEED
    amount: 0.1
```

---

## `potion`

Alias de ADD_POTION_EFFECT para otorgar efectos de poción.

## Ejemplo de Configuración

```yaml
effects:
  - type: POTION
    effect: SPEED
    duration: 100
    amplifier: 1
```

---

## `resurrection`

Resucita al jugador cuando su salud llega a cero con efectos de tótem.

## Ejemplo de Configuración

```yaml
effects:
  - type: RESURRECTION
    health: 10.0
```

---

## `set_max_health`

Ajusta la salud máxima del jugador o entidad.

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_MAX_HEALTH
    max_health: 40.0
```

---

