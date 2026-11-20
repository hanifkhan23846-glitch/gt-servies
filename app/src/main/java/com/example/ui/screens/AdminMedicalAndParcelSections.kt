package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.GharTakConstants
import com.example.data.model.MedicalProduct
import com.example.data.model.MedicalStore
import com.example.data.model.ParcelRiderRequest
import com.example.ui.components.AdditionalImagesUploadSection
import com.example.ui.components.DiscountBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GharTakImage
import com.example.ui.components.OperatingHoursBadge
import com.example.ui.components.OperatingHoursEditorDialog
import com.example.ui.components.PhotoPickerUploadRow
import com.example.ui.components.PriceRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.medicalCategoryImageUri
import com.example.ui.components.medicalStoreImageUri
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakMedicalTeal
import com.example.ui.theme.GharTakPink
import com.example.ui.viewmodel.AdminViewModel

@Composable
internal fun AdminAddMedicalSection(
    adminVm: AdminViewModel,
    onSaved: () -> Unit,
    onOpenManage: () -> Unit
) {
    val stores by adminVm.medicalStores.collectAsStateWithLifecycle()
    val categories by adminVm.medicalCategories.collectAsStateWithLifecycle()
    val existingCount by adminVm.medicalProducts.collectAsStateWithLifecycle()

    var selectedStoreId by remember { mutableStateOf("") }
    LaunchedEffect(stores) {
        if (selectedStoreId.isBlank() || stores.none { it.id == selectedStoreId }) {
            selectedStoreId = stores.firstOrNull()?.id ?: ""
        }
    }
    val catNames = categories.map { it.name }.ifEmpty { GharTakConstants.MEDICAL_CATEGORIES }
    var selectedCat by remember(catNames) { mutableStateOf(catNames.first()) }
    var selectedPresetUnit by remember { mutableStateOf(GharTakConstants.MEDICAL_UNITS.first()) }
    var customUnit by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var regPrice by remember { mutableStateOf("") }
    var discPrice by remember { mutableStateOf("0") }
    var stock by remember { mutableStateOf("35") }
    var imageUrl by remember { mutableStateOf("") }
    var additionalImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isAvailable by remember { mutableStateOf(true) }
    var isFeatured by remember { mutableStateOf(false) }
    var showQuickAddStore by remember { mutableStateOf(false) }

    val selectedStore = stores.firstOrNull { it.id == selectedStoreId }
    val resolvedUnit = if (selectedPresetUnit == "Custom") customUnit.trim() else selectedPresetUnit
    val reg = regPrice.toDoubleOrNull() ?: 0.0
    val rawDisc = discPrice.toDoubleOrNull() ?: 0.0
    val stockQty = stock.toLongOrNull() ?: -1L
    val validationError = when {
        selectedStore == null -> "Please select or quick-add a pharmacy first."
        name.trim().length < 2 -> "Enter a medicine/product name (at least 2 characters)."
        resolvedUnit.isBlank() || resolvedUnit.length > 30 -> "Select a pack unit or enter a custom unit."
        reg <= 0.0 -> "Enter a valid Regular Price (PKR > 0)."
        rawDisc < 0.0 || rawDisc > reg -> "Discount Price must be between 0 and Regular Price."
        stockQty < 0L -> "Available stock cannot be negative."
        else -> null
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Add Medical Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GharTakMedicalTeal)
                    Text("Live synced to all customer devices via Cloud Firestore", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onOpenManage) { Text("Manage (${existingCount.size})") }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Pharmacy / Medical Store *", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { showQuickAddStore = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Pharmacy")
                }
            }
            if (stores.isEmpty()) {
                Text("No pharmacies exist yet. Tap '+ New Pharmacy' or initialize catalog on Dashboard.", color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stores) { s ->
                        FilterChip(selected = selectedStoreId == s.id, onClick = { selectedStoreId = s.id }, label = { Text(s.name) })
                    }
                }
            }
        }
        item {
            Text("Medical Category *", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(catNames) { c ->
                    FilterChip(selected = selectedCat == c, onClick = { selectedCat = c }, label = { Text(c) })
                }
            }
        }
        item {
            Text("Pack or Unit *", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GharTakConstants.MEDICAL_UNITS + "Custom") { u ->
                    FilterChip(selected = selectedPresetUnit == u, onClick = { selectedPresetUnit = u }, label = { Text(u) })
                }
            }
            if (selectedPresetUnit == "Custom") {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = customUnit,
                    onValueChange = { customUnit = it },
                    label = { Text("Custom Unit (e.g. 120 ml, 10 tablets)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            PhotoPickerUploadRow(
                imageUrl = imageUrl,
                onImageChanged = { imageUrl = it },
                label = "Product Photo (Gallery / Files / Camera)",
                isMedicalFallback = true
            )
            TextButton(onClick = { imageUrl = medicalCategoryImageUri(selectedCat, name) }) {
                Text("Use '$selectedCat' Stock Photo")
            }
        }
        item {
            AdditionalImagesUploadSection(
                images = additionalImages,
                onImagesChanged = { additionalImages = it },
                isMedicalFallback = true
            )
        }
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medical Product Name *") }, modifier = Modifier.fillMaxWidth().testTag("admin_medical_name_input")) }
        item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Usage / Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = regPrice,
                    onValueChange = { regPrice = it },
                    label = { Text("Regular Price (PKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("admin_medical_price_input")
                )
                OutlinedTextField(
                    value = discPrice,
                    onValueChange = { discPrice = it },
                    label = { Text("Discount Price (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Available Stock") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Availability Status")
                Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Featured Product")
                Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
            }
        }
        if (validationError != null) {
            item { Text(validationError, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            Button(
                enabled = validationError == null,
                colors = ButtonDefaults.buttonColors(containerColor = GharTakMedicalTeal),
                onClick = {
                    val store = selectedStore!!
                    adminVm.saveMedicalProduct(
                        MedicalProduct(
                            storeId = store.id,
                            storeName = store.name,
                            ownerId = store.ownerId.ifBlank { adminVm.adminId },
                            categoryId = "mcat_${catNames.indexOf(selectedCat).coerceAtLeast(0) + 1}",
                            categoryName = selectedCat,
                            name = name.trim(),
                            description = desc.trim(),
                            regularPricePkr = reg,
                            discountPricePkr = rawDisc,
                            unit = resolvedUnit,
                            stockQuantity = stockQty.coerceAtLeast(0L),
                            imageUrl = imageUrl.ifBlank { null },
                            additionalImages = additionalImages,
                            isAvailable = isAvailable,
                            isFeatured = isFeatured,
                            city = store.city
                        )
                    )
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().testTag("save_medical_product_button")
            ) { Text("Save Medical Product") }
        }
    }

    if (showQuickAddStore) {
        QuickVendorDialog(
            title = "Quick Add Medical Store / Pharmacy",
            nameLabel = "Pharmacy Name",
            onDismiss = { showQuickAddStore = false },
            onCreate = { vName, vAddress, vCity, vPhone ->
                adminVm.saveMedicalStoreAdmin(
                    MedicalStore(
                        name = vName,
                        ownerName = "GT Service Pharmacy Partner",
                        phone = vPhone,
                        address = vAddress,
                        city = vCity
                    ),
                    onCreated = { newId -> selectedStoreId = newId }
                )
                showQuickAddStore = false
            }
        )
    }
}

@Composable
internal fun AdminManageMedicalSection(
    adminVm: AdminViewModel,
    onOpenAdd: () -> Unit
) {
    val products by adminVm.medicalProducts.collectAsStateWithLifecycle()
    val stores by adminVm.medicalStores.collectAsStateWithLifecycle()
    val categories by adminVm.medicalCategories.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var editing by remember { mutableStateOf<MedicalProduct?>(null) }
    var deletingTarget by remember { mutableStateOf<MedicalProduct?>(null) }

    val catNames = listOf("All") + categories.map { it.name }.ifEmpty { GharTakConstants.MEDICAL_CATEGORIES }
    val filtered = products.filter { prod ->
        val matchesQuery = search.isBlank() ||
            prod.name.contains(search, ignoreCase = true) ||
            prod.storeName.contains(search, ignoreCase = true) ||
            prod.categoryName.contains(search, ignoreCase = true)
        val matchesCat = selectedCategory == "All" || prod.categoryName.equals(selectedCategory, ignoreCase = true)
        matchesQuery && matchesCat
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Medical Inventory (${filtered.size}/${products.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onOpenAdd, colors = ButtonDefaults.buttonColors(containerColor = GharTakMedicalTeal)) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Medical")
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search medicines by name, category, or pharmacy...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(catNames) { c ->
                    FilterChip(selected = selectedCategory == c, onClick = { selectedCategory = c }, label = { Text(c) })
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateView("No medical products match", "Try clearing filters or add a new medical item.", "Add Medical Product", onOpenAdd)
            }
        } else {
            items(filtered, key = { it.id }) { prod ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val displayImage = prod.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_medical" }
                            ?: medicalCategoryImageUri(prod.categoryName, prod.name)
                        Box {
                            GharTakImage(displayImage, prod.name, Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)), isMedicalFallback = true)
                            if (prod.discountPercent > 0) {
                                DiscountBadge(prod.discountPercent, Modifier.align(Alignment.TopStart).padding(4.dp))
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${prod.storeName} • ${prod.categoryName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Unit: ${prod.unit} • Stock: ${prod.stockQuantity}", style = MaterialTheme.typography.labelSmall)
                            PriceRow(prod.regularPricePkr, prod.discountPricePkr, prod.unit)
                            StatusBadge(if (prod.isAvailable && prod.stockQuantity > 0L) "In Stock" else if (!prod.isAvailable) "Inactive" else "Out of Stock")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(checked = prod.isAvailable, onCheckedChange = { adminVm.saveMedicalProduct(prod.copy(isAvailable = it)) })
                            Row {
                                IconButton(onClick = { editing = prod }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                                IconButton(onClick = { deletingTarget = prod }) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = GharTakCoralError) }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { prod ->
        val store = stores.firstOrNull { it.id == prod.storeId } ?: stores.firstOrNull()
        if (store != null) {
            MedicalProductEditorDialog(
                store = store,
                existing = prod,
                availableStores = stores,
                availableCategories = categories.map { it.name }.ifEmpty { GharTakConstants.MEDICAL_CATEGORIES },
                onDismiss = { editing = null },
                onSave = { updated ->
                    adminVm.saveMedicalProduct(updated)
                    editing = null
                }
            )
        }
    }

    deletingTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingTarget = null },
            title = { Text("Delete Medical Product?") },
            text = { Text("Permanently remove '${target.name}' from GT Service Pharmacy?") },
            confirmButton = {
                Button(onClick = {
                    adminVm.deleteMedicalProduct(target)
                    deletingTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
internal fun MedicalProductEditorDialog(
    store: MedicalStore,
    existing: MedicalProduct,
    availableStores: List<MedicalStore>,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (MedicalProduct) -> Unit
) {
    var selectedStoreId by remember(existing) { mutableStateOf(existing.storeId.ifBlank { store.id }) }
    var name by remember(existing) { mutableStateOf(existing.name) }
    val catOptions = availableCategories.ifEmpty { GharTakConstants.MEDICAL_CATEGORIES }
    var category by remember(existing) { mutableStateOf(existing.categoryName.ifBlank { catOptions.first() }) }
    var unit by remember(existing) { mutableStateOf(existing.unit) }
    var desc by remember(existing) { mutableStateOf(existing.description) }
    var price by remember(existing) { mutableStateOf(existing.regularPricePkr.toInt().toString()) }
    var discountPrice by remember(existing) { mutableStateOf(existing.discountPricePkr.toInt().toString()) }
    var stock by remember(existing) { mutableStateOf(existing.stockQuantity.toString()) }
    var imageUrl by remember(existing) { mutableStateOf(existing.imageUrl ?: "") }
    var isAvailable by remember(existing) { mutableStateOf(existing.isAvailable) }

    val activeStore = availableStores.firstOrNull { it.id == selectedStoreId } ?: store
    val reg = price.toDoubleOrNull() ?: 0.0
    val rawDisc = discountPrice.toDoubleOrNull() ?: 0.0
    val stockQty = stock.toLongOrNull() ?: -1L
    val isValid = name.trim().length >= 2 && unit.isNotBlank() && reg > 0.0 && rawDisc in 0.0..reg && stockQty >= 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Medical Product") },
        text = {
            LazyColumn(modifier = Modifier.height(430.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PhotoPickerUploadRow(
                        imageUrl = imageUrl,
                        onImageChanged = { imageUrl = it },
                        label = "Product Image",
                        isMedicalFallback = true
                    )
                }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(catOptions) { c ->
                            FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                        }
                    }
                }
                item { OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit / Pack") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountPrice,
                            onValueChange = { discountPrice = it },
                            label = { Text("Discount (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Available")
                        Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    onSave(
                        existing.copy(
                            storeId = activeStore.id,
                            storeName = activeStore.name,
                            categoryName = category,
                            name = name.trim(),
                            description = desc.trim(),
                            regularPricePkr = reg,
                            discountPricePkr = rawDisc,
                            unit = unit.trim(),
                            stockQuantity = stockQty,
                            imageUrl = imageUrl.ifBlank { null },
                            isAvailable = isAvailable,
                            city = activeStore.city
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
internal fun AdminMedicalStoresSection(adminVm: AdminViewModel) {
    val stores by adminVm.medicalStores.collectAsStateWithLifecycle()
    var hoursTargetStore by remember { mutableStateOf<com.example.data.model.MedicalStore?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Medical Stores (${stores.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { adminVm.seedStarterCatalog() }) {
                    Text("Sync Pharmacies")
                }
            }
        }
        items(stores, key = { it.id }) { s ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        GharTakImage(
                            imageUrl = medicalStoreImageUri(s.name, s.imageUrl),
                            contentDescription = s.name,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                            isMedicalFallback = true
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(s.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                StatusBadge(s.approvalStatus)
                            }
                            Text("${s.ownerName} • ${s.phone} • ${s.city}", style = MaterialTheme.typography.bodySmall)
                            Text("${s.address} (${s.city})", style = MaterialTheme.typography.labelMedium)
                            OperatingHoursBadge(isOpen = s.isOpen, openingTime = s.openingTime, closingTime = s.closingTime)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Store Open / Accepting Orders", style = MaterialTheme.typography.labelMedium)
                        Switch(
                            checked = s.isOpen,
                            onCheckedChange = { nextOpen ->
                                adminVm.updateVendorHoursAndOpen("medicalStores", s.id, s.name, nextOpen, s.openingTime, s.closingTime)
                            }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { adminVm.updateMedicalStoreApproval(s, "approved") }) { Text("Approve") }
                        OutlinedButton(onClick = { adminVm.updateMedicalStoreApproval(s, "rejected") }) { Text("Reject") }
                        OutlinedButton(onClick = { adminVm.updateMedicalStoreApproval(s, "suspended") }) { Text("Suspend") }
                    }
                    OutlinedButton(
                        onClick = { hoursTargetStore = s },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Edit Operating Hours")
                    }
                }
            }
        }
    }

    hoursTargetStore?.let { store ->
        OperatingHoursEditorDialog(
            vendorName = store.name,
            initialIsOpen = store.isOpen,
            initialOpeningTime = store.openingTime,
            initialClosingTime = store.closingTime,
            onDismiss = { hoursTargetStore = null },
            onSave = { isOpenVal, openTime, closeTime ->
                adminVm.updateVendorHoursAndOpen("medicalStores", store.id, store.name, isOpenVal, openTime, closeTime)
                hoursTargetStore = null
            }
        )
    }
}

@Composable
internal fun AdminParcelRiderRequestsSection(adminVm: AdminViewModel) {
    val requests by adminVm.parcelRiderRequests.collectAsStateWithLifecycle()
    var reviewingTarget by remember { mutableStateOf<Pair<ParcelRiderRequest, String>?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(
                "Send Parcel & Hire Rider Requests (${requests.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Requests from Customers & Restaurants. Only requests approved by Super Admin become visible to GT Service riders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (requests.isEmpty()) {
            item {
                EmptyStateView(
                    "No parcel or rider hire requests",
                    "Requests submitted by Customers or Restaurants for Send Parcel or Hire a Rider will appear here for approval."
                )
            }
        } else {
            items(requests, key = { it.id }) { req ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${if (req.requestType == "send_parcel") "Send Parcel" else "Hire Rider"} • ${req.restaurantName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(req.approvalStatus.replace('_', ' '))
                        }
                        Text("Pickup: ${req.pickupAddress}", style = MaterialTheme.typography.bodySmall)
                        Text("Dropoff: ${req.dropoffAddress}", style = MaterialTheme.typography.bodySmall)
                        if (req.recipientName.isNotBlank()) {
                            Text("Contact: ${req.recipientName} (${req.recipientPhone})", style = MaterialTheme.typography.bodySmall)
                        }
                        if (req.details.isNotBlank()) {
                            Text(req.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "Offered Fee: ${GharTakConstants.formatPkr(req.offeredFeePkr)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GharTakEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        if (req.riderName.isNotBlank()) {
                            Text("Claimed by Rider: ${req.riderName}", style = MaterialTheme.typography.labelMedium, color = GharTakPink)
                        }
                        if (req.adminNote.isNotBlank()) {
                            Text("Admin Note: ${req.adminNote}", style = MaterialTheme.typography.labelSmall)
                        }
                        if (req.approvalStatus == "pending") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { reviewingTarget = req to "approved" },
                                    modifier = Modifier.testTag("admin_approve_parcel_${req.id}")
                                ) {
                                    Text("Approve")
                                }
                                OutlinedButton(
                                    onClick = { reviewingTarget = req to "rejected" },
                                    modifier = Modifier.testTag("admin_reject_parcel_${req.id}")
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    reviewingTarget?.let { (req, decision) ->
        var adminNote by remember(req, decision) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reviewingTarget = null },
            title = { Text(if (decision == "approved") "Approve Request?" else "Reject Request?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${if (req.requestType == "send_parcel") "Send Parcel" else "Hire Rider"} for ${req.restaurantName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Admin Note (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.reviewParcelRiderRequest(req, decision, adminNote.trim())
                    reviewingTarget = null
                }) {
                    Text(if (decision == "approved") "Approve & Dispatch to Riders" else "Confirm Reject")
                }
            },
            dismissButton = { TextButton(onClick = { reviewingTarget = null }) { Text("Cancel") } }
        )
    }
}
