# Categoría: Mundo_bloques

Esta sección recopila todos los efectos pertenecientes a **mundo_bloques** registrados en ZenCore.

---

## `drain_fluid`

Drena y elimina agua o lava en un radio alrededor del bloque objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: DRAIN_FLUID
    radius: 3
```

---

## `drain_liquid`

Absorbe líquidos cercanos al impacto convirtiéndolos en aire o esponja.

## Ejemplo de Configuración

```yaml
effects:
  - type: DRAIN_LIQUID
    radius: 3
```

---

## `lava_sponge`

Absorbe lava circundante actuando como esponja de magma.

## Ejemplo de Configuración

```yaml
effects:
  - type: LAVA_SPONGE
    radius: 3
```

---

## `place_block`

Coloca un bloque específico en la posición objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: PLACE_BLOCK
    block: STONE
```

---

## `set_biome`

Cambia el bioma del bloque o área objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_BIOME
    biome: PLAINS
```

---

## `set_time`

Cambia la hora del mundo actual (e.g. día, noche o ticks).

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_TIME
    time: 1000
```

---

## `set_weather`

Cambia el clima del mundo (soleado, lluvia o tormenta).

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_WEATHER
    weather: CLEAR
```

---

## `spawn_lightning_strike`

Genera un rayo en el lugar indicado.

## Ejemplo de Configuración

```yaml
effects:
  - type: SPAWN_LIGHTNING_STRIKE
    target: TARGET
```

---

## `weather`

Modifica las condiciones atmosféricas del entorno.

## Ejemplo de Configuración

```yaml
effects:
  - type: WEATHER
    type: CLEAR
```

---

