# Categoría: Combate

Esta sección recopila todos los efectos pertenecientes a **combate** registrados en ZenCore.

---

## `add_damage`

Añade daño adicional plano o mediante fórmula al daño del ataque actual.

## Ejemplo de Configuración

```yaml
effects:
  - type: ADD_DAMAGE
    damage: 2.0
```

---

## `aoe_damage`

Aplica daño en área a todas las entidades vivas alrededor del objetivo o jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: AOE_DAMAGE
    radius: 5.0
    damage: 4.0
```

---

## `arctic_freeze`

Congela intensamente al objetivo aplicando ticks de congelación y partículas de hielo.

## Ejemplo de Configuración

```yaml
effects:
  - type: ARCTIC_FREEZE
    ticks: 140
    damage: 2.0
```

---

## `black_hole`

Crea un vórtice gravitacional oscuro que atrae e inflige daño a entidades cercanas.

## Ejemplo de Configuración

```yaml
effects:
  - type: BLACK_HOLE
    radius: 6.0
    duration: 80
    damage: 2.0
```

---

## `bleed`

Provoca un sangrado continuo en la entidad objetivo infligiendo daño periódico.

## Ejemplo de Configuración

```yaml
effects:
  - type: BLEED
    duration: 60
    damage: 1.0
```

---

## `chain_lightning`

Dispara un rayo que encadena e impacta a múltiples entidades vivas consecutivas.

## Ejemplo de Configuración

```yaml
effects:
  - type: CHAIN_LIGHTNING
    chains: 3
    radius: 5.0
    damage: 4.0
```

---

## `clear_invulnerability`

Elimina los ticks de invulnerabilidad temporal del objetivo para permitir golpes continuos.

## Ejemplo de Configuración

```yaml
effects:
  - type: CLEAR_INVULNERABILITY
    target: TARGET
```

---

## `crit_multiplier`

Multiplica el daño únicamente si el golpe cumple condiciones de golpe crítico.

## Ejemplo de Configuración

```yaml
effects:
  - type: CRIT_MULTIPLIER
    multiplier: 2.0
```

---

## `curse_clumsiness`

Maldición de torpeza que puede desarmar o hacer fallar el golpe al usuario.

## Ejemplo de Configuración

```yaml
effects:
  - type: CURSE_CLUMSINESS
    drop_weapon: false
```

---

## `curse_decay`

Maldición de deterioro que aplica efecto Wither/descomposición al usuario.

## Ejemplo de Configuración

```yaml
effects:
  - type: CURSE_DECAY
    duration: 60
    amplifier: 0
```

---

## `damage_entity`

Aplica daño directo a una entidad viva específica.

## Ejemplo de Configuración

```yaml
effects:
  - type: DAMAGE_ENTITY
    target: TARGET
    amount: 4.0
```

---

## `damage_multiplier`

Multiplica el daño total del evento de ataque por un factor decimal.

## Ejemplo de Configuración

```yaml
effects:
  - type: DAMAGE_MULTIPLIER
    multiplier: 1.5
```

---

## `damage_nearby_entities`

Inflige daño a todas las criaturas hostiles o jugadores en un radio determinado.

## Ejemplo de Configuración

```yaml
effects:
  - type: DAMAGE_NEARBY_ENTITIES
    radius: 4.0
    damage: 4.0
```

---

## `execute_target`

Ejecuta instantáneamente a objetivos con salud por debajo de un umbral porcentual.

## Ejemplo de Configuración

```yaml
effects:
  - type: EXECUTE_TARGET
    threshold: 0.15
```

---

## `freeze`

Aplica lentitud y efectos visuales de hielo al objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: FREEZE
    duration: 60
    amplifier: 1
```

---

## `freeze_target`

Inmoviliza y congela por completo a la criatura o jugador objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: FREEZE_TARGET
    duration: 80
```

---

## `frost_nova`

Emite una onda expansiva de escarcha en 360 grados congelando y dañando a los rivales.

## Ejemplo de Configuración

```yaml
effects:
  - type: FROST_NOVA
    radius: 5.0
    damage: 3.0
```

---

## `ignite`

Prende fuego al objetivo durante una cantidad especificada de segundos o ticks.

## Ejemplo de Configuración

```yaml
effects:
  - type: IGNITE
    duration: 80
```

---

## `kill_player`

Mata inmediatamente a la entidad o jugador objetivo ignorando armaduras.

## Ejemplo de Configuración

```yaml
effects:
  - type: KILL_PLAYER
    target: TARGET
```

---

## `lifesteal`

Roba un porcentaje del daño infligido curando al atacante.

## Ejemplo de Configuración

```yaml
effects:
  - type: LIFESTEAL
    percentage: 0.25
```

---

## `negate_projectile`

Destruye o anula proyectiles entrantes antes de que impacten al jugador.

## Ejemplo de Configuración

```yaml
effects:
  - type: NEGATE_PROJECTILE
    radius: 3.0
```

---

## `purge_beneficial_effects`

Elimina todos los efectos de poción positivos del oponente en combate.

## Ejemplo de Configuración

```yaml
effects:
  - type: PURGE_BENEFICIAL_EFFECTS
    target: TARGET
```

---

## `random_chaos`

Desencadena un efecto caótico aleatorio entre múltiples posibilidades.

## Ejemplo de Configuración

```yaml
effects:
  - type: RANDOM_CHAOS
    target: TARGET
```

---

## `set_damage`

Sobrescribe directamente el daño del evento de ataque.

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_DAMAGE
    damage: 10.0
```

---

## `set_freeze_ticks`

Ajusta los ticks de congelación directa de la entidad.

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_FREEZE_TICKS
    ticks: 140
```

---

## `shockwave`

Genera una onda de choque sísmica que empuja y daña en área.

## Ejemplo de Configuración

```yaml
effects:
  - type: SHOCKWAVE
    radius: 5.0
    damage: 5.0
```

---

## `silence`

Impide que el objetivo active habilidades o encantamientos durante un tiempo.

## Ejemplo de Configuración

```yaml
effects:
  - type: SILENCE
    duration: 80
```

---

## `smite`

Invoca un rayo directo sobre el objetivo infligiendo daño eléctrico.

## Ejemplo de Configuración

```yaml
effects:
  - type: SMITE
    damage: 5.0
```

---

## `strip_ai`

Desactiva temporalmente la inteligencia artificial de las entidades golpeadas.

## Ejemplo de Configuración

```yaml
effects:
  - type: STRIP_AI
    duration: 100
```

---

## `stun`

Aturde completamente al rival congelando su movimiento e impidiendo acciones.

## Ejemplo de Configuración

```yaml
effects:
  - type: STUN
    duration: 40
```

---

## `sweeping_damage_ratio`

Ajusta la proporción de daño de barrido de espadas.

## Ejemplo de Configuración

```yaml
effects:
  - type: SWEEPING_DAMAGE_RATIO
    ratio: 0.75
```

---

## `total_damage_multiplier`

Multiplicador final acumulativo para el daño total.

## Ejemplo de Configuración

```yaml
effects:
  - type: TOTAL_DAMAGE_MULTIPLIER
    multiplier: 1.5
```

---

## `transfer_effects`

Transfiere todos los efectos negativos del portador hacia su agresor.

## Ejemplo de Configuración

```yaml
effects:
  - type: TRANSFER_EFFECTS
    target: TARGET
```

---

## `water_tsunami`

Desata una ola de agua expansiva que arrasa y empuja entidades a su paso.

## Ejemplo de Configuración

```yaml
effects:
  - type: WATER_TSUNAMI
    radius: 7.0
    damage: 4.0
```

---

## `web_shot`

Dispara telarañas que atrapan e inmovilizan temporalmente al objetivo.

## Ejemplo de Configuración

```yaml
effects:
  - type: WEB_SHOT
    duration: 5
```

---

