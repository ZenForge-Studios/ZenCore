# 🗄️ 03. Base de Datos y Persistencia Asíncrona

El módulo `ZenDatabaseEngine` proporciona una capa de abstracción para persistencia de datos con soporte transparente para:
1. **SQLite Local:** Cero configuración; ideal para servidores pequeños o configuraciones por defecto.
2. **MySQL / MariaDB:** Rendimiento óptimo con pool de conexiones gestionado por HikariCP para servidores en red o BungeeCord.
3. **Migraciones Automáticas:** Control de versiones de tablas para que no tengas que preocuparte por scripts manuales al actualizar tus plugins.

---

## 💻 Conexión y Uso Básico

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.database.ZenDatabaseEngine.DatabaseType
import java.io.File

// Configuración para SQLite (archivo local)
val db = ZenCoreAPI.createDatabase(
    type = DatabaseType.SQLITE,
    sqliteFile = File(dataFolder, "spawners.db")
).connect()

// O configuración para MySQL remoto:
/*
val db = ZenDatabaseEngine(
    type = DatabaseType.MYSQL,
    host = "127.0.0.1",
    port = 3306,
    database = "servidor_spawners",
    user = "usuario",
    pass = "clave123"
).connect()
*/

// 1. Aplicar Migraciones Automáticas (Se ejecutan solo una vez si la versión no existe)
db.applyMigrations(
    // Versión 1: Crear tabla de spawners
    """
    CREATE TABLE IF NOT EXISTS active_spawners (
        id VARCHAR(36) PRIMARY KEY,
        owner_uuid VARCHAR(36),
        spawner_type VARCHAR(32),
        level INT DEFAULT 1,
        stacked_amount INT DEFAULT 1,
        world VARCHAR(64),
        x INT,
        y INT,
        z INT
    );
    """.trimIndent(),
    // Versión 2: Añadir columna de almacenamiento en una actualización posterior
    """
    ALTER TABLE active_spawners ADD COLUMN stored_drops INT DEFAULT 0;
    """.trimIndent()
)

// 2. Insertar o Actualizar Datos de Forma Asíncrona
db.executeAsync(
    "INSERT OR REPLACE INTO active_spawners (id, owner_uuid, spawner_type, level, stacked_amount, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
    spawnerId.toString(),
    player.uniqueId.toString(),
    "iron_golem",
    1,
    5,
    "world",
    100, 64, -200
).thenAccept { rowsAffected ->
    // Ejecutado al terminar en hilo asíncrono
}

// 3. Consultar Datos Asíncronamente con Mapeador
db.queryAsync(
    "SELECT * FROM active_spawners WHERE owner_uuid = ?;",
    player.uniqueId.toString()
) { rs ->
    // Mapear cada fila del ResultSet a un objeto o datos
    rs.getString("spawner_type") to rs.getInt("stacked_amount")
}.thenAccept { spawnerList ->
    player.sendMessage("§aTienes ${spawnerList.size} spawners activos en tu isla.")
}

// 4. Cerrar en el onDisable() de tu plugin
// db.close()
```
