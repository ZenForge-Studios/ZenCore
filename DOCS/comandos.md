# CategorÃ­a: Comandos

Esta secciÃ³n documenta los efectos correspondientes a **Comandos** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `console_command`

- **Identificador / Alias:** `CONSOLE_COMMAND`
- **DescripciÃ³n:** Ejecuta un comando con permisos de la consola de Minecraft.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: CONSOLE_COMMAND
    command: "eco give %player% 100"
```

---

## `player_command`

- **Identificador / Alias:** `PLAYER_COMMAND`
- **DescripciÃ³n:** Ejecuta un comando en nombre del jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: PLAYER_COMMAND
    command: "spawn"
```

---

