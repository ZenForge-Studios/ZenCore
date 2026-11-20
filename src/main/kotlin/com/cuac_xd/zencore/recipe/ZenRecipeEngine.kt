package com.cuac_xd.zencore.recipe

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.Plugin

/**
 * Motor declarativo de recetas de crafteo para ítems del ecosistema Zen.
 */
object ZenRecipeEngine {

    /**
     * Registra una receta con forma (Shaped).
     * Ejemplo:
     * ```kotlin
     * ZenRecipeEngine.registerShaped(plugin, "spawner_pickaxe", resultItem,
     *     shape = arrayOf("DDD", " S ", " S "),
     *     ingredients = mapOf('D' to Material.DIAMOND, 'S' to Material.STICK)
     * )
     * ```
     */
    @JvmStatic
    fun registerShaped(
        plugin: Plugin,
        id: String,
        result: ItemStack,
        shape: Array<String>,
        ingredients: Map<Char, Any>
    ): ShapedRecipe {
        val key = NamespacedKey(plugin, id)
        // Remover si ya existía para permitir recargas en vivo
        runCatching { Bukkit.removeRecipe(key) }

        val recipe = ShapedRecipe(key, result)
        recipe.shape(*shape)
        for ((char, ing) in ingredients) {
            when (ing) {
                is Material -> recipe.setIngredient(char, ing)
                is ItemStack -> recipe.setIngredient(char, RecipeChoice.ExactChoice(ing))
                is RecipeChoice -> recipe.setIngredient(char, ing)
            }
        }
        Bukkit.addRecipe(recipe)
        return recipe
    }

    /**
     * Registra una receta sin forma (Shapeless).
     */
    @JvmStatic
    fun registerShapeless(
        plugin: Plugin,
        id: String,
        result: ItemStack,
        ingredients: List<Any>
    ): ShapelessRecipe {
        val key = NamespacedKey(plugin, id)
        runCatching { Bukkit.removeRecipe(key) }

        val recipe = ShapelessRecipe(key, result)
        for (ing in ingredients) {
            when (ing) {
                is Material -> recipe.addIngredient(ing)
                is ItemStack -> recipe.addIngredient(RecipeChoice.ExactChoice(ing))
                is RecipeChoice -> recipe.addIngredient(ing)
            }
        }
        Bukkit.addRecipe(recipe)
        return recipe
    }
}
