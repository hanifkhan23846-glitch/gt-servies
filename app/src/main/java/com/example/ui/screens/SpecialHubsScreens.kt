package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CelebrationItem
import com.example.data.model.GharTakConstants
import com.example.data.model.MultanItem
import com.example.ui.components.DiscountBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GharTakImage
import com.example.ui.components.PriceRow
import com.example.ui.components.celebrationItemImageUri
import com.example.ui.components.multanItemImageUri
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.GtLightGreyCard
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.GharTakViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultanHubScreen(
    viewModel: GharTakViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    BackHandler { onBack() }

    val itemsState by viewModel.multanItemsState.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedMultanDestination by viewModel.selectedMultanDestination.collectAsStateWithLifecycle()
    val multanCart by viewModel.multanCart.collectAsStateWithLifecycle()

    val locationFeeOptions = remember(settings) {
        viewModel.getMultanLocationsWithFees(settings)
    }
    val targetDeliveryCity = remember(selectedMultanDestination, selectedCity, locationFeeOptions) {
        val preferred = selectedMultanDestination?.trim().orEmpty()
            .takeIf { it.isNotBlank() && !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            ?: selectedCity.takeUnless { it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            ?: locationFeeOptions.firstOrNull()?.first
            ?: GharTakConstants.DEFAULT_CITY
        locationFeeOptions.firstOrNull { it.first.equals(preferred, ignoreCase = true) }?.first ?: preferred
    }
    val expressFeePkr = remember(targetDeliveryCity, settings) {
        viewModel.resolveMultanCityDeliveryFee(targetDeliveryCity, settings)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var customizingItem by remember { mutableStateOf<MultanItem?>(null) }
    var showCustomItemDialog by remember { mutableStateOf(false) }

    val brandFilters = remember(itemsState) {
        val loaded = (itemsState as? UiState.Success)?.data?.map { it.brandName }?.filter { it.isNotBlank() }.orEmpty()
        listOf("All") + (GharTakConstants.MULTAN_BRANDS + loaded).distinct()
    }
    val categoryFilters = remember(itemsState) {
        val loaded = (itemsState as? UiState.Success)?.data?.map { it.categoryName }?.filter { it.isNotBlank() }.orEmpty()
        listOf("All") + (GharTakConstants.MULTAN_CATEGORIES + loaded).distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Anything From Multan", fontWeight = FontWeight.Bold)
                        Text(
                            "Express Courier • Delivering to $targetDeliveryCity (${GharTakConstants.formatPkr(expressFeePkr)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("multan_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = gtNavyTopAppBarColors()
            )
        },
        bottomBar = {
            if (multanCart.isNotEmpty()) {
                val count = multanCart.sumOf { it.quantity }
                val subtotal = multanCart.sumOf { it.unitPricePkr * it.quantity }
                Surface(
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$count Multan item(s) • To $targetDeliveryCity", fontWeight = FontWeight.Bold)
                            Text(
                                "${GharTakConstants.formatPkr(subtotal)} + ${GharTakConstants.formatPkr(expressFeePkr)} Express",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.setMultanDestination(targetDeliveryCity)
                                onNavigateToCheckout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("multan_view_cart_button")
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Checkout")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GharTakEmerald.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, GharTakEmerald.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GharTakEmerald)
                            Text(
                                "Multan → $targetDeliveryCity Express Delivery: ${GharTakConstants.formatPkr(expressFeePkr)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "Tap your delivery city/town below to see Admin-updated Express delivery charges live:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(locationFeeOptions, key = { it.first.lowercase() }) { (city, fee) ->
                                FilterChip(
                                    selected = targetDeliveryCity.equals(city, ignoreCase = true),
                                    onClick = {
                                        viewModel.setMultanDestination(city)
                                    },
                                    label = { Text("$city • ${GharTakConstants.formatPkr(fee)}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GharTakEmerald,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("multan_city_chip_$city")
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GharTakPink.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, GharTakPink.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = GharTakPink, modifier = Modifier.size(30.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Want something not listed below?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Add any item, brand, bakery or shop from Multan with your budget.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showCustomItemDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GharTakPink),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("multan_add_custom_item_button")
                        ) {
                            Text("Add Item")
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Crumble, Sohan Halwa, KFC, Zanzibar...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("multan_search_input")
                )
            }

            item {
                Text("Brands from Multan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(brandFilters) { brand ->
                        FilterChip(
                            selected = selectedBrand == brand,
                            onClick = { selectedBrand = brand },
                            label = { Text(brand) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("multan_brand_chip_$brand")
                        )
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryFilters) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GharTakEmerald,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("multan_cat_chip_$cat")
                        )
                    }
                }
            }

            when (val state = itemsState) {
                is UiState.Loading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> item {
                    EmptyStateView(title = "Could not load Multan catalog", subtitle = state.message)
                }
                is UiState.Success -> {
                    val q = searchQuery.trim().lowercase()
                    val filtered = state.data.filter { item ->
                        val brandMatch = selectedBrand == "All" || item.brandName.equals(selectedBrand, ignoreCase = true)
                        val catMatch = selectedCategory == "All" || item.categoryName.equals(selectedCategory, ignoreCase = true)
                        val queryMatch = q.isBlank() ||
                            item.name.lowercase().contains(q) ||
                            item.brandName.lowercase().contains(q) ||
                            item.categoryName.lowercase().contains(q) ||
                            item.description.lowercase().contains(q)
                        brandMatch && catMatch && queryMatch
                    }
                    if (filtered.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Multan items found",
                                subtitle = "Try clearing filters or add your own custom Multan request.",
                                actionLabel = "Add Custom Item",
                                onAction = { showCustomItemDialog = true }
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { item ->
                            MultanCatalogCard(
                                item = item,
                                onAdd = { customizingItem = item },
                                onBuyNow = {
                                    viewModel.buyNowMultanItem(item = item, onReadyForCheckout = onNavigateToCheckout)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    customizingItem?.let { item ->
        SpecialItemOrderDialog(
            title = "${item.brandName} • ${item.name}",
            subtitle = "From ${item.originCity} • ${GharTakConstants.formatPkr(item.effectivePricePkr)} / ${item.unit}",
            noteLabel = "Flavor / Custom Request (Optional)",
            notePlaceholder = "e.g. Pistachio & Walnut halwa, Lotus Biscoff cookie flavor",
            maxStock = item.stockQuantity.toInt().coerceAtLeast(1),
            onDismiss = { customizingItem = null },
            onAddToCart = { qty, note ->
                viewModel.addMultanToCart(item, note, qty)
                customizingItem = null
            },
            onBuyNow = { qty, note ->
                viewModel.buyNowMultanItem(item, note, qty, onNavigateToCheckout)
                customizingItem = null
            }
        )
    }

    if (showCustomItemDialog) {
        CustomMultanItemDialog(
            targetCity = targetDeliveryCity,
            expressFeePkr = expressFeePkr,
            onDismiss = { showCustomItemDialog = false },
            onAddToCart = { itemName, brand, price, unit, qty, note ->
                viewModel.addCustomMultanItemToCart(
                    itemName = itemName,
                    brandOrShop = brand,
                    estimatedPricePkr = price,
                    unit = unit,
                    quantity = qty,
                    note = note,
                    buyNow = false
                )
                showCustomItemDialog = false
            },
            onBuyNow = { itemName, brand, price, unit, qty, note ->
                viewModel.addCustomMultanItemToCart(
                    itemName = itemName,
                    brandOrShop = brand,
                    estimatedPricePkr = price,
                    unit = unit,
                    quantity = qty,
                    note = note,
                    buyNow = true,
                    onReadyForCheckout = onNavigateToCheckout
                )
                showCustomItemDialog = false
            }
        )
    }
}

@Composable
private fun MultanCatalogCard(
    item: MultanItem,
    onAdd: () -> Unit,
    onBuyNow: () -> Unit
) {
    val resolvedUri = item.imageUrl?.takeIf { it.isNotBlank() }
        ?: multanItemImageUri(item.brandName, item.categoryName, item.name)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GtLightGreyCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(92.dp)) {
                GharTakImage(
                    imageUrl = resolvedUri,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
                if (item.discountPercent > 0) {
                    DiscountBadge(
                        discountPercent = item.discountPercent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            item.brandName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        item.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotBlank()) {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                PriceRow(
                    regularPricePkr = item.regularPricePkr,
                    discountPricePkr = item.discountPricePkr,
                    unitSuffix = item.unit
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("add_multan_${item.id}")
                    ) {
                        Text("Add")
                    }
                    Button(
                        onClick = onBuyNow,
                        colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("buy_multan_${item.id}")
                    ) {
                        Text("Order Now")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CelebrationHubScreen(
    viewModel: GharTakViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    BackHandler { onBack() }

    val itemsState by viewModel.celebrationItemsState.collectAsStateWithLifecycle()
    val celebrationCart by viewModel.celebrationCart.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var customizingItem by remember { mutableStateOf<CelebrationItem?>(null) }

    val categoryFilters = remember(itemsState) {
        val loaded = (itemsState as? UiState.Success)?.data?.map { it.categoryName }?.filter { it.isNotBlank() }.orEmpty()
        listOf("All") + (GharTakConstants.CELEBRATION_CATEGORIES + loaded).distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Celebration Studio", fontWeight = FontWeight.Bold)
                        Text(
                            "Cakes, Bouquets, Balloons & Surprise Gifts",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("celebration_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = gtNavyTopAppBarColors()
            )
        },
        bottomBar = {
            if (celebrationCart.isNotEmpty()) {
                val count = celebrationCart.sumOf { it.quantity }
                val subtotal = celebrationCart.sumOf { it.unitPricePkr * it.quantity }
                Surface(shadowElevation = 10.dp, color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$count Celebration item(s)", fontWeight = FontWeight.Bold)
                            Text(
                                "Subtotal: ${GharTakConstants.formatPkr(subtotal)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onNavigateToCheckout,
                            colors = ButtonDefaults.buttonColors(containerColor = GharTakPink),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("celebration_view_cart_button")
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Checkout")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GharTakPink.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, GharTakPink.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = GharTakPink, modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Make Every Moment Special",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Add a custom cake inscription or greeting card note when ordering.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search cakes, rose bouquets, balloons, gift boxes...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("celebration_search_input")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryFilters) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GharTakPink,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("celebration_cat_chip_$cat")
                        )
                    }
                }
            }

            when (val state = itemsState) {
                is UiState.Loading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> item {
                    EmptyStateView(title = "Could not load Celebration items", subtitle = state.message)
                }
                is UiState.Success -> {
                    val q = searchQuery.trim().lowercase()
                    val filtered = state.data.filter { item ->
                        val catMatch = selectedCategory == "All" || item.categoryName.equals(selectedCategory, ignoreCase = true)
                        val queryMatch = q.isBlank() ||
                            item.name.lowercase().contains(q) ||
                            item.categoryName.lowercase().contains(q) ||
                            item.description.lowercase().contains(q)
                        catMatch && queryMatch
                    }
                    if (filtered.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Celebration items found",
                                subtitle = "Try another category or clear search."
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { item ->
                            CelebrationCatalogCard(
                                item = item,
                                onAdd = { customizingItem = item },
                                onBuyNow = { customizingItem = item }
                            )
                        }
                    }
                }
            }
        }
    }

    customizingItem?.let { item ->
        SpecialItemOrderDialog(
            title = item.name,
            subtitle = "${item.categoryName} • ${GharTakConstants.formatPkr(item.effectivePricePkr)} / ${item.unit}",
            noteLabel = "Cake Name / Greeting Card Note",
            notePlaceholder = "e.g. Happy Birthday Ayesha! Deliver at 8 PM",
            maxStock = item.stockQuantity.toInt().coerceAtLeast(1),
            onDismiss = { customizingItem = null },
            onAddToCart = { qty, note ->
                viewModel.addCelebrationToCart(item, note, qty)
                customizingItem = null
            },
            onBuyNow = { qty, note ->
                viewModel.buyNowCelebrationItem(item, note, qty, onNavigateToCheckout)
                customizingItem = null
            }
        )
    }
}

@Composable
private fun CelebrationCatalogCard(
    item: CelebrationItem,
    onAdd: () -> Unit,
    onBuyNow: () -> Unit
) {
    val resolvedUri = item.imageUrl?.takeIf { it.isNotBlank() }
        ?: celebrationItemImageUri(item.categoryName, item.name)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GtLightGreyCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(92.dp)) {
                GharTakImage(
                    imageUrl = resolvedUri,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
                if (item.discountPercent > 0) {
                    DiscountBadge(
                        discountPercent = item.discountPercent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    color = GharTakAmber.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GharTakPink, modifier = Modifier.size(12.dp))
                        Text(
                            item.categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotBlank()) {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                PriceRow(
                    regularPricePkr = item.regularPricePkr,
                    discountPricePkr = item.discountPricePkr,
                    unitSuffix = item.unit
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("add_celebration_${item.id}")
                    ) {
                        Text("Add")
                    }
                    Button(
                        onClick = onBuyNow,
                        colors = ButtonDefaults.buttonColors(containerColor = GharTakPink),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("buy_celebration_${item.id}")
                    ) {
                        Text("Order")
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialItemOrderDialog(
    title: String,
    subtitle: String,
    noteLabel: String,
    notePlaceholder: String,
    maxStock: Int,
    onDismiss: () -> Unit,
    onAddToCart: (quantity: Int, note: String) -> Unit,
    onBuyNow: (quantity: Int, note: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quantity", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.testTag("special_qty_minus")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                        }
                        Text("$quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { if (quantity < maxStock) quantity++ },
                            modifier = Modifier.testTag("special_qty_plus")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(180) },
                    label = { Text(noteLabel) },
                    placeholder = { Text(notePlaceholder) },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("special_item_note_input")
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onAddToCart(quantity, note) },
                    modifier = Modifier.testTag("special_dialog_add_button")
                ) {
                    Text("Add to Cart")
                }
                Button(
                    onClick = { onBuyNow(quantity, note) },
                    colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                    modifier = Modifier.testTag("special_dialog_buy_button")
                ) {
                    Text("Checkout")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomMultanItemDialog(
    targetCity: String,
    expressFeePkr: Double,
    onDismiss: () -> Unit,
    onAddToCart: (itemName: String, brandOrShop: String, pricePkr: Double, unit: String, quantity: Int, note: String) -> Unit,
    onBuyNow: (itemName: String, brandOrShop: String, pricePkr: Double, unit: String, quantity: Int, note: String) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var brandOrShop by remember { mutableStateOf(GharTakConstants.MULTAN_BRANDS.first()) }
    var priceText by remember { mutableStateOf("1500") }
    var unitText by remember { mutableStateOf("1 item") }
    var quantity by remember { mutableIntStateOf(1) }
    var note by remember { mutableStateOf("") }

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
    val isValid = itemName.trim().length >= 2 && parsedPrice > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Item from Multan") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Delivering to $targetCity • Express Fee: ${GharTakConstants.formatPkr(expressFeePkr)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GharTakEmerald,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it.take(90) },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Lotus Cookies 6-pc Box, Special Sohan Halwa") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_multan_name_input")
                )
                Text("Shop / Brand in Multan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    (GharTakConstants.MULTAN_BRANDS + "Any Multan Shop").forEach { b ->
                        FilterChip(
                            selected = brandOrShop.equals(b, ignoreCase = true),
                            onClick = { brandOrShop = b },
                            label = { Text(b) }
                        )
                    }
                }
                OutlinedTextField(
                    value = brandOrShop,
                    onValueChange = { brandOrShop = it.take(60) },
                    label = { Text("Brand / Shop Name") },
                    placeholder = { Text("e.g. Crumble, KFC, Hafiz Sohan Halwa, Chase Up") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_multan_brand_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Est. Price (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_multan_price_input")
                    )
                    OutlinedTextField(
                        value = unitText,
                        onValueChange = { unitText = it.take(25) },
                        label = { Text("Pack / Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quantity", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                        }
                        Text("$quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (quantity < 20) quantity++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(180) },
                    label = { Text("Flavor, Branch, Weight or Instructions") },
                    placeholder = { Text("e.g. Bosan Road branch, Pistachio flavor, fresh pack") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_multan_note_input")
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    enabled = isValid,
                    onClick = {
                        onAddToCart(
                            itemName,
                            brandOrShop,
                            parsedPrice,
                            unitText,
                            quantity,
                            note
                        )
                    },
                    modifier = Modifier.testTag("custom_multan_add_cart_button")
                ) {
                    Text("Add to Cart")
                }
                Button(
                    enabled = isValid,
                    onClick = {
                        onBuyNow(
                            itemName,
                            brandOrShop,
                            parsedPrice,
                            unitText,
                            quantity,
                            note
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                    modifier = Modifier.testTag("custom_multan_buy_now_button")
                ) {
                    Text("Order Now")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

