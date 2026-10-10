# CategorÃ­a: Sonidos

Esta secciÃ³n documenta los efectos correspondientes a **Sonidos** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `play_sound`

- **Identificador / Alias:** `PLAY_SOUND / SOUND`
- **DescripciÃ³n:** Reproduce un sonido de Minecraft con volumen y tono ajustables.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PLAY_SOUND
    sound: ENTITY_WITHER_SPAWN
    volume: 1.0
    pitch: 1.2
```

---

## `stop_sound`

- **Identificador / Alias:** `STOP_SOUND`
- **DescripciÃ³n:** Detiene un sonido en curso o todos los sonidos del jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: STOP_SOUND
    sound: MUSIC_DISC_PIGSTEP
```

---

