# Argumentos Opcionales de Efectos (Optional Arguments)

En **ZenCore**, todos los efectos admiten un conjunto unificado de **argumentos opcionales** dentro de su bloque `args:` (o a nivel de parámetros en el efecto). Estos argumentos permiten controlar la probabilidad, costos, tiempos de espera, enfriamientos, repeticiones y condiciones de activación dinámicas de forma idéntica a Libreforge y Eco.

---

## 1. Probabilidad (`chance`)

Define la probabilidad porcentual (de `0` a `100`) de que el efecto se ejecute. Admite expresiones matemáticas y placeholders. Si se omite, su valor predeterminado es `100`.

```yaml
effects:
  - type: SPAWN_LIGHTNING_STRIKE
    args:
      chance: 25 # 25% de probabilidad
```

Con placeholders o fórmulas matemáticas:
```yaml
effects:
  - type: HEAL
    args:
      chance: "10 + (%level% * 5)" # Escala con el nivel de la habilidad
      amount: 4.0
```

---

## 2. Costo Económico (`cost`)

Requiere **Vault** en el servidor. Deduce una cantidad de dinero de la cuenta del jugador para que el efecto se active. Si el jugador no cuenta con el dinero suficiente, el efecto se cancela y se le envía un mensaje de advertencia.

```yaml
effects:
  - type: FEED
    args:
      cost: 50.0 # Cobra $50 al jugador
      amount: 6
```

---

## 3. Contador de Activaciones (`every`)

El efecto solo se activa cada $X$ veces consecutivas que se dispare el evento o ataque.

```yaml
effects:
  - type: CRIT_MULTIPLIER
    args:
      every: 3 # Se activa exactamente cada 3 golpes (3er, 6to, 9no golpe...)
      multiplier: 2.0
```

---

## 4. Requisito Expresional (`require`)

Una expresión matemática o lógica con placeholders que debe dar un resultado mayor a `0` para que el efecto se ejecute.

```yaml
effects:
  - type: STRIKE_LIGHTNING
    args:
      require: "%player_health% < 10" # Solo si el jugador tiene menos de 10 de vida
```

---

## 5. Retardo en Ticks (`delay`)

Especifica cuántos ticks del servidor (20 ticks = 1 segundo) se debe esperar antes de ejecutar el efecto.

```yaml
effects:
  - type: MESSAGE
    args:
      message: "<yellow>¡Preparando lanzamiento en 1 segundo!</yellow>"
  - type: LAUNCH
    args:
      delay: 20 # Se ejecuta 1 segundo después
      strength: 2.5
```

---

## 6. Repeticiones (`repeat`)

Permite ejecutar el efecto repetidamente durante múltiples intervalos, proporcionando el placeholder contextual `%repeat_count%`.

```yaml
effects:
  - type: PARTICLE_CIRCLE
    args:
      delay: 5 # Espera entre cada repetición
      repeat:
        times: 5 # Se repetirá 5 veces
        start: 1.0 # Valor inicial de %repeat_count%
        increment: 0.5 # Aumento de %repeat_count% en cada paso
      radius: "%repeat_count%" # El círculo se expande en cada repetición
```

---

## 7. Enfriamiento (`cooldown`) y Grupos

Controla el tiempo de recarga del efecto (en segundos). Soporta cooldowns locales y agrupados, mensajes personalizables y persistencia automática en el PDC para enfriamientos largos.

```yaml
effects:
  - type: DASH
    args:
      cooldown: 8.0 # 8 segundos de enfriamiento
      cooldown_group: "movilidad_espada" # Comparte cooldown con otras habilidades del grupo
      send_cooldown_message: true
      cooldown_message: "<red>¡Habilidad en enfriamiento! Espera <yellow>%seconds%s</yellow>.</red>"
```

---

## 8. Prioridad de Ejecución (`run-order`)

Define el orden relativo en el que se ejecuta este efecto frente a otros efectos concurrentes dentro de la misma habilidad:
- `start`
- `early`
- `normal` (predeterminado)
- `late`
- `end`
