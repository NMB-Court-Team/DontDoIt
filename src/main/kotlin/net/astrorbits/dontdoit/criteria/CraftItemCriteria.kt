package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.InventoryItemInspectCandidate
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

class CraftItemCriteria : Criteria(), Listener, InventoryItemInspectCandidate {
    override val type: CriteriaType = CriteriaType.CRAFT_ITEM
    lateinit var itemTypes: Set<Material>
    var isWildcard: Boolean = false

    override fun getCandidateInventoryItemTypes(): Set<Material> {
        return itemTypes.flatMap { material ->
            val item = ItemStack.of(material)
            Bukkit.getRecipesFor(item).flatMap { getRecipeIngredient(it) }
        }.toSet()
    }

    override fun canMatchAnyInventoryItem(): Boolean {
        return isWildcard
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setItemTypes(ITEM_TYPES_KEY) { itemTypes, isWildcard ->
            this.itemTypes = itemTypes
            this.isWildcard = isWildcard
        }
    }

    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        val item = event.recipe.result
        val clicker = event.whoClicked
        if (clicker is Player && (isWildcard || item.type in itemTypes)) {
            trigger(clicker)
        }
    }

    companion object {
        const val ITEM_TYPES_KEY = "item"

        fun getRecipeIngredient(recipe: Recipe): Set<Material> {
            return when (recipe) {
                is ShapedRecipe -> recipe.choiceMap.values.flatMap(::getChoiceMaterials)
                is ShapelessRecipe -> recipe.choiceList.flatMap(::getChoiceMaterials)
                else -> emptyList()
            }.toSet()
        }

        fun getChoiceMaterials(choice: RecipeChoice): Collection<Material> {
            return when (choice) {
                is MaterialChoice -> choice.choices
                is ExactChoice -> choice.choices.map { it.type }
                else -> emptyList()
            }
        }
    }
}