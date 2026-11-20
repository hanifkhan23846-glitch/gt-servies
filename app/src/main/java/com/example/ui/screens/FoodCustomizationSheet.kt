package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FoodItem
import com.example.data.model.GharTakConstants
import com.example.ui.components.DiscountBadge
import com.example.ui.components.GharTakImage
import com.example.ui.components.foodCategoryImageUri
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCharcoal
import com.example.ui.theme.GharTakPink

data class FoodOptionChoice(
    val id: String,
    val label: String,
    val extraPricePkr: Double = 0.0,
    val imageUri: String = ""
)

data class FoodCustomizationConfig(
    val styleSectionTitle: String,
    val styleOptions: List<FoodOptionChoice>,
    val dipSectionTitle: String,
    val dipOptions: List<FoodOptionChoice>,
    val addonOptions: List<FoodOptionChoice>,
    val quickChoiceStyleId: String,
    val quickChoiceDipId: String,
    val quickChoiceAddonIds: Set<String>
)

internal fun buildFoodCustomizationConfig(item: FoodItem): FoodCustomizationConfig {
    val cat = item.categoryName.trim().lowercase()
    val nameLower = item.name.trim().lowercase()

    val styleSection: Pair<String, List<FoodOptionChoice>> = when {
        cat.contains("pizza") || nameLower.contains("pizza") -> "Size & Crust" to listOf(
            FoodOptionChoice("reg_crust", "Regular Pan (Medium)", 0.0),
            FoodOptionChoice("large_crust", "Large Crown / Pan", 350.0),
            FoodOptionChoice("cheese_crust", "Cheese Stuffed Crust", 280.0),
            FoodOptionChoice("thin_crust", "Crispy Thin Crust", 0.0)
        )
        cat.contains("biryani") || nameLower.contains("biryani") -> "Serving & Spice" to listOf(
            FoodOptionChoice("single_med", "Single Plate • Medium Spice", 0.0),
            FoodOptionChoice("single_spicy", "Single Plate • Extra Masala Spicy", 0.0),
            FoodOptionChoice("full_handi", "Full Family Handi (Double)", 420.0)
        )
        cat.contains("pakistani") || nameLower.contains("karahi") || nameLower.contains("nihari") -> "Portion & Style" to listOf(
            FoodOptionChoice("half_classic", "Half Portion • Classic Spice", 0.0),
            FoodOptionChoice("peshawari", "Green Chili Peshawari Style", 50.0),
            FoodOptionChoice("full_desighee", "Full Karahi • Desi Ghee", 550.0)
        )
        cat.contains("bbq") || nameLower.contains("tikka") || nameLower.contains("kabab") -> "Grill & Spice" to listOf(
            FoodOptionChoice("charcoal_med", "Charcoal Grilled • Medium", 0.0),
            FoodOptionChoice("malai_mild", "Creamy Malai • Mild", 60.0),
            FoodOptionChoice("extra_skewer", "Add Extra Seekh / Tikka", 260.0)
        )
        cat.contains("burger") || cat.contains("fast") || nameLower.contains("zinger") -> "Spice & Style" to listOf(
            FoodOptionChoice("crispy_med", "Classic Crispy • Medium", 0.0),
            FoodOptionChoice("fiery_spicy", "Hot & Spicy Zinger Glaze", 0.0),
            FoodOptionChoice("double_cheese", "Double Melt Cheese Slice", 160.0)
        )
        cat.contains("shawarma") || nameLower.contains("shawarma") -> "Wrap Style" to listOf(
            FoodOptionChoice("classic_pita", "Classic Pita Wrap", 0.0),
            FoodOptionChoice("arabic_platter", "Jumbo Arabic Open Platter", 240.0),
            FoodOptionChoice("cheesy_wrap", "Cheesy Jalapeno Wrap", 110.0)
        )
        cat.contains("chinese") || nameLower.contains("manchurian") || nameLower.contains("chow") -> "Serving & Spice" to listOf(
            FoodOptionChoice("bowl_reg", "Regular Bowl • Balanced Spice", 0.0),
            FoodOptionChoice("chili_garlic", "Extra Chili Garlic Style", 0.0),
            FoodOptionChoice("combo_rice", "Combo with Egg Fried Rice", 260.0)
        )
        cat.contains("dessert") || nameLower.contains("cake") || nameLower.contains("kheer") || nameLower.contains("gulab") -> "Portion" to listOf(
            FoodOptionChoice("single_sweet", "Single Serving", 0.0),
            FoodOptionChoice("warm_pistachio", "Warm with Pistachio Garnish", 80.0),
            FoodOptionChoice("family_box", "Family Celebration Box", 450.0)
        )
        cat.contains("drink") || nameLower.contains("lassi") || nameLower.contains("shake") || nameLower.contains("margarita") -> "Size & Ice" to listOf(
            FoodOptionChoice("chilled_reg", "Regular Chilled", 0.0),
            FoodOptionChoice("large_cup", "Large 500ml Cup", 90.0),
            FoodOptionChoice("less_ice", "Less Ice • Light Sugar", 0.0)
        )
        else -> "Spice / Preparation" to listOf(
            FoodOptionChoice("mild", "Mild", 0.0),
            FoodOptionChoice("medium", "Medium", 0.0),
            FoodOptionChoice("spicy", "Spicy", 0.0)
        )
    }

    val dipSection: Pair<String, List<FoodOptionChoice>> = when {
        cat.contains("dessert") -> "Sweet Toppings" to listOf(
            FoodOptionChoice("rabri", "Rich Rabri Drizzle", 0.0),
            FoodOptionChoice("choc_fudge", "Warm Chocolate Sauce", 40.0),
            FoodOptionChoice("no_topping", "Plain / No Topping", 0.0)
        )
        cat.contains("drink") -> "Flavor Boost" to listOf(
            FoodOptionChoice("fresh_mint", "Fresh Mint & Lemon", 0.0),
            FoodOptionChoice("chia_seeds", "Basil / Tukhm-e-Malanga", 30.0),
            FoodOptionChoice("plain_drink", "Standard Recipe", 0.0)
        )
        else -> "Dips" to listOf(
            FoodOptionChoice("garlic_dip", "Garlic Dip", 0.0),
            FoodOptionChoice("mint_raita", "Mint Raita", 0.0),
            FoodOptionChoice("chili_garlic_dip", "Chili Garlic Sauce", 0.0),
            FoodOptionChoice("special_mayo", "Chef's Smokey Mayo", 30.0),
            FoodOptionChoice("no_dip", "No Dip", 0.0)
        )
    }

    val addons: List<FoodOptionChoice> = when {
        cat.contains("dessert") -> listOf(
            FoodOptionChoice("vanilla_scoop", "Vanilla Ice Cream Scoop", 140.0, "res://img_food_desserts"),
            FoodOptionChoice("pistachio_crush", "Crushed Pistachio & Almond", 110.0, "res://img_food_desserts"),
            FoodOptionChoice("cold_coffee", "Iced Mocha 300ml", 240.0, "res://img_food_drinks"),
            FoodOptionChoice("extra_gulab", "2 Pcs Warm Gulab Jamun", 160.0, "res://img_food_desserts"),
            FoodOptionChoice("gift_box", "Celebration Candle & Box", 80.0, "res://img_food_desserts")
        )
        cat.contains("drink") -> listOf(
            FoodOptionChoice("ice_cream_float", "Vanilla Ice Cream Float", 130.0, "res://img_food_desserts"),
            FoodOptionChoice("crispy_fries_side", "Regular Crispy Fries", 249.0, "res://img_food_fastfood"),
            FoodOptionChoice("cheese_sticks", "3 Pcs Mozzarella Sticks", 290.0, "res://img_food_fastfood"),
            FoodOptionChoice("mint_lemon_extra", "Extra Mint & Lime Shot", 40.0, "res://img_food_drinks")
        )
        else -> listOf(
            FoodOptionChoice("patty_burger", "Crispy Chicken Patty", 320.0, "res://img_food_burger"),
            FoodOptionChoice("regular_fries", "Regular Crispy Fries", 299.0, "res://img_food_fastfood"),
            FoodOptionChoice("extra_garlic_dip", "Extra Garlic Dip", 70.0, "res://img_food_shawarma"),
            FoodOptionChoice("pepsi_drink", "Chilled Cola 345ml", 120.0, "res://img_food_drinks"),
            FoodOptionChoice("extra_cheese", "Extra Mozzarella Cheese", 160.0, "res://img_food_pizza"),
            FoodOptionChoice("garlic_naan", "Fresh Butter Garlic Naan", 90.0, "res://img_food_pakistani"),
            FoodOptionChoice("coleslaw", "Creamy Fresh Coleslaw", 95.0, "res://img_groc_vegetables")
        )
    }

    val quickStyleId = styleSection.second.firstOrNull()?.id ?: ""
    val quickDipId = dipSection.second.firstOrNull()?.id ?: ""
    val quickAddonId = if (cat.contains("dessert") || cat.contains("drink")) {
        addons.firstOrNull()?.id?.let { setOf(it) } ?: emptySet()
    } else {
        setOfNotNull(addons.firstOrNull { it.id == "pepsi_drink" }?.id)
    }

    return FoodCustomizationConfig(
        styleSectionTitle = styleSection.first,
        styleOptions = styleSection.second,
        dipSectionTitle = dipSection.first,
        dipOptions = dipSection.second,
        addonOptions = addons,
        quickChoiceStyleId = quickStyleId,
        quickChoiceDipId = quickDipId,
        quickChoiceAddonIds = quickAddonId
    )
}

internal fun formatFoodCustomizationNote(
    selectedStyle: FoodOptionChoice?,
    selectedDip: FoodOptionChoice?,
    selectedAddons: List<FoodOptionChoice>,
    instructions: String,
    unavailablePreference: String
): String {
    val parts = mutableListOf<String>()
    selectedStyle?.let { parts += it.label }
    selectedDip?.takeIf { it.id != "no_dip" && it.id != "no_topping" && it.id != "plain_drink" }?.let {
        parts += it.label
    }
    if (selectedAddons.isNotEmpty()) {
        parts += "Add: ${selectedAddons.joinToString(", ") { it.label }}"
    }
    if (instructions.isNotBlank()) {
        parts += "Note: ${instructions.trim()}"
    }
    if (unavailablePreference != "Remove it from my order") {
        parts += "If unavailable: $unavailablePreference"
    }
    return parts.joinToString(" • ")
}

@Composable
fun FoodCustomizationSheet(
    item: FoodItem,
    onDismiss: () -> Unit,
    onAddToCart: (note: String, quantity: Int, extraAddonsPricePkr: Double) -> Unit,
    onOrderNow: (note: String, quantity: Int, extraAddonsPricePkr: Double) -> Unit,
    onOpenRestaurant: (() -> Unit)? = null
) {
    val config = remember(item.id, item.categoryName, item.name) { buildFoodCustomizationConfig(item) }
    val gallery = item.allImages
    val fallbackImage = foodCategoryImageUri(item.categoryName, item.name)
    var activeImage by remember(item) {
        mutableStateOf(gallery.firstOrNull()?.takeUnless { it == "res://img_hero_food" } ?: fallbackImage)
    }

    var selectedStyleId by remember(item.id) { mutableStateOf(config.styleOptions.first().id) }
    var selectedDipId by remember(item.id) { mutableStateOf(config.dipOptions.first().id) }
    var selectedAddonIds by remember(item.id) { mutableStateOf(setOf<String>()) }
    var showAllAddons by remember(item.id) { mutableStateOf(false) }
    var instructions by remember(item.id) { mutableStateOf("") }
    val unavailableOptions = remember {
        listOf(
            "Remove it from my order",
            "Call me (${GharTakConstants.SUPPORT_PHONE_LOCAL})",
            "Restaurant's best recommendation"
        )
    }
    var unavailablePreference by remember(item.id) { mutableStateOf(unavailableOptions.first()) }
    var unavailableExpanded by remember(item.id) { mutableStateOf(false) }
    var quantity by remember(item.id) { mutableIntStateOf(1) }

    val available = item.isAvailable && item.stockQuantity > 0L
    val maxQty = item.stockQuantity.toInt().coerceAtLeast(1)

    val selectedStyle = config.styleOptions.firstOrNull { it.id == selectedStyleId }
    val selectedDip = config.dipOptions.firstOrNull { it.id == selectedDipId }
    val selectedAddons = config.addonOptions.filter { it.id in selectedAddonIds }

    val extraUnitPrice = (selectedStyle?.extraPricePkr ?: 0.0) +
        (selectedDip?.extraPricePkr ?: 0.0) +
        selectedAddons.sumOf { it.extraPricePkr }
    val unitTotal = item.effectivePricePkr + extraUnitPrice
    val grandTotal = unitTotal * quantity

    val quickPresetSelected = selectedStyleId == config.quickChoiceStyleId &&
        selectedDipId == config.quickChoiceDipId &&
        selectedAddonIds == config.quickChoiceAddonIds

    val quickPresetExtraPrice = (config.styleOptions.firstOrNull { it.id == config.quickChoiceStyleId }?.extraPricePkr ?: 0.0) +
        (config.dipOptions.firstOrNull { it.id == config.quickChoiceDipId }?.extraPricePkr ?: 0.0) +
        config.addonOptions.filter { it.id in config.quickChoiceAddonIds }.sumOf { it.extraPricePkr }

    val quickPresetSummary = buildList {
        config.dipOptions.firstOrNull { it.id == config.quickChoiceDipId }?.let { add(it.label) }
        config.addonOptions.filter { it.id in config.quickChoiceAddonIds }.forEach { add(it.label) }
    }.joinToString("\n")

    val visibleAddons = if (showAllAddons || config.addonOptions.size <= 3) {
        config.addonOptions
    } else {
        config.addonOptions.take(3)
    }

    fun buildSummaryNote(): String = formatFoodCustomizationNote(
        selectedStyle = selectedStyle,
        selectedDip = selectedDip,
        selectedAddons = selectedAddons,
        instructions = instructions,
        unavailablePreference = unavailablePreference
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .imePadding(),
                    contentPadding = PaddingValues(bottom = 148.dp)
                ) {
                    // Top Hero Photo + Circular Dismiss Button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(22.dp)
                                    )
                            ) {
                                GharTakImage(
                                    imageUrl = activeImage,
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (item.discountPercent > 0) {
                                    DiscountBadge(
                                        discountPercent = item.discountPercent,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                shadowElevation = 4.dp,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                            ) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("close_food_customization_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close customization",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (gallery.size > 1) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(gallery) { img ->
                                    GharTakImage(
                                        imageUrl = img,
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = if (img == activeImage) 2.dp else 1.dp,
                                                color = if (img == activeImage) GharTakPink else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { activeImage = img }
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Dish Title, Price, Description
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.restaurantName} • ${item.categoryName}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GharTakPink,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (onOpenRestaurant != null) {
                                    TextButton(
                                        onClick = onOpenRestaurant,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Visit Restaurant")
                                    }
                                }
                            }
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = GharTakConstants.formatPkr(item.effectivePricePkr),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GharTakPink
                                )
                                if (item.discountPricePkr in 1.0..(item.regularPricePkr - 0.01)) {
                                    Text(
                                        text = GharTakConstants.formatPkr(item.regularPricePkr),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "• ~${item.preparationTimeMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.description.isNotBlank()) {
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Try these quick choices card (Foodpanda style)
                    item {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Try these quick choices",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "You can edit your choices from the list",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(2.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        width = if (quickPresetSelected) 1.5.dp else 1.dp,
                                        color = if (quickPresetSelected) GharTakPink else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedStyleId = config.quickChoiceStyleId
                                            selectedDipId = config.quickChoiceDipId
                                            selectedAddonIds = config.quickChoiceAddonIds
                                        }
                                        .testTag("quick_choice_preset_card")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocalFireDepartment,
                                                    contentDescription = null,
                                                    tint = GharTakAmber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "500+ orders",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (quickPresetSummary.isNotBlank()) {
                                                Text(
                                                    text = quickPresetSummary,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = GharTakConstants.formatPkr(item.effectivePricePkr + quickPresetExtraPrice),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GharTakPink
                                                )
                                                if (item.discountPricePkr in 1.0..(item.regularPricePkr - 0.01)) {
                                                    Text(
                                                        text = GharTakConstants.formatPkr(item.regularPricePkr + quickPresetExtraPrice),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        textDecoration = TextDecoration.LineThrough,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        RadioButton(
                                            selected = quickPresetSelected,
                                            onClick = {
                                                selectedStyleId = config.quickChoiceStyleId
                                                selectedDipId = config.quickChoiceDipId
                                                selectedAddonIds = config.quickChoiceAddonIds
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = GharTakPink)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 1: Style / Size / Preparation (Required)
                    item {
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(thickness = 6.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        SectionTitleWithBadge(
                            title = config.styleSectionTitle,
                            subtitle = "Select one",
                            badgeText = "Required"
                        )
                    }
                    items(config.styleOptions, key = { "style_${it.id}" }) { opt ->
                        RadioOptionRow(
                            label = opt.label,
                            extraPricePkr = opt.extraPricePkr,
                            selected = selectedStyleId == opt.id,
                            onSelect = { selectedStyleId = opt.id }
                        )
                    }

                    // Section 2: Dips / Sauces (Required)
                    item {
                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(thickness = 6.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        SectionTitleWithBadge(
                            title = config.dipSectionTitle,
                            subtitle = "Select one",
                            badgeText = "Required"
                        )
                    }
                    items(config.dipOptions, key = { "dip_${it.id}" }) { opt ->
                        RadioOptionRow(
                            label = opt.label,
                            extraPricePkr = opt.extraPricePkr,
                            selected = selectedDipId == opt.id,
                            onSelect = { selectedDipId = opt.id }
                        )
                    }

                    // Section 3: Customize Add-Ons (Multi-Select with Thumbnails)
                    item {
                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(thickness = 6.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        SectionTitleWithBadge(
                            title = "Customize",
                            subtitle = "Optional extras • Tap to add",
                            badgeText = "Optional",
                            isRequired = false
                        )
                    }
                    items(visibleAddons, key = { "addon_${it.id}" }) { addon ->
                        val checked = addon.id in selectedAddonIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable {
                                    selectedAddonIds = if (checked) selectedAddonIds - addon.id else selectedAddonIds + addon.id
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GharTakImage(
                                imageUrl = addon.imageUri.ifBlank { fallbackImage },
                                contentDescription = addon.label,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            )
                            Text(
                                text = addon.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "+ ${GharTakConstants.formatPkr(addon.extraPricePkr)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (checked) GharTakPink else MaterialTheme.colorScheme.onSurface
                            )
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedAddonIds = if (isChecked) selectedAddonIds + addon.id else selectedAddonIds - addon.id
                                },
                                colors = CheckboxDefaults.colors(checkedColor = GharTakPink)
                            )
                        }
                    }
                    if (config.addonOptions.size > 3) {
                        item {
                            val remaining = config.addonOptions.size - 3
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clickable { showAllAddons = !showAllAddons }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (showAllAddons) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null
                                )
                                Text(
                                    text = if (showAllAddons) "Show fewer add-ons" else "View $remaining more",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Section 4: Special Instructions (0/500)
                    item {
                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(thickness = 6.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Special instructions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Please let us know if you are allergic to anything or if we need to avoid anything",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = instructions,
                                onValueChange = { if (it.length <= 500) instructions = it },
                                label = { Text("Instructions") },
                                placeholder = { Text("e.g. no mayo, less spicy, extra cheese") },
                                minLines = 3,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("special_instructions_input")
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${instructions.length}/500",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }
                    }

                    // Section 5: If this product is not available
                    item {
                        HorizontalDivider(thickness = 6.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "If this product is not available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { unavailableExpanded = !unavailableExpanded }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(unavailablePreference, style = MaterialTheme.typography.bodyMedium)
                                    Icon(
                                        imageVector = if (unavailableExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Choose availability preference"
                                    )
                                }
                            }
                            AnimatedVisibility(visible = unavailableExpanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    unavailableOptions.forEach { option ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .minimumInteractiveComponentSize()
                                                .clickable {
                                                    unavailablePreference = option
                                                    unavailableExpanded = false
                                                }
                                                .padding(horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = unavailablePreference == option,
                                                onClick = {
                                                    unavailablePreference = option
                                                    unavailableExpanded = false
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = GharTakPink)
                                            )
                                            Text(option, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sticky Bottom Bar: Quantity Stepper + Add to Cart + Order Now
                Surface(
                    shadowElevation = 14.dp,
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (extraUnitPrice > 0.0) {
                                    "Unit: ${GharTakConstants.formatPkr(unitTotal)} (incl. +${GharTakConstants.formatPkr(extraUnitPrice)} extras)"
                                } else {
                                    "Unit price: ${GharTakConstants.formatPkr(unitTotal)}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Total: ${GharTakConstants.formatPkr(grandTotal)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = GharTakPink
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Quantity stepper (- 1 +)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity -= 1 },
                                        enabled = available && quantity > 1,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("customization_qty_minus")
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity", modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(
                                    text = "$quantity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Surface(
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    IconButton(
                                        onClick = { if (quantity < maxQty) quantity += 1 },
                                        enabled = available && quantity < maxQty,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("customization_qty_plus")
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase quantity", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // Add to Cart button
                            OutlinedButton(
                                enabled = available,
                                onClick = { onAddToCart(buildSummaryNote(), quantity, extraUnitPrice) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("dialog_add_to_cart_button")
                            ) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (available) "Add to Cart" else "Sold Out",
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Order Now primary pink button
                            Button(
                                enabled = available,
                                onClick = { onOrderNow(buildSummaryNote(), quantity, extraUnitPrice) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GharTakPink),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("dialog_order_now_button")
                            ) {
                                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "Order Now",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitleWithBadge(
    title: String,
    subtitle: String,
    badgeText: String,
    isRequired: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            color = if (isRequired) GharTakCharcoal else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(999.dp)
        ) {
            Text(
                text = badgeText,
                color = if (isRequired) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun RadioOptionRow(
    label: String,
    extraPricePkr: Double,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = GharTakPink)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (extraPricePkr > 0.0) "+ ${GharTakConstants.formatPkr(extraPricePkr)}" else "Free",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (extraPricePkr > 0.0) GharTakPink else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
