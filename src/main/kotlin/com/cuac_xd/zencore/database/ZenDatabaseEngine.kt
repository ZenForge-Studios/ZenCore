package com.cuac_xd.zencore.database

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Motor unificado y asíncrono de base de datos para plugins del ecosistema Zen.
 * Admite SQLite local de cero configuración (mediante DriverManager nativo de Java/Paper)
 * o MySQL/MariaDB remoto de alto rendimiento mediante HikariCP (si está en classpath) o conexión gestionada.
 */
class ZenDatabaseEngine(
    val type: DatabaseType,
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "minecraft",
    val user: String = "root",
    val pass: String = "",
    val sqliteFile: File? = null
) {
    enum class DatabaseType {
        SQLITE,
        MYSQL,
        MARIADB
    }

    private var hikariDataSource: Any? = null
    private var singleConnection: Connection? = null
    private val connectionLock = Any()
    private val poolExecutor: ExecutorService = Executors.newFixedThreadPool(4)

    fun connect(): ZenDatabaseEngine {
        synchronized(connectionLock) {
            when (type) {
                DatabaseType.SQLITE -> {
                    val dbPath = sqliteFile?.absolutePath ?: "plugins/ZenCore/database.db"
                    File(dbPath).parentFile?.mkdirs()
                    singleConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
                }
                DatabaseType.MYSQL, DatabaseType.MARIADB -> {
                    // Intentar usar HikariCP si está disponible
                    val hikariInitialized = runCatching {
                        val configClass = Class.forName("com.zaxxer.hikari.HikariConfig")
                        val dsClass = Class.forName("com.zaxxer.hikari.HikariDataSource")
                        val config = configClass.getDeclaredConstructor().newInstance()

                        val setJdbcUrl = configClass.getMethod("setJdbcUrl", String::class.java)
                        val setUsername = configClass.getMethod("setUsername", String::class.java)
                        val setPassword = configClass.getMethod("setPassword", String::class.java)
                        val setMaximumPoolSize = configClass.getMethod("setMaximumPoolSize", Int::class.javaPrimitiveType)

                        val jdbcScheme = if (type == DatabaseType.MARIADB) "mariadb" else "mysql"
                        val jdbcUrl = "jdbc:$jdbcScheme://$host:$port/$database?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8"
                        setJdbcUrl.invoke(config, jdbcUrl)
                        setUsername.invoke(config, user)
                        setPassword.invoke(config, pass)
                        setMaximumPoolSize.invoke(config, 10)

                        hikariDataSource = dsClass.getConstructor(configClass).newInstance(config)
                        true
                    }.getOrDefault(false)

                    if (!hikariInitialized) {
                        val jdbcScheme = if (type == DatabaseType.MARIADB) "mariadb" else "mysql"
                        // Fallback a conexión directa
                        singleConnection = DriverManager.getConnection(
                            "jdbc:$jdbcScheme://$host:$port/$database?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8",
                            user,
                            pass
                        )
                    }
                }
            }
        }
        return this
    }

    val isConnected: Boolean
        get() {
            if (hikariDataSource != null) return true
            return synchronized(connectionLock) {
                singleConnection?.isClosed == false
            }
        }

    fun getConnection(): Connection {
        val ds = hikariDataSource
        if (ds != null) {
            val getConnectionMethod = ds.javaClass.getMethod("getConnection")
            return getConnectionMethod.invoke(ds) as Connection
        }
        return synchronized(connectionLock) {
            val conn = singleConnection
            if (conn != null && !conn.isClosed) {
                conn
            } else {
                // Reconectar si cerró
                connect()
                singleConnection ?: throw IllegalStateException("No se pudo obtener conexión con la base de datos.")
            }
        }
    }

    private fun <T> withConnection(block: (Connection) -> T): T {
        val ds = hikariDataSource
        if (ds != null) {
            val conn = getConnection()
            return try {
                block(conn)
            } finally {
                runCatching { conn.close() }
            }
        }
        return synchronized(connectionLock) {
            val conn = getConnection()
            block(conn)
        }
    }

    /**
     * Ejecuta una sentencia DDL o DML de forma asíncrona (CREATE TABLE, INSERT, UPDATE, DELETE).
     */
    fun executeAsync(sql: String, vararg params: Any): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync({
            withConnection { conn ->
                conn.prepareStatement(sql).use { ps ->
                    for (i in params.indices) {
                        ps.setObject(i + 1, params[i])
                    }
                    ps.executeUpdate()
                }
            }
        }, poolExecutor)
    }

    /**
     * Ejecuta una consulta SELECT y mapea el resultado de forma asíncrona.
     */
    fun <T> queryAsync(sql: String, vararg params: Any, mapper: (ResultSet) -> T): CompletableFuture<List<T>> {
        return CompletableFuture.supplyAsync({
            val results = mutableListOf<T>()
            withConnection { conn ->
                conn.prepareStatement(sql).use { ps ->
                    for (i in params.indices) {
                        ps.setObject(i + 1, params[i])
                    }
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            results.add(mapper(rs))
                        }
                    }
                }
            }
            results
        }, poolExecutor)
    }

    /**
     * Ejecuta una consulta SELECT y mapea únicamente el primer resultado (o null si no existe).
     */
    fun <T> querySingleAsync(sql: String, vararg params: Any, mapper: (ResultSet) -> T): CompletableFuture<T?> {
        return CompletableFuture.supplyAsync({
            withConnection { conn ->
                conn.prepareStatement(sql).use { ps ->
                    for (i in params.indices) {
                        ps.setObject(i + 1, params[i])
                    }
                    ps.executeQuery().use { rs ->
                        if (rs.next()) mapper(rs) else null
                    }
                }
            }
        }, poolExecutor)
    }

    /**
     * Ejecuta un bloque transaccional atómico de forma síncrona (commit/rollback automático).
     */
    fun <T> withTransactionSync(block: (Connection) -> T): T {
        return withConnection { conn ->
            val prevAutoCommit = conn.autoCommit
            conn.autoCommit = false
            try {
                val result = block(conn)
                conn.commit()
                result
            } catch (e: Throwable) {
                runCatching { conn.rollback() }
                throw e
            } finally {
                runCatching { conn.autoCommit = prevAutoCommit }
            }
        }
    }

    /**
     * Ejecuta un bloque transaccional atómico de forma asíncrona.
     */
    fun <T> withTransactionAsync(block: (Connection) -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync({ withTransactionSync(block) }, poolExecutor)
    }

    /**
     * Aplica migraciones secuenciales ordenadas para crear o actualizar tablas.
     * Compatible con SQLite, MySQL (incluyendo modo estricto) y MariaDB.
     */
    fun applyMigrations(vararg migrationSqls: String) {
        executeAsync(
            "CREATE TABLE IF NOT EXISTS zen_migrations (version INT PRIMARY KEY, applied_at BIGINT NOT NULL);"
        ).join()

        val applied = queryAsync("SELECT version FROM zen_migrations") { rs -> rs.getInt("version") }.join().toSet()

        for ((index, sql) in migrationSqls.withIndex()) {
            val version = index + 1
            if (!applied.contains(version)) {
                withTransactionSync { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute(sql)
                    }
                    conn.prepareStatement("INSERT INTO zen_migrations (version, applied_at) VALUES (?, ?);").use { ps ->
                        ps.setInt(1, version)
                        ps.setLong(2, System.currentTimeMillis())
                        ps.executeUpdate()
                    }
                }
            }
        }
    }

    fun close() {
        val ds = hikariDataSource
        if (ds != null) {
            runCatching { ds.javaClass.getMethod("close").invoke(ds) }
            hikariDataSource = null
        }
        synchronized(connectionLock) {
            runCatching { singleConnection?.close() }
            singleConnection = null
        }
        poolExecutor.shutdown()
    }
}
