package com.cuac_xd.zencore.message

import com.cuac_xd.zencore.integration.IntegrationHub
import com.cuac_xd.zencore.util.AdventureUtil
import com.cuac_xd.zencore.util.MinecraftAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.time.Duration
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor avanzado de localización y mensajes multi-canal.
 * Soporta textos simples, listas multilínea, ActionBars, Titles y Sonidos contextuales.
 */
class ZenMessageService(
    var prefix: String = ""
) {
    private val messages = ConcurrentHashMap<String, Any>()

    fun loadFromYaml(file: File) {
        if (!file.exists()) return
        val config = YamlConfiguration.loadConfiguration(file)
        loadFromConfig(config)
    }

    fun loadFromConfig(config: YamlConfiguration) {
        if (config.contains("prefix")) {
            prefix = config.getString("prefix") ?: ""
        }
        for (key in config.getKeys(true)) {
            val value = config.get(key)
            if (value != null && value !is org.bukkit.configuration.ConfigurationSection) {
                messages[key] = value
            }
        }
    }

    fun set(key: String, message: Any) {
        messages[key] = message
    }

    /**
     * Envía un mensaje configurado reemplazando variables y Placeholders.
     */
    fun send(target: CommandSender, key: String, placeholders: Map<String, Any> = emptyMap()) {
        val player = target as? Player

        // Comprobar si es un mensaje compuesto (Actionbar, Title o Sound)
        val soundKey = "$key.sound"
        val actionbarKey = "$key.actionbar"
        val titleKey = "$key.title"
        val subtitleKey = "$key.subtitle"

        if (messages.containsKey(soundKey) && player != null) {
            playSound(player, messages[soundKey]?.toString() ?: "")
        }

        if (messages.containsKey(actionbarKey) && player != null) {
            val text = format(messages[actionbarKey]?.toString() ?: "", player, placeholders, usePrefix = false)
            player.sendActionBar(AdventureUtil.parse(text))
        }

        if ((messages.containsKey(titleKey) || messages.containsKey(subtitleKey)) && player != null) {
            val titleText = format(messages[titleKey]?.toString() ?: "", player, placeholders, usePrefix = false)
            val subText = format(messages[subtitleKey]?.toString() ?: "", player, placeholders, usePrefix = false)
            player.showTitle(
                Title.title(
                    AdventureUtil.parse(titleText),
                    AdventureUtil.parse(subText),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(300))
                )
            )
        }

        // Mensaje de chat principal
        val rawMessage = messages[key] ?: messages["$key.message"] ?: return
        if (rawMessage is List<*>) {
            for (line in rawMessage) {
                val formatted = format(line.toString(), player, placeholders, usePrefix = false)
                target.sendMessage(AdventureUtil.parse(formatted))
            }
        } else {
            val formatted = format(rawMessage.toString(), player, placeholders, usePrefix = true)
            target.sendMessage(AdventureUtil.parse(formatted))
        }
    }

    fun format(text: String, player: Player?, placeholders: Map<String, Any>, usePrefix: Boolean = true): String {
        var result = if (usePrefix && prefix.isNotBlank() && !text.contains("<no-prefix>")) {
            "$prefix $text"
        } else {
            text.replace("<no-prefix>", "")
        }

        for ((k, v) in placeholders) {
            result = result.replace("%$k%", v.toString())
            result = result.replace("{$k}", v.toString())
        }

        if (player != null) {
            result = result.replace("%player%", player.name)
            result = IntegrationHub.setPlaceholders(player, result)
        }
        return result
    }

    private fun playSound(player: Player, soundString: String) {
        runCatching {
            val parts = soundString.split(":")
            val soundName = parts[0].trim()
            val pitch = parts.getOrNull(1)?.toFloatOrNull() ?: 1.0f
            val sound = MinecraftAdapter.resolveSound(soundName) ?: return
            player.playSound(player.location, sound, 1.0f, pitch)
        }
    }
}
