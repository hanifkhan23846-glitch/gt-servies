package com.example

import com.example.ui.components.groceryCategoryDrawableRes
import com.example.ui.components.groceryCategoryImageUri
import com.example.ui.components.groceryStoreDrawableRes
import com.example.ui.components.groceryStoreImageUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GroceryImageMappingTest {

    @Test
    fun allRequestedGroceryCategories_resolveToDedicatedRealDrawables() {
        val cases = listOf(
            ("Fruits" to "") to "res://img_groc_fruits",
            ("Vegetables" to "") to "res://img_groc_vegetables",
            ("Milk and Eggs" to "") to "res://img_groc_milk",
            ("Dairy Products" to "") to "res://img_groc_dairy",
            ("Meat" to "") to "res://img_groc_meat",
            ("Chicken" to "") to "res://img_groc_chicken",
            ("Rice and Flour" to "Super Kernel Basmati Rice") to "res://img_groc_rice",
            ("Rice and Flour" to "Chakki Atta Flour") to "res://img_groc_flour",
            ("Cooking Oil and Ghee" to "Dalda Cooking Oil") to "res://img_groc_oil",
            ("Cooking Oil and Ghee" to "Pure Desi Ghee") to "res://img_groc_ghee",
            ("Spices" to "") to "res://img_groc_spices",
            ("Bakery Items" to "") to "res://img_groc_bakery",
            ("Beverages" to "") to "res://img_groc_beverages",
            ("Snacks" to "") to "res://img_groc_snacks",
            ("Frozen Foods" to "") to "res://img_groc_frozen",
            ("Personal Care" to "") to "res://img_groc_personal_care",
            ("Household Cleaning" to "") to "res://img_groc_household"
        )

        val resolvedDrawables = cases.map { (input, expectedUri) ->
            val (category, product) = input
            assertEquals(expectedUri, groceryCategoryImageUri(category, product))
            val resId = groceryCategoryDrawableRes(category, product)
            assertNotEquals(R.drawable.img_hero_grocery, resId)
            resId
        }

        // All 17 requested grocery concepts map to 17 distinct real drawable assets
        assertEquals(17, resolvedDrawables.distinct().size)
    }

    @Test
    fun allGroceryStores_resolveToDedicatedRealDrawablesAndUpgradeLegacyPlaceholder() {
        val storeCases = listOf(
            "Al-Madina Fresh Mart" to "res://img_store_almadina",
            "Victoria Bazaar Grocers" to "res://img_store_victoria",
            "Bahawalpur Mega Mart" to "res://img_store_mega_mart",
            "Green Basket Organic Store" to "res://img_store_green_basket",
            "Chenab Cash & Carry" to "res://img_store_chenab"
        )

        val drawables = storeCases.map { (name, expectedUri) ->
            assertEquals(expectedUri, groceryStoreImageUri(name, null))
            assertEquals(expectedUri, groceryStoreImageUri(name, "res://img_hero_grocery"))
            val resId = groceryStoreDrawableRes(name, "res://img_hero_grocery")
            assertNotEquals(R.drawable.img_hero_grocery, resId)
            resId
        }

        assertEquals(5, drawables.distinct().size)
    }
}
