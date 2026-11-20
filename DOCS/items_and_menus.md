# 🛠️ Creación de Ítems y Sistema Completo de Menús en ZenCore

Inspirado en la arquitectura de **Eco** y **Libreforge** (`ItemStackBuilder`, `Menu`, `FillerMask`, `Slot`), **ZenCore** incluye ahora dos subsistemas centrales para que ningún plugin hermano tenga que reinventar la rueda:

1. **Motor Fluido de Creación de Ítems (`ItemBuilder` & `ItemParser`)**
2. **Motor Completo de Menús Interactivos (`ZenMenu`, `MenuSlot`, `MenuMask`, `MenuParser`)**

---

## 🗡️ 1. Creación y Manipulación de Ítems

### 1.1. `ItemBuilder` (Programático en Kotlin / Java)
Permite construir cualquier `ItemStack` con sintaxis fluida y soporte directo para MiniMessage, texturas de cabezas, armaduras de cuero coloreadas, CustomModelData, PDC y efectos.

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag

// Crear un item desde cero
val spawnerWand = ZenCoreAPI.createItem(Material.BLAZE_ROD)
    .name("<gradient:#ff9900:#ff0055><b>Vara de Spawner Mágica</b></gradient>")
    .lore(
        "<gray>Recoge spawners con un solo clic.",
        "<dark_gray>-------------------------",
        "<yellow>⚡ Cargas restantes: <white>5/5",
        "<gold>★ Rareza: <light_purple>Mítica"
    )
    .customModelData(1005)
    .glow(true)
    .unbreakable(true)
    .flag(ItemFlag.HIDE_ATTRIBUTES)
    .build()

// Crear una cabeza con textura Base64 o URL de skin
val customHead = ZenCoreAPI.createItem(Material.PLAYER_HEAD)
    .name("<green>Cabeza de Gólem")
    .skullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODlhOGFjNzhmMWExYTQ5...")
    .build()

// Editar un ItemStack existente
val upgraded = ZenCoreAPI.editItem(player.inventory.itemInMainHand)
    .addLore("<green>✔ Mejorado a Nivel 2")
    .build()
```

### 1.2. `ItemParser` (Desde Strings o YAML)
Permite deserializar items de configuraciones de forma instantánea:

- **String Simple:**
  - `ItemParser.parseSimple("DIAMOND", 64)`
  - `ItemParser.parseSimple("head:Notch")`
  - `ItemParser.parseSimple("head:eyJ0ZXh0dXJlcyI6...")`
  - `ItemParser.parseSimple("custom:SPAWNER_PICKAXE")` *(Resuelve de ZenCustomItems)*

- **Sección YAML Completa:**
```yaml
item:
  material: NETHERITE_SWORD
  amount: 1
  name: "<red>Espada de Sangre"
  lore:
    - "<gray>Inflige daño crítico masivo."
  custom-model-data: 102
  glow: true
  unbreakable: true
  flags:
    - HIDE_ENCHANTS
    - HIDE_ATTRIBUTES
```
```kotlin
val itemStack = ZenCoreAPI.itemParser.parseSection(config.getConfigurationSection("item")!!).build()
```

---

## 🪟 2. Sistema Completo de Menús (`ZenMenu`)

Un framework declarativo con:
- Coordenadas naturales de cuadrícula: **`row` (1..6)** y **`column` (1..9)**.
- **Máscaras y patrones visuales** para bordes y fondos (`MenuMask`).
- **Paginación nativa automática** con botones anterior/siguiente.
- **Ranuras dinámicas reactivas por jugador** (`PlaceholderAPI` evaluado al abrir o refrescar).
- **Ranuras cautivas** (`setCaptive(true)`) para que los jugadores depositen items.
- Refresco en vivo sin reabrir (`menu.refresh(player)`).

### 2.1. Menú Programático con Paginación y Máscara
```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.gui.MenuMask
import com.cuac_xd.zencore.gui.MenuSlot
import org.bukkit.Material
import org.bukkit.entity.Player

fun openSpawnersMenu(player: Player, spawnerList: List<ItemStack>) {
    val menu = ZenCoreAPI.createMenu(rows = 6, title = "<gradient:#00e5ff:#1200ff><b>Panel de Spawners</b></gradient>")

    // 1. Aplicar Máscara de fondo
    val borderItem = ZenCoreAPI.createItem(Material.BLACK_STAINED_GLASS_PANE).name(" ").build()
    menu.applyMask(
        MenuMask(
            listOf(
                "xxxxxxxxx",
                "x.......x",
                "x.......x",
                "x.......x",
                "x.......x",
                "xxxxxxxxx"
            ),
            mapOf('x' to borderItem)
        )
    )

    // 2. Configurar Área de Paginación (filas 2 a 5, columnas 2 a 8 = 28 slots por página)
    menu.setPagedArea(
        startRow = 2,
        startColumn = 2,
        width = 7,
        height = 4,
        itemsProvider = { p -> spawnerList }
    ) { p, clickedItem, index ->
        p.sendMessage("§aHas seleccionado el spawner #${index + 1}!")
    }

    // 3. Botón de Página Anterior (Fila 6, Columna 3)
    menu.setPreviousPageButton(row = 6, column = 3) { p, hasPrev ->
        ZenCoreAPI.createItem(if (hasPrev) Material.ARROW else Material.GRAY_DYE)
            .name(if (hasPrev) "<yellow>« Página Anterior" else "<dark_gray>No hay más páginas")
            .build()
    }

    // 4. Botón de Página Siguiente (Fila 6, Columna 7)
    menu.setNextPageButton(row = 6, column = 7) { p, hasNext ->
        ZenCoreAPI.createItem(if (hasNext) Material.ARROW else Material.GRAY_DYE)
            .name(if (hasNext) "<yellow>Página Siguiente »" else "<dark_gray>No hay más páginas")
            .build()
    }

    // 5. Botón de Cierre (Fila 6, Columna 5)
    menu.setItem(
        row = 6, column = 5,
        item = ZenCoreAPI.createItem(Material.BARRIER).name("<red><b>Cerrar</b>").build()
    ) { p, _ -> p.closeInventory() }

    // Abrir al jugador
    menu.open(player)
}
```

---

## 📄 3. Cargar Menús Completos desde Archivos YAML (`MenuParser`)

Puedes delegar toda la estructura del menú a un archivo YAML:

### `menus/spawner_settings.yml`:
```yaml
title: "<gradient:#ffaa00:#ff5500>Configuración de Spawner</gradient>"
rows: 5

mask:
  pattern:
    - "xxxxxxxxx"
    - "x.......x"
    - "x...#...x"
    - "x.......x"
    - "xxxxxxxxx"
  items:
    x: "GRAY_STAINED_GLASS_PANE"
    "#": "NETHER_STAR"

items:
  speed_upgrade:
    row: 3
    column: 3
    material: SUGAR
    name: "<aqua><b>Mejora de Velocidad</b></aqua>"
    lore:
      - "<gray>Aumenta la frecuencia de generación."
      - "<yellow>Costo: <white>500 Monedas"
    action: "UPGRADE_SPEED"

  drop_upgrade:
    row: 3
    column: 7
    material: GOLD_INGOT
    name: "<gold><b>Mejora de Drops x2</b></gold>"
    lore:
      - "<gray>Duplica los drops generados."
      - "<yellow>Costo: <white>1000 Monedas"
    action: "UPGRADE_DROPS"
```

### Cargar y manejar en código con 1 sola línea:
```kotlin
val menuConfig = YamlConfiguration.loadConfiguration(file)

val menu = ZenCoreAPI.menuParser.parse(menuConfig) { player, actionId, m ->
    when (actionId) {
        "UPGRADE_SPEED" -> {
            player.sendMessage("§a¡Velocidad mejorada!")
            m.refresh(player)
        }
        "UPGRADE_DROPS" -> {
            player.sendMessage("§6¡Drops duplicados!")
            m.refresh(player)
        }
    }
}

menu.open(player)
```

---

## 🚀 Ventajas para Plugins como `ZenSpawner`
- **Sin dependencias externas pesadas:** No requiere librerías secundarias (IF, Triumph-GUI, FastInv). Todo está optimizado en Paper/Kotlin.
- **Ecosistema integrado:** Soporta resolver de forma transparente items de `ZenCustomItems` y divisas de `ZenEconomy` directamente dentro de cualquier slot o menú.
