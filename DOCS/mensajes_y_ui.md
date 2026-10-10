# CategorÃ­a: Mensajes & UI

Esta secciÃ³n documenta los efectos correspondientes a **Mensajes & UI** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `actionbar`

- **Identificador / Alias:** `ACTIONBAR`
- **DescripciÃ³n:** Muestra un texto flotante en la barra de acción sobre la hotbar.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: ACTIONBAR
    message: "<yellow>⚔ Furia activada: +25% Daño"
```

---

## `bossbar`

- **Identificador / Alias:** `BOSSBAR`
- **DescripciÃ³n:** Muestra una barra de jefe personalizada en la parte superior.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: BOSSBAR
 title: "<gold>Tiempo restante"
    progress: 1.0
    color: PURPLE
```

---

## `message`

- **Identificador / Alias:** `MESSAGE`
- **DescripciÃ³n:** Envía un mensaje formateado al chat con soporte de MiniMessage y variables.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: MESSAGE
    message: "<gradient:#ffaa00:#ffff55><bold>¡Poder Activado!</bold></gradient>"
```

---

## `title`

- **Identificador / Alias:** `TITLE`
- **DescripciÃ³n:** Muestra un título y subtítulo en el centro de la pantalla.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: TITLE
 title: "<red><bold>¡CRÍTICO DEVIRGADO!"
    subtitle: "<gray>Daño máximo aplicado"
```

---

