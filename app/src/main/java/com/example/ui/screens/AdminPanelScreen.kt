package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.BeneficiaryAccount
import com.example.data.model.Category
import com.example.data.model.CouponRecord
import com.example.data.model.FoodItem
import com.example.data.model.GharTakConstants
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.MedicalProduct
import com.example.data.model.MedicalStore
import com.example.data.model.OrderRecord
import com.example.data.model.ParcelRiderRequest
import com.example.data.model.PlatformSettings
import com.example.data.model.PromoOffer
import com.example.data.model.Restaurant
import com.example.data.model.UserProfile
import com.example.ui.components.AdditionalImagesUploadSection
import com.example.ui.components.DiscountBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FoodDayBadges
import com.example.ui.components.GharTakImage
import com.example.ui.components.MetricCard
import com.example.ui.components.OperatingHoursBadge
import com.example.ui.components.OperatingHoursEditorDialog
import com.example.ui.components.PhotoPickerUploadRow
import com.example.ui.components.PriceRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.foodCategoryImageUri
import com.example.ui.components.groceryCategoryImageUri
import com.example.ui.components.groceryStoreImageUri
import com.example.ui.components.medicalCategoryImageUri
import com.example.ui.components.medicalStoreImageUri
import com.example.ui.components.restaurantImageUri
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCharcoal
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakMedicalTeal
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.GtMintSubtext
import com.example.ui.theme.GtNavyHeader
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

enum class AdminSection(val title: String) {
    DASHBOARD("Dashboard"),
    TRACKING("Live Tracking"),
    ADD_FOOD("Food Products"),
    MANAGE_FOOD("Manage Food Products"),
    ADD_GROCERY("Grocery Products"),
    MANAGE_GROCERY("Manage Grocery Products"),
    ADD_MEDICAL("Medical Products"),
    MANAGE_MEDICAL("Manage Medical Products"),
    MULTAN_SPECIALS("Multan Express"),
    CELEBRATION("Celebration Store"),
    FOOD_CATEGORIES("Food Categories"),
    GROCERY_CATEGORIES("Grocery Categories"),
    MEDICAL_CATEGORIES("Medical Categories"),
    RESTAURANTS("Restaurants"),
    PROMOS("Promo Banners (% OFF)"),
    GROCERY_STORES("Grocery Stores"),
    MEDICAL_STORES("Medical Stores"),
    DELIVERY_FEES("Area & Multan Fees"),
    PARCEL_RIDER_REQUESTS("Parcel & Rider Requests"),
    CUSTOMERS("Customers"),
    RIDERS("Riders"),
    ORDERS("Orders"),
    PAYMENTS("Payments"),
    COMMISSIONS("Commissions"),
    COUPONS("Coupons & Discounts"),
    REVIEWS("Reviews"),
    REPORTS("Reports & Analytics"),
    NOTIFICATIONS("Notifications"),
    SUPPORT_CHATS("Support Chats"),
    ACTIVITY_LOGS("Activity Logs"),
    SETTINGS("Admin Settings"),
    PROFILE("Admin Profile"),
    LOGOUT("Exit Admin Portal")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    adminVm: AdminViewModel,
    onExitToCustomer: () -> Unit,
    onSignOut: () -> Unit
) {
    BackHandler { onExitToCustomer() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeSection by remember { mutableStateOf(AdminSection.DASHBOARD) }
    val msg by adminVm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(msg) {
        if (msg != null) {
            snackbar.showSnackbar(msg!!)
            adminVm.clearMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(290.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFFF7F8FD),
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gt_service_logo),
                            contentDescription = "GT Service Admin Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(3.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "GT Service Super Admin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GharTakPink
                        )
                        Text(
                            adminVm.adminEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                HorizontalDivider()
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(AdminSection.entries) { sec ->
                        NavigationDrawerItem(
                            label = { Text(sec.title) },
                            selected = activeSection == sec,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (sec == AdminSection.LOGOUT) {
                                    onExitToCustomer()
                                } else {
                                    activeSection = sec
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = gtNavyTopAppBarColors(),
                    title = { Text(activeSection.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open Admin Menu")
                        }
                    },
                    actions = {
                        TextButton(onClick = onExitToCustomer) {
                            Text("Customer View", color = Color.White)
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                when (activeSection) {
                    AdminSection.DASHBOARD -> AdminDashboardSection(adminVm, onNavigate = { activeSection = it })
                    AdminSection.TRACKING -> AdminTrackingSection(adminVm)
                    AdminSection.ADD_FOOD -> AdminAddFoodSection(adminVm, onSaved = { activeSection = AdminSection.MANAGE_FOOD }, onOpenManage = { activeSection = AdminSection.MANAGE_FOOD })
                    AdminSection.MANAGE_FOOD -> AdminManageFoodSection(adminVm, onOpenAdd = { activeSection = AdminSection.ADD_FOOD })
                    AdminSection.ADD_GROCERY -> AdminAddGrocerySection(adminVm, onSaved = { activeSection = AdminSection.MANAGE_GROCERY }, onOpenManage = { activeSection = AdminSection.MANAGE_GROCERY })
                    AdminSection.MANAGE_GROCERY -> AdminManageGrocerySection(adminVm, onOpenAdd = { activeSection = AdminSection.ADD_GROCERY })
                    AdminSection.ADD_MEDICAL -> AdminAddMedicalSection(adminVm, onSaved = { activeSection = AdminSection.MANAGE_MEDICAL }, onOpenManage = { activeSection = AdminSection.MANAGE_MEDICAL })
                    AdminSection.MANAGE_MEDICAL -> AdminManageMedicalSection(adminVm, onOpenAdd = { activeSection = AdminSection.ADD_MEDICAL })
                    AdminSection.MULTAN_SPECIALS -> AdminMultanSection(adminVm, onOpenDeliveryFees = { activeSection = AdminSection.DELIVERY_FEES })
                    AdminSection.CELEBRATION -> AdminCelebrationSection(adminVm)
                    AdminSection.FOOD_CATEGORIES -> AdminCategoriesSection(adminVm, "foodCategories", "Food Categories")
                    AdminSection.GROCERY_CATEGORIES -> AdminCategoriesSection(adminVm, "groceryCategories", "Grocery Categories")
                    AdminSection.MEDICAL_CATEGORIES -> AdminCategoriesSection(adminVm, "medicalCategories", "Medical Categories")
                    AdminSection.RESTAURANTS -> AdminRestaurantsSection(adminVm)
                    AdminSection.PROMOS -> AdminPromosSection(adminVm)
                    AdminSection.GROCERY_STORES -> AdminGroceryStoresSection(adminVm)
                    AdminSection.MEDICAL_STORES -> AdminMedicalStoresSection(adminVm)
                    AdminSection.DELIVERY_FEES -> AdminDeliveryFeesSection(adminVm)
                    AdminSection.PARCEL_RIDER_REQUESTS -> AdminParcelRiderRequestsSection(adminVm)
                    AdminSection.CUSTOMERS -> AdminCustomersSection(adminVm)
                    AdminSection.RIDERS -> AdminRidersSection(adminVm)
                    AdminSection.ORDERS -> AdminOrdersSection(adminVm, onOpenTracking = { activeSection = AdminSection.TRACKING })
                    AdminSection.PAYMENTS -> AdminPaymentsSection(adminVm)
                    AdminSection.COMMISSIONS -> AdminCommissionsSection(adminVm)
                    AdminSection.COUPONS -> AdminCouponsSection(adminVm, onOpenPromos = { activeSection = AdminSection.PROMOS })
                    AdminSection.REVIEWS -> AdminReviewsSection(adminVm)
                    AdminSection.REPORTS -> AdminReportsSection(adminVm)
                    AdminSection.NOTIFICATIONS -> AdminNotificationsSection(adminVm)
                    AdminSection.SUPPORT_CHATS -> AdminSupportChatsSection(adminVm)
                    AdminSection.ACTIVITY_LOGS -> AdminLogsSection(adminVm)
                    AdminSection.SETTINGS -> AdminSettingsSection(adminVm)
                    AdminSection.PROFILE -> AdminProfileSection(adminVm, onExitToCustomer, onSignOut)
                    AdminSection.LOGOUT -> {}
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardSection(adminVm: AdminViewModel, onNavigate: (AdminSection) -> Unit) {
    val users by adminVm.users.collectAsStateWithLifecycle()
    val restaurants by adminVm.restaurants.collectAsStateWithLifecycle()
    val stores by adminVm.groceryStores.collectAsStateWithLifecycle()
    val medicalStores by adminVm.medicalStores.collectAsStateWithLifecycle()
    val riders by adminVm.riders.collectAsStateWithLifecycle()
    val foodItems by adminVm.foodItems.collectAsStateWithLifecycle()
    val groceryProducts by adminVm.groceryProducts.collectAsStateWithLifecycle()
    val medicalProducts by adminVm.medicalProducts.collectAsStateWithLifecycle()
    val parcelRequests by adminVm.parcelRiderRequests.collectAsStateWithLifecycle()
    val orders by adminVm.orders.collectAsStateWithLifecycle()
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    val cityRequests by adminVm.cityRequests.collectAsStateWithLifecycle()

    val revenue = orders.filter { it.status == "Delivered" }.sumOf { it.totalPkr }
    val commission = orders.filter { it.status == "Delivered" }.sumOf { it.commissionPkr }
    val pendingParcelCount = parcelRequests.count { it.approvalStatus == "pending" }
    val pendingCityCount = cityRequests.count { it.status == "pending" }
    val activeSupportPhone = settings.supportPhoneLocal.ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = GharTakPink.copy(alpha = 0.10f))) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Products & Image Upload", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GharTakPink)
                    Text("Add Food, Grocery & Medical items with real photos from gallery, files, or camera.", style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigate(AdminSection.ADD_FOOD) },
                            modifier = Modifier.weight(1f).testTag("admin_dash_add_food")
                        ) {
                            Text("+ Food")
                        }
                        Button(
                            onClick = { onNavigate(AdminSection.ADD_GROCERY) },
                            modifier = Modifier.weight(1f).testTag("admin_dash_add_grocery")
                        ) {
                            Text("+ Grocery")
                        }
                        Button(
                            onClick = { onNavigate(AdminSection.ADD_MEDICAL) },
                            modifier = Modifier.weight(1f).testTag("admin_dash_add_medical")
                        ) {
                            Text("+ Medical")
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onNavigate(AdminSection.MANAGE_FOOD) }, modifier = Modifier.weight(1f)) {
                            Text("Food (${foodItems.size})")
                        }
                        OutlinedButton(onClick = { onNavigate(AdminSection.MANAGE_GROCERY) }, modifier = Modifier.weight(1f)) {
                            Text("Grocery (${groceryProducts.size})")
                        }
                        OutlinedButton(onClick = { onNavigate(AdminSection.MANAGE_MEDICAL) }, modifier = Modifier.weight(1f)) {
                            Text("Medical (${medicalProducts.size})")
                        }
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bahawalpur Starter Catalog", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Populate Food, Grocery & Medical categories, Bahawalpur restaurants, stores, pharmacies, and coupons in Firestore.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { adminVm.seedStarterCatalog() }) {
                        Text("Initialize Bahawalpur Catalog in Firestore")
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Customers", "${users.size}", Modifier.weight(1f))
                MetricCard("Restaurants", "${restaurants.size}", Modifier.weight(1f))
                MetricCard("Grocery", "${stores.size}", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Pharmacies", "${medicalStores.size}", Modifier.weight(1f), GharTakMedicalTeal)
                MetricCard("Riders", "${riders.size}", Modifier.weight(1f))
                MetricCard("Medical Items", "${medicalProducts.size}", Modifier.weight(1f), GharTakMedicalTeal)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Total Orders", "${orders.size}", Modifier.weight(1f))
                MetricCard("Delivered Rev", GharTakConstants.formatPkr(revenue), Modifier.weight(1f), GharTakEmerald)
                MetricCard("Commission", GharTakConstants.formatPkr(commission), Modifier.weight(1f), GharTakPink)
            }
        }
        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Button(
                        onClick = { onNavigate(AdminSection.MULTAN_SPECIALS) },
                        modifier = Modifier.testTag("admin_dash_multan")
                    ) { Text("Multan Express") }
                }
                item {
                    Button(
                        onClick = { onNavigate(AdminSection.CELEBRATION) },
                        modifier = Modifier.testTag("admin_dash_celebration")
                    ) { Text("Celebration Store") }
                }
                item {
                    Button(
                        onClick = { onNavigate(AdminSection.DELIVERY_FEES) },
                        modifier = Modifier.testTag("admin_dash_delivery_fees")
                    ) { Text("Area & Multan Fees") }
                }
                item {
                    Button(
                        onClick = { onNavigate(AdminSection.PROMOS) },
                        modifier = Modifier.testTag("admin_dash_promos")
                    ) { Text("% OFF Promo Banners") }
                }
                item {
                    Button(
                        onClick = { onNavigate(AdminSection.SUPPORT_CHATS) },
                        modifier = Modifier.testTag("admin_dash_support_chat")
                    ) { Text("Support Chats ($activeSupportPhone)") }
                }
                item {
                    Button(
                        onClick = { onNavigate(AdminSection.PARCEL_RIDER_REQUESTS) },
                        modifier = Modifier.testTag("admin_dash_parcel_requests")
                    ) { Text("Parcel/Rider ($pendingParcelCount)") }
                }
                item {
                    OutlinedButton(
                        onClick = { onNavigate(AdminSection.SETTINGS) },
                        modifier = Modifier.testTag("admin_dash_city_requests")
                    ) { Text("Cities & Promos${if (pendingCityCount > 0) " ($pendingCityCount)" else ""}") }
                }
                item {
                    OutlinedButton(
                        onClick = { onNavigate(AdminSection.TRACKING) },
                        modifier = Modifier.testTag("admin_dash_tracking")
                    ) { Text("Live Tracking") }
                }
                item { OutlinedButton(onClick = { onNavigate(AdminSection.ADD_MEDICAL) }) { Text("+ Add Medical") } }
                item { OutlinedButton(onClick = { onNavigate(AdminSection.ORDERS) }) { Text("Manage Orders") } }
                item { OutlinedButton(onClick = { onNavigate(AdminSection.RIDERS) }) { Text("Approve Riders") } }
            }
        }
        item { Text("Recent Orders (${orders.take(5).size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(orders.take(5), key = { it.id }) { order ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${order.vendorName} • ${order.customerName}", style = MaterialTheme.typography.titleSmall)
                        Text("${order.orderType.uppercase()} • ${GharTakConstants.formatPkr(order.totalPkr)}", style = MaterialTheme.typography.bodySmall)
                    }
                    StatusBadge(order.status)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminAddFoodSection(
    adminVm: AdminViewModel,
    onSaved: () -> Unit,
    onOpenManage: () -> Unit
) {
    val restaurants by adminVm.restaurants.collectAsStateWithLifecycle()
    val categories by adminVm.foodCategories.collectAsStateWithLifecycle()
    val existingFoodCount by adminVm.foodItems.collectAsStateWithLifecycle()

    var selectedRestId by remember { mutableStateOf("") }
    LaunchedEffect(restaurants) {
        if (selectedRestId.isBlank() || restaurants.none { it.id == selectedRestId }) {
            selectedRestId = restaurants.firstOrNull()?.id ?: ""
        }
    }
    val catNames = categories.map { it.name }.ifEmpty { GharTakConstants.FOOD_CATEGORIES }
    var selectedCat by remember(catNames) { mutableStateOf(catNames.first()) }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var regPrice by remember { mutableStateOf("") }
    var discPrice by remember { mutableStateOf("0") }
    var prepTime by remember { mutableStateOf("20") }
    var stock by remember { mutableStateOf("50") }
    var imageUrl by remember { mutableStateOf("") }
    var additionalImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isAvailable by remember { mutableStateOf(true) }
    var isFeatured by remember { mutableStateOf(false) }
    var availableDays by remember { mutableStateOf<List<String>>(emptyList()) }
    var isDailySpecial by remember { mutableStateOf(false) }
    var showQuickAddRest by remember { mutableStateOf(false) }

    val selectedRest = restaurants.firstOrNull { it.id == selectedRestId }
    val reg = regPrice.toDoubleOrNull() ?: 0.0
    val rawDisc = discPrice.toDoubleOrNull() ?: 0.0
    val prepMinutes = prepTime.toLongOrNull() ?: 20L
    val stockQty = stock.toLongOrNull() ?: -1L

    val validationError = when {
        selectedRest == null -> "Please select or quick-add a restaurant first."
        name.trim().length < 2 -> "Enter a food name (at least 2 characters)."
        reg <= 0.0 -> "Enter a valid Regular Price (PKR > 0)."
        rawDisc < 0.0 || rawDisc > reg -> "Discount Price must be between 0 and Regular Price."
        prepMinutes !in 1L..300L -> "Preparation time must be 1–300 minutes."
        stockQty < 0L -> "Available quantity cannot be negative."
        else -> null
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Add Food Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Live synced to all customer devices via Cloud Firestore", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onOpenManage) {
                    Text("Manage (${existingFoodCount.size})")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Restaurant Name *", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { showQuickAddRest = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Restaurant")
                }
            }
            if (restaurants.isEmpty()) {
                Text("No restaurants exist yet. Tap '+ New Restaurant' or initialize the catalog on Dashboard.", color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(restaurants) { r ->
                        FilterChip(selected = selectedRestId == r.id, onClick = { selectedRestId = r.id }, label = { Text(r.name) })
                    }
                }
            }
        }
        item {
            Text("Food Category *", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(catNames) { c ->
                    FilterChip(selected = selectedCat == c, onClick = { selectedCat = c }, label = { Text(c) })
                }
            }
        }
        item {
            PhotoPickerUploadRow(
                imageUrl = imageUrl,
                onImageChanged = { imageUrl = it },
                label = "Food Image Upload (Gallery / Files / Camera)"
            )
            TextButton(
                onClick = { imageUrl = foodCategoryImageUri(selectedCat, name) },
                modifier = Modifier.testTag("admin_food_use_category_photo")
            ) {
                Text("Use Real '$selectedCat' Stock Photo")
            }
        }
        item {
            AdditionalImagesUploadSection(
                images = additionalImages,
                onImagesChanged = { additionalImages = it }
            )
        }
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food Name *") }, modifier = Modifier.fillMaxWidth().testTag("admin_food_name_input")) }
        item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Product Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = regPrice,
                    onValueChange = { regPrice = it },
                    label = { Text("Regular Price (PKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("admin_food_price_input")
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = prepTime,
                    onValueChange = { prepTime = it },
                    label = { Text("Preparation Time (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Available Quantity") },
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
                    modifier = Modifier.testTag("admin_food_days_all")
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
                        modifier = Modifier.testTag("admin_food_day_$day")
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Today's / Daily Special")
                Switch(
                    checked = isDailySpecial,
                    onCheckedChange = { isDailySpecial = it },
                    modifier = Modifier.testTag("admin_food_daily_special_switch")
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
            item { Text(validationError, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            Button(
                enabled = validationError == null,
                onClick = {
                    val rest = selectedRest!!
                    adminVm.saveFoodItem(
                        FoodItem(
                            restaurantId = rest.id,
                            restaurantName = rest.name,
                            ownerId = rest.ownerId.ifBlank { adminVm.adminId },
                            categoryId = "fcat_${catNames.indexOf(selectedCat).coerceAtLeast(0) + 1}",
                            categoryName = selectedCat,
                            name = name.trim(),
                            description = desc.trim(),
                            regularPricePkr = reg,
                            discountPricePkr = rawDisc,
                            imageUrl = imageUrl.ifBlank { null },
                            additionalImages = additionalImages,
                            preparationTimeMinutes = prepMinutes,
                            stockQuantity = stockQty.coerceAtLeast(0L),
                            isAvailable = isAvailable,
                            isFeatured = isFeatured,
                            availableDays = availableDays,
                            isDailySpecial = isDailySpecial,
                            city = rest.city
                        )
                    )
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().testTag("save_food_product_button")
            ) { Text("Save Product") }
        }
    }

    if (showQuickAddRest) {
        QuickVendorDialog(
            title = "Quick Add Restaurant",
            nameLabel = "Restaurant Name",
            showHoursControls = true,
            onDismiss = { showQuickAddRest = false },
            onCreate = { vName, vAddress, vCity, vPhone ->
                adminVm.saveRestaurantAdmin(
                    Restaurant(
                        name = vName,
                        ownerName = "GT Service Partner",
                        phone = vPhone,
                        address = vAddress,
                        city = vCity
                    ),
                    onCreated = { newId -> selectedRestId = newId }
                )
                showQuickAddRest = false
            },
            onCreateWithHours = { vName, vAddress, vCity, vPhone, vIsOpen, vOpenTime, vCloseTime ->
                adminVm.saveRestaurantAdmin(
                    Restaurant(
                        name = vName,
                        ownerName = "GT Service Partner",
                        phone = vPhone,
                        address = vAddress,
                        city = vCity,
                        isOpen = vIsOpen,
                        openingTime = vOpenTime,
                        closingTime = vCloseTime
                    ),
                    onCreated = { newId -> selectedRestId = newId }
                )
                showQuickAddRest = false
            }
        )
    }
}

@Composable
private fun AdminManageFoodSection(
    adminVm: AdminViewModel,
    onOpenAdd: () -> Unit
) {
    val items by adminVm.foodItems.collectAsStateWithLifecycle()
    val restaurants by adminVm.restaurants.collectAsStateWithLifecycle()
    val categories by adminVm.foodCategories.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedRestId by remember { mutableStateOf("All") }
    var selectedDayFilter by remember { mutableStateOf("All Items") }
    var editing by remember { mutableStateOf<FoodItem?>(null) }
    var deletingTarget by remember { mutableStateOf<FoodItem?>(null) }

    val todayKey = remember { GharTakConstants.todayDayKey() }
    val dayFilters = remember { listOf("All Items", "Today", "Daily Special") + GharTakConstants.DAYS_OF_WEEK }
    val catNames = listOf("All") + categories.map { it.name }.ifEmpty { GharTakConstants.FOOD_CATEGORIES }
    val filtered = items.filter { item ->
        val matchesQuery = search.isBlank() ||
            item.name.contains(search, ignoreCase = true) ||
            item.restaurantName.contains(search, ignoreCase = true) ||
            item.categoryName.contains(search, ignoreCase = true)
        val matchesCat = selectedCategory == "All" || item.categoryName.equals(selectedCategory, ignoreCase = true)
        val matchesRest = selectedRestId == "All" || item.restaurantId == selectedRestId
        val matchesDay = when (selectedDayFilter) {
            "All Items" -> true
            "Today" -> item.isAvailableOnDay(todayKey)
            "Daily Special" -> item.isDailySpecial
            else -> item.isAvailableOnDay(selectedDayFilter)
        }
        matchesQuery && matchesCat && matchesRest && matchesDay
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Food Inventory (${filtered.size}/${items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onOpenAdd) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Food")
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search food by name, category, or restaurant...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Filter by Category", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(catNames) { c ->
                    FilterChip(selected = selectedCategory == c, onClick = { selectedCategory = c }, label = { Text(c) })
                }
            }
        }
        item {
            Text("Filter by Day Schedule", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(dayFilters) { day ->
                    val label = if (day == "Today") "Today ($todayKey)" else day
                    FilterChip(
                        selected = selectedDayFilter == day,
                        onClick = { selectedDayFilter = day },
                        label = { Text(label) },
                        modifier = Modifier.testTag("admin_food_day_filter_$day")
                    )
                }
            }
        }
        if (restaurants.isNotEmpty()) {
            item {
                Text("Filter by Restaurant", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(selected = selectedRestId == "All", onClick = { selectedRestId = "All" }, label = { Text("All Restaurants") })
                    }
                    items(restaurants) { r ->
                        FilterChip(selected = selectedRestId == r.id, onClick = { selectedRestId = r.id }, label = { Text(r.name) })
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateView("No food products match", "Try clearing filters or add a new food product.", "Add Food Product", onOpenAdd)
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val displayImage = item.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_food" }
                            ?: foodCategoryImageUri(item.categoryName, item.name)
                        Box {
                            GharTakImage(displayImage, item.name, Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)))
                            if (item.discountPercent > 0) {
                                DiscountBadge(item.discountPercent, Modifier.align(Alignment.TopStart).padding(4.dp))
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${item.restaurantName} • ${item.categoryName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Prep ~${item.preparationTimeMinutes} min • Qty: ${item.stockQuantity}" + if (item.additionalImages.isNotEmpty()) " • +${item.additionalImages.size} photos" else "", style = MaterialTheme.typography.labelSmall)
                            FoodDayBadges(availableDays = item.availableDays, isDailySpecial = item.isDailySpecial)
                            PriceRow(item.regularPricePkr, item.discountPricePkr)
                            StatusBadge(if (item.isAvailable) "Active" else "Inactive")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(checked = item.isAvailable, onCheckedChange = { adminVm.saveFoodItem(item.copy(isAvailable = it)) })
                            Row {
                                IconButton(onClick = { editing = item }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                                IconButton(onClick = { deletingTarget = item }) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = GharTakCoralError) }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { food ->
        val rest = restaurants.firstOrNull { it.id == food.restaurantId } ?: restaurants.firstOrNull()
        if (rest != null) {
            FoodItemEditorDialog(
                restaurant = rest,
                existing = food,
                onDismiss = { editing = null },
                onSave = { updated ->
                    adminVm.saveFoodItem(updated)
                    editing = null
                },
                availableRestaurants = restaurants,
                availableCategories = categories.map { it.name }.ifEmpty { GharTakConstants.FOOD_CATEGORIES }
            )
        }
    }

    deletingTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingTarget = null },
            title = { Text("Delete Food Product?") },
            text = { Text("Permanently remove '${target.name}' from GT Service? Customers will immediately stop seeing it.") },
            confirmButton = {
                Button(onClick = {
                    adminVm.deleteFoodItem(target)
                    deletingTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminAddGrocerySection(
    adminVm: AdminViewModel,
    onSaved: () -> Unit,
    onOpenManage: () -> Unit
) {
    val stores by adminVm.groceryStores.collectAsStateWithLifecycle()
    val categories by adminVm.groceryCategories.collectAsStateWithLifecycle()
    val existingCount by adminVm.groceryProducts.collectAsStateWithLifecycle()

    var selectedStoreId by remember { mutableStateOf("") }
    LaunchedEffect(stores) {
        if (selectedStoreId.isBlank() || stores.none { it.id == selectedStoreId }) {
            selectedStoreId = stores.firstOrNull()?.id ?: ""
        }
    }
    val catNames = categories.map { it.name }.ifEmpty { GharTakConstants.GROCERY_CATEGORIES }
    var selectedCat by remember(catNames) { mutableStateOf(catNames.first()) }
    var selectedPresetUnit by remember { mutableStateOf(GharTakConstants.GROCERY_UNITS.first()) }
    var customUnit by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var regPrice by remember { mutableStateOf("") }
    var discPrice by remember { mutableStateOf("0") }
    var stock by remember { mutableStateOf("40") }
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
        selectedStore == null -> "Please select or quick-add a grocery store first."
        name.trim().length < 2 -> "Enter a product name (at least 2 characters)."
        resolvedUnit.isBlank() || resolvedUnit.length > 30 -> "Select a weight/unit or enter a custom unit (1–30 chars)."
        reg <= 0.0 -> "Enter a valid Regular Price (PKR > 0)."
        rawDisc < 0.0 || rawDisc > reg -> "Discount Price must be between 0 and Regular Price."
        stockQty < 0L -> "Available stock cannot be negative."
        else -> null
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Add Grocery Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Live synced to all customer devices via Cloud Firestore", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onOpenManage) {
                    Text("Manage (${existingCount.size})")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Grocery Store Name *", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { showQuickAddStore = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Store")
                }
            }
            if (stores.isEmpty()) {
                Text("No grocery stores exist yet. Tap '+ New Store' or initialize catalog on Dashboard.", color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stores) { s ->
                        FilterChip(selected = selectedStoreId == s.id, onClick = { selectedStoreId = s.id }, label = { Text(s.name) })
                    }
                }
            }
        }
        item {
            Text("Grocery Category *", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(catNames) { c ->
                    FilterChip(selected = selectedCat == c, onClick = { selectedCat = c }, label = { Text(c) })
                }
            }
        }
        item {
            Text("Product Weight or Unit *", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GharTakConstants.GROCERY_UNITS + "Custom") { u ->
                    FilterChip(selected = selectedPresetUnit == u, onClick = { selectedPresetUnit = u }, label = { Text(u) })
                }
            }
            if (selectedPresetUnit == "Custom") {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = customUnit,
                    onValueChange = { customUnit = it },
                    label = { Text("Custom Unit (e.g. 5 kg, 1 dozen, 300 ml)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            PhotoPickerUploadRow(
                imageUrl = imageUrl,
                onImageChanged = { imageUrl = it },
                label = "Product Image Upload (Gallery / Files / Camera)",
                isGroceryFallback = true
            )
            TextButton(
                onClick = { imageUrl = groceryCategoryImageUri(selectedCat, name) },
                modifier = Modifier.testTag("admin_grocery_use_category_photo")
            ) {
                Text("Use Real '$selectedCat' Stock Photo")
            }
        }
        item {
            AdditionalImagesUploadSection(
                images = additionalImages,
                onImagesChanged = { additionalImages = it },
                isGroceryFallback = true
            )
        }
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth().testTag("admin_grocery_name_input")) }
        item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Product Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = regPrice,
                    onValueChange = { regPrice = it },
                    label = { Text("Regular Price (PKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("admin_grocery_price_input")
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
                onClick = {
                    val store = selectedStore!!
                    adminVm.saveGroceryProduct(
                        GroceryProduct(
                            storeId = store.id,
                            storeName = store.name,
                            ownerId = store.ownerId.ifBlank { adminVm.adminId },
                            categoryId = "gcat_${catNames.indexOf(selectedCat).coerceAtLeast(0) + 1}",
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
                modifier = Modifier.fillMaxWidth().testTag("save_grocery_product_button")
            ) { Text("Save Product") }
        }
    }

    if (showQuickAddStore) {
        QuickVendorDialog(
            title = "Quick Add Grocery Store",
            nameLabel = "Grocery Store Name",
            onDismiss = { showQuickAddStore = false },
            onCreate = { vName, vAddress, vCity, vPhone ->
                adminVm.saveGroceryStoreAdmin(
                    GroceryStore(
                        name = vName,
                        ownerName = "GT Service Mart Partner",
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
private fun AdminManageGrocerySection(
    adminVm: AdminViewModel,
    onOpenAdd: () -> Unit
) {
    val products by adminVm.groceryProducts.collectAsStateWithLifecycle()
    val stores by adminVm.groceryStores.collectAsStateWithLifecycle()
    val categories by adminVm.groceryCategories.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStoreId by remember { mutableStateOf("All") }
    var editing by remember { mutableStateOf<GroceryProduct?>(null) }
    var deletingTarget by remember { mutableStateOf<GroceryProduct?>(null) }

    val catNames = listOf("All") + categories.map { it.name }.ifEmpty { GharTakConstants.GROCERY_CATEGORIES }
    val filtered = products.filter { prod ->
        val matchesQuery = search.isBlank() ||
            prod.name.contains(search, ignoreCase = true) ||
            prod.storeName.contains(search, ignoreCase = true) ||
            prod.categoryName.contains(search, ignoreCase = true)
        val matchesCat = selectedCategory == "All" || prod.categoryName.equals(selectedCategory, ignoreCase = true)
        val matchesStore = selectedStoreId == "All" || prod.storeId == selectedStoreId
        matchesQuery && matchesCat && matchesStore
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Grocery Inventory (${filtered.size}/${products.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onOpenAdd) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Product")
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search grocery by name, category, or store...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Filter by Category", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(catNames) { c ->
                    FilterChip(selected = selectedCategory == c, onClick = { selectedCategory = c }, label = { Text(c) })
                }
            }
        }
        if (stores.isNotEmpty()) {
            item {
                Text("Filter by Grocery Store", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(selected = selectedStoreId == "All", onClick = { selectedStoreId = "All" }, label = { Text("All Stores") })
                    }
                    items(stores) { s ->
                        FilterChip(selected = selectedStoreId == s.id, onClick = { selectedStoreId = s.id }, label = { Text(s.name) })
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateView("No grocery products match", "Try clearing filters or add a new grocery product.", "Add Grocery Product", onOpenAdd)
            }
        } else {
            items(filtered, key = { it.id }) { prod ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val displayImage = prod.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_grocery" }
                            ?: groceryCategoryImageUri(prod.categoryName, prod.name)
                        Box {
                            GharTakImage(displayImage, prod.name, Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)), isGroceryFallback = true)
                            if (prod.discountPercent > 0) {
                                DiscountBadge(prod.discountPercent, Modifier.align(Alignment.TopStart).padding(4.dp))
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${prod.storeName} • ${prod.categoryName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Unit: ${prod.unit} • Stock: ${prod.stockQuantity}" + if (prod.additionalImages.isNotEmpty()) " • +${prod.additionalImages.size} photos" else "", style = MaterialTheme.typography.labelSmall)
                            PriceRow(prod.regularPricePkr, prod.discountPricePkr, prod.unit)
                            StatusBadge(if (prod.isAvailable && prod.stockQuantity > 0L) "In Stock" else if (!prod.isAvailable) "Inactive" else "Out of Stock")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(checked = prod.isAvailable, onCheckedChange = { adminVm.saveGroceryProduct(prod.copy(isAvailable = it)) })
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
            GroceryProductEditorDialog(
                store = store,
                existing = prod,
                onDismiss = { editing = null },
                onSave = { updated ->
                    adminVm.saveGroceryProduct(updated)
                    editing = null
                },
                availableStores = stores,
                availableCategories = categories.map { it.name }.ifEmpty { GharTakConstants.GROCERY_CATEGORIES }
            )
        }
    }

    deletingTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingTarget = null },
            title = { Text("Delete Grocery Product?") },
            text = { Text("Permanently remove '${target.name}' from GT Service? Customers will immediately stop seeing it.") },
            confirmButton = {
                Button(onClick = {
                    adminVm.deleteGroceryProduct(target)
                    deletingTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingTarget = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickVendorDialog(
    title: String,
    nameLabel: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit,
    showHoursControls: Boolean = false,
    onCreateWithHours: ((String, String, String, String, Boolean, String, String) -> Unit)? = null
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("Model Town A, Bahawalpur") }
    var city by remember { mutableStateOf(GharTakConstants.DEFAULT_CITY) }
    var phone by remember { mutableStateOf("+923001234567") }
    var isOpen by remember { mutableStateOf(true) }
    var openingTime by remember { mutableStateOf("09:00") }
    var closingTime by remember { mutableStateOf("23:00") }
    val validHours = !showHoursControls || (
        GharTakConstants.parseMinutesOfDay(openingTime) != null &&
            GharTakConstants.parseMinutesOfDay(closingTime) != null
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("$nameLabel *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (+92...)") }, modifier = Modifier.fillMaxWidth())
                if (showHoursControls) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isOpen) "Status: Open Now" else "Status: Closed", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Switch(checked = isOpen, onCheckedChange = { isOpen = it })
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = openingTime == "00:00" && closingTime == "23:59",
                            onClick = { openingTime = "00:00"; closingTime = "23:59" },
                            label = { Text("24 Hours") }
                        )
                        FilterChip(
                            selected = openingTime == "09:00" && closingTime == "23:00",
                            onClick = { openingTime = "09:00"; closingTime = "23:00" },
                            label = { Text("9 AM – 11 PM") }
                        )
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
        },
        confirmButton = {
            Button(
                enabled = name.trim().length >= 2 && address.trim().length >= 3 && city.trim().length >= 2 && validHours,
                onClick = {
                    if (showHoursControls && onCreateWithHours != null) {
                        onCreateWithHours(name.trim(), address.trim(), city.trim(), phone.trim(), isOpen, openingTime.trim(), closingTime.trim())
                    } else {
                        onCreate(name.trim(), address.trim(), city.trim(), phone.trim())
                    }
                }
            ) { Text("Create & Approve") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminCategoriesSection(adminVm: AdminViewModel, collection: String, title: String) {
    val cats by when (collection) {
        "foodCategories" -> adminVm.foodCategories.collectAsStateWithLifecycle()
        "medicalCategories" -> adminVm.medicalCategories.collectAsStateWithLifecycle()
        else -> adminVm.groceryCategories.collectAsStateWithLifecycle()
    }
    var newName by remember { mutableStateOf("") }
    var editingCat by remember { mutableStateOf<Category?>(null) }
    var editName by remember { mutableStateOf("") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New Category Name") }, modifier = Modifier.weight(1f))
                Button(
                    enabled = newName.trim().length >= 2,
                    onClick = {
                        adminVm.saveCategory(collection, Category(name = newName.trim(), iconKey = "category", isActive = true, sortOrder = cats.size + 1L))
                        newName = ""
                    }
                ) { Text("Add") }
            }
        }
        items(cats, key = { it.id }) { cat ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(cat.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(if (cat.isActive) "Active" else "Inactive")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = cat.isActive, onCheckedChange = { adminVm.saveCategory(collection, cat.copy(isActive = it)) })
                        IconButton(onClick = { editingCat = cat; editName = cat.name }) { Icon(Icons.Filled.Edit, contentDescription = "Edit category") }
                        IconButton(onClick = { adminVm.deleteCategory(collection, cat) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = GharTakCoralError) }
                    }
                }
            }
        }
    }

    editingCat?.let { cat ->
        AlertDialog(
            onDismissRequest = { editingCat = null },
            title = { Text("Edit Category") },
            text = {
                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Category Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(
                    enabled = editName.trim().length >= 2,
                    onClick = {
                        adminVm.saveCategory(collection, cat.copy(name = editName.trim()))
                        editingCat = null
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingCat = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminRestaurantsSection(adminVm: AdminViewModel) {
    val restaurants by adminVm.restaurants.collectAsStateWithLifecycle()
    val credentials by adminVm.restaurantCredentials.collectAsStateWithLifecycle()
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    var credentialTarget by remember { mutableStateOf<Restaurant?>(null) }
    var promoTargetRest by remember { mutableStateOf<Restaurant?>(null) }
    var hoursTargetRest by remember { mutableStateOf<Restaurant?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Restaurants (${restaurants.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { adminVm.seedStarterCatalog() }) {
                    Text("Sync Restaurants & Photos")
                }
            }
        }
        items(restaurants, key = { it.id }) { r ->
            val cred = credentials.firstOrNull { it.restaurantId == r.id }
            val loginIdDisplay = cred?.loginId?.ifBlank { null } ?: r.portalLoginId.ifBlank { null }
            val restOffers = settings.promoOffers.filter { it.restaurantId == r.id }
            val hasPrimaryPromo = settings.promoBannerEnabled && settings.promoRestaurantId == r.id
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GharTakImage(
                            imageUrl = restaurantImageUri(r.name, r.imageUrl),
                            contentDescription = r.name,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(r.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                StatusBadge(r.approvalStatus)
                            }
                            Text("${r.ownerName} • ${r.phone} • ${r.city}", style = MaterialTheme.typography.bodySmall)
                            Text("Commission: ${r.commissionPercent}%", style = MaterialTheme.typography.labelMedium)
                            OperatingHoursBadge(isOpen = r.isOpen, openingTime = r.openingTime, closingTime = r.closingTime)
                        }
                    }
                    Surface(
                        color = if (r.isOpen) GharTakEmerald.copy(alpha = 0.08f) else GharTakCoralError.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = if (r.isOpen) "Status: OPEN (Accepting Orders)" else "Status: CLOSED (Paused)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.isOpen) GharTakEmerald else GharTakCoralError
                                )
                                Text(
                                    text = "Timing: ${r.operatingHoursLabel()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TextButton(
                                    onClick = { hoursTargetRest = r },
                                    modifier = Modifier.testTag("admin_rest_quick_timing_${r.id}")
                                ) {
                                    Text("Edit Timing")
                                }
                                Switch(
                                    checked = r.isOpen,
                                    onCheckedChange = { nextOpen ->
                                        adminVm.updateVendorHoursAndOpen("restaurants", r.id, r.name, nextOpen, r.openingTime, r.closingTime)
                                    },
                                    modifier = Modifier.testTag("admin_rest_open_switch_${r.id}")
                                )
                            }
                        }
                    }
                    if (hasPrimaryPromo || restOffers.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (hasPrimaryPromo) {
                                Surface(color = GharTakEmerald.copy(alpha = 0.14f), shape = RoundedCornerShape(999.dp)) {
                                    Text(
                                        "Home Banner: ${settings.promoDiscountPercent}% OFF • ${settings.promoCode}",
                                        color = GharTakEmerald,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            restOffers.forEach { offer ->
                                Surface(color = GharTakEmerald.copy(alpha = 0.14f), shape = RoundedCornerShape(999.dp)) {
                                    Text(
                                        "${offer.discountPercent}% OFF • ${offer.code}",
                                        color = GharTakEmerald,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Portal Login ID: ${loginIdDisplay ?: "Not configured"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (loginIdDisplay != null) GharTakEmerald else GharTakAmber
                            )
                            if (cred != null && cred.password.isNotBlank()) {
                                Text(
                                    text = "Password: ${cred.password}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(onClick = { adminVm.updateRestaurantApproval(r, "approved") }) { Text("Approve") }
                        OutlinedButton(onClick = { adminVm.updateRestaurantApproval(r, "rejected") }) { Text("Reject") }
                        OutlinedButton(onClick = { adminVm.updateRestaurantApproval(r, "suspended") }) { Text("Suspend") }
                        OutlinedButton(
                            onClick = { hoursTargetRest = r },
                            modifier = Modifier.testTag("admin_rest_hours_${r.id}")
                        ) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Hours / Open-Close")
                        }
                        OutlinedButton(
                            onClick = { promoTargetRest = r },
                            modifier = Modifier.testTag("admin_rest_promo_${r.id}")
                        ) {
                            Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Set % OFF Promo")
                        }
                        OutlinedButton(
                            onClick = { credentialTarget = r },
                            modifier = Modifier.testTag("admin_rest_login_${r.id}")
                        ) {
                            Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Portal Login")
                        }
                    }
                }
            }
        }
    }

    promoTargetRest?.let { targetRest ->
        val existingOffer = settings.promoOffers.firstOrNull { it.restaurantId == targetRest.id }
        val suggestedCode = remember(targetRest, existingOffer) {
            existingOffer?.code?.ifBlank { null }
                ?: (targetRest.name.filter { ch -> ch.isLetterOrDigit() }.take(5).uppercase().ifBlank { "REST" } + "20")
        }
        var pctText by remember(targetRest, existingOffer) {
            mutableStateOf((existingOffer?.discountPercent ?: 20).toString())
        }
        var codeText by remember(targetRest, existingOffer) { mutableStateOf(suggestedCode) }
        var titleText by remember(targetRest, existingOffer) {
            mutableStateOf(
                existingOffer?.title?.ifBlank { null }
                    ?: "20% OFF at ${targetRest.name}"
            )
        }
        var subtitleText by remember(targetRest, existingOffer) {
            mutableStateOf(
                existingOffer?.subtitle?.ifBlank { null }
                    ?: "Order your favourites from ${targetRest.name}!"
            )
        }
        var setAsPrimary by remember(targetRest) { mutableStateOf(false) }
        var syncCoupon by remember(targetRest) { mutableStateOf(true) }

        val previewPct = (pctText.toIntOrNull() ?: 20).coerceIn(1, 90)
        val cleanPreviewCode = codeText.trim().uppercase().ifBlank { suggestedCode }

        AlertDialog(
            onDismissRequest = { promoTargetRest = null },
            title = { Text("Set % OFF Promo • ${targetRest.name}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminPromoBannerPreview(
                        badge = "USE CODE: $cleanPreviewCode",
                        restaurantName = targetRest.name,
                        title = titleText.ifBlank { "$previewPct% OFF at ${targetRest.name}" },
                        subtitle = subtitleText.ifBlank { "Tap to copy code $cleanPreviewCode & order!" }
                    )
                    Text("Quick % OFF", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf(10, 15, 20, 25, 30, 50)) { preset ->
                            FilterChip(
                                selected = pctText == preset.toString(),
                                onClick = {
                                    pctText = preset.toString()
                                    titleText = "$preset% OFF at ${targetRest.name}"
                                },
                                label = { Text("$preset% OFF") }
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = codeText,
                            onValueChange = { codeText = it.uppercase() },
                            label = { Text("Promo Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("dialog_rest_promo_code")
                        )
                        OutlinedTextField(
                            value = pctText,
                            onValueChange = { next ->
                                val digits = next.filter { it.isDigit() }.take(2)
                                pctText = digits
                                digits.toIntOrNull()?.takeIf { it > 0 }?.let { pct ->
                                    titleText = "$pct% OFF at ${targetRest.name}"
                                }
                            },
                            label = { Text("% OFF") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.8f).testTag("dialog_rest_promo_pct")
                        )
                    }
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Banner Headline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subtitleText,
                        onValueChange = { subtitleText = it },
                        label = { Text("Banner Subtitle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Replace Main Home Banner", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = setAsPrimary, onCheckedChange = { setAsPrimary = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Sync Checkout Coupon Code", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = syncCoupon, onCheckedChange = { syncCoupon = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = cleanPreviewCode.length >= 2 && (pctText.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        val finalPct = (pctText.toIntOrNull() ?: 20).coerceIn(1, 90)
                        val finalTitle = titleText.trim().ifBlank { "$finalPct% OFF at ${targetRest.name}" }
                        val finalSub = subtitleText.trim().ifBlank { "Order your favourites from ${targetRest.name}!" }
                        if (setAsPrimary) {
                            adminVm.savePromoSettingsAndSyncCoupon(
                                updated = settings.copy(
                                    promoBannerEnabled = true,
                                    promoCode = cleanPreviewCode,
                                    promoDiscountPercent = finalPct,
                                    promoBadgeText = "USE CODE: $cleanPreviewCode",
                                    promoTitle = finalTitle,
                                    promoSubtitle = finalSub,
                                    promoRestaurantId = targetRest.id,
                                    promoRestaurantName = targetRest.name
                                ),
                                syncCoupon = syncCoupon
                            )
                        } else {
                            adminVm.addRestaurantPromoOffer(
                                offer = PromoOffer(
                                    id = existingOffer?.id ?: "",
                                    restaurantId = targetRest.id,
                                    restaurantName = targetRest.name,
                                    code = cleanPreviewCode,
                                    badgeText = "USE CODE: $cleanPreviewCode",
                                    title = finalTitle,
                                    subtitle = finalSub,
                                    discountPercent = finalPct
                                ),
                                syncCoupon = syncCoupon
                            )
                        }
                        promoTargetRest = null
                    },
                    modifier = Modifier.testTag("dialog_rest_promo_save")
                ) { Text("Save % OFF Promo") }
            },
            dismissButton = { TextButton(onClick = { promoTargetRest = null }) { Text("Cancel") } }
        )
    }

    credentialTarget?.let { targetRest ->
        val existingCred = credentials.firstOrNull { it.restaurantId == targetRest.id }
        val suggestedLogin = remember(targetRest, existingCred) {
            existingCred?.loginId?.ifBlank { null }
                ?: targetRest.portalLoginId.ifBlank { null }
                ?: targetRest.name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        }
        var loginId by remember(targetRest, existingCred) { mutableStateOf(suggestedLogin) }
        var password by remember(targetRest, existingCred) { mutableStateOf(existingCred?.password ?: "") }
        var showPass by remember { mutableStateOf(false) }

        val validCreds = loginId.trim().length >= 3 && password.length >= 4

        AlertDialog(
            onDismissRequest = { credentialTarget = null },
            title = { Text("Portal Login • ${targetRest.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Assign a unique Login ID & Password so staff for ${targetRest.name} can sign in from any mobile device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { loginId = it },
                        label = { Text("Restaurant Login ID (min 3 chars)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_rest_cred_id_input")
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (min 4 chars)") },
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
                        modifier = Modifier.fillMaxWidth().testTag("admin_rest_cred_pass_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = validCreds,
                    onClick = {
                        adminVm.saveRestaurantCredentials(targetRest, loginId, password)
                        credentialTarget = null
                    },
                    modifier = Modifier.testTag("admin_rest_cred_save_button")
                ) { Text("Save Credentials") }
            },
            dismissButton = { TextButton(onClick = { credentialTarget = null }) { Text("Cancel") } }
        )
    }

    hoursTargetRest?.let { rest ->
        OperatingHoursEditorDialog(
            vendorName = rest.name,
            initialIsOpen = rest.isOpen,
            initialOpeningTime = rest.openingTime,
            initialClosingTime = rest.closingTime,
            onDismiss = { hoursTargetRest = null },
            onSave = { isOpenVal, openTime, closeTime ->
                adminVm.updateVendorHoursAndOpen("restaurants", rest.id, rest.name, isOpenVal, openTime, closeTime)
                hoursTargetRest = null
            }
        )
    }
}

@Composable
private fun AdminGroceryStoresSection(adminVm: AdminViewModel) {
    val stores by adminVm.groceryStores.collectAsStateWithLifecycle()
    var hoursTargetStore by remember { mutableStateOf<GroceryStore?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Grocery Stores (${stores.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { adminVm.seedStarterCatalog() }) {
                    Text("Sync 5 Stores & Photos")
                }
            }
        }
        items(stores, key = { it.id }) { s ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GharTakImage(
                            imageUrl = groceryStoreImageUri(s.name, s.imageUrl),
                            contentDescription = s.name,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                            isGroceryFallback = true
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
                                adminVm.updateVendorHoursAndOpen("groceryStores", s.id, s.name, nextOpen, s.openingTime, s.closingTime)
                            }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { adminVm.updateGroceryStoreApproval(s, "approved") }) { Text("Approve") }
                        OutlinedButton(onClick = { adminVm.updateGroceryStoreApproval(s, "rejected") }) { Text("Reject") }
                        OutlinedButton(onClick = { adminVm.updateGroceryStoreApproval(s, "suspended") }) { Text("Suspend") }
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
                adminVm.updateVendorHoursAndOpen("groceryStores", store.id, store.name, isOpenVal, openTime, closeTime)
                hoursTargetStore = null
            }
        )
    }
}

@Composable
private fun AdminCustomersSection(adminVm: AdminViewModel) {
    val users by adminVm.users.collectAsStateWithLifecycle()
    var creditTarget by remember { mutableStateOf<UserProfile?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users, key = { it.id }) { u ->
            Card(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(u.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${u.email} • ${u.phone} • ${u.city}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Portal: ${u.activePortal} • Wallet: ${GharTakConstants.formatPkr(u.walletBalancePkr)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GharTakPink,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(onClick = { creditTarget = u }) {
                        Text("Credit Wallet")
                    }
                }
            }
        }
    }

    creditTarget?.let { user ->
        var amountText by remember { mutableStateOf("500") }
        var reasonText by remember { mutableStateOf("GT Service Bonus Credit") }
        val amt = amountText.toDoubleOrNull() ?: 0.0
        AlertDialog(
            onDismissRequest = { creditTarget = null },
            title = { Text("Credit ${user.fullName}'s Wallet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Balance: ${GharTakConstants.formatPkr(user.walletBalancePkr)}", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason / Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = amt in 1.0..500000.0,
                    onClick = {
                        adminVm.creditCustomerWallet(user, amt, reasonText)
                        creditTarget = null
                    }
                ) { Text("Send Credit") }
            },
            dismissButton = { TextButton(onClick = { creditTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminRidersSection(adminVm: AdminViewModel) {
    val riders by adminVm.riders.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (riders.isEmpty()) {
            item { EmptyStateView("No rider registrations yet", "Rider applications will appear here for CNIC & license verification.") }
        } else {
            items(riders, key = { it.id }) { rider ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(rider.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            StatusBadge(rider.approvalStatus)
                        }
                        Text("CNIC: ${rider.cnicNumber} • License: ${rider.licenseNumber}", style = MaterialTheme.typography.bodySmall)
                        Text("${rider.vehicleType} (${rider.vehicleNumber}) • ${rider.city} • ${rider.phone}", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { adminVm.updateRiderApproval(rider, "approved") }) { Text("Approve") }
                            OutlinedButton(onClick = { adminVm.updateRiderApproval(rider, "rejected") }) { Text("Reject") }
                            OutlinedButton(onClick = { adminVm.updateRiderApproval(rider, "suspended") }) { Text("Suspend") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrdersSection(adminVm: AdminViewModel, onOpenTracking: () -> Unit) {
    val orders by adminVm.orders.collectAsStateWithLifecycle()
    val riders by adminVm.riders.collectAsStateWithLifecycle()
    var statusFilter by remember { mutableStateOf("All") }
    var assignTarget by remember { mutableStateOf<OrderRecord?>(null) }

    val approvedRiders = riders.filter { it.approvalStatus == "approved" }
    val filtered = if (statusFilter == "All") orders else orders.filter { it.status == statusFilter }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order Queue (${filtered.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onOpenTracking,
                    modifier = Modifier.testTag("orders_open_tracking")
                ) { Text("Live Tracking") }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("All") + GharTakConstants.ORDER_STATUSES) { st ->
                    FilterChip(selected = statusFilter == st, onClick = { statusFilter = st }, label = { Text(st) })
                }
            }
        }
        items(filtered, key = { it.id }) { order ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${order.id.takeLast(6).uppercase()} • ${order.vendorName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(order.status)
                    }
                    Text("Customer: ${order.customerName} (${order.customerPhone})", style = MaterialTheme.typography.bodySmall)
                    Text("Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
                    Text("Total: ${GharTakConstants.formatPkr(order.totalPkr)} • Payment: ${order.paymentMethod} (${order.paymentStatus})", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (order.riderId.isBlank() && approvedRiders.isNotEmpty()) {
                            OutlinedButton(onClick = { assignTarget = order }) { Text("Assign Rider") }
                        }
                        if (order.status != "Delivered") {
                            Button(onClick = { adminVm.updateOrderAdmin(order, "Delivered", paymentStatus = "paid") }) { Text("Mark Delivered") }
                        }
                    }
                }
            }
        }
    }

    assignTarget?.let { order ->
        AlertDialog(
            onDismissRequest = { assignTarget = null },
            title = { Text("Assign Approved Rider") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    approvedRiders.forEach { rider ->
                        OutlinedButton(
                            onClick = {
                                adminVm.updateOrderAdmin(order, "Rider Assigned", rider = rider)
                                assignTarget = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${rider.fullName} (${rider.city})")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { assignTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminPaymentsSection(adminVm: AdminViewModel) {
    val orders by adminVm.orders.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(orders, key = { it.id }) { order ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${order.paymentMethod} • ${GharTakConstants.formatPkr(order.totalPkr)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("#${order.id.takeLast(6).uppercase()} • ${order.customerName} (${order.customerPhone})", style = MaterialTheme.typography.bodySmall)
                        if ((order.paymentMethod == "JazzCash" || order.paymentMethod == "Easypaisa") && order.paymentStatus == "awaiting_verification") {
                            Text(
                                "Check Support Chats for customer's payment confirmation message",
                                style = MaterialTheme.typography.labelSmall,
                                color = GharTakPink
                            )
                        }
                    }
                    StatusBadge(order.paymentStatus)
                    if (order.paymentStatus != "paid") {
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { adminVm.updateOrderAdmin(order, order.status, paymentStatus = "paid") }) {
                            Text("Verify Paid")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCommissionsSection(adminVm: AdminViewModel) {
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    var restComm by remember(settings) { mutableStateOf(settings.restaurantCommissionPercent.toString()) }
    var grocComm by remember(settings) { mutableStateOf(settings.groceryCommissionPercent.toString()) }
    var baseFee by remember(settings) { mutableStateOf(settings.baseDeliveryFeePkr.toString()) }
    var riderPayout by remember(settings) { mutableStateOf(settings.riderPayoutPkr.toString()) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Commission & Delivery Charges (PKR)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(value = restComm, onValueChange = { restComm = it }, label = { Text("Restaurant Commission (%)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = grocComm, onValueChange = { grocComm = it }, label = { Text("Grocery Store Commission (%)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = baseFee, onValueChange = { baseFee = it }, label = { Text("Base Delivery Fee (PKR)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = riderPayout, onValueChange = { riderPayout = it }, label = { Text("Rider Payout per Order (PKR)") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Surface(
                color = GharTakEmerald.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (settings.radiusPricingEnabled) {
                        "Note: City Radius Pricing is ON (0–${settings.deliveryBaseRadiusKm.toInt()} km = ${GharTakConstants.formatPkr(settings.withinRadiusFeePkr)}). Edit Radius & Multan location rates in the Delivery & Area Fees tab."
                    } else {
                        "Active Mode: Standard Base Delivery Fee (${GharTakConstants.formatPkr(settings.baseDeliveryFeePkr)}) is applied when no Area/Multan rule matches."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GharTakEmerald,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        item {
            Button(
                onClick = {
                    adminVm.saveSettings(
                        settings.copy(
                            restaurantCommissionPercent = (restComm.toDoubleOrNull() ?: 15.0).coerceIn(0.0, 50.0),
                            groceryCommissionPercent = (grocComm.toDoubleOrNull() ?: 10.0).coerceIn(0.0, 50.0),
                            baseDeliveryFeePkr = (baseFee.toDoubleOrNull() ?: 80.0).coerceIn(0.0, 2000.0),
                            riderPayoutPkr = (riderPayout.toDoubleOrNull() ?: 70.0).coerceIn(0.0, 2000.0)
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Financial Settings") }
        }
    }
}

@Composable
private fun AdminCouponsSection(
    adminVm: AdminViewModel,
    onOpenPromos: () -> Unit = {}
) {
    val coupons by adminVm.coupons.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("percentage") }
    var value by remember { mutableStateOf("15") }
    var minOrder by remember { mutableStateOf("500") }
    var maxDiscount by remember { mutableStateOf("250") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Restaurant & Home % OFF Banners", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Want to add or customize the % OFF promo banner shown on Home & Restaurant menus (e.g. USE CODE: GHARTAK20)?",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = onOpenPromos,
                        modifier = Modifier.fillMaxWidth().testTag("admin_coupons_open_promos")
                    ) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open Promo Banners (% OFF)")
                    }
                }
            }
        }
        item { Text("Create Promo Coupon", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Coupon Code (e.g. EID25)") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = discountType == "percentage", onClick = { discountType = "percentage" }, label = { Text("Percentage %") })
                FilterChip(selected = discountType == "fixed", onClick = { discountType = "fixed" }, label = { Text("Fixed PKR") })
            }
        }
        item { OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Discount Value") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = minOrder, onValueChange = { minOrder = it }, label = { Text("Min Order (PKR)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = maxDiscount, onValueChange = { maxDiscount = it }, label = { Text("Max Discount (PKR)") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                enabled = code.trim().length >= 2 && (value.toDoubleOrNull() ?: 0.0) > 0.0,
                onClick = {
                    adminVm.saveCoupon(
                        CouponRecord(
                            code = code.trim().uppercase(),
                            discountType = discountType,
                            discountValue = value.toDoubleOrNull() ?: 10.0,
                            minOrderPkr = minOrder.toDoubleOrNull() ?: 500.0,
                            maxDiscountPkr = maxDiscount.toDoubleOrNull() ?: 250.0,
                            isActive = true
                        )
                    )
                    code = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create Coupon") }
        }
        items(coupons, key = { it.id }) { c ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(c.code, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${c.discountType} • ${c.discountValue} (Min ${GharTakConstants.formatPkr(c.minOrderPkr)})", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = c.isActive, onCheckedChange = { adminVm.saveCoupon(c.copy(isActive = it)) })
                        IconButton(onClick = { adminVm.deleteCoupon(c) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReviewsSection(adminVm: AdminViewModel) {
    val reviews by adminVm.reviews.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (reviews.isEmpty()) {
            item { EmptyStateView("No customer reviews yet", "Reviews appear here for moderation.") }
        } else {
            items(reviews, key = { it.id }) { rev ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${rev.targetName} • ${rev.rating.toInt()} ★ by ${rev.userName}", style = MaterialTheme.typography.titleSmall)
                            Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                            StatusBadge(if (rev.isHidden) "Hidden" else "Visible")
                        }
                        TextButton(onClick = { adminVm.toggleReviewHidden(rev) }) {
                            Text(if (rev.isHidden) "Restore" else "Hide")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReportsSection(adminVm: AdminViewModel) {
    val orders by adminVm.orders.collectAsStateWithLifecycle()
    val delivered = orders.filter { it.status == "Delivered" }
    val cancelled = orders.filter { it.status == "Cancelled" || it.status == "Rejected" }
    val pending = orders.filter { it.status !in GharTakConstants.TERMINAL_ORDER_STATUSES }
    val foodRev = delivered.filter { it.orderType == "food" }.sumOf { it.totalPkr }
    val groceryRev = delivered.filter { it.orderType == "grocery" }.sumOf { it.totalPkr }
    val medicalRev = delivered.filter { it.orderType == "medical" }.sumOf { it.totalPkr }
    val totalComm = delivered.sumOf { it.commissionPkr }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Sales & Commission Reports (PKR)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Pending Orders", "${pending.size}", Modifier.weight(1f), GharTakAmber)
                MetricCard("Delivered", "${delivered.size}", Modifier.weight(1f), GharTakEmerald)
                MetricCard("Cancelled", "${cancelled.size}", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Food Revenue", GharTakConstants.formatPkr(foodRev), Modifier.weight(1f))
                MetricCard("Grocery Revenue", GharTakConstants.formatPkr(groceryRev), Modifier.weight(1f), GharTakEmerald)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Medical Revenue", GharTakConstants.formatPkr(medicalRev), Modifier.weight(1f), GharTakMedicalTeal)
                MetricCard("Platform Commission", GharTakConstants.formatPkr(totalComm), Modifier.weight(1f), GharTakPink)
            }
        }
    }
}

@Composable
private fun AdminNotificationsSection(adminVm: AdminViewModel) {
    val users by adminVm.users.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Send In-App Notification to Customers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Notification Title") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message Body") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                enabled = title.isNotBlank() && body.isNotBlank() && users.isNotEmpty(),
                onClick = {
                    users.forEach { u -> adminVm.sendNotificationToUser(u.userId, title, body) }
                    title = ""
                    body = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Send to All ${users.size} Registered Users") }
        }
    }
}

@Composable
private fun AdminLogsSection(adminVm: AdminViewModel) {
    val logs by adminVm.logs.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (logs.isEmpty()) {
            item { EmptyStateView("No admin logs yet", "Every admin mutation records an immutable audit log entry here.") }
        } else {
            items(logs, key = { it.id }) { log ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${log.action} • ${log.targetType}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(log.details, style = MaterialTheme.typography.bodySmall)
                        Text("By: ${log.adminEmail}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminSettingsSection(adminVm: AdminViewModel) {
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    val cityRequests by adminVm.cityRequests.collectAsStateWithLifecycle()
    val restaurants by adminVm.restaurants.collectAsStateWithLifecycle()

    var newCity by remember { mutableStateOf("") }
    var supportPhoneInput by remember(settings) {
        mutableStateOf(settings.supportPhoneLocal.ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL })
    }
    var supportWhatsappInput by remember(settings) {
        mutableStateOf(settings.supportWhatsappNumber.ifBlank { GharTakConstants.toWhatsappDigits(settings.supportPhoneLocal) })
    }

    var jazzCash by remember(settings) { mutableStateOf(settings.jazzCashConfigured) }
    var easypaisa by remember(settings) { mutableStateOf(settings.easypaisaConfigured) }
    var jazzCashNumber by remember(settings) {
        mutableStateOf(settings.jazzCashNumber.ifBlank { GharTakConstants.DEFAULT_JAZZCASH_NUMBER })
    }
    var jazzCashTitle by remember(settings) {
        mutableStateOf(settings.jazzCashAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE })
    }
    var easypaisaNumber by remember(settings) {
        mutableStateOf(settings.easypaisaNumber.ifBlank { GharTakConstants.DEFAULT_EASYPAISA_NUMBER })
    }
    var easypaisaTitle by remember(settings) {
        mutableStateOf(settings.easypaisaAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE })
    }

    var newChannel by remember { mutableStateOf("") }
    var newAccTitle by remember { mutableStateOf("") }
    var newAccNumber by remember { mutableStateOf("") }

    val pendingCityRequests = remember(cityRequests) {
        cityRequests.filter { it.status == "pending" }
    }
    val missingDefaultCities = remember(settings.cities) {
        GharTakConstants.INITIAL_PAKISTAN_CITIES.filterNot { defCity ->
            settings.cities.any { it.equals(defCity, ignoreCase = true) }
        }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Locations, Chat Number, Promos & Gateways", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // 1. Admin Location / City Management + Approval Queue
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_city_settings_card")
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Approved Locations (${settings.cities.size} Cities)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Only cities listed below appear to customers. Add custom cities or approve/reject customer location requests.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newCity,
                            onValueChange = { newCity = it },
                            label = { Text("Add Custom City Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_add_city_input")
                        )
                        Button(
                            enabled = newCity.trim().length >= 2,
                            onClick = {
                                val candidate = newCity.trim()
                                val exists = settings.cities.any { it.equals(candidate, ignoreCase = true) }
                                val updated = if (exists) settings.cities else settings.cities + candidate
                                adminVm.saveSettings(settings.copy(cities = updated))
                                newCity = ""
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("admin_add_city_button")
                        ) { Text("Add") }
                    }

                    if (settings.cities.size > 1) {
                        Text(
                            "Tap any city chip to remove it from approved customer locations:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        settings.cities.forEach { city ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    if (settings.cities.size > 1) {
                                        val remaining = settings.cities.filterNot { it.equals(city, ignoreCase = true) }
                                        if (remaining.isNotEmpty()) {
                                            adminVm.saveSettings(settings.copy(cities = remaining))
                                        }
                                    }
                                },
                                label = { Text(city) },
                                trailingIcon = if (settings.cities.size > 1) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove $city",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.testTag("admin_city_chip_$city")
                            )
                        }
                    }
                    if (missingDefaultCities.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val merged = (settings.cities + GharTakConstants.INITIAL_PAKISTAN_CITIES).distinctBy { it.lowercase() }
                                adminVm.saveSettings(settings.copy(cities = merged))
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("admin_sync_20_cities_button")
                        ) {
                            Text("Load All 20 Pakistan Cities (+${missingDefaultCities.size} missing)")
                        }
                    }

                    if (pendingCityRequests.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            "Pending Location Requests (${pendingCityRequests.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GharTakPink
                        )
                        pendingCityRequests.forEach { req ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(req.cityName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Requested by ${req.requesterName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { adminVm.reviewCityRequest(req, approve = true) },
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("admin_approve_city_${req.id}")
                                        ) { Text("Approve") }
                                        OutlinedButton(
                                            onClick = { adminVm.reviewCityRequest(req, approve = false) },
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("admin_reject_city_${req.id}")
                                        ) { Text("Reject") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Support Chat & Helpline Number Editor
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_support_phone_settings_card")
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Support Chat & Helpline Number", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Edit the helpline & WhatsApp number displayed across Live Support Chat, Call, and payment alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = supportPhoneInput,
                        onValueChange = { next ->
                            supportPhoneInput = next
                            supportWhatsappInput = GharTakConstants.toWhatsappDigits(next)
                        },
                        label = { Text("Chat / Call Helpline Number (e.g. 03036767152)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_support_phone_input")
                    )
                    OutlinedTextField(
                        value = supportWhatsappInput,
                        onValueChange = { next -> supportWhatsappInput = next.filter { it.isDigit() } },
                        label = { Text("WhatsApp Digits (923...)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_support_whatsapp_input")
                    )
                    Button(
                        enabled = supportPhoneInput.trim().length >= 7,
                        onClick = {
                            val cleanPhone = supportPhoneInput.trim()
                            val cleanWa = supportWhatsappInput.trim().ifBlank { GharTakConstants.toWhatsappDigits(cleanPhone) }
                            adminVm.saveSettings(
                                settings.copy(
                                    supportPhoneLocal = cleanPhone,
                                    supportWhatsappNumber = cleanWa
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("admin_save_support_phone_button")
                    ) { Text("Save Chat & Helpline Number") }
                }
            }
        }

        // 3. Primary Home Promo + Restaurant-Specific Promos Builder
        item {
            AdminPromoManagementCard(
                adminVm = adminVm,
                settings = settings,
                restaurants = restaurants
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("JazzCash & Easypaisa Receiving Numbers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("JazzCash Enabled")
                        Switch(checked = jazzCash, onCheckedChange = { jazzCash = it })
                    }
                    OutlinedTextField(
                        value = jazzCashNumber,
                        onValueChange = { jazzCashNumber = it },
                        label = { Text("JazzCash Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_jazzcash_number_input")
                    )
                    OutlinedTextField(
                        value = jazzCashTitle,
                        onValueChange = { jazzCashTitle = it },
                        label = { Text("JazzCash Account Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Easypaisa Enabled")
                        Switch(checked = easypaisa, onCheckedChange = { easypaisa = it })
                    }
                    OutlinedTextField(
                        value = easypaisaNumber,
                        onValueChange = { easypaisaNumber = it },
                        label = { Text("Easypaisa Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_easypaisa_number_input")
                    )
                    OutlinedTextField(
                        value = easypaisaTitle,
                        onValueChange = { easypaisaTitle = it },
                        label = { Text("Easypaisa Account Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            adminVm.saveSettings(
                                settings.copy(
                                    jazzCashConfigured = jazzCash,
                                    easypaisaConfigured = easypaisa,
                                    jazzCashNumber = jazzCashNumber.trim().ifBlank { GharTakConstants.DEFAULT_JAZZCASH_NUMBER },
                                    jazzCashAccountTitle = jazzCashTitle.trim().ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE },
                                    easypaisaNumber = easypaisaNumber.trim().ifBlank { GharTakConstants.DEFAULT_EASYPAISA_NUMBER },
                                    easypaisaAccountTitle = easypaisaTitle.trim().ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_save_gateway_settings_button")
                    ) { Text("Save Gateway Settings") }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Extra Beneficiary Accounts (${settings.beneficiaryAccounts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Add bank IBAN, SadaPay, NayaPay, or extra wallet accounts shown to customers at Wallet Top-Up & Checkout.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    settings.beneficiaryAccounts.forEach { acc ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("${acc.channel} • ${acc.accountTitle}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text(acc.accountNumber, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(
                                    onClick = {
                                        adminVm.saveSettings(
                                            settings.copy(
                                                beneficiaryAccounts = settings.beneficiaryAccounts.filterNot { it.id == acc.id }
                                            )
                                        )
                                    }
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove beneficiary account", tint = GharTakCoralError)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newChannel,
                        onValueChange = { newChannel = it },
                        label = { Text("Channel / Bank Name (e.g. Meezan Bank, SadaPay)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAccTitle,
                        onValueChange = { newAccTitle = it },
                        label = { Text("Beneficiary Account Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAccNumber,
                        onValueChange = { newAccNumber = it },
                        label = { Text("Account Number / IBAN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        enabled = newChannel.trim().length >= 2 && newAccTitle.trim().length >= 2 && newAccNumber.trim().length >= 4,
                        onClick = {
                            val newEntry = BeneficiaryAccount(
                                id = "ben_${System.currentTimeMillis()}",
                                channel = newChannel.trim(),
                                accountTitle = newAccTitle.trim(),
                                accountNumber = newAccNumber.trim()
                            )
                            adminVm.saveSettings(
                                settings.copy(beneficiaryAccounts = settings.beneficiaryAccounts + newEntry)
                            )
                            newChannel = ""
                            newAccTitle = ""
                            newAccNumber = ""
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_add_beneficiary_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Beneficiary Account")
                    }
                }
            }
        }
        item {
            SupportHelplineCard(
                subtitle = "Configured Helpline & Merchant WhatsApp Number",
                supportPhoneLocal = settings.supportPhoneLocal,
                supportWhatsappNumber = settings.supportWhatsappNumber
            )
        }
    }
}

@Composable
private fun AdminProfileSection(adminVm: AdminViewModel, onExitToCustomer: () -> Unit, onSignOut: () -> Unit) {
    val adminAuth by adminVm.adminAuthConfig.collectAsStateWithLifecycle()
    var loginIdInput by remember(adminAuth.loginEmail) { mutableStateOf(adminAuth.loginEmail) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }

    val canSaveCreds = loginIdInput.trim().length >= 2 &&
        newPassword.length >= 4 &&
        newPassword == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Super Admin Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Google Email: ${adminVm.adminEmail}", style = MaterialTheme.typography.bodyMedium)
                Text("Portal Login ID: ${adminAuth.loginEmail}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Role: Verified Bootstrapped Super Admin", style = MaterialTheme.typography.bodySmall, color = GharTakEmerald)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Change Super Admin Login ID & Password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Update the credentials required on the GT Service Admin Login screen across all devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = loginIdInput,
                    onValueChange = { loginIdInput = it },
                    label = { Text("Admin Login ID (min 2 chars)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_change_login_id_input")
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (min 4 chars)") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(
                                imageVector = if (showPasswords) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPasswords) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_change_password_input")
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("admin_confirm_password_input")
                )
                if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text("Passwords do not match.", color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    enabled = canSaveCreds,
                    onClick = {
                        adminVm.updateAdminAuthCredentials(loginIdInput.trim(), newPassword)
                        newPassword = ""
                        confirmPassword = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_update_credentials_button")
                ) {
                    Text("Update Admin Credentials")
                }
            }
        }

        OutlinedButton(onClick = onExitToCustomer, modifier = Modifier.fillMaxWidth()) { Text("Switch to Customer App") }
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
    }
}

@Composable
private fun AdminPromosSection(adminVm: AdminViewModel) {
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    val restaurants by adminVm.restaurants.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AdminPromoManagementCard(
                adminVm = adminVm,
                settings = settings,
                restaurants = restaurants
            )
        }
    }
}

@Composable
private fun AdminPromoManagementCard(
    adminVm: AdminViewModel,
    settings: PlatformSettings,
    restaurants: List<Restaurant>
) {
    var promoBannerEnabled by remember(settings) { mutableStateOf(settings.promoBannerEnabled) }
    var promoRestId by remember(settings) { mutableStateOf(settings.promoRestaurantId) }
    var promoRestName by remember(settings) { mutableStateOf(settings.promoRestaurantName) }
    var promoCode by remember(settings) { mutableStateOf(settings.promoCode.ifBlank { "GHARTAK20" }) }
    var promoDiscountPercent by remember(settings) { mutableStateOf(settings.promoDiscountPercent.toString()) }
    var promoBadgeText by remember(settings) { mutableStateOf(settings.promoBadgeText.ifBlank { "USE CODE: GHARTAK20" }) }
    var promoTitle by remember(settings) { mutableStateOf(settings.promoTitle.ifBlank { "20% OFF Food & Groceries" }) }
    var promoSubtitle by remember(settings) { mutableStateOf(settings.promoSubtitle.ifBlank { "Your Favourite Food, Delivered to Your Door!" }) }
    var syncPromoCoupon by remember { mutableStateOf(true) }

    var editingOfferId by remember { mutableStateOf("") }
    var offerRestId by remember { mutableStateOf("") }
    var offerRestName by remember { mutableStateOf("") }
    var offerCode by remember { mutableStateOf("") }
    var offerPct by remember { mutableStateOf("20") }
    var offerTitle by remember { mutableStateOf("") }
    var offerSubtitle by remember { mutableStateOf("") }

    val cleanPrimaryCode = promoCode.trim().uppercase().ifBlank { "GHARTAK20" }
    val primaryPct = (promoDiscountPercent.toIntOrNull() ?: 20).coerceIn(0, 90)
    val previewTitle = promoTitle.ifBlank {
        if (promoRestName.isNotBlank()) "$primaryPct% OFF at $promoRestName" else "$primaryPct% OFF Food & Groceries"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_promo_settings_card")
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Home Promo & Restaurant % OFF Offers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Create or customize % OFF promo banners for any Restaurant or all Food & Groceries. Customers tap the banner to copy the promo code & open the restaurant.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Live Banner Preview", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            AdminPromoBannerPreview(
                badge = promoBadgeText.ifBlank { "USE CODE: $cleanPrimaryCode" },
                restaurantName = promoRestName,
                title = previewTitle,
                subtitle = promoSubtitle.ifBlank { "Your Favourite Food, Delivered to Your Door!" }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show Primary Promo Banner on Home")
                Switch(checked = promoBannerEnabled, onCheckedChange = { promoBannerEnabled = it })
            }
            Text("Target Restaurant (Primary Banner)", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = promoRestId.isBlank(),
                        onClick = {
                            promoRestId = ""
                            promoRestName = ""
                            val pct = promoDiscountPercent.toIntOrNull() ?: 20
                            promoTitle = "$pct% OFF Food & Groceries"
                        },
                        label = { Text("All Food & Groceries") }
                    )
                }
                items(restaurants, key = { it.id }) { r ->
                    FilterChip(
                        selected = promoRestId == r.id,
                        onClick = {
                            promoRestId = r.id
                            promoRestName = r.name
                            val pct = promoDiscountPercent.toIntOrNull() ?: 20
                            promoTitle = "$pct% OFF at ${r.name}"
                        },
                        label = { Text(r.name) }
                    )
                }
            }
            Text("Quick Discount %", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf(10, 15, 20, 25, 30, 50)) { preset ->
                    FilterChip(
                        selected = promoDiscountPercent == preset.toString(),
                        onClick = {
                            promoDiscountPercent = preset.toString()
                            promoTitle = if (promoRestName.isNotBlank()) {
                                "$preset% OFF at $promoRestName"
                            } else {
                                "$preset% OFF Food & Groceries"
                            }
                        },
                        label = { Text("$preset% OFF") }
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { next ->
                        promoCode = next.uppercase()
                        promoBadgeText = "USE CODE: ${next.trim().uppercase()}"
                    },
                    label = { Text("Promo Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f).testTag("admin_promo_code_input")
                )
                OutlinedTextField(
                    value = promoDiscountPercent,
                    onValueChange = { next ->
                        val digits = next.filter { it.isDigit() }.take(2)
                        promoDiscountPercent = digits
                        val pct = digits.toIntOrNull()
                        if (pct != null && pct > 0) {
                            promoTitle = if (promoRestName.isNotBlank()) {
                                "$pct% OFF at $promoRestName"
                            } else {
                                "$pct% OFF Food & Groceries"
                            }
                        }
                    },
                    label = { Text("Discount %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.8f).testTag("admin_promo_percent_input")
                )
            }
            OutlinedTextField(
                value = promoBadgeText,
                onValueChange = { promoBadgeText = it },
                label = { Text("Banner Badge Text") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = promoTitle,
                onValueChange = { promoTitle = it },
                label = { Text("Banner Headline (e.g. 20% OFF at Restaurant)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_promo_title_input")
            )
            OutlinedTextField(
                value = promoSubtitle,
                onValueChange = { promoSubtitle = it },
                label = { Text("Banner Subtitle") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Sync matching Checkout Coupon", style = MaterialTheme.typography.bodySmall)
                Switch(checked = syncPromoCoupon, onCheckedChange = { syncPromoCoupon = it })
            }
            Button(
                onClick = {
                    val pct = (promoDiscountPercent.toIntOrNull() ?: 20).coerceIn(0, 90)
                    adminVm.savePromoSettingsAndSyncCoupon(
                        updated = settings.copy(
                            promoBannerEnabled = promoBannerEnabled,
                            promoCode = promoCode.trim().uppercase().ifBlank { "GHARTAK20" },
                            promoDiscountPercent = pct,
                            promoBadgeText = promoBadgeText.trim(),
                            promoTitle = promoTitle.trim(),
                            promoSubtitle = promoSubtitle.trim(),
                            promoRestaurantId = promoRestId,
                            promoRestaurantName = promoRestName
                        ),
                        syncCoupon = syncPromoCoupon
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("admin_save_promo_settings_button")
            ) { Text("Save Primary Promo Settings") }

            HorizontalDivider()

            Text(
                "Restaurant Promo Banners (${settings.promoOffers.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            settings.promoOffers.forEach { offer ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                offer.title.ifBlank { "${offer.discountPercent}% OFF • ${offer.restaurantName}" },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${offer.restaurantName.ifBlank { "All Restaurants" }} • Code: ${offer.code} (${offer.discountPercent}% OFF)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    editingOfferId = offer.id
                                    offerRestId = offer.restaurantId
                                    offerRestName = offer.restaurantName
                                    offerCode = offer.code
                                    offerPct = offer.discountPercent.toString()
                                    offerTitle = offer.title
                                    offerSubtitle = offer.subtitle
                                },
                                modifier = Modifier.testTag("admin_edit_promo_${offer.id}")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit restaurant promo", tint = GharTakEmerald)
                            }
                            IconButton(
                                onClick = {
                                    if (editingOfferId == offer.id) editingOfferId = ""
                                    adminVm.removeRestaurantPromoOffer(offer.id)
                                },
                                modifier = Modifier.testTag("admin_delete_promo_${offer.id}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove restaurant promo", tint = GharTakCoralError)
                            }
                        }
                    }
                }
            }

            Text(
                if (editingOfferId.isNotBlank()) "Edit Restaurant Promo" else "Add Restaurant Promo",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = offerRestId.isBlank(),
                        onClick = {
                            offerRestId = ""
                            offerRestName = ""
                        },
                        label = { Text("All Restaurants") }
                    )
                }
                items(restaurants, key = { "offer_${it.id}" }) { r ->
                    FilterChip(
                        selected = offerRestId == r.id,
                        onClick = {
                            offerRestId = r.id
                            offerRestName = r.name
                            val pct = offerPct.toIntOrNull() ?: 20
                            if (offerTitle.isBlank() || editingOfferId.isBlank()) {
                                offerTitle = "$pct% OFF at ${r.name}"
                            }
                            if (offerCode.isBlank()) {
                                offerCode = r.name.filter { ch -> ch.isLetterOrDigit() }.take(5).uppercase().ifBlank { "REST" } + pct
                            }
                        },
                        label = { Text(r.name) }
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf(10, 15, 20, 25, 30, 50)) { preset ->
                    FilterChip(
                        selected = offerPct == preset.toString(),
                        onClick = {
                            offerPct = preset.toString()
                            if (offerRestName.isNotBlank()) {
                                offerTitle = "$preset% OFF at $offerRestName"
                            } else if (offerTitle.isBlank()) {
                                offerTitle = "$preset% OFF on Food Orders"
                            }
                        },
                        label = { Text("$preset% OFF") }
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = offerCode,
                    onValueChange = { offerCode = it.uppercase() },
                    label = { Text("Code (e.g. PIZZA25)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f).testTag("admin_new_offer_code_input")
                )
                OutlinedTextField(
                    value = offerPct,
                    onValueChange = { next ->
                        val digits = next.filter { ch -> ch.isDigit() }.take(2)
                        offerPct = digits
                        digits.toIntOrNull()?.takeIf { it > 0 && offerRestName.isNotBlank() }?.let { pct ->
                            offerTitle = "$pct% OFF at $offerRestName"
                        }
                    },
                    label = { Text("% OFF") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.8f)
                )
            }
            OutlinedTextField(
                value = offerTitle,
                onValueChange = { offerTitle = it },
                label = { Text("Restaurant Promo Headline") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_new_offer_title_input")
            )
            OutlinedTextField(
                value = offerSubtitle,
                onValueChange = { offerSubtitle = it },
                label = { Text("Promo Subtitle (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editingOfferId.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            editingOfferId = ""
                            offerRestId = ""
                            offerRestName = ""
                            offerCode = ""
                            offerPct = "20"
                            offerTitle = ""
                            offerSubtitle = ""
                        },
                        modifier = Modifier.weight(0.9f).heightIn(min = 48.dp)
                    ) { Text("Cancel") }
                }
                OutlinedButton(
                    enabled = offerCode.trim().length >= 2 && (offerPct.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        val pct = (offerPct.toIntOrNull() ?: 20).coerceIn(1, 90)
                        val cleanCode = offerCode.trim().uppercase()
                        adminVm.addRestaurantPromoOffer(
                            offer = PromoOffer(
                                id = editingOfferId,
                                restaurantId = offerRestId,
                                restaurantName = offerRestName,
                                code = cleanCode,
                                badgeText = "USE CODE: $cleanCode",
                                title = offerTitle.trim().ifBlank {
                                    if (offerRestName.isNotBlank()) "$pct% OFF at $offerRestName" else "$pct% OFF"
                                },
                                subtitle = offerSubtitle.trim(),
                                discountPercent = pct
                            ),
                            syncCoupon = syncPromoCoupon
                        )
                        editingOfferId = ""
                        offerRestId = ""
                        offerRestName = ""
                        offerCode = ""
                        offerPct = "20"
                        offerTitle = ""
                        offerSubtitle = ""
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .heightIn(min = 48.dp)
                        .testTag("admin_add_restaurant_promo_button")
                ) {
                    Icon(if (editingOfferId.isNotBlank()) Icons.Filled.Edit else Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (editingOfferId.isNotBlank()) "Update Restaurant Promo" else "Add Restaurant Promo Banner")
                }
            }
        }
    }
}

@Composable
private fun AdminPromoBannerPreview(
    badge: String,
    restaurantName: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        GharTakImage(
            imageUrl = "res://img_banner_deals",
            contentDescription = title,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(GharTakCharcoal.copy(alpha = 0.90f), GharTakCharcoal.copy(alpha = 0.35f))
                    )
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = GharTakEmerald, shape = RoundedCornerShape(999.dp)) {
                        Text(
                            badge,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    if (restaurantName.isNotBlank()) {
                        Surface(color = GtNavyHeader, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                "$restaurantName • Order",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Column {
                    Text(
                        title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        subtitle,
                        color = GtMintSubtext,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
