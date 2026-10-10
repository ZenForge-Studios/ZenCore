# Categoría: Entidades_proyectiles

Esta sección recopila todos los efectos pertenecientes a **entidades_proyectiles** registrados en ZenCore.

---

## `arrow_ring`

Dispara un anillo circular de flechas alrededor del jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: ARROW_RING
    amount: 8
    speed: 1.5
```

---

## `boost_projectile`

Aumenta significativamente la velocidad y el alcance de los proyectiles disparados.

## Ejemplo de Configuración

```yaml
effects:
  - type: BOOST_PROJECTILE
    multiplier: 1.5
```

---

## `homing`

Hace que las flechas o proyectiles busquen y persigan al objetivo más cercano.

## Ejemplo de Configuración

```yaml
effects:
  - type: HOMING
    radius: 8.0
```

---

## `name_entity`

Asigna un nombre visible personalizado con formato a la criatura objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: NAME_ENTITY
    name: '<yellow>Súbdito</yellow>'
```

---

## `shoot_arrow`

Dispara una flecha con velocidad y dirección configurable.

## Ejemplo de Configuración

```yaml
effects:
  - type: SHOOT_ARROW
    speed: 2.0
```

---

## `summon_minion`

Invoca una criatura aliada temporal que defiende al jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: SUMMON_MINION
    type: ZOMBIE
    duration: 200
```

---

