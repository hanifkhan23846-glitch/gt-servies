package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GharTakConstants
import com.example.data.model.MedicalProduct
import com.example.data.model.MedicalStore
import com.example.ui.components.DiscountBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GharTakImage
import com.example.ui.components.OperatingHoursBadge
import com.example.ui.components.PriceRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.medicalCategoryDrawableRes
import com.example.ui.components.medicalCategoryImageUri
import com.example.ui.components.medicalStoreImageUri
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCharcoal
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakMedicalTeal
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.GtMintSubtext
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.GharTakViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun MedicalStoreCard(store: MedicalStore, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val displayImage = medicalStoreImageUri(store.name, store.imageUrl)
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(126.dp)) {
                GharTakImage(
                    imageUrl = displayImage,
                    contentDescription = store.name,
                    modifier = Modifier.fillMaxSize(),
                    isMedicalFallback = true
                )
                Surface(
                    color = GharTakMedicalTeal.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.DeliveryDining, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${store.deliveryTimeMinutes} min • ${GharTakConstants.formatPkr(store.deliveryFeePkr)}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        store.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = GharTakAmber, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(String.format("%.1f", store.rating), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text(
                    "${store.city} • Pharmacy • Min ${GharTakConstants.formatPkr(store.minOrderPkr)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GharTakMedicalTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                OperatingHoursBadge(
                    isOpen = store.isOpen,
                    openingTime = store.openingTime,
                    closingTime = store.closingTime
                )
            }
        }
    }
}

@Composable
fun MedicalMiniCard(
    product: MedicalProduct,
    onAddClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val canOrder = product.isAvailable && product.stockQuantity > 0L
    val displayImage = product.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_medical" }
        ?: medicalCategoryImageUri(product.categoryName, product.name)
    Card(
        modifier = Modifier.width(214.dp).clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(126.dp)) {
                GharTakImage(
                    imageUrl = displayImage,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    isMedicalFallback = true
                )
                if (product.discountPercent > 0) {
                    DiscountBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(8.dp))
                }
                Surface(
                    color = GharTakMedicalTeal.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                ) {
                    Text(
                        product.unit,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(product.categoryName, style = MaterialTheme.typography.labelSmall, color = GharTakMedicalTeal, fontWeight = FontWeight.SemiBold)
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${product.storeName} • Stock: ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PriceRow(product.regularPricePkr, product.discountPricePkr, product.unit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAddClick,
                        enabled = canOrder,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add ${product.name}", modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Add", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    Button(
                        onClick = onBuyNowClick,
                        enabled = canOrder,
                        colors = ButtonDefaults.buttonColors(containerColor = GharTakMedicalTeal),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(36.dp)
                            .testTag("buy_now_medical_${product.id}")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = "Buy ${product.name} now", modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(if (canOrder) "Buy Now" else "Out", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalProductRowCard(
    product: MedicalProduct,
    inCartQty: Int,
    onCardClick: () -> Unit,
    onAdd: () -> Unit,
    onBuyNow: () -> Unit,
    onDelta: (Int) -> Unit
) {
    val available = product.isAvailable && product.stockQuantity > 0L
    val displayImage = product.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_medical" }
        ?: medicalCategoryImageUri(product.categoryName, product.name)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(14.dp))) {
                GharTakImage(displayImage, product.name, modifier = Modifier.fillMaxSize(), isMedicalFallback = true)
                if (product.discountPercent > 0) {
                    DiscountBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(6.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${product.categoryName} • ${product.unit}", style = MaterialTheme.typography.labelSmall, color = GharTakMedicalTeal, fontWeight = FontWeight.SemiBold)
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${product.storeName} • Stock: ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PriceRow(product.regularPricePkr, product.discountPricePkr, product.unit)
                if (!available) {
                    StatusBadge("Out of Stock")
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (inCartQty == 0) {
                            OutlinedButton(
                                onClick = onAdd,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Add to Cart", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onDelta(-1) }, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                                }
                                Text("$inCartQty", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onDelta(1) }, enabled = inCartQty < product.stockQuantity, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase")
                                }
                            }
                        }
                        Button(
                            onClick = onBuyNow,
                            colors = ButtonDefaults.buttonColors(containerColor = GharTakMedicalTeal),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("buy_now_medical_${product.id}")
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Buy Now", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalProductDetailDialog(
    product: MedicalProduct,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onOpenStore: (() -> Unit)? = null
) {
    val gallery = product.allImages
    val fallbackImage = medicalCategoryImageUri(product.categoryName, product.name)
    var activeImage by remember(product) {
        mutableStateOf(
            gallery.firstOrNull()?.takeUnless { it == "res://img_hero_medical" } ?: fallbackImage
        )
    }
    val available = product.isAvailable && product.stockQuantity > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            LazyColumn(modifier = Modifier.height(360.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp))) {
                        GharTakImage(activeImage, product.name, modifier = Modifier.fillMaxSize(), isMedicalFallback = true)
                        if (product.discountPercent > 0) {
                            DiscountBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(8.dp))
                        }
                    }
                    if (gallery.size > 1) {
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(gallery) { img ->
                                GharTakImage(
                                    imageUrl = img,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { activeImage = img },
                                    isMedicalFallback = true
                                )
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${product.storeName} • ${product.categoryName}", style = MaterialTheme.typography.labelLarge, color = GharTakMedicalTeal)
                            Text("Pack/Unit: ${product.unit} • Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (onOpenStore != null) {
                            TextButton(onClick = onOpenStore) { Text("Visit Pharmacy") }
                        }
                    }
                    if (product.description.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(product.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    PriceRow(product.regularPricePkr, product.discountPricePkr, product.unit)
                }
            }
        },
        confirmButton = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(enabled = available, onClick = onAddToCart) {
                    Text(if (available) "Add to Cart" else "Out of Stock")
                }
                Button(
                    enabled = available,
                    onClick = onBuyNow,
                    colors = ButtonDefaults.buttonColors(containerColor = GharTakMedicalTeal),
                    modifier = Modifier.testTag("dialog_buy_now_medical_button")
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Buy Now")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHubScreen(
    viewModel: GharTakViewModel,
    onBack: () -> Unit,
    onOpenMedicalStore: (String) -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val categories by viewModel.medicalCategories.collectAsStateWithLifecycle()
    val storesState by viewModel.medicalStoresState.collectAsStateWithLifecycle()
    val productsState by viewModel.medicalProductsState.collectAsStateWithLifecycle()
    val medicalCart by viewModel.medicalCart.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("All") }
    var inspectingProduct by remember { mutableStateOf<MedicalProduct?>(null) }

    val stores = (storesState as? UiState.Success)?.data.orEmpty()
    val products = (productsState as? UiState.Success)?.data.orEmpty().filter { it.isAvailable }
    val catNames = listOf("All") + categories.map { it.name }.ifEmpty { GharTakConstants.MEDICAL_CATEGORIES }
    val filteredProducts = if (selectedCategory == "All") products else products.filter { it.categoryName.equals(selectedCategory, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = gtNavyTopAppBarColors(),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = GtMintSubtext)
                        Text("Medical & Pharmacy", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("medical_hub_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("24/7 Pharmacy Delivery", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GharTakMedicalTeal)
                Text("Medicines, first aid, vitamins, baby care & healthcare supplies", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(catNames) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            leadingIcon = if (cat != "All") {
                                {
                                    Image(
                                        painter = painterResource(id = medicalCategoryDrawableRes(cat)),
                                        contentDescription = cat,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(22.dp).clip(CircleShape)
                                    )
                                }
                            } else null,
                            label = { Text(cat) }
                        )
                    }
                }
            }
            item {
                Text("Approved Pharmacies (${stores.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (stores.isEmpty()) {
                item { EmptyStateView("No pharmacies open yet", "Approved medical stores will appear here.") }
            } else {
                items(stores, key = { it.id }) { store ->
                    MedicalStoreCard(store = store, onClick = { onOpenMedicalStore(store.id) }, modifier = Modifier.fillMaxWidth())
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text("Products in $selectedCategory (${filteredProducts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (filteredProducts.isEmpty()) {
                item { EmptyStateView("No medical items in $selectedCategory yet", "New healthcare items added by Admin appear here automatically.") }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    val store = stores.firstOrNull { it.id == product.storeId }
                    val inCartQty = medicalCart.firstOrNull { it.productId == product.id }?.quantity ?: 0
                    MedicalProductRowCard(
                        product = product,
                        inCartQty = inCartQty,
                        onCardClick = { inspectingProduct = product },
                        onAdd = { viewModel.addMedicalToCart(product, store) },
                        onBuyNow = { viewModel.buyNowMedicalProduct(product, store, onNavigateToCheckout) },
                        onDelta = { delta -> viewModel.updateCartQuantity("medical", product.id, delta) }
                    )
                }
            }
        }
    }

    inspectingProduct?.let { prod ->
        val store = stores.firstOrNull { it.id == prod.storeId }
        MedicalProductDetailDialog(
            product = prod,
            onDismiss = { inspectingProduct = null },
            onOpenStore = {
                inspectingProduct = null
                onOpenMedicalStore(prod.storeId)
            },
            onAddToCart = {
                viewModel.addMedicalToCart(prod, store)
                inspectingProduct = null
            },
            onBuyNow = {
                inspectingProduct = null
                viewModel.buyNowMedicalProduct(prod, store, onNavigateToCheckout)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalStoreDetailScreen(
    storeId: String,
    viewModel: GharTakViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val storesState by viewModel.medicalStoresState.collectAsStateWithLifecycle()
    val productsState by viewModel.medicalProductsState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val medicalCart by viewModel.medicalCart.collectAsStateWithLifecycle()

    val store = (storesState as? UiState.Success)?.data?.firstOrNull { it.id == storeId }
    val products = (productsState as? UiState.Success)?.data?.filter { it.storeId == storeId }.orEmpty()
    val isFav = favorites.any { it.targetId == storeId && it.targetType == "medicalStore" }
    var inspectingProduct by remember { mutableStateOf<MedicalProduct?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = gtNavyTopAppBarColors(),
                title = { Text(store?.name ?: "Pharmacy", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (store != null) {
                        IconButton(onClick = {
                            viewModel.toggleFavorite(store.id, "medicalStore", store.name, store.address, store.imageUrl)
                        }) {
                            Icon(
                                if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) GtMintSubtext else Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (store == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GharTakImage(
                        imageUrl = medicalStoreImageUri(store.name, store.imageUrl),
                        contentDescription = store.name,
                        modifier = Modifier.fillMaxWidth().height(175.dp).clip(RoundedCornerShape(20.dp)),
                        isMedicalFallback = true
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(store.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${store.address}, ${store.city}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusBadge("${store.rating} ★")
                        Text("${store.deliveryTimeMinutes} mins", style = MaterialTheme.typography.labelMedium)
                        Text("Fee: ${GharTakConstants.formatPkr(store.deliveryFeePkr)}", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    OperatingHoursBadge(
                        isOpen = store.isOpen,
                        openingTime = store.openingTime,
                        closingTime = store.closingTime
                    )
                    if (!store.isCurrentlyOpenNow()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = GharTakCoralError.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "${store.name} is currently closed (${store.operatingHoursLabel()}). Ordering is paused until opening.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GharTakCoralError,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    if (!store.description.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(store.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    HorizontalDivider()
                    Spacer(Modifier.height(6.dp))
                    Text("Available Medicines & Care (${products.size})", style = MaterialTheme.typography.titleLarge)
                }
                if (products.isEmpty()) {
                    item { EmptyStateView("No medical products in store", "Products added for ${store.name} appear here automatically.") }
                } else {
                    items(products, key = { it.id }) { prod ->
                        val qty = medicalCart.firstOrNull { it.productId == prod.id }?.quantity ?: 0
                        MedicalProductRowCard(
                            product = prod,
                            inCartQty = qty,
                            onCardClick = { inspectingProduct = prod },
                            onAdd = { viewModel.addMedicalToCart(prod, store) },
                            onBuyNow = { viewModel.buyNowMedicalProduct(prod, store, onNavigateToCheckout) },
                            onDelta = { delta -> viewModel.updateCartQuantity("medical", prod.id, delta) }
                        )
                    }
                }
            }
        }
    }

    inspectingProduct?.let { prod ->
        MedicalProductDetailDialog(
            product = prod,
            onDismiss = { inspectingProduct = null },
            onAddToCart = {
                viewModel.addMedicalToCart(prod, store)
                inspectingProduct = null
            },
            onBuyNow = {
                inspectingProduct = null
                viewModel.buyNowMedicalProduct(prod, store, onNavigateToCheckout)
            }
        )
    }
}
