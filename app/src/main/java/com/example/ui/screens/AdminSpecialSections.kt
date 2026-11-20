package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CelebrationItem
import com.example.data.model.DeliveryAreaFeeRule
import com.example.data.model.GharTakConstants
import com.example.data.model.MultanCityFeeRule
import com.example.data.model.MultanItem
import com.example.ui.components.AdditionalImagesUploadSection
import com.example.ui.components.DiscountBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GharTakImage
import com.example.ui.components.PhotoPickerUploadRow
import com.example.ui.components.PriceRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.celebrationItemImageUri
import com.example.ui.components.multanItemImageUri
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakPink
import com.example.ui.viewmodel.AdminViewModel
import java.util.UUID

@Composable
internal fun AdminMultanSection(
    adminVm: AdminViewModel,
    onOpenDeliveryFees: () -> Unit = {}
) {
    val items by adminVm.multanItems.collectAsStateWithLifecycle()
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf("All") }
    var showEditor by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MultanItem?>(null) }
    var deletingTarget by remember { mutableStateOf<MultanItem?>(null) }

    val brands = remember(items) {
        (listOf("All") + (GharTakConstants.MULTAN_BRANDS + items.map { it.brandName })).distinct()
    }
    val filtered = items.filter { item ->
        val matchesSearch = search.isBlank() ||
            item.name.contains(search, ignoreCase = true) ||
            item.brandName.contains(search, ignoreCase = true) ||
            item.categoryName.contains(search, ignoreCase = true)
        val matchesBrand = selectedBrand == "All" || item.brandName.equals(selectedBrand, ignoreCase = true)
        matchesSearch && matchesBrand
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Anything You Want from Multan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Default Express Delivery Fee: ${GharTakConstants.formatPkr(settings.multanDefaultDeliveryFeePkr)} • ${settings.multanCityDeliveryFees.size} custom city rates",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = GharTakEmerald)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                editingItem = null
                                showEditor = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("admin_add_multan_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Item")
                        }
                        OutlinedButton(
                            onClick = onOpenDeliveryFees,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("admin_multan_location_fees_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Charges")
                        }
                        OutlinedButton(
                            onClick = { adminVm.seedStarterCatalog() },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Seed")
                        }
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search Multan items or brands...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(brands) { brand ->
                    FilterChip(
                        selected = selectedBrand == brand,
                        onClick = { selectedBrand = brand },
                        label = { Text(brand) }
                    )
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateView(
                    "No Multan Express items yet",
                    "Tap Add Multan Item to list Crumble cookies, KFC deals, Hafiz Sohan Halwa, or any custom Multan product.",
                    "Add First Item"
                ) {
                    editingItem = null
                    showEditor = true
                }
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box {
                            GharTakImage(
                                imageUrl = multanItemImageUri(item.brandName, item.categoryName, item.name).let {
                                    item.imageUrl?.takeIf { u -> u.isNotBlank() } ?: it
                                },
                                contentDescription = item.name,
                                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
                            )
                            if (item.discountPercent > 0) {
                                DiscountBadge(item.discountPercent, Modifier.align(Alignment.TopStart).padding(4.dp))
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${item.brandName} • ${item.categoryName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${item.unit} • Qty: ${item.stockQuantity}", style = MaterialTheme.typography.labelSmall)
                            PriceRow(item.regularPricePkr, item.discountPricePkr, item.unit)
                            StatusBadge(if (item.isAvailable) "Active" else "Inactive")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { adminVm.saveMultanItem(item.copy(isAvailable = it)) }
                            )
                            Row {
                                IconButton(onClick = { editingItem = item; showEditor = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Multan Item")
                                }
                                IconButton(onClick = { deletingTarget = item }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Multan Item", tint = GharTakCoralError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        MultanItemEditorDialog(
            existing = editingItem,
            onDismiss = { showEditor = false },
            onSave = { item ->
                adminVm.saveMultanItem(item)
                showEditor = false
            }
        )
    }

    deletingTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingTarget = null },
            title = { Text("Delete Multan Item?") },
            text = { Text("Remove '${target.name}' (${target.brandName}) from Multan Express?") },
            confirmButton = {
                Button(onClick = {
                    adminVm.deleteMultanItem(target)
                    deletingTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingTarget = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultanItemEditorDialog(
    existing: MultanItem?,
    onDismiss: () -> Unit,
    onSave: (MultanItem) -> Unit
) {
    var brandName by remember(existing) { mutableStateOf(existing?.brandName ?: GharTakConstants.MULTAN_BRANDS.first()) }
    var categoryName by remember(existing) { mutableStateOf(existing?.categoryName ?: GharTakConstants.MULTAN_CATEGORIES.first()) }
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var regularPrice by remember(existing) { mutableStateOf(existing?.regularPricePkr?.takeIf { it > 0 }?.toInt()?.toString() ?: "") }
    var discountPrice by remember(existing) { mutableStateOf(existing?.discountPricePkr?.toInt()?.toString() ?: "0") }
    var unit by remember(existing) { mutableStateOf(existing?.unit ?: "1 box") }
    var stock by remember(existing) { mutableStateOf(existing?.stockQuantity?.toString() ?: "30") }
    var originCity by remember(existing) { mutableStateOf(existing?.originCity ?: "Multan") }
    var imageUrl by remember(existing) { mutableStateOf(existing?.imageUrl ?: "") }
    var additionalImages by remember(existing) { mutableStateOf(existing?.additionalImages ?: emptyList()) }
    var isAvailable by remember(existing) { mutableStateOf(existing?.isAvailable ?: true) }
    var isFeatured by remember(existing) { mutableStateOf(existing?.isFeatured ?: false) }

    val reg = regularPrice.toDoubleOrNull() ?: 0.0
    val disc = discountPrice.toDoubleOrNull() ?: 0.0
    val stockQty = stock.toLongOrNull() ?: -1L
    val error = when {
        name.trim().length < 2 -> "Item name must be at least 2 characters."
        brandName.trim().isEmpty() -> "Enter or choose a brand name."
        reg <= 0.0 -> "Regular price must be > 0 PKR."
        disc < 0.0 || disc > reg -> "Discount price must be between 0 and Regular price."
        unit.trim().isEmpty() -> "Enter a unit (e.g. 1 box, 1 pack)."
        stockQty < 0L -> "Stock quantity cannot be negative."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Multan Express Item" else "Edit Multan Item") },
        text = {
            LazyColumn(modifier = Modifier.height(470.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PhotoPickerUploadRow(
                        imageUrl = imageUrl,
                        onImageChanged = { imageUrl = it },
                        label = "Product Image"
                    )
                    TextButton(onClick = { imageUrl = multanItemImageUri(brandName, categoryName, name) }) {
                        Text("Use Stock Photo for '$brandName'")
                    }
                }
                item {
                    AdditionalImagesUploadSection(
                        images = additionalImages,
                        onImagesChanged = { additionalImages = it }
                    )
                }
                item {
                    Text("Brand Name", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GharTakConstants.MULTAN_BRANDS.forEach { b ->
                            FilterChip(selected = brandName.equals(b, ignoreCase = true), onClick = { brandName = b }, label = { Text(b) })
                        }
                    }
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("Custom Brand Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GharTakConstants.MULTAN_CATEGORIES) { cat ->
                            FilterChip(selected = categoryName == cat, onClick = { categoryName = cat }, label = { Text(cat) })
                        }
                    }
                }
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regularPrice,
                            onValueChange = { regularPrice = it },
                            label = { Text("Regular PKR *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountPrice,
                            onValueChange = { discountPrice = it },
                            label = { Text("Discount PKR") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(value = originCity, onValueChange = { originCity = it }, label = { Text("Origin City") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Available")
                        Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Featured")
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    }
                }
                if (error != null) {
                    item { Text(error, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = error == null,
                onClick = {
                    onSave(
                        (existing ?: MultanItem()).copy(
                            brandName = brandName.trim(),
                            categoryName = categoryName,
                            name = name.trim(),
                            description = desc.trim(),
                            regularPricePkr = reg,
                            discountPricePkr = disc,
                            unit = unit.trim(),
                            stockQuantity = stockQty.coerceAtLeast(0L),
                            imageUrl = imageUrl.ifBlank { null },
                            additionalImages = additionalImages,
                            originCity = originCity.trim().ifBlank { "Multan" },
                            isAvailable = isAvailable,
                            isFeatured = isFeatured,
                            city = GharTakConstants.ALL_CITIES
                        )
                    )
                }
            ) { Text("Save Multan Item") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
internal fun AdminCelebrationSection(adminVm: AdminViewModel) {
    val items by adminVm.celebrationItems.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showEditor by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CelebrationItem?>(null) }
    var deletingTarget by remember { mutableStateOf<CelebrationItem?>(null) }

    val cats = listOf("All") + GharTakConstants.CELEBRATION_CATEGORIES
    val filtered = items.filter { item ->
        val matchesSearch = search.isBlank() ||
            item.name.contains(search, ignoreCase = true) ||
            item.categoryName.contains(search, ignoreCase = true)
        val matchesCat = selectedCategory == "All" || item.categoryName.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCat
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = GharTakPink.copy(alpha = 0.10f))) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Celebration Store Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GharTakPink)
                            Text("Manage Birthday Cakes, Flowers, Bouquets, Balloons, Decor & Gift Boxes.", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Filled.Celebration, contentDescription = null, tint = GharTakPink)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                editingItem = null
                                showEditor = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("admin_add_celebration_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Celebration Item")
                        }
                        OutlinedButton(
                            onClick = { adminVm.seedStarterCatalog() },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Seed Catalog")
                        }
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search celebration products...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cats) { c ->
                    FilterChip(selected = selectedCategory == c, onClick = { selectedCategory = c }, label = { Text(c) })
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateView(
                    "No Celebration items yet",
                    "Tap Add Celebration Item to list birthday cakes, flower bouquets, balloon bundles & gifts.",
                    "Add First Item"
                ) {
                    editingItem = null
                    showEditor = true
                }
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box {
                            GharTakImage(
                                imageUrl = item.imageUrl?.takeIf { it.isNotBlank() } ?: celebrationItemImageUri(item.categoryName, item.name),
                                contentDescription = item.name,
                                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
                            )
                            if (item.discountPercent > 0) {
                                DiscountBadge(item.discountPercent, Modifier.align(Alignment.TopStart).padding(4.dp))
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${item.categoryName} • Qty: ${item.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            PriceRow(item.regularPricePkr, item.discountPricePkr, item.unit)
                            StatusBadge(if (item.isAvailable) "Active" else "Inactive")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { adminVm.saveCelebrationItem(item.copy(isAvailable = it)) }
                            )
                            Row {
                                IconButton(onClick = { editingItem = item; showEditor = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Celebration Item")
                                }
                                IconButton(onClick = { deletingTarget = item }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Celebration Item", tint = GharTakCoralError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        CelebrationItemEditorDialog(
            existing = editingItem,
            onDismiss = { showEditor = false },
            onSave = { item ->
                adminVm.saveCelebrationItem(item)
                showEditor = false
            }
        )
    }

    deletingTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingTarget = null },
            title = { Text("Delete Celebration Item?") },
            text = { Text("Remove '${target.name}' from Celebration Store?") },
            confirmButton = {
                Button(onClick = {
                    adminVm.deleteCelebrationItem(target)
                    deletingTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CelebrationItemEditorDialog(
    existing: CelebrationItem?,
    onDismiss: () -> Unit,
    onSave: (CelebrationItem) -> Unit
) {
    var categoryName by remember(existing) { mutableStateOf(existing?.categoryName ?: GharTakConstants.CELEBRATION_CATEGORIES.first()) }
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var regularPrice by remember(existing) { mutableStateOf(existing?.regularPricePkr?.takeIf { it > 0 }?.toInt()?.toString() ?: "") }
    var discountPrice by remember(existing) { mutableStateOf(existing?.discountPricePkr?.toInt()?.toString() ?: "0") }
    var unit by remember(existing) { mutableStateOf(existing?.unit ?: "1 cake") }
    var stock by remember(existing) { mutableStateOf(existing?.stockQuantity?.toString() ?: "25") }
    var imageUrl by remember(existing) { mutableStateOf(existing?.imageUrl ?: "") }
    var additionalImages by remember(existing) { mutableStateOf(existing?.additionalImages ?: emptyList()) }
    var isAvailable by remember(existing) { mutableStateOf(existing?.isAvailable ?: true) }
    var isFeatured by remember(existing) { mutableStateOf(existing?.isFeatured ?: false) }

    val reg = regularPrice.toDoubleOrNull() ?: 0.0
    val disc = discountPrice.toDoubleOrNull() ?: 0.0
    val stockQty = stock.toLongOrNull() ?: -1L
    val error = when {
        name.trim().length < 2 -> "Product name must be at least 2 characters."
        reg <= 0.0 -> "Regular price must be > 0 PKR."
        disc < 0.0 || disc > reg -> "Discount price must be between 0 and Regular price."
        unit.trim().isEmpty() -> "Enter a unit (e.g. 1 cake, 1 bouquet)."
        stockQty < 0L -> "Stock quantity cannot be negative."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Celebration Item" else "Edit Celebration Item") },
        text = {
            LazyColumn(modifier = Modifier.height(470.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PhotoPickerUploadRow(
                        imageUrl = imageUrl,
                        onImageChanged = { imageUrl = it },
                        label = "Celebration Item Photo"
                    )
                    TextButton(onClick = { imageUrl = celebrationItemImageUri(categoryName, name) }) {
                        Text("Use Stock Photo for '$categoryName'")
                    }
                }
                item {
                    AdditionalImagesUploadSection(
                        images = additionalImages,
                        onImagesChanged = { additionalImages = it }
                    )
                }
                item {
                    Text("Celebration Category", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GharTakConstants.CELEBRATION_CATEGORIES) { cat ->
                            FilterChip(selected = categoryName == cat, onClick = { categoryName = cat }, label = { Text(cat) })
                        }
                    }
                }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description / Cake Notes") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regularPrice,
                            onValueChange = { regularPrice = it },
                            label = { Text("Regular PKR *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountPrice,
                            onValueChange = { discountPrice = it },
                            label = { Text("Discount PKR") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (e.g. 2 lbs, 1 bouquet) *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Available")
                        Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Featured")
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    }
                }
                if (error != null) {
                    item { Text(error, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = error == null,
                onClick = {
                    onSave(
                        (existing ?: CelebrationItem()).copy(
                            categoryName = categoryName,
                            name = name.trim(),
                            description = desc.trim(),
                            regularPricePkr = reg,
                            discountPricePkr = disc,
                            unit = unit.trim(),
                            stockQuantity = stockQty.coerceAtLeast(0L),
                            imageUrl = imageUrl.ifBlank { null },
                            additionalImages = additionalImages,
                            isAvailable = isAvailable,
                            isFeatured = isFeatured,
                            city = GharTakConstants.ALL_CITIES
                        )
                    )
                }
            ) { Text("Save Celebration Item") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
internal fun AdminDeliveryFeesSection(adminVm: AdminViewModel) {
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    val cityOptions = remember(settings.cities) {
        settings.cities.ifEmpty { GharTakConstants.INITIAL_PAKISTAN_CITIES }
    }

    val multanQuickLocations = remember(cityOptions, settings.multanCityDeliveryFees) {
        (settings.multanCityDeliveryFees.map { it.city } + cityOptions)
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            .distinctBy { it.lowercase() }
    }

    var radiusEnabled by remember(settings.radiusPricingEnabled) { mutableStateOf(settings.radiusPricingEnabled) }
    var baseRadiusKmText by remember(settings.deliveryBaseRadiusKm) {
        mutableStateOf(
            if (settings.deliveryBaseRadiusKm == settings.deliveryBaseRadiusKm.toLong().toDouble()) {
                settings.deliveryBaseRadiusKm.toLong().toString()
            } else {
                settings.deliveryBaseRadiusKm.toString()
            }
        )
    }
    var withinRadiusFeeText by remember(settings.withinRadiusFeePkr) {
        mutableStateOf(settings.withinRadiusFeePkr.toInt().toString())
    }
    var extraPerKmFeeText by remember(settings.extraPerKmFeePkr) {
        mutableStateOf(settings.extraPerKmFeePkr.toInt().toString())
    }

    var areaCity by remember { mutableStateOf(cityOptions.first()) }
    var areaName by remember { mutableStateOf("") }
    var maxKmText by remember { mutableStateOf("5") }
    var areaFeeText by remember { mutableStateOf("150") }
    var editingAreaRule by remember { mutableStateOf<DeliveryAreaFeeRule?>(null) }

    var multanDefaultFeeText by remember(settings.multanDefaultDeliveryFeePkr) {
        mutableStateOf(settings.multanDefaultDeliveryFeePkr.toInt().toString())
    }
    var multanCity by remember { mutableStateOf(multanQuickLocations.firstOrNull() ?: cityOptions.first()) }
    var multanCityFeeText by remember {
        val initialRate = settings.multanCityDeliveryFees
            .firstOrNull { it.city.equals(multanCity, ignoreCase = true) }?.feePkr
            ?: settings.multanDefaultDeliveryFeePkr
        mutableStateOf(initialRate.toInt().toString())
    }
    var alsoAddToApprovedCities by remember { mutableStateOf(true) }
    var editingMultanRule by remember { mutableStateOf<MultanCityFeeRule?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            val previewBaseRadius = (baseRadiusKmText.toDoubleOrNull() ?: 5.0).coerceIn(0.5, 100.0)
            val previewBaseFee = (withinRadiusFeeText.toDoubleOrNull() ?: 100.0).coerceAtLeast(0.0)
            val previewExtraFee = (extraPerKmFeeText.toDoubleOrNull() ?: 25.0).coerceAtLeast(0.0)
            val example8Km = previewBaseFee + kotlin.math.max(0.0, kotlin.math.ceil(8.0 - previewBaseRadius)) * previewExtraFee
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("City Radius Pricing (5 km Base Rule)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Charge one fixed delivery rate up to ${previewBaseRadius.toInt()} km in every city, plus per-km charges beyond the radius.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = radiusEnabled,
                            onCheckedChange = { radiusEnabled = it },
                            modifier = Modifier.testTag("admin_radius_enabled_switch")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = baseRadiusKmText,
                            onValueChange = { baseRadiusKmText = it },
                            label = { Text("Base Radius (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_radius_km_input")
                        )
                        OutlinedTextField(
                            value = withinRadiusFeeText,
                            onValueChange = { withinRadiusFeeText = it },
                            label = { Text("Up to ${previewBaseRadius.toInt()} km (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_radius_fee_input")
                        )
                    }
                    OutlinedTextField(
                        value = extraPerKmFeeText,
                        onValueChange = { extraPerKmFeeText = it },
                        label = { Text("Extra Charge After ${previewBaseRadius.toInt()} km (PKR per km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_radius_extra_per_km_input")
                    )
                    Surface(
                        color = GharTakEmerald.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (radiusEnabled) {
                                "Active Formula: 0–${previewBaseRadius.toInt()} km = ${GharTakConstants.formatPkr(previewBaseFee)} • 8 km = ${GharTakConstants.formatPkr(example8Km)}"
                            } else {
                                "Radius pricing disabled • Using vendor/city base fee (${GharTakConstants.formatPkr(settings.baseDeliveryFeePkr)})"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = GharTakEmerald,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Button(
                        enabled = (baseRadiusKmText.toDoubleOrNull() ?: 0.0) in 0.5..100.0 &&
                            (withinRadiusFeeText.toDoubleOrNull() ?: -1.0) >= 0.0 &&
                            (extraPerKmFeeText.toDoubleOrNull() ?: -1.0) >= 0.0,
                        onClick = {
                            adminVm.saveSettings(
                                settings.copy(
                                    radiusPricingEnabled = radiusEnabled,
                                    deliveryBaseRadiusKm = (baseRadiusKmText.toDoubleOrNull() ?: 5.0).coerceIn(0.5, 100.0),
                                    withinRadiusFeePkr = (withinRadiusFeeText.toDoubleOrNull() ?: 100.0).coerceIn(0.0, 5000.0),
                                    extraPerKmFeePkr = (extraPerKmFeeText.toDoubleOrNull() ?: 25.0).coerceIn(0.0, 1000.0)
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("admin_save_radius_settings_button")
                    ) {
                        Text("Save Radius Delivery Settings")
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Area / Zone Delivery Charges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Optional per-area overrides (e.g. Bahawalpur: Model Town A, Satellite Town, Ibrahim City). Tap Edit on any rule below to modify.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.LocationCity, contentDescription = null, tint = GharTakEmerald)
                    }

                    Text("City", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cityOptions) { city ->
                            FilterChip(selected = areaCity == city, onClick = { areaCity = city }, label = { Text(city) })
                        }
                    }

                    OutlinedTextField(
                        value = areaName,
                        onValueChange = { areaName = it },
                        label = { Text("Area / Housing Society / Zone Name *") },
                        placeholder = { Text("e.g. Model Town A, Satellite Town, Ibrahim City") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_area_name_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = maxKmText,
                            onValueChange = { maxKmText = it },
                            label = { Text("Max Distance (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = areaFeeText,
                            onValueChange = { areaFeeText = it },
                            label = { Text("Delivery Fee (PKR) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_area_fee_input")
                        )
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = areaName.trim().length >= 2 && (areaFeeText.toDoubleOrNull() ?: -1.0) >= 0.0,
                            onClick = {
                                val fee = areaFeeText.toDoubleOrNull() ?: 150.0
                                val maxKm = maxKmText.toDoubleOrNull() ?: 5.0
                                val cleanArea = areaName.trim()
                                val existingId = settings.deliveryAreaFeeRules
                                    .firstOrNull { it.city.equals(areaCity, true) && it.areaName.equals(cleanArea, true) }
                                    ?.id
                                val updated = settings.deliveryAreaFeeRules
                                    .filterNot { it.city.equals(areaCity, true) && it.areaName.equals(cleanArea, true) } +
                                    DeliveryAreaFeeRule(
                                        id = existingId ?: "area_${UUID.randomUUID().toString().take(8)}",
                                        city = areaCity,
                                        areaName = cleanArea,
                                        feePkr = fee,
                                        maxDistanceKm = maxKm
                                    )
                                adminVm.saveSettings(settings.copy(deliveryAreaFeeRules = updated))
                                areaName = ""
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 48.dp)
                                .testTag("admin_add_area_rule_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save Area Fee")
                        }
                        OutlinedButton(
                            onClick = { adminVm.seedStarterCatalog() },
                            modifier = Modifier.weight(0.9f).heightIn(min = 48.dp)
                        ) {
                            Text("Seed Zones")
                        }
                    }
                }
            }
        }

        item {
            Text("Configured Area Rules (${settings.deliveryAreaFeeRules.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        if (settings.deliveryAreaFeeRules.isEmpty()) {
            item {
                EmptyStateView("No area rules configured yet", "Add an area rule above or tap Seed Zones to add Bahawalpur zones.")
            }
        } else {
            items(settings.deliveryAreaFeeRules, key = { "${it.city}_${it.areaName}_${it.id}" }) { rule ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${rule.areaName} (${rule.city})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "${GharTakConstants.formatPkr(rule.feePkr)} • Up to ${rule.maxDistanceKm.toInt()} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = GharTakEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { editingAreaRule = rule },
                                modifier = Modifier.testTag("edit_area_rule_${rule.id}")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Area Rule", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = {
                                    val updated = settings.deliveryAreaFeeRules.filterNot {
                                        it.id == rule.id || (it.city.equals(rule.city, true) && it.areaName.equals(rule.areaName, true))
                                    }
                                    adminVm.saveSettings(settings.copy(deliveryAreaFeeRules = updated))
                                },
                                modifier = Modifier.testTag("delete_area_rule_${rule.id}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Area Rule", tint = GharTakCoralError)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Multan Express Location Delivery Fees", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Set or edit per-location Express delivery fees for 'Anything You Want from Multan' orders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = multanDefaultFeeText,
                            onValueChange = { multanDefaultFeeText = it },
                            label = { Text("Default Multan Fee (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            enabled = (multanDefaultFeeText.toDoubleOrNull() ?: -1.0) >= 0.0,
                            onClick = {
                                val defFee = multanDefaultFeeText.toDoubleOrNull() ?: 350.0
                                adminVm.saveSettings(settings.copy(multanDefaultDeliveryFeePkr = defFee))
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Save Default")
                        }
                    }

                    Text("Select or Type Location / City", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(multanQuickLocations, key = { "admin_multan_quick_${it.lowercase()}" }) { city ->
                            val existingFee = settings.multanCityDeliveryFees.firstOrNull { it.city.equals(city, true) }?.feePkr
                            FilterChip(
                                selected = multanCity.equals(city, ignoreCase = true),
                                onClick = {
                                    multanCity = city
                                    multanCityFeeText = (existingFee ?: settings.multanDefaultDeliveryFeePkr).toInt().toString()
                                },
                                label = {
                                    Text(
                                        if (existingFee != null) "$city • ${GharTakConstants.formatPkr(existingFee)}"
                                        else city
                                    )
                                }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = multanCity,
                        onValueChange = { multanCity = it.take(60) },
                        label = { Text("Location / City Name *") },
                        placeholder = { Text("e.g. Khanewal, Kabirwala, Mian Channu, Abdul Hakim") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_multan_location_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = multanCityFeeText,
                            onValueChange = { multanCityFeeText = it },
                            label = { Text("${multanCity.ifBlank { "Location" }} Fee (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_multan_city_fee_input")
                        )
                        Button(
                            enabled = multanCity.trim().length >= 2 && (multanCityFeeText.toDoubleOrNull() ?: -1.0) >= 0.0,
                            onClick = {
                                val fee = (multanCityFeeText.toDoubleOrNull() ?: 350.0).coerceIn(0.0, 50000.0)
                                val cleanCity = multanCity.trim()
                                val updatedFees = (settings.multanCityDeliveryFees
                                    .filterNot { it.city.trim().equals(cleanCity, ignoreCase = true) } +
                                    MultanCityFeeRule(city = cleanCity, feePkr = fee))
                                    .distinctBy { it.city.trim().lowercase() }
                                val updatedCities = if (alsoAddToApprovedCities &&
                                    settings.cities.none { it.trim().equals(cleanCity, ignoreCase = true) }
                                ) {
                                    (settings.cities + cleanCity).distinctBy { it.trim().lowercase() }
                                } else {
                                    settings.cities
                                }
                                adminVm.saveSettings(
                                    settings.copy(
                                        cities = updatedCities,
                                        multanCityDeliveryFees = updatedFees
                                    )
                                )
                            },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_save_multan_city_fee_button")
                        ) {
                            Text("Set Rate")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = alsoAddToApprovedCities,
                            onCheckedChange = { alsoAddToApprovedCities = it }
                        )
                        Text(
                            "Also show this location in Customer City Picker & Addresses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        val distinctMultanRules = settings.multanCityDeliveryFees.distinctBy { it.city.trim().lowercase() }
        if (distinctMultanRules.isNotEmpty()) {
            item {
                Text("Multan Location Rates (${distinctMultanRules.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            items(distinctMultanRules, key = { it.city.trim().lowercase() }) { rule ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(rule.city, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(GharTakConstants.formatPkr(rule.feePkr), style = MaterialTheme.typography.bodySmall, color = GharTakAmber, fontWeight = FontWeight.SemiBold)
                        }
                        Row {
                            IconButton(
                                onClick = { editingMultanRule = rule },
                                modifier = Modifier.testTag("edit_multan_fee_${rule.city}")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit City Fee", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = {
                                    val updated = settings.multanCityDeliveryFees.filterNot { it.city.trim().equals(rule.city.trim(), true) }
                                    adminVm.saveSettings(settings.copy(multanCityDeliveryFees = updated))
                                },
                                modifier = Modifier.testTag("delete_multan_fee_${rule.city}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete City Fee", tint = GharTakCoralError)
                            }
                        }
                    }
                }
            }
        }
    }

    editingAreaRule?.let { target ->
        var editCity by remember(target) { mutableStateOf(target.city) }
        var editArea by remember(target) { mutableStateOf(target.areaName) }
        var editMaxKm by remember(target) {
            mutableStateOf(
                if (target.maxDistanceKm == target.maxDistanceKm.toLong().toDouble()) {
                    target.maxDistanceKm.toLong().toString()
                } else {
                    target.maxDistanceKm.toString()
                }
            )
        }
        var editFee by remember(target) { mutableStateOf(target.feePkr.toInt().toString()) }
        val valid = editCity.trim().length >= 2 && editArea.trim().length >= 2 && (editFee.toDoubleOrNull() ?: -1.0) >= 0.0

        AlertDialog(
            onDismissRequest = { editingAreaRule = null },
            title = { Text("Edit Location Delivery Charge") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editCity,
                        onValueChange = { editCity = it.take(60) },
                        label = { Text("City *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editArea,
                        onValueChange = { editArea = it.take(80) },
                        label = { Text("Area / Zone *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editMaxKm,
                            onValueChange = { editMaxKm = it },
                            label = { Text("Max KM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editFee,
                            onValueChange = { editFee = it },
                            label = { Text("Fee (PKR) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = valid,
                    onClick = {
                        val updatedRule = target.copy(
                            city = editCity.trim(),
                            areaName = editArea.trim(),
                            maxDistanceKm = (editMaxKm.toDoubleOrNull() ?: target.maxDistanceKm).coerceIn(0.5, 200.0),
                            feePkr = (editFee.toDoubleOrNull() ?: target.feePkr).coerceIn(0.0, 10000.0)
                        )
                        val updatedList = settings.deliveryAreaFeeRules.map {
                            if (it.id == target.id || (it.city.equals(target.city, true) && it.areaName.equals(target.areaName, true))) {
                                updatedRule
                            } else it
                        }
                        adminVm.saveSettings(settings.copy(deliveryAreaFeeRules = updatedList))
                        editingAreaRule = null
                    },
                    modifier = Modifier.testTag("confirm_edit_area_rule_button")
                ) { Text("Update Charge") }
            },
            dismissButton = {
                TextButton(onClick = { editingAreaRule = null }) { Text("Cancel") }
            }
        )
    }

    editingMultanRule?.let { target ->
        var editLocation by remember(target) { mutableStateOf(target.city) }
        var editFee by remember(target) { mutableStateOf(target.feePkr.toInt().toString()) }
        val valid = editLocation.trim().length >= 2 && (editFee.toDoubleOrNull() ?: -1.0) >= 0.0

        AlertDialog(
            onDismissRequest = { editingMultanRule = null },
            title = { Text("Edit Multan Express Charge") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it.take(60) },
                        label = { Text("Delivery City / Location *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editFee,
                        onValueChange = { editFee = it },
                        label = { Text("Express Delivery Fee (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = valid,
                    onClick = {
                        val cleanLoc = editLocation.trim()
                        val fee = (editFee.toDoubleOrNull() ?: target.feePkr).coerceIn(0.0, 50000.0)
                        val updatedList = (settings.multanCityDeliveryFees
                            .filterNot { it.city.trim().equals(target.city.trim(), true) || it.city.trim().equals(cleanLoc, true) } +
                            MultanCityFeeRule(city = cleanLoc, feePkr = fee))
                            .distinctBy { it.city.trim().lowercase() }
                        val updatedCities = if (settings.cities.none { it.trim().equals(cleanLoc, ignoreCase = true) }) {
                            (settings.cities + cleanLoc).distinctBy { it.trim().lowercase() }
                        } else {
                            settings.cities
                        }
                        adminVm.saveSettings(
                            settings.copy(
                                cities = updatedCities,
                                multanCityDeliveryFees = updatedList
                            )
                        )
                        editingMultanRule = null
                    },
                    modifier = Modifier.testTag("confirm_edit_multan_fee_button")
                ) { Text("Update Rate") }
            },
            dismissButton = {
                TextButton(onClick = { editingMultanRule = null }) { Text("Cancel") }
            }
        )
    }
}

