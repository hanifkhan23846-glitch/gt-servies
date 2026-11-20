package com.example

import com.example.ui.components.foodCategoryDrawableRes
import com.example.ui.components.foodCategoryImageUri
import com.example.ui.components.restaurantDrawableRes
import com.example.ui.components.restaurantImageUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FoodImageMappingTest {

    @Test
    fun allRequestedFoodCategories_resolveToDedicatedRealDrawables() {
        val cases = listOf(
            ("Pizza" to "Chicken Tikka Crown Pizza (Large)") to "res://img_food_pizza",
            ("Burger" to "Crispy Zinger Crunch Burger") to "res://img_food_burger",
            ("Biryani" to "Special Sindhi Chicken Biryani") to "res://img_food_biryani",
            ("BBQ" to "Seekh Kabab & Malai Boti Platter") to "res://img_food_bbq",
            ("Chinese" to "Chicken Manchurian & Egg Fried Rice") to "res://img_food_chinese",
            ("Fast Food" to "Crispy Chicken Broast & Fries Combo") to "res://img_food_fastfood",
            ("Pakistani Food" to "Lahori Chicken Karahi (Half)") to "res://img_food_pakistani",
            ("Shawarma" to "Arabic Garlic Chicken Shawarma") to "res://img_food_shawarma",
            ("Desserts" to "Warm Gulab Jamun & Pistachio Kheer") to "res://img_food_desserts",
            ("Drinks" to "Fresh Mint Margarita & Mango Lassi") to "res://img_food_drinks"
        )

        val resolvedDrawables = cases.map { (input, expectedUri) ->
            val (category, itemName) = input
            assertEquals(expectedUri, foodCategoryImageUri(category, itemName))
            val resId = foodCategoryDrawableRes(category, itemName)
            assertNotEquals(R.drawable.img_hero_food, resId)
            resId
        }

        // All 10 requested food categories resolve to 10 distinct real drawable assets
        assertEquals(10, resolvedDrawables.distinct().size)
    }

    @Test
    fun allRestaurants_resolveToDedicatedRealDrawablesAndUpgradeLegacyPlaceholder() {
        val restaurantCases = listOf(
            "Royal Biryani & BBQ" to "res://img_rest_royal_biryani",
            "Baghdad Pizza & Grill" to "res://img_rest_baghdad_grill",
            "Sadiq Spice Kitchen" to "res://img_rest_sadiq_spice",
            "Noor Mahal Karahi & Cafe" to "res://img_rest_noor_mahal"
        )

        val drawables = restaurantCases.map { (name, expectedUri) ->
            assertEquals(expectedUri, restaurantImageUri(name, null))
            assertEquals(expectedUri, restaurantImageUri(name, "res://img_hero_food"))
            val resId = restaurantDrawableRes(name, "res://img_hero_food")
            assertNotEquals(R.drawable.img_hero_food, resId)
            resId
        }

        assertEquals(4, drawables.distinct().size)
    }
}
