package com.cuac_xd.zencore.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

/**
 * High-performance Adventure API utilities for Paper plugins.
 */
object AdventureUtil {
    private val miniMessage = MiniMessage.miniMessage()
    private val ampersandSerializer = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build()
    private val sectionSerializer = LegacyComponentSerializer.builder()
        .character('§')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build()

    @JvmStatic
    fun parse(text: String?): Component {
        if (text.isNullOrEmpty()) return Component.empty()
        val comp = when {
            text.contains("<") && text.contains(">") -> {
                try {
                    miniMessage.deserialize(text)
                } catch (_: Exception) {
                    Component.text(text)
                }
            }
            text.contains("&") -> ampersandSerializer.deserialize(text)
            text.contains("§") -> sectionSerializer.deserialize(text)
            else -> Component.text(text)
        }
        return comp.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }

    @JvmStatic
    fun sendMessage(player: Player?, message: String?) {
        if (player == null || message.isNullOrBlank()) return
        player.sendMessage(parse(message))
    }

    @JvmStatic
    fun sendActionbar(player: Player?, message: String?) {
        if (player == null || message.isNullOrBlank()) return
        player.sendActionBar(parse(message))
    }

    @JvmStatic
    fun sendTitle(player: Player?, title: String?, subtitle: String?, fadeInTicks: Int = 10, stayTicks: Int = 40, fadeOutTicks: Int = 10) {
        if (player == null) return
        val t = Title.title(
            parse(title ?: ""),
            parse(subtitle ?: ""),
            Title.Times.times(
                Duration.ofMillis((fadeInTicks * 50).toLong()),
                Duration.ofMillis((stayTicks * 50).toLong()),
                Duration.ofMillis((fadeOutTicks * 50).toLong())
            )
        )
        player.showTitle(t)
    }

    @JvmStatic
    fun toLegacy(component: Component): String = sectionSerializer.serialize(component)

    @JvmStatic
    fun miniMessage(): MiniMessage = miniMessage
}
