# ⚙️ 07. Configuración YAML con Auto-Update

El módulo `ZenConfig` soluciona uno de los mayores problemas al distribuir actualizaciones de plugins: **las nuevas opciones agregadas a los archivos de configuración no aparecían a menos que el usuario borrara su archivo existente**.

## 🚀 Cómo Funciona
- Si el archivo `.yml` no existe en la carpeta del servidor, extrae automáticamente el recurso embebido en el `.jar`.
- Si el archivo ya existe (y el administrador ha modificado valores), `ZenConfig` compara las claves del archivo con las del recurso interno.
- **Inyecta automáticamente cualquier clave o sección nueva que falte**, preservando al 100% las configuraciones personalizadas del usuario.

---

## 💻 Ejemplo de Uso

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import java.io.File

// Cargar config.yml con actualización automática desde el recurso embebido
val configFile = File(dataFolder, "config.yml")
val config = ZenCoreAPI.createConfig(configFile) {
    // Proveedor del recurso interno en el JAR del plugin
    getResource("config.yml")
}

// Lectura tipada simple y segura
val enabled = config.getBoolean("system.auto-sell", true)
val defaultRange = config.getInt("spawner.default-range", 16)
val multiplier = config.getDouble("economy.multiplier", 1.5)
val prefix = config.getString("messages.prefix", "&7[ZenSpawner]")

// Recargar en vivo (ej. en comando /zs reload)
config.reload()
```
