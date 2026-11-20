# 💬 02. Gestor Universal de Mensajes y Localización

El módulo `ZenMessageService` estandariza el envío de notificaciones y textos en todos los plugins de la serie Zen. Permite asociar mensajes a través de archivos `messages.yml` y activar automáticamente sonido, título o barra de acción (Action Bar).

---

## 📄 Formato del Archivo `messages.yml`

```yaml
prefix: "<dark_gray>[<gradient:#ffaa00:#ff5500>ZenSpawner</gradient>]</dark_gray>"

# Mensaje de texto simple
no-permission: "<red>No tienes permisos suficientes para realizar esta acción."

# Mensaje multilínea
help:
  - "<yellow>Comandos disponibles de ZenSpawner:"
  - "<gray>/zs menu - Abre el menú principal"
  - "<gray>/zs shop - Tienda de spawners"

# Mensaje compuesto con Título, Subtítulo, ActionBar y Sonido
spawner-bought:
  message: "<green>¡Has comprado exitosamente un spawner de <yellow>%type%<green> por <gold>$%cost%<green>!"
  sound: "ENTITY_PLAYER_LEVELUP:1.2"
  actionbar: "<green>+1 Spawner añadido a tu inventario"
  title: "<gradient:#00ff88:#00aaff><b>¡COMPRA EXITOSA!</b></gradient>"
  subtitle: "<gray>Spawner de %type%"
```

---

## 💻 Ejemplo de Uso en Código

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import java.io.File

// 1. Inicializar el servicio de mensajes con un archivo messages.yml
val messageFile = File(dataFolder, "messages.yml")
val messages = ZenCoreAPI.createMessageService(prefix = "")
messages.loadFromYaml(messageFile)

// 2. Enviar mensaje simple
messages.send(player, "no-permission")

// 3. Enviar mensaje con placeholders contextuales
messages.send(
    player,
    "spawner-bought",
    mapOf(
        "type" to "Iron Golem",
        "cost" to "50,000"
    )
)
```

### Características Especiales
- **Tags `<no-prefix>`**: Si una línea de chat incluye la etiqueta `<no-prefix>`, el prefijo configurado no se mostrará para esa línea en particular.
- **Soporte PAPI:** Si `PlaceholderAPI` está instalado, cualquier placeholder como `%vault_eco_balance%` o `%player_name%` será reemplazado automáticamente.
