package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodItem
import com.example.data.model.GharTakConstants
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.OrderRecord
import com.example.data.model.ParcelRiderRequest
import com.example.data.model.Restaurant
import com.example.data.model.RiderProfile
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.AccessTime
import com.example.ui.components.AdditionalImagesUploadSection
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FoodDayBadges
import com.example.ui.components.GharTakImage
import com.example.ui.components.MetricCard
import com.example.ui.components.OperatingHoursBadge
import com.example.ui.components.OperatingHoursEditorDialog
import com.example.ui.components.PhotoPickerUploadRow
import com.example.ui.components.PriceRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrackingRouteAndDistanceDialog
import com.example.ui.components.calculateDistanceKm
import com.example.ui.components.foodCategoryImageUri
import com.example.ui.components.formatDistanceWithEta
import com.example.ui.components.groceryCategoryImageUri
import com.example.ui.components.openGoogleMapsDirections
import com.example.ui.components.openGoogleMapsLocation
import com.example.ui.components.rememberDeviceLocationState
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.PartnerViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantPortalScreen(
    partnerVm: PartnerViewModel,
    onExitToCustomer: () -> Unit
) {
    BackHandler { onExitToCustomer() }

    val restaurants by partnerVm.myRestaurants.collectAsStateWithLifecycle()
    val foodItems by partnerVm.myFoodItems.collectAsStateWithLifecycle()
    val orders by partnerVm.myVendorOrders.collectAsStateWithLifecycle()
    val parcelRequests by partnerVm.myParcelRiderRequests.collectAsStateWithLifecycle()
    val staffSessionIds by partnerVm.staffSessionRestaurantIds.collectAsStateWithLifecycle()
    val selectedRestId by partnerVm.selectedRestaurantId.collectAsStateWithLifecycle()
    val staffLoginLoading by partnerVm.staffLoginLoading.collectAsStateWithLifecycle()
    val msg by partnerVm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(msg) {
        if (msg != null) {
            snackbar.showSnackbar(msg!!)
            partnerVm.clearMessage()
        }
    }

    val myRest = restaurants.firstOrNull { it.id == selectedRestId } ?: restaurants.firstOrNull()
    val restFoodItems = if (myRest != null) foodItems.filter { it.restaurantId == myRest.id } else emptyList()
    val restOrders = if (myRest != null) orders.filter { it.orderType == "food" && it.vendorId == myRest.id } else emptyList()
    val restParcelReqs = if (myRest != null) parcelRequests.filter { it.restaurantId == myRest.id } else emptyList()

    var unauthedMode by remember { mutableStateOf(0) } // 0: Staff Login, 1: Register New Restaurant
    var tab by remember { mutableStateOf(0) } // 0: Orders, 1: Menu, 2: Parcel/Rider, 3: Profile
    var editingFood by remember { mutableStateOf<FoodItem?>(null) }
    var activeRequestTypeDialog by remember { mutableStateOf<String?>(null) }
    var showSwitchLoginDialog by remember { mutableStateOf(false) }
    var showHoursDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = gtNavyTopAppBarColors(),
                title = { Text(myRest?.name ?: "Restaurant Portal") },
                navigationIcon = {
                    IconButton(onClick = onExitToCustomer) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Customer") }
                },
                actions = {
                    if (myRest != null) {
                        TextButton(
                            onClick = { showSwitchLoginDialog = true },
                            modifier = Modifier.testTag("rest_switch_login_button")
                        ) {
                            Text("Switch Login", color = Color.White)
                        }
                        if (myRest.id in staffSessionIds) {
                            IconButton(
                                onClick = { partnerVm.logoutFromRestaurantPortal(myRest.id) },
                                modifier = Modifier.testTag("rest_portal_logout_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out Restaurant")
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (myRest == null) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    listOf("Restaurant Login", "Register Restaurant").forEachIndexed { idx, label ->
                        SegmentedButton(
                            selected = unauthedMode == idx,
                            onClick = { unauthedMode = idx },
                            shape = SegmentedButtonDefaults.itemShape(idx, 2)
                        ) {
                            Text(label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (unauthedMode == 0) {
                    RestaurantPortalLoginSection(
                        isLoading = staffLoginLoading,
                        onSubmit = { loginId, password ->
                            partnerVm.loginToRestaurantPortal(loginId, password)
                        }
                    )
                } else {
                    RestaurantRegistrationForm(
                        existing = null,
                        defaultOwnerName = partnerVm.currentUserName,
                        onSubmit = { partnerVm.registerOrUpdateRestaurant(it) }
                    )
                }
            } else {
                if (restaurants.size > 1) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(restaurants, key = { it.id }) { restOption ->
                            FilterChip(
                                selected = restOption.id == myRest.id,
                                onClick = { partnerVm.selectActiveRestaurant(restOption.id) },
                                label = { Text(restOption.name) }
                            )
                        }
                    }
                }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    listOf("Orders", "Menu (${restFoodItems.size})", "Parcel/Rider", "Profile").forEachIndexed { idx, label ->
                        SegmentedButton(
                            selected = tab == idx,
                            onClick = { tab = idx },
                            shape = SegmentedButtonDefaults.itemShape(idx, 4)
                        ) {
                            Text(
                                label,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                when (tab) {
                    0 -> {
                        val deliveredSales = restOrders.filter { it.status == "Delivered" }.sumOf { it.vendorEarningsPkr }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f)) {
                                                Text("Account Status", style = MaterialTheme.typography.labelMedium)
                                                Text(
                                                    "Commission: ${myRest.commissionPercent}%" +
                                                        if (myRest.portalLoginId.isNotBlank()) " • Login ID: ${myRest.portalLoginId}" else "",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            StatusBadge(myRest.approvalStatus)
                                        }
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OperatingHoursBadge(
                                                isOpen = myRest.isOpen,
                                                openingTime = myRest.openingTime,
                                                closingTime = myRest.closingTime,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            OutlinedButton(
                                                onClick = { showHoursDialog = true },
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("rest_portal_edit_hours_button")
                                            ) {
                                                Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Hours / Open-Close")
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MetricCard("Total Orders", "${restOrders.size}", Modifier.weight(1f))
                                    MetricCard("Net Earnings", GharTakConstants.formatPkr(deliveredSales), Modifier.weight(1f), GharTakEmerald)
                                }
                            }
                            if (restOrders.isEmpty()) {
                                item { EmptyStateView("No incoming food orders", "Orders placed by customers will appear here in real time.") }
                            } else {
                                items(restOrders, key = { it.id }) { order ->
                                    VendorOrderActionCard(order = order, onUpdateStatus = { st -> partnerVm.updateVendorOrderStatus(order, st) })
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                    Text(
                                        "Food menu items are added exclusively by Super Admin. You can edit pricing or stock on existing dishes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                            if (restFoodItems.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        title = "No food items yet",
                                        subtitle = "Dishes added by Super Admin for ${myRest.name} will appear here."
                                    )
                                }
                            } else {
                                items(restFoodItems, key = { it.id }) { item ->
                                    Card(Modifier.fillMaxWidth()) {
                                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            val displayImage = item.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_food" }
                                                ?: foodCategoryImageUri(item.categoryName, item.name)
                                            GharTakImage(displayImage, item.name, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text("${item.categoryName} • Stock: ${item.stockQuantity}", style = MaterialTheme.typography.bodySmall)
                                                FoodDayBadges(availableDays = item.availableDays, isDailySpecial = item.isDailySpecial)
                                                PriceRow(item.regularPricePkr, item.discountPricePkr)
                                            }
                                            IconButton(onClick = { editingFood = item }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                                            IconButton(onClick = { partnerVm.deleteFoodItem(item.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Send Parcel & Hire a Rider", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Submit a request to dispatch a parcel or hire a dedicated rider. Super Admin approval is required before riders receive the job.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Button(
                                                onClick = { activeRequestTypeDialog = "send_parcel" },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("rest_send_parcel_button")
                                            ) {
                                                Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Send Parcel")
                                            }
                                            OutlinedButton(
                                                onClick = { activeRequestTypeDialog = "hire_rider" },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("rest_hire_rider_button")
                                            ) {
                                                Icon(Icons.Filled.TwoWheeler, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Hire a Rider")
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Text("My Dispatch Requests (${restParcelReqs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            if (restParcelReqs.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        "No parcel or rider requests yet",
                                        "Tap Send Parcel or Hire a Rider above to submit a request for Admin approval."
                                    )
                                }
                            } else {
                                items(restParcelReqs, key = { it.id }) { req ->
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    if (req.requestType == "send_parcel") "Send Parcel" else "Hire a Rider",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                StatusBadge(req.approvalStatus.replace('_', ' '))
                                            }
                                            Text("Pickup: ${req.pickupAddress}", style = MaterialTheme.typography.bodySmall)
                                            Text("Dropoff: ${req.dropoffAddress}", style = MaterialTheme.typography.bodySmall)
                                            if (req.recipientName.isNotBlank()) {
                                                Text("Recipient: ${req.recipientName} (${req.recipientPhone})", style = MaterialTheme.typography.bodySmall)
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
                                                Text("Assigned Rider: ${req.riderName}", style = MaterialTheme.typography.labelMedium, color = GharTakPink)
                                            }
                                            if (req.adminNote.isNotBlank()) {
                                                Text("Admin Note: ${req.adminNote}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> RestaurantRegistrationForm(myRest, partnerVm.currentUserName) { partnerVm.registerOrUpdateRestaurant(it) }
                }
            }
        }
    }

    if (showSwitchLoginDialog) {
        var loginId by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPass by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSwitchLoginDialog = false },
            title = { Text("Sign In Another Restaurant") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the Login ID and Password assigned by Super Admin for that restaurant.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { loginId = it },
                        label = { Text("Restaurant Login ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPass) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = loginId.trim().length >= 3 && password.length >= 4 && !staffLoginLoading,
                    onClick = {
                        partnerVm.loginToRestaurantPortal(loginId, password) {
                            showSwitchLoginDialog = false
                        }
                    }
                ) {
                    Text(if (staffLoginLoading) "Signing in..." else "Sign In")
                }
            },
            dismissButton = { TextButton(onClick = { showSwitchLoginDialog = false }) { Text("Cancel") } }
        )
    }

    editingFood?.let { targetFood ->
        if (myRest != null) {
            FoodItemEditorDialog(
                restaurant = myRest,
                existing = targetFood,
                onDismiss = { editingFood = null },
                onSave = { item ->
                    partnerVm.saveFoodItem(item)
                    editingFood = null
                }
            )
        }
    }

    activeRequestTypeDialog?.let { reqType ->
        if (myRest != null) {
            ParcelRiderRequestDialog(
                requestType = reqType,
                restaurant = myRest,
                onDismiss = { activeRequestTypeDialog = null },
                onSubmit = { pickup, dropoff, recipientName, recipientPhone, details, fee ->
                    partnerVm.submitParcelRiderRequest(
                        requestType = reqType,
                        restaurant = myRest,
                        pickupAddress = pickup,
                        dropoffAddress = dropoff,
                        recipientName = recipientName,
                        recipientPhone = recipientPhone,
                        details = details,
                        offeredFeePkr = fee
                    )
                    activeRequestTypeDialog = null
                }
            )
        }
    }

    if (showHoursDialog && myRest != null) {
        OperatingHoursEditorDialog(
            vendorName = myRest.name,
            initialIsOpen = myRest.isOpen,
            initialOpeningTime = myRest.openingTime,
            initialClosingTime = myRest.closingTime,
            onDismiss = { showHoursDialog = false },
            onSave = { isOpenVal, openTime, closeTime ->
                partnerVm.updateRestaurantHoursAndOpen(myRest, isOpenVal, openTime, closeTime)
                showHoursDialog = false
            }
        )
    }
}

@Composable
private fun RestaurantPortalLoginSection(
    isLoading: Boolean,
    onSubmit: (loginId: String, password: String) -> Unit
) {
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Key, contentDescription = null, tint = GharTakPink)
                        Text(
                            "Sign In to Your Restaurant",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Use the Restaurant Login ID and Password created by GT Service Super Admin to manage your orders, menu, and rider dispatches from any device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { loginId = it },
                        label = { Text("Restaurant Login ID") },
                        placeholder = { Text("e.g. royal_biryani") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("restaurant_portal_login_id_input")
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPass) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("restaurant_portal_password_input")
                    )
                    Button(
                        enabled = !isLoading && loginId.trim().length >= 3 && password.length >= 4,
                        onClick = { onSubmit(loginId, password) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("restaurant_portal_login_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Sign In to Restaurant Portal")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryPortalScreen(
    partnerVm: PartnerViewModel,
    onExitToCustomer: () -> Unit
) {
    BackHandler { onExitToCustomer() }

    val stores by partnerVm.myGroceryStores.collectAsStateWithLifecycle()
    val products by partnerVm.myGroceryProducts.collectAsStateWithLifecycle()
    val orders by partnerVm.myVendorOrders.collectAsStateWithLifecycle()
    val msg by partnerVm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(msg) {
        if (msg != null) {
            snackbar.showSnackbar(msg!!)
            partnerVm.clearMessage()
        }
    }

    val myStore = stores.firstOrNull()
    val groceryOrders = orders.filter { it.orderType == "grocery" }
    var tab by remember { mutableStateOf(0) }
    var editingProduct by remember { mutableStateOf<GroceryProduct?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showHoursDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = gtNavyTopAppBarColors(),
                title = { Text(myStore?.name ?: "Grocery Store Partner") },
                navigationIcon = {
                    IconButton(onClick = onExitToCustomer) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (myStore != null && tab == 1) {
                FloatingActionButton(onClick = { editingProduct = null; showProductDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Grocery Product")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (myStore == null) {
                GroceryStoreRegistrationForm(
                    existing = null,
                    defaultOwnerName = partnerVm.currentUserName,
                    onSubmit = { partnerVm.registerOrUpdateGroceryStore(it) }
                )
            } else {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    listOf("Orders", "Inventory (${products.size})", "Store").forEachIndexed { idx, label ->
                        SegmentedButton(
                            selected = tab == idx,
                            onClick = { tab = idx },
                            shape = SegmentedButtonDefaults.itemShape(idx, 3)
                        ) { Text(label) }
                    }
                }
                when (tab) {
                    0 -> {
                        val deliveredSales = groceryOrders.filter { it.status == "Delivered" }.sumOf { it.vendorEarningsPkr }
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f)) {
                                                Text("Store Verification", style = MaterialTheme.typography.labelMedium)
                                                Text("Commission: ${myStore.commissionPercent}%", style = MaterialTheme.typography.bodySmall)
                                            }
                                            StatusBadge(myStore.approvalStatus)
                                        }
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OperatingHoursBadge(
                                                isOpen = myStore.isOpen,
                                                openingTime = myStore.openingTime,
                                                closingTime = myStore.closingTime,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            OutlinedButton(
                                                onClick = { showHoursDialog = true },
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("grocery_portal_edit_hours_button")
                                            ) {
                                                Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Hours / Open-Close")
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MetricCard("Grocery Orders", "${groceryOrders.size}", Modifier.weight(1f))
                                    MetricCard("Net Earnings", GharTakConstants.formatPkr(deliveredSales), Modifier.weight(1f), GharTakEmerald)
                                }
                            }
                            if (groceryOrders.isEmpty()) {
                                item { EmptyStateView("No incoming grocery orders", "Grocery orders will appear here in real time.") }
                            } else {
                                items(groceryOrders, key = { it.id }) { order ->
                                    VendorOrderActionCard(order = order, onUpdateStatus = { st -> partnerVm.updateVendorOrderStatus(order, st) })
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (products.isEmpty()) {
                                item { EmptyStateView("No grocery products yet", "Tap + to add items to your store.", "Add First Product") { editingProduct = null; showProductDialog = true } }
                            } else {
                                items(products, key = { it.id }) { prod ->
                                    Card(Modifier.fillMaxWidth()) {
                                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            val displayImage = prod.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_grocery" }
                                                ?: groceryCategoryImageUri(prod.categoryName, prod.name)
                                            GharTakImage(displayImage, prod.name, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), isGroceryFallback = true)
                                            Column(Modifier.weight(1f)) {
                                                Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text("${prod.categoryName} • Stock: ${prod.stockQuantity} ${prod.unit}", style = MaterialTheme.typography.bodySmall)
                                                PriceRow(prod.regularPricePkr, prod.discountPricePkr, prod.unit)
                                            }
                                            IconButton(onClick = { editingProduct = prod; showProductDialog = true }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                                            IconButton(onClick = { partnerVm.deleteGroceryProduct(prod.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> GroceryStoreRegistrationForm(myStore, partnerVm.currentUserName) { partnerVm.registerOrUpdateGroceryStore(it) }
                }
            }
        }
    }

    if (showProductDialog && myStore != null) {
        GroceryProductEditorDialog(
            store = myStore,
            existing = editingProduct,
            onDismiss = { showProductDialog = false },
            onSave = { prod ->
                partnerVm.saveGroceryProduct(prod)
                showProductDialog = false
            }
        )
    }

    if (showHoursDialog && myStore != null) {
        OperatingHoursEditorDialog(
            vendorName = myStore.name,
            initialIsOpen = myStore.isOpen,
            initialOpeningTime = myStore.openingTime,
            initialClosingTime = myStore.closingTime,
            onDismiss = { showHoursDialog = false },
            onSave = { isOpenVal, openTime, closeTime ->
                partnerVm.updateGroceryStoreHoursAndOpen(myStore, isOpenVal, openTime, closeTime)
                showHoursDialog = false
            }
        )
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderPortalScreen(
    partnerVm: PartnerViewModel,
    onExitToCustomer: () -> Unit
) {
    BackHandler { onExitToCustomer() }

    val context = LocalContext.current
    val rider by partnerVm.riderProfile.collectAsStateWithLifecycle()
    val available by partnerVm.availableRiderOrders.collectAsStateWithLifecycle()
    val assigned by partnerVm.assignedRiderOrders.collectAsStateWithLifecycle()
    val availableParcelJobs by partnerVm.availableParcelRiderJobs.collectAsStateWithLifecycle()
    val assignedParcelJobs by partnerVm.assignedParcelRiderJobs.collectAsStateWithLifecycle()
    val msg by partnerVm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val deviceLocationState = rememberDeviceLocationState()
    var routeInspectOrder by remember { mutableStateOf<OrderRecord?>(null) }

    val locLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    partnerVm.toggleRiderOnline(rider?.isOnline ?: true, loc.latitude, loc.longitude)
                }
            }
        }
    }

    LaunchedEffect(msg) {
        if (msg != null) {
            snackbar.showSnackbar(msg!!)
            partnerVm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = gtNavyTopAppBarColors(),
                title = { Text("GT Service Rider") },
                navigationIcon = {
                    IconButton(onClick = onExitToCustomer) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val currentRider = rider
        if (currentRider == null) {
            RiderRegistrationForm(
                defaultName = partnerVm.currentUserName,
                onSubmit = { partnerVm.registerRider(it) },
                modifier = Modifier.padding(padding)
            )
        } else {
            val completedOrders = assigned.filter { it.status == "Delivered" }
            val earnedPkr = completedOrders.sumOf { it.riderEarningsPkr }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(currentRider.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${currentRider.vehicleType} (${currentRider.vehicleNumber}) • ${currentRider.city}", style = MaterialTheme.typography.bodySmall)
                                }
                                StatusBadge(currentRider.approvalStatus)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(if (currentRider.isOnline) "Status: Online & Receiving Requests" else "Status: Offline", style = MaterialTheme.typography.labelLarge)
                                Switch(
                                    checked = currentRider.isOnline,
                                    enabled = currentRider.approvalStatus == "approved",
                                    onCheckedChange = { partnerVm.toggleRiderOnline(it, currentRider.latitude, currentRider.longitude) }
                                )
                            }
                            OutlinedButton(onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    fusedClient.lastLocation.addOnSuccessListener { loc ->
                                        if (loc != null) partnerVm.toggleRiderOnline(currentRider.isOnline, loc.latitude, loc.longitude)
                                    }
                                } else {
                                    locLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }) {
                                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Sync Live GPS Location")
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Completed", "${completedOrders.size}", Modifier.weight(1f))
                        MetricCard("Earnings", GharTakConstants.formatPkr(earnedPkr), Modifier.weight(1f), GharTakEmerald)
                    }
                }
                item { Text("Available Delivery Requests (${available.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                if (available.isEmpty()) {
                    item { Text("No orders currently Ready for Pickup.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(available, key = { it.id }) { order ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pickup: ${order.vendorName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(GharTakConstants.formatPkr(order.riderEarningsPkr), color = GharTakEmerald, fontWeight = FontWeight.Bold)
                                }
                                Text("Dropoff: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
                                Button(
                                    onClick = { partnerVm.acceptDelivery(order) },
                                    enabled = currentRider.approvalStatus == "approved" && currentRider.isOnline
                                ) {
                                    Text("Accept Delivery")
                                }
                            }
                        }
                    }
                }
                item { Text("My Assigned Deliveries (${assigned.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(assigned, key = { it.id }) { order ->
                    val riderToDropoffKm = remember(currentRider.latitude, currentRider.longitude, order.deliveryLat, order.deliveryLng) {
                        calculateDistanceKm(currentRider.latitude, currentRider.longitude, order.deliveryLat, order.deliveryLng)
                    }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${order.vendorName} → ${order.customerName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                StatusBadge(order.status)
                            }
                            Text("Dropoff: ${order.deliveryAddress} • ${order.customerPhone}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Route Distance to Address: ${formatDistanceWithEta(riderToDropoffKm)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = GharTakEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Collect: ${if (order.paymentMethod == "COD") GharTakConstants.formatPkr(order.totalPkr) else "Paid / Online"}", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { routeInspectOrder = order }) {
                                    Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Route")
                                }
                                OutlinedButton(onClick = {
                                    openGoogleMapsDirections(
                                        context,
                                        currentRider.latitude,
                                        currentRider.longitude,
                                        order.deliveryLat,
                                        order.deliveryLng
                                    )
                                }) {
                                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Navigate Path")
                                }
                                when (order.status) {
                                    "Rider Assigned" -> Button(onClick = { partnerVm.advanceDelivery(order, "Picked Up") }) { Text("Mark Picked Up") }
                                    "Picked Up" -> Button(onClick = { partnerVm.advanceDelivery(order, "On the Way") }) { Text("On the Way") }
                                    "On the Way" -> Button(onClick = { partnerVm.advanceDelivery(order, "Delivered") }) { Text("Mark Delivered") }
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Approved Parcel & Hire Rider Jobs (${availableParcelJobs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (availableParcelJobs.isEmpty()) {
                    item { Text("No approved parcel or hire-rider jobs available.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(availableParcelJobs, key = { "ap_${it.id}" }) { job ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        "${if (job.requestType == "send_parcel") "Parcel" else "Hire Rider"} • ${job.restaurantName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(GharTakConstants.formatPkr(job.offeredFeePkr), color = GharTakEmerald, fontWeight = FontWeight.Bold)
                                }
                                Text("Pickup: ${job.pickupAddress}", style = MaterialTheme.typography.bodySmall)
                                Text("Dropoff: ${job.dropoffAddress}", style = MaterialTheme.typography.bodySmall)
                                if (job.details.isNotBlank()) {
                                    Text(job.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { partnerVm.claimParcelRiderJob(job) },
                                    enabled = currentRider.approvalStatus == "approved" && currentRider.isOnline
                                ) {
                                    Text(if (job.requestType == "send_parcel") "Accept Parcel Job" else "Accept Hire Request")
                                }
                            }
                        }
                    }
                }
                if (assignedParcelJobs.isNotEmpty()) {
                    item {
                        Text(
                            "My Parcel & Rider Jobs (${assignedParcelJobs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(assignedParcelJobs, key = { "mp_${it.id}" }) { job ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        "${if (job.requestType == "send_parcel") "Parcel" else "Hire"} • ${job.restaurantName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusBadge(job.approvalStatus.replace('_', ' '))
                                }
                                Text("Pickup: ${job.pickupAddress}", style = MaterialTheme.typography.bodySmall)
                                Text("Dropoff: ${job.dropoffAddress}", style = MaterialTheme.typography.bodySmall)
                                if (job.recipientPhone.isNotBlank()) {
                                    Text("Contact: ${job.recipientName} (${job.recipientPhone})", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Fee: ${GharTakConstants.formatPkr(job.offeredFeePkr)}", style = MaterialTheme.typography.labelMedium, color = GharTakEmerald)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    when (job.approvalStatus) {
                                        "rider_assigned" -> Button(onClick = { partnerVm.advanceParcelRiderJob(job, "picked_up") }) {
                                            Text("Mark Picked Up")
                                        }
                                        "picked_up" -> Button(onClick = { partnerVm.advanceParcelRiderJob(job, "completed") }) {
                                            Text("Mark Completed")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    routeInspectOrder?.let { order ->
        TrackingRouteAndDistanceDialog(
            order = order,
            matchedRider = rider,
            locationState = deviceLocationState,
            onDismiss = { routeInspectOrder = null }
        )
    }
}

@Composable
private fun VendorOrderActionCard(order: OrderRecord, onUpdateStatus: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("#${order.id.takeLast(6).uppercase()} • ${order.customerName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                StatusBadge(order.status)
            }
            Text(order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }, style = MaterialTheme.typography.bodySmall)
            Text("Total: ${GharTakConstants.formatPkr(order.totalPkr)} • Your Share: ${GharTakConstants.formatPkr(order.vendorEarningsPkr)}", style = MaterialTheme.typography.labelMedium, color = GharTakPink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (order.status) {
                    "Pending" -> {
                        Button(onClick = { onUpdateStatus("Accepted") }) { Text("Accept") }
                        OutlinedButton(onClick = { onUpdateStatus("Rejected") }) { Text("Reject") }
                    }
                    "Accepted" -> Button(onClick = { onUpdateStatus("Preparing") }) { Text("Start Preparing") }
                    "Preparing" -> Button(onClick = { onUpdateStatus("Ready for Pickup") }) { Text("Ready for Pickup") }
                }
            }
        }
    }
}

@Composable
private fun RestaurantRegistrationForm(
    existing: Restaurant?,
    defaultOwnerName: String,
    onSubmit: (Restaurant) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var ownerName by remember(existing) { mutableStateOf(existing?.ownerName ?: defaultOwnerName) }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "+92") }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }
    var city by remember(existing) { mutableStateOf(existing?.city ?: GharTakConstants.DEFAULT_CITY) }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember(existing) { mutableStateOf(existing?.imageUrl ?: "") }
    var isOpen by remember(existing) { mutableStateOf(existing?.isOpen ?: true) }
    var openingTime by remember(existing) { mutableStateOf(existing?.openingTime?.ifBlank { "00:00" } ?: "00:00") }
    var closingTime by remember(existing) { mutableStateOf(existing?.closingTime?.ifBlank { "23:59" } ?: "23:59") }

    val validHours = GharTakConstants.parseMinutesOfDay(openingTime) != null &&
        GharTakConstants.parseMinutesOfDay(closingTime) != null

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(if (existing == null) "Register Your Restaurant" else "Restaurant Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item { PhotoPickerUploadRow(imageUrl = imageUrl, onImageChanged = { imageUrl = it }, label = "Restaurant Banner / Photo") }
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Restaurant Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (+92...)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Store Status & Operating Hours", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                if (validHours) GharTakConstants.formatOperatingHours(openingTime, closingTime) else "Enter 24h HH:mm times",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = isOpen, onCheckedChange = { isOpen = it })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openingTime,
                            onValueChange = { openingTime = it.take(5) },
                            label = { Text("Open (HH:mm)") },
                            singleLine = true,
                            isError = GharTakConstants.parseMinutesOfDay(openingTime) == null,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = closingTime,
                            onValueChange = { closingTime = it.take(5) },
                            label = { Text("Close (HH:mm)") },
                            singleLine = true,
                            isError = GharTakConstants.parseMinutesOfDay(closingTime) == null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Business / Verification Details") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                enabled = name.length >= 2 && address.length >= 3 && validHours,
                onClick = {
                    onSubmit(
                        (existing ?: Restaurant()).copy(
                            name = name,
                            ownerName = ownerName,
                            phone = phone,
                            address = address,
                            city = city,
                            description = desc,
                            imageUrl = imageUrl.ifBlank { null },
                            isOpen = isOpen,
                            openingTime = openingTime.trim(),
                            closingTime = closingTime.trim()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(if (existing == null) "Submit Restaurant for Approval" else "Save Changes")
            }
        }
    }
}

@Composable
private fun GroceryStoreRegistrationForm(
    existing: GroceryStore?,
    defaultOwnerName: String,
    onSubmit: (GroceryStore) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var ownerName by remember(existing) { mutableStateOf(existing?.ownerName ?: defaultOwnerName) }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "+92") }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }
    var city by remember(existing) { mutableStateOf(existing?.city ?: GharTakConstants.DEFAULT_CITY) }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember(existing) { mutableStateOf(existing?.imageUrl ?: "") }
    var isOpen by remember(existing) { mutableStateOf(existing?.isOpen ?: true) }
    var openingTime by remember(existing) { mutableStateOf(existing?.openingTime?.ifBlank { "00:00" } ?: "00:00") }
    var closingTime by remember(existing) { mutableStateOf(existing?.closingTime?.ifBlank { "23:59" } ?: "23:59") }

    val validHours = GharTakConstants.parseMinutesOfDay(openingTime) != null &&
        GharTakConstants.parseMinutesOfDay(closingTime) != null

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(if (existing == null) "Register Your Grocery Store" else "Grocery Store Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item { PhotoPickerUploadRow(imageUrl = imageUrl, onImageChanged = { imageUrl = it }, label = "Store Front / Logo") }
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Store Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (+92...)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Store Status & Operating Hours", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                if (validHours) GharTakConstants.formatOperatingHours(openingTime, closingTime) else "Enter 24h HH:mm times",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = isOpen, onCheckedChange = { isOpen = it })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openingTime,
                            onValueChange = { openingTime = it.take(5) },
                            label = { Text("Open (HH:mm)") },
                            singleLine = true,
                            isError = GharTakConstants.parseMinutesOfDay(openingTime) == null,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = closingTime,
                            onValueChange = { closingTime = it.take(5) },
                            label = { Text("Close (HH:mm)") },
                            singleLine = true,
                            isError = GharTakConstants.parseMinutesOfDay(closingTime) == null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Verification / License Details") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                enabled = name.length >= 2 && address.length >= 3 && validHours,
                onClick = {
                    onSubmit(
                        (existing ?: GroceryStore()).copy(
                            name = name,
                            ownerName = ownerName,
                            phone = phone,
                            address = address,
                            city = city,
                            description = desc,
                            imageUrl = imageUrl.ifBlank { null },
                            isOpen = isOpen,
                            openingTime = openingTime.trim(),
                            closingTime = closingTime.trim()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(if (existing == null) "Submit Store for Approval" else "Save Changes")
            }
        }
    }
}

@Composable
private fun RiderRegistrationForm(
    defaultName: String,
    onSubmit: (RiderProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf(defaultName) }
    var phone by remember { mutableStateOf("+92") }
    var cnic by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Motorbike") }
    var vehicleNumber by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(GharTakConstants.DEFAULT_CITY) }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Rider Partner Registration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile (+92...)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC Number (e.g. 31202-1234567-1)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Driving License #") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = vehicleType, onValueChange = { vehicleType = it }, label = { Text("Vehicle Type (Motorbike/Cycle/Car)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = { Text("Vehicle Registration #") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                enabled = fullName.length >= 2 && cnic.length >= 5 && vehicleNumber.length >= 2,
                onClick = {
                    onSubmit(
                        RiderProfile(
                            fullName = fullName,
                            phone = phone,
                            cnicNumber = cnic,
                            licenseNumber = license,
                            vehicleType = vehicleType,
                            vehicleNumber = vehicleNumber,
                            city = city
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Rider Verification")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodItemEditorDialog(
    restaurant: Restaurant,
    existing: FoodItem?,
    onDismiss: () -> Unit,
    onSave: (FoodItem) -> Unit,
    availableRestaurants: List<Restaurant> = emptyList(),
    availableCategories: List<String> = GharTakConstants.FOOD_CATEGORIES
) {
    var selectedRestId by remember(existing, restaurant) { mutableStateOf(existing?.restaurantId?.ifBlank { restaurant.id } ?: restaurant.id) }
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    val catOptions = availableCategories.ifEmpty { GharTakConstants.FOOD_CATEGORIES }
    var category by remember(existing) { mutableStateOf(existing?.categoryName?.ifBlank { catOptions.first() } ?: catOptions.first()) }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var price by remember(existing) { mutableStateOf(existing?.regularPricePkr?.takeIf { it > 0 }?.toInt()?.toString() ?: "") }
    var discountPrice by remember(existing) { mutableStateOf(existing?.discountPricePkr?.toInt()?.toString() ?: "0") }
    var prepTime by remember(existing) { mutableStateOf(existing?.preparationTimeMinutes?.toString() ?: "20") }
    var stock by remember(existing) { mutableStateOf(existing?.stockQuantity?.toString() ?: "50") }
    var imageUrl by remember(existing) { mutableStateOf(existing?.imageUrl ?: "") }
    var additionalImages by remember(existing) { mutableStateOf(existing?.additionalImages ?: emptyList()) }
    var isAvailable by remember(existing) { mutableStateOf(existing?.isAvailable ?: true) }
    var isFeatured by remember(existing) { mutableStateOf(existing?.isFeatured ?: false) }
    var availableDays by remember(existing) { mutableStateOf(existing?.availableDays ?: emptyList()) }
    var isDailySpecial by remember(existing) { mutableStateOf(existing?.isDailySpecial ?: false) }

    val activeRestaurant = availableRestaurants.firstOrNull { it.id == selectedRestId } ?: restaurant
    val reg = price.toDoubleOrNull() ?: 0.0
    val rawDisc = discountPrice.toDoubleOrNull() ?: 0.0
    val prepMinutes = prepTime.toLongOrNull() ?: 20L
    val stockQty = stock.toLongOrNull() ?: -1L
    val validationError = when {
        name.trim().length < 2 -> "Food name must be at least 2 characters."
        reg <= 0.0 -> "Regular price must be greater than 0 PKR."
        rawDisc < 0.0 || rawDisc > reg -> "Discount price must be between 0 and Regular price."
        prepMinutes !in 1L..300L -> "Preparation time must be 1 to 300 minutes."
        stockQty < 0L -> "Available quantity cannot be negative."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Food Product" else "Edit Food Product") },
        text = {
            LazyColumn(modifier = Modifier.height(480.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PhotoPickerUploadRow(
                        imageUrl = imageUrl,
                        onImageChanged = { imageUrl = it },
                        label = "Food Image Upload"
                    )
                    TextButton(onClick = { imageUrl = foodCategoryImageUri(category, name) }) {
                        Text("Use Real '$category' Stock Photo")
                    }
                }
                item {
                    AdditionalImagesUploadSection(
                        images = additionalImages,
                        onImagesChanged = { additionalImages = it }
                    )
                }
                if (availableRestaurants.size > 1) {
                    item {
                        Text("Restaurant Name", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(availableRestaurants) { r ->
                                FilterChip(selected = selectedRestId == r.id, onClick = { selectedRestId = r.id }, label = { Text(r.name) })
                            }
                        }
                    }
                }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food Name *") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text("Food Category", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(catOptions) { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                }
                item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Product Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Regular Price (PKR) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountPrice,
                            onValueChange = { discountPrice = it },
                            label = { Text("Discount Price (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prepTime,
                            onValueChange = { prepTime = it },
                            label = { Text("Prep Time (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Available Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("Day-wise Menu Schedule (Empty = All Days)", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = availableDays.isEmpty(),
                            onClick = { availableDays = emptyList() },
                            label = { Text("All Days") },
                            modifier = Modifier.testTag("dialog_food_days_all")
                        )
                        GharTakConstants.DAYS_OF_WEEK.forEach { day ->
                            val selected = availableDays.any { it.equals(day, ignoreCase = true) }
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    availableDays = if (selected) {
                                        availableDays.filterNot { it.equals(day, ignoreCase = true) }
                                    } else {
                                        (availableDays + day).distinct()
                                    }
                                },
                                label = { Text(day) },
                                modifier = Modifier.testTag("dialog_food_day_$day")
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Today's / Daily Special")
                        Switch(
                            checked = isDailySpecial,
                            onCheckedChange = { isDailySpecial = it },
                            modifier = Modifier.testTag("dialog_food_daily_special_switch")
                        )
                    }
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
                    item {
                        Text(validationError, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = validationError == null,
                onClick = {
                    onSave(
                        (existing ?: FoodItem()).copy(
                            restaurantId = activeRestaurant.id,
                            restaurantName = activeRestaurant.name,
                            ownerId = activeRestaurant.ownerId,
                            categoryId = "fcat_${catOptions.indexOf(category).coerceAtLeast(0) + 1}",
                            categoryName = category,
                            name = name.trim(),
                            description = desc.trim(),
                            regularPricePkr = reg,
                            discountPricePkr = rawDisc,
                            preparationTimeMinutes = prepMinutes,
                            stockQuantity = stockQty.coerceAtLeast(0L),
                            imageUrl = imageUrl.ifBlank { null },
                            additionalImages = additionalImages,
                            isAvailable = isAvailable,
                            isFeatured = isFeatured,
                            availableDays = availableDays,
                            isDailySpecial = isDailySpecial,
                            city = activeRestaurant.city
                        )
                    )
                }
            ) { Text("Save Product") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun GroceryProductEditorDialog(
    store: GroceryStore,
    existing: GroceryProduct?,
    onDismiss: () -> Unit,
    onSave: (GroceryProduct) -> Unit,
    availableStores: List<GroceryStore> = emptyList(),
    availableCategories: List<String> = GharTakConstants.GROCERY_CATEGORIES
) {
    var selectedStoreId by remember(existing, store) { mutableStateOf(existing?.storeId?.ifBlank { store.id } ?: store.id) }
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    val catOptions = availableCategories.ifEmpty { GharTakConstants.GROCERY_CATEGORIES }
    var category by remember(existing) { mutableStateOf(existing?.categoryName?.ifBlank { catOptions.first() } ?: catOptions.first()) }
    val initialUnit = existing?.unit ?: GharTakConstants.GROCERY_UNITS.first()
    val isInitialPreset = initialUnit in GharTakConstants.GROCERY_UNITS
    var selectedPresetUnit by remember(existing) { mutableStateOf(if (isInitialPreset) initialUnit else "Custom") }
    var customUnit by remember(existing) { mutableStateOf(if (isInitialPreset) "" else initialUnit) }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var price by remember(existing) { mutableStateOf(existing?.regularPricePkr?.takeIf { it > 0 }?.toInt()?.toString() ?: "") }
    var discountPrice by remember(existing) { mutableStateOf(existing?.discountPricePkr?.toInt()?.toString() ?: "0") }
    var stock by remember(existing) { mutableStateOf(existing?.stockQuantity?.toString() ?: "25") }
    var imageUrl by remember(existing) { mutableStateOf(existing?.imageUrl ?: "") }
    var additionalImages by remember(existing) { mutableStateOf(existing?.additionalImages ?: emptyList()) }
    var isAvailable by remember(existing) { mutableStateOf(existing?.isAvailable ?: true) }
    var isFeatured by remember(existing) { mutableStateOf(existing?.isFeatured ?: false) }

    val activeStore = availableStores.firstOrNull { it.id == selectedStoreId } ?: store
    val resolvedUnit = if (selectedPresetUnit == "Custom") customUnit.trim() else selectedPresetUnit
    val reg = price.toDoubleOrNull() ?: 0.0
    val rawDisc = discountPrice.toDoubleOrNull() ?: 0.0
    val stockQty = stock.toLongOrNull() ?: -1L
    val validationError = when {
        name.trim().length < 2 -> "Product name must be at least 2 characters."
        resolvedUnit.isBlank() || resolvedUnit.length > 30 -> "Enter a valid unit or weight (1–30 characters)."
        reg <= 0.0 -> "Regular price must be greater than 0 PKR."
        rawDisc < 0.0 || rawDisc > reg -> "Discount price must be between 0 and Regular price."
        stockQty < 0L -> "Available stock cannot be negative."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Grocery Product" else "Edit Grocery Product") },
        text = {
            LazyColumn(modifier = Modifier.height(470.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PhotoPickerUploadRow(
                        imageUrl = imageUrl,
                        onImageChanged = { imageUrl = it },
                        label = "Product Image Upload",
                        isGroceryFallback = true
                    )
                }
                item {
                    AdditionalImagesUploadSection(
                        images = additionalImages,
                        onImagesChanged = { additionalImages = it },
                        isGroceryFallback = true
                    )
                }
                if (availableStores.size > 1) {
                    item {
                        Text("Grocery Store Name", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(availableStores) { s ->
                                FilterChip(selected = selectedStoreId == s.id, onClick = { selectedStoreId = s.id }, label = { Text(s.name) })
                            }
                        }
                    }
                }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text("Grocery Category", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(catOptions) { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                }
                item {
                    Text("Product Weight or Unit", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GharTakConstants.GROCERY_UNITS + "Custom") { u ->
                            FilterChip(selected = selectedPresetUnit == u, onClick = { selectedPresetUnit = u }, label = { Text(u) })
                        }
                    }
                    if (selectedPresetUnit == "Custom") {
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customUnit,
                            onValueChange = { customUnit = it },
                            label = { Text("Custom Unit (e.g. 2 kg, 6 pack, 1.5 litre)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Product Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Regular Price (PKR) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountPrice,
                            onValueChange = { discountPrice = it },
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
                    item {
                        Text(validationError, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = validationError == null,
                onClick = {
                    onSave(
                        (existing ?: GroceryProduct()).copy(
                            storeId = activeStore.id,
                            storeName = activeStore.name,
                            ownerId = activeStore.ownerId,
                            categoryId = "gcat_${catOptions.indexOf(category).coerceAtLeast(0) + 1}",
                            categoryName = category,
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
                            city = activeStore.city
                        )
                    )
                }
            ) { Text("Save Product") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ParcelRiderRequestDialog(
    requestType: String,
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onSubmit: (
        pickupAddress: String,
        dropoffAddress: String,
        recipientName: String,
        recipientPhone: String,
        details: String,
        offeredFeePkr: Double
    ) -> Unit
) {
    val isParcel = requestType == "send_parcel"
    var pickupAddress by remember(restaurant) { mutableStateOf(restaurant.address) }
    var dropoffAddress by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("+92") }
    var details by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf(if (isParcel) "150" else "300") }

    val fee = feeText.toDoubleOrNull() ?: 0.0
    val isValid = pickupAddress.trim().length >= 3 &&
        (dropoffAddress.trim().length >= 3 || !isParcel) &&
        fee > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isParcel) "Send Parcel Request" else "Hire a Rider Request") },
        text = {
            LazyColumn(modifier = Modifier.height(370.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(
                        "Requests are submitted with Pending status and require Super Admin approval before riders receive them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    OutlinedTextField(
                        value = pickupAddress,
                        onValueChange = { pickupAddress = it },
                        label = { Text("Pickup Address *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = dropoffAddress,
                        onValueChange = { dropoffAddress = it },
                        label = { Text(if (isParcel) "Dropoff Address *" else "Primary Duty / Delivery Area") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text(if (isParcel) "Recipient Name" else "Shift / Contact Person") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("Contact Phone (+92...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text(if (isParcel) "Parcel Weight / Instructions" else "Shift Hours / Rider Requirements") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Offered Rider Fee (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    onSubmit(
                        pickupAddress.trim(),
                        dropoffAddress.trim().ifBlank { restaurant.city },
                        recipientName.trim(),
                        recipientPhone.trim(),
                        details.trim(),
                        fee
                    )
                },
                modifier = Modifier.testTag("submit_parcel_rider_request")
            ) {
                Text("Submit for Admin Approval")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

