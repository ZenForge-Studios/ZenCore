package com.cuac_xd.zencore.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

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
        if (text.contains("<") && text.contains(">")) {
            try {
                return miniMessage.deserialize(text)
            } catch (_: Exception) {}
        }
        if (text.contains("&")) {
            return ampersandSerializer.deserialize(text)
        }
        if (text.contains("§")) {
            return sectionSerializer.deserialize(text)
        }
        return Component.text(text)
    }

    @JvmStatic
    fun toLegacy(component: Component): String = sectionSerializer.serialize(component)

    @JvmStatic
    fun miniMessage(): MiniMessage = miniMessage
}
