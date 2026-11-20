# 🎮 01. Sistema Fluido de Comandos y Subcomandos

El módulo `ZenCommand` permite registrar comandos y subcomandos directamente en tiempo de ejecución **sin necesidad de declararlos en el archivo `plugin.yml`** de tu plugin.

## 🚀 Características Principales
- **Registro Dinámico:** Se inyecta automáticamente en el `CommandMap` de Bukkit.
- **Validaciones Integradas:** Validación de permisos y protección contra ejecución de consola (`playerOnly = true`).
- **Autocompletado Automático (Tab Completion):** Si no defines un autocompletado propio, el sistema sugiere automáticamente los nombres de subcomandos y de los jugadores en línea según los permisos.
- **Menú de Ayuda Automático:** Si el usuario no introduce argumentos o escribe un subcomando erróneo, se le muestra la lista formateada de subcomandos disponibles.

---

## 💻 Ejemplo de Uso (Kotlin)

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI

// En el onEnable() de tu plugin (ej. ZenSpawner):
val spawnerCmd = ZenCoreAPI.createCommand(
    name = "zenspawner",
    commandPermission = "zenspawner.admin",
    playerOnly = false,
    commandAliases = listOf("spawner", "zs")
)

// 1. Subcomando: /zenspawner give <jugador> <tipo> [cantidad]
spawnerCmd.sub(name = "give", permission = "zenspawner.admin") {
    onExecute { ctx ->
        val target = ctx.getTargetPlayer(0)
        if (target == null) {
            ctx.reply("<red>Jugador no encontrado o desconectado.")
            return@onExecute
        }
        val type = ctx.getArg(1) ?: "zombie"
        val amount = ctx.getInt(2, default = 1)

        ctx.reply("<green>Has entregado <yellow>$amount spawner(s) de $type <green>a <white>${target.name}<green>.")
        target.sendMessage("§a¡Has recibido un spawner de $type!")
    }
    onTabComplete { ctx ->
        when (ctx.args.size) {
            1 -> org.bukkit.Bukkit.getOnlinePlayers().map { it.name }
            2 -> listOf("zombie", "skeleton", "blaze", "iron_golem")
            3 -> listOf("1", "4", "16", "64")
            else -> emptyList()
        }
    }
}

// 2. Subcomando: /zenspawner menu (Solo para jugadores)
spawnerCmd.sub(name = "menu", permission = "zenspawner.use", playerOnly = true) {
    onExecute { ctx ->
        val player = ctx.player!!
        ctx.reply("<aqua>Abriendo el panel de spawners...")
        // Abrir tu ZenMenu aquí
    }
}

// 3. Subcomando: /zenspawner reload
spawnerCmd.sub(name = "reload", permission = "zenspawner.admin") {
    onExecute { ctx ->
        ctx.reply("<green>¡Configuración de ZenSpawner recargada con éxito!")
    }
}

// 4. Registrar en el servidor
spawnerCmd.register(fallbackPrefix = "zenspawner")
```

---

## 🛠️ Métodos Útiles de `ZenCommandContext`
| Método | Tipo de Retorno | Descripción |
| :--- | :--- | :--- |
| `ctx.player` | `Player?` | Retorna la instancia de Player si quien ejecutó el comando es un jugador. |
| `ctx.sender` | `CommandSender` | Quien ejecutó el comando (Jugador o Consola). |
| `ctx.isPlayer` | `Boolean` | True si es un jugador. |
| `ctx.reply("<gradient:...>Texto")` | `Unit` | Envía un mensaje con formato MiniMessage de vuelta. |
| `ctx.getArg(index)` | `String?` | Obtiene el argumento de la posición (0, 1, 2...). |
| `ctx.getInt(index, default)` | `Int` | Parsea el argumento a entero de forma segura. |
| `ctx.getDouble(index, default)` | `Double` | Parsea el argumento a double de forma segura. |
| `ctx.getTargetPlayer(index)` | `Player?` | Busca un jugador en línea por su nombre en ese argumento. |
