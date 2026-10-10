# Categoría: Mensajes & UI

Esta sección documenta los efectos correspondientes a **Mensajes & UI** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `actionbar`

- **Identificador / Alias:** `ACTIONBAR`
- **Descripción:** Muestra un texto flotante en la barra de acción sobre la hotbar.

### Ejemplo de Configuración
```yaml
effects:
 - type: ACTIONBAR
    message: "<yellow>⚔ Furia activada: +25% Daño"
```

---

## `bossbar`

- **Identificador / Alias:** `BOSSBAR`
- **Descripción:** Muestra una barra de jefe personalizada en la parte superior.

### Ejemplo de Configuración
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
- **Descripción:** Envía un mensaje formateado al chat con soporte de MiniMessage y variables.

### Ejemplo de Configuración
```yaml
effects:
 - type: MESSAGE
    message: "<gradient:#ffaa00:#ffff55><bold>¡Poder Activado!</bold></gradient>"
```

---

## `title`

- **Identificador / Alias:** `TITLE`
- **Descripción:** Muestra un título y subtítulo en el centro de la pantalla.

### Ejemplo de Configuración
```yaml
effects:
 - type: TITLE
 title: "<red><bold>¡CRÍTICO DEVIRGADO!"
    subtitle: "<gray>Daño máximo aplicado"
```

---

