# 📜 05. Motor de Recetas de Crafteo

El módulo `ZenRecipeEngine` facilita el registro de recetas de crafteo personalizadas en la mesa de trabajo (`Shaped` y `Shapeless`). Soporta tanto materiales vanilla como ítems personalizados creados con `ItemBuilder` o `ZenCustomItems`.

---

## 💻 Registro de Recetas con Forma (`Shaped`)

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import org.bukkit.Material

// 1. Crear el ítem resultante
val spawnerWand = ZenCoreAPI.createItem(Material.BLAZE_ROD)
    .name("<gold><b>Vara de Spawner</b></gold>")
    .glow(true)
    .build()

// 2. Registrar la receta (con soporte para reemplazo en vivo al recargar el plugin)
ZenCoreAPI.recipes.registerShaped(
    plugin = myPlugin,
    id = "spawner_wand_recipe",
    result = spawnerWand,
    shape = arrayOf(
        " N ",
        " B ",
        " S "
    ),
    ingredients = mapOf(
        'N' to Material.NETHER_STAR,
        'B' to Material.BLAZE_ROD,
        'S' to Material.DIAMOND
    )
)
```

---

## 💻 Registro de Recetas sin Forma (`Shapeless`)

```kotlin
// Desarmar o craftear fragmentos sin posición fija
val spawnerFragment = ZenCoreAPI.createItem(Material.PRISMARINE_SHARD)
    .name("<aqua>Fragmento de Spawner")
    .build()

ZenCoreAPI.recipes.registerShapeless(
    plugin = myPlugin,
    id = "spawner_shard_recipe",
    result = spawnerFragment,
    ingredients = listOf(
        Material.IRON_BLOCK,
        Material.REDSTONE,
        Material.AMETHYST_SHARD
    )
)
```
