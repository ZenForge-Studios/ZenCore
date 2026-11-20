package com.cuac_xd.zencore.config

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader

/**
 * Gestor de archivos de configuración YAML con migración y actualización automática de claves ausentes.
 * Compara el archivo en disco del servidor con el recurso original interno embebido en el JAR.
 * Si el desarrollador agrega nuevas opciones o secciones en una nueva versión, se inyectan automáticamente
 * sin sobrescribir los valores modificados por el administrador del servidor.
 */
class ZenConfig(
    val file: File,
    val resourceStreamProvider: (() -> java.io.InputStream?)? = null
) {
    var config: YamlConfiguration = YamlConfiguration()
        private set

    init {
        reload()
    }

    fun reload() {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            val stream = resourceStreamProvider?.invoke()
            if (stream != null) {
                file.outputStream().use { out -> stream.copyTo(out) }
            } else {
                file.createNewFile()
            }
        }

        config = YamlConfiguration.loadConfiguration(file)

        // Comprobar y fusionar claves nuevas del default
        val defStream = resourceStreamProvider?.invoke()
        if (defStream != null) {
            val defConfig = YamlConfiguration.loadConfiguration(InputStreamReader(defStream))
            var changed = false
            for (key in defConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defConfig.get(key))
                    changed = true
                }
            }
            if (changed) {
                config.save(file)
            }
        }
    }

    fun save() {
        config.save(file)
    }

    fun getString(path: String, def: String = ""): String = config.getString(path, def) ?: def

    fun getInt(path: String, def: Int = 0): Int = config.getInt(path, def)

    fun getDouble(path: String, def: Double = 0.0): Double = config.getDouble(path, def)

    fun getBoolean(path: String, def: Boolean = false): Boolean = config.getBoolean(path, def)

    fun getStringList(path: String): List<String> = config.getStringList(path)

    fun set(path: String, value: Any?) {
        config.set(path, value)
    }
}
