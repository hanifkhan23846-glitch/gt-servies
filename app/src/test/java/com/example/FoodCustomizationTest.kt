package com.example

import com.example.data.model.FoodItem
import com.example.ui.screens.buildFoodCustomizationConfig
import com.example.ui.screens.formatFoodCustomizationNote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodCustomizationTest {
    @Test
    fun pizzaConfigHasSizeAndDipsAndAddons() {
        val pizza = FoodItem(
            id = "pizza_1",
            name = "Chicken Fajita Pizza",
            categoryName = "Pizza",
            regularPricePkr = 1200.0,
            discountPricePkr = 950.0
        )
        val config = buildFoodCustomizationConfig(pizza)
        assertEquals("Size & Crust", config.styleSectionTitle)
        assertEquals("Dips", config.dipSectionTitle)
        assertTrue(config.styleOptions.any { it.extraPricePkr > 0.0 })
        assertTrue(config.dipOptions.any { it.label.contains("Garlic", ignoreCase = true) })
        assertTrue(config.addonOptions.size >= 4)
        assertTrue(config.quickChoiceDipId.isNotBlank())
    }

    @Test
    fun formatCustomizationNoteIncludesSelectedExtrasAndInstructions() {
        val pizza = FoodItem(id = "p2", name = "Crown Crust Pizza", categoryName = "Pizza")
        val config = buildFoodCustomizationConfig(pizza)
        val style = config.styleOptions.first { it.id == "large_crust" }
        val dip = config.dipOptions.first { it.id == "garlic_dip" }
        val addons = config.addonOptions.take(2)
        val note = formatFoodCustomizationNote(
            selectedStyle = style,
            selectedDip = dip,
            selectedAddons = addons,
            instructions = "no jalapenos please",
            unavailablePreference = "Call me (03036767152)"
        )
        assertTrue(note.contains("Large Crown / Pan"))
        assertTrue(note.contains("Garlic Dip"))
        assertTrue(note.contains("Add:"))
        assertTrue(note.contains("Note: no jalapenos please"))
        assertTrue(note.contains("If unavailable: Call me (03036767152)"))
    }

    @Test
    fun dessertAndDrinksHaveTailoredCustomizationSections() {
        val dessert = FoodItem(id = "d1", name = "Molten Lava Cake", categoryName = "Desserts")
        val drink = FoodItem(id = "dr1", name = "Mint Margarita", categoryName = "Drinks")
        assertEquals("Sweet Toppings", buildFoodCustomizationConfig(dessert).dipSectionTitle)
        assertEquals("Flavor Boost", buildFoodCustomizationConfig(drink).dipSectionTitle)
    }
}
