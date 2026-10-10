package com.cuac_xd.zencore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for working with Adventure Components and formatting.
 * Bridges MiniMessage and legacy section/ampersand formats seamlessly.
 */
public final class AdventureUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private AdventureUtil() {}

    @NotNull
    public static Component parse(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        if (text.contains("<") && text.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(text);
            } catch (Exception ignored) {
                // Fallback to legacy if malformed MiniMessage tag
            }
        }
        if (text.contains("&")) {
            return AMPERSAND_SERIALIZER.deserialize(text);
        }
        if (text.contains("§")) {
            return SECTION_SERIALIZER.deserialize(text);
        }
        return Component.text(text);
    }

    @NotNull
    public static String toLegacy(@NotNull Component component) {
        return SECTION_SERIALIZER.serialize(component);
    }

    @NotNull
    public static MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }
}
