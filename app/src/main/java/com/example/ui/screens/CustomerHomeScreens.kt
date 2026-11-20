package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CartItem
import com.example.data.model.FoodItem
import com.example.data.model.GharTakConstants
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.MedicalProduct
import com.example.data.model.PlatformSettings
import com.example.data.model.Restaurant
import com.example.ui.components.CityPickerDialog
import com.example.ui.components.DiscountBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FoodDayBadges
import com.example.ui.components.GharTakImage
import com.example.ui.components.OperatingHoursBadge
import com.example.ui.components.PriceRow
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.foodCategoryDrawableRes
import com.example.ui.components.foodCategoryImageUri
import com.example.ui.components.groceryCategoryDrawableRes
import com.example.ui.components.groceryCategoryImageUri
import com.example.ui.components.groceryStoreImageUri
import com.example.ui.components.medicalCategoryDrawableRes
import com.example.ui.components.medicalStoreImageUri
import com.example.ui.components.restaurantImageUri
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCharcoal
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakMedicalTeal
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.GtLightGreyCard
import com.example.ui.theme.GtMintSubtext
import com.example.ui.theme.GtNavyHeader
import com.example.ui.theme.GtSlateNavyMuted
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.GharTakViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun CustomerHomeScreen(
    viewModel: GharTakViewModel,
    onOpenFoodHub: () -> Unit,
    onOpenGroceryHub: () -> Unit,
    onOpenMedicalHub: () -> Unit,
    onOpenMultanHub: () -> Unit = {},
    onOpenCelebrationHub: () -> Unit = {},
    onOpenSearch: () -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onOpenGroceryStore: (String) -> Unit,
    onOpenMedicalStore: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenSupportChat: () -> Unit = {},
    onNavigateToCheckout: () -> Unit,
    onOpenAdminSeedShortcut: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val foodCategories by viewModel.foodCategories.collectAsStateWithLifecycle()
    val groceryCategories by viewModel.groceryCategories.collectAsStateWithLifecycle()
    val medicalCategories by viewModel.medicalCategories.collectAsStateWithLifecycle()
    val restaurantsState by viewModel.restaurantsState.collectAsStateWithLifecycle()
    val foodItemsState by viewModel.foodItemsState.collectAsStateWithLifecycle()
    val groceryStoresState by viewModel.groceryStoresState.collectAsStateWithLifecycle()
    val groceryProductsState by viewModel.groceryProductsState.collectAsStateWithLifecycle()
    val medicalStoresState by viewModel.medicalStoresState.collectAsStateWithLifecycle()
    val medicalProductsState by viewModel.medicalProductsState.collectAsStateWithLifecycle()
    val ordersState by viewModel.customerOrdersState.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val supportChatThread by viewModel.supportChatThread.collectAsStateWithLifecycle()
    val myCityRequests by viewModel.myCityRequests.collectAsStateWithLifecycle()
    val myParcelRiderRequests by viewModel.myParcelRiderRequests.collectAsStateWithLifecycle()

    var showCityPicker by remember { mutableStateOf(false) }
    var parcelRiderDialogType by remember { mutableStateOf<String?>(null) }
    var customizingFood by remember { mutableStateOf<FoodItem?>(null) }
    var inspectingGrocery by remember { mutableStateOf<GroceryProduct?>(null) }
    var inspectingMedical by remember { mutableStateOf<MedicalProduct?>(null) }

    val walletBalance = userProfile?.walletBalancePkr ?: 0.0
    val unreadCount = notifications.count { !it.isRead }
    val unreadChatCount = supportChatThread?.unreadByCustomer ?: 0L
    val restaurants = (restaurantsState as? UiState.Success)?.data.orEmpty()
    val foodItems = (foodItemsState as? UiState.Success)?.data.orEmpty()
    val groceryStores = (groceryStoresState as? UiState.Success)?.data.orEmpty()
    val groceryProducts = (groceryProductsState as? UiState.Success)?.data.orEmpty()
    val medicalStores = (medicalStoresState as? UiState.Success)?.data.orEmpty()
    val medicalProducts = (medicalProductsState as? UiState.Success)?.data.orEmpty()
    val orders = (ordersState as? UiState.Success)?.data.orEmpty()
    val isAllCities = selectedCity.equals(GharTakConstants.ALL_CITIES, ignoreCase = true)

    val cityRestaurants = remember(restaurants, selectedCity, isAllCities) {
        if (isAllCities) restaurants
        else {
            val matching = restaurants.filter { it.city.equals(selectedCity, ignoreCase = true) }
            val others = restaurants.filterNot { it.city.equals(selectedCity, ignoreCase = true) }
            matching + others
        }
    }
    val cityStores = remember(groceryStores, selectedCity, isAllCities) {
        if (isAllCities) groceryStores
        else {
            val matching = groceryStores.filter { it.city.equals(selectedCity, ignoreCase = true) }
            val others = groceryStores.filterNot { it.city.equals(selectedCity, ignoreCase = true) }
            matching + others
        }
    }
    val cityMedicalStores = remember(medicalStores, selectedCity, isAllCities) {
        if (isAllCities) medicalStores
        else {
            val matching = medicalStores.filter { it.city.equals(selectedCity, ignoreCase = true) }
            val others = medicalStores.filterNot { it.city.equals(selectedCity, ignoreCase = true) }
            matching + others
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header with Navy Blue bar, GT Service branding, city selector & White search bar
        item {
            Surface(
                color = GtNavyHeader,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = Color(0xFFF7F8FD),
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 2.dp
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.gt_service_logo),
                                    contentDescription = "GT Service Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .padding(3.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "GT Service",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showCityPicker = true }
                                        .padding(vertical = 2.dp)
                                        .testTag("city_selector_button")
                                ) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = "City", tint = GtMintSubtext, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        if (isAllCities) "All Cities, Pakistan" else "$selectedCity, Pakistan",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Change city", tint = GtMintSubtext, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                onClick = onOpenWallet,
                                color = GharTakEmerald,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.testTag("home_wallet_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.AccountBalanceWallet,
                                        contentDescription = "GT Service Wallet",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        GharTakConstants.formatPkr(walletBalance),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                            Surface(
                                onClick = onOpenAdminSeedShortcut,
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.testTag("home_admin_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.AdminPanelSettings,
                                        contentDescription = "Admin Login",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        "Admin",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            IconButton(
                                onClick = onOpenSupportChat,
                                modifier = Modifier.testTag("home_chat_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadChatCount > 0) {
                                            Badge { Text("$unreadChatCount") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Support Chat", tint = Color.White)
                                }
                            }
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier.testTag("notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge { Text("$unreadCount") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenSearch)
                            .testTag("home_search_bar"),
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = GtSlateNavyMuted)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Search biryani, pizza, mangoes, stores...",
                                color = GtSlateNavyMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Promo Banners (Admin-configurable Primary Banner + Restaurant Offers)
        val promoCards = buildList {
            if (settings.promoBannerEnabled) {
                val pCode = settings.promoCode.trim().uppercase().ifBlank { "GHARTAK20" }
                val pBadge = settings.promoBadgeText.ifBlank { "USE CODE: $pCode" }
                val pTitle = settings.promoTitle.ifBlank {
                    if (settings.promoRestaurantName.isNotBlank()) {
                        "${settings.promoDiscountPercent}% OFF at ${settings.promoRestaurantName}"
                    } else {
                        "${settings.promoDiscountPercent}% OFF Food & Groceries"
                    }
                }
                val pSub = settings.promoSubtitle.ifBlank { "Your Favourite Food, Delivered to Your Door!" }
                add(
                    HomePromoBannerModel(
                        id = "primary",
                        code = pCode,
                        badge = pBadge,
                        title = pTitle,
                        subtitle = pSub,
                        restaurantId = settings.promoRestaurantId,
                        restaurantName = settings.promoRestaurantName
                    )
                )
            }
            settings.promoOffers.forEach { offer ->
                val oCode = offer.code.trim().uppercase().ifBlank { "PROMO" }
                val oBadge = offer.badgeText.ifBlank { "USE CODE: $oCode" }
                val oTitle = offer.title.ifBlank {
                    if (offer.restaurantName.isNotBlank()) "${offer.discountPercent}% OFF at ${offer.restaurantName}" else "${offer.discountPercent}% OFF"
                }
                val oSub = offer.subtitle.ifBlank { "Tap to copy code $oCode & order!" }
                add(
                    HomePromoBannerModel(
                        id = offer.id.ifBlank { oCode },
                        code = oCode,
                        badge = oBadge,
                        title = oTitle,
                        subtitle = oSub,
                        restaurantId = offer.restaurantId,
                        restaurantName = offer.restaurantName
                    )
                )
            }
        }
        if (promoCards.isNotEmpty()) {
            item {
                if (promoCards.size == 1) {
                    val card = promoCards.first()
                    HomePromoBannerCard(
                        banner = card,
                        onClick = {
                            clipboard.setText(AnnotatedString(card.code))
                            Toast.makeText(context, "Copied promo code: ${card.code}", Toast.LENGTH_SHORT).show()
                            if (card.restaurantId.isNotBlank()) {
                                onOpenRestaurant(card.restaurantId)
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(152.dp)
                            .testTag("home_promo_banner")
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(promoCards, key = { it.id }) { card ->
                            HomePromoBannerCard(
                                banner = card,
                                onClick = {
                                    clipboard.setText(AnnotatedString(card.code))
                                    Toast.makeText(context, "Copied promo code: ${card.code}", Toast.LENGTH_SHORT).show()
                                    if (card.restaurantId.isNotBlank()) {
                                        onOpenRestaurant(card.restaurantId)
                                    }
                                },
                                modifier = Modifier
                                    .width(316.dp)
                                    .height(152.dp)
                                    .testTag(if (card.id == "primary") "home_promo_banner" else "home_promo_offer_${card.id}")
                            )
                        }
                    }
                }
            }
        }

        // Two Clearly Separated Experiences: Food Delivery vs Grocery Delivery + Medical & Rider Hire
        item {
            SectionHeader(
                title = "Choose Your Experience",
                subtitle = "Separate fast-track Food, Grocery & Medical carts"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VerticalSplitCard(
                    title = "Food Delivery",
                    subtitle = "${restaurants.size} Restaurants",
                    imageRes = "res://img_hero_food",
                    accentColor = GharTakPink,
                    onClick = onOpenFoodHub,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_food_card")
                )
                VerticalSplitCard(
                    title = "Grocery Mart",
                    subtitle = "${groceryStores.size} Stores",
                    imageRes = "res://img_hero_grocery",
                    accentColor = GharTakEmerald,
                    onClick = onOpenGroceryHub,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_grocery_card")
                )
            }
            Spacer(Modifier.height(10.dp))
            VerticalSplitCard(
                title = "Medical & Pharmacy",
                subtitle = "${medicalStores.size} Pharmacies • 24/7 Care",
                imageRes = "res://img_hero_medical",
                accentColor = GharTakMedicalTeal,
                onClick = onOpenMedicalHub,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_medical_card")
            )
            Spacer(Modifier.height(10.dp))
            val activeMultanCity = if (selectedCity.equals(GharTakConstants.ALL_CITIES, ignoreCase = true)) {
                GharTakConstants.DEFAULT_CITY
            } else selectedCity
            val multanFee = viewModel.resolveMultanCityDeliveryFee(activeMultanCity, settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VerticalSplitCard(
                    title = "Multan Express",
                    subtitle = "Crumble, KFC, Halwa • ${GharTakConstants.formatPkr(multanFee)} Fee",
                    imageRes = "res://img_banner_deals",
                    accentColor = GtNavyHeader,
                    onClick = onOpenMultanHub,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_multan_card")
                )
                VerticalSplitCard(
                    title = "Celebration",
                    subtitle = "Cakes, Flowers & Gifts",
                    imageRes = "res://img_food_desserts",
                    accentColor = GharTakPink,
                    onClick = onOpenCelebrationHub,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_celebration_card")
                )
            }
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_parcel_rider_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GtLightGreyCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Hire a Rider or Send Parcel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Request a dedicated GT Service rider or courier parcel. Super Admin approves before rider dispatch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { parcelRiderDialogType = "hire_rider" },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("customer_hire_rider_button")
                        ) {
                            Icon(Icons.Filled.DeliveryDining, contentDescription = "Hire a Rider", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Hire a Rider", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { parcelRiderDialogType = "send_parcel" },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("customer_send_parcel_button")
                        ) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = "Send Parcel", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Send Parcel", maxLines = 1)
                        }
                    }
                    if (myParcelRiderRequests.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            "Your Rider & Parcel Requests (${myParcelRiderRequests.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        myParcelRiderRequests.take(3).forEach { req ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${if (req.requestType == "send_parcel") "Send Parcel" else "Hire Rider"} • ${req.city} • ${GharTakConstants.formatPkr(req.offeredFeePkr)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val meta = if (req.riderName.isNotBlank()) "Rider: ${req.riderName}" else "${req.pickupAddress} -> ${req.dropoffAddress}"
                                    Text(
                                        meta,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                StatusBadge(req.approvalStatus.replace('_', ' '))
                            }
                        }
                    }
                }
            }
        }

        // Empty catalog prompt for Admin on first launch
        if (restaurantsState is UiState.Success && restaurants.isEmpty() && groceryStores.isEmpty() && medicalStores.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Live Catalog Empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "No restaurants or grocery stores are published in Firestore yet. Tap below to open Admin Login and add products with images.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = onOpenAdminSeedShortcut) {
                            Text("Open Admin Login")
                        }
                    }
                }
            }
        }

        // Food Categories Strip
        item {
            val catNames = foodCategories.map { it.name }.ifEmpty { GharTakConstants.FOOD_CATEGORIES }
            SectionHeader("Food Categories", subtitle = "Pizza, Burger, Biryani, BBQ, Chinese & more", actionLabel = "See All", onActionClick = onOpenFoodHub)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(catNames) { name ->
                    Card(
                        modifier = Modifier
                            .width(108.dp)
                            .clickable(onClick = onOpenFoodHub),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = foodCategoryDrawableRes(name)),
                                contentDescription = name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GharTakPink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Helpline & Live Support Chat Card (Admin-configurable number)
        item {
            SupportHelplineCard(
                onOpenLiveChat = onOpenSupportChat,
                unreadCount = unreadChatCount,
                supportPhoneLocal = settings.supportPhoneLocal,
                supportWhatsappNumber = settings.supportWhatsappNumber,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Popular & Nearby Restaurants
        item {
            SectionHeader(
                if (isAllCities) "Popular Restaurants (All Cities)" else "Popular Restaurants in $selectedCity",
                actionLabel = "Browse Food",
                onActionClick = onOpenFoodHub
            )
        }
        if (restaurantsState is UiState.Loading) {
            item { Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        } else if (cityRestaurants.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cityRestaurants, key = { it.id }) { rest ->
                        RestaurantCard(
                            rest = rest,
                            onClick = { onOpenRestaurant(rest.id) },
                            promoBadge = settings.activePromoBadgeForRestaurant(rest.id),
                            modifier = Modifier.width(268.dp)
                        )
                    }
                }
            }
        }

        // Latest, Popular & Discounted Food Items (newest first so newly added items sync first across devices)
        val featuredFood = foodItems.filter { it.isAvailable && it.stockQuantity > 0L }
            .take(25)
        if (featuredFood.isNotEmpty()) {
            item {
                SectionHeader("Popular Dishes & Offers", subtitle = "Freshly prepared • Tap to view or Add to Cart", actionLabel = "All Dishes", onActionClick = onOpenFoodHub)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredFood, key = { it.id }) { item ->
                        val rest = restaurants.firstOrNull { it.id == item.restaurantId }
                        FoodMiniCard(
                            item = item,
                            onAddClick = { customizingFood = item },
                            onOrderNowClick = { viewModel.buyNowFoodItem(item, rest, onReadyForCheckout = onNavigateToCheckout) },
                            onCardClick = { customizingFood = item }
                        )
                    }
                }
            }
        }

        // Featured Grocery Stores
        item {
            SectionHeader("Featured Grocery Stores", actionLabel = "Browse Mart", onActionClick = onOpenGroceryHub)
        }
        if (cityStores.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cityStores, key = { it.id }) { store ->
                        GroceryStoreCard(store = store, onClick = { onOpenGroceryStore(store.id) }, modifier = Modifier.width(268.dp))
                    }
                }
            }
        }

        // Grocery Categories & Best-Selling Products
        item {
            val gCats = groceryCategories.map { it.name }.ifEmpty { GharTakConstants.GROCERY_CATEGORIES }
            SectionHeader("Grocery Aisles", subtitle = "Fresh produce, dairy, meat, pantry & home care", actionLabel = "All 18 Aisles", onActionClick = onOpenGroceryHub)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(gCats) { name ->
                    Card(
                        modifier = Modifier
                            .width(108.dp)
                            .clickable(onClick = onOpenGroceryHub),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = groceryCategoryDrawableRes(name)),
                                contentDescription = name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GharTakEmerald,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        val bestGrocery = groceryProducts.filter { it.isAvailable && it.stockQuantity > 0L }
            .take(25)
        if (bestGrocery.isNotEmpty()) {
            item {
                SectionHeader("Best-Selling Grocery Essentials", subtitle = "Fresh produce, dairy, meat & pantry staples", actionLabel = "Browse Mart", onActionClick = onOpenGroceryHub)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bestGrocery, key = { it.id }) { prod ->
                        val store = groceryStores.firstOrNull { it.id == prod.storeId }
                        GroceryMiniCard(
                            product = prod,
                            onAddClick = { viewModel.addGroceryToCart(prod, store) },
                            onBuyNowClick = { viewModel.buyNowGroceryProduct(prod, store, onNavigateToCheckout) },
                            onCardClick = { inspectingGrocery = prod }
                        )
                    }
                }
            }
        }

        // Medical & Pharmacy Stores
        item {
            SectionHeader("Medical Stores & Pharmacies", actionLabel = "Browse Pharmacy", onActionClick = onOpenMedicalHub)
        }
        if (cityMedicalStores.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cityMedicalStores, key = { it.id }) { store ->
                        MedicalStoreCard(store = store, onClick = { onOpenMedicalStore(store.id) }, modifier = Modifier.width(268.dp))
                    }
                }
            }
        }

        // Medical Categories
        item {
            val mCats = medicalCategories.map { it.name }.ifEmpty { GharTakConstants.MEDICAL_CATEGORIES }
            SectionHeader("Medical & Pharmacy Aisles", subtitle = "Medicines, First Aid, Vitamins & Baby Care", actionLabel = "All Care", onActionClick = onOpenMedicalHub)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mCats) { name ->
                    Card(
                        modifier = Modifier
                            .width(108.dp)
                            .clickable(onClick = onOpenMedicalHub),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = medicalCategoryDrawableRes(name)),
                                contentDescription = name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GharTakMedicalTeal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        val bestMedical = medicalProducts.filter { it.isAvailable && it.stockQuantity > 0L }.take(25)
        if (bestMedical.isNotEmpty()) {
            item {
                SectionHeader("Medicines & Healthcare", subtitle = "Fast pharmacy delivery • Tap to view or Buy Now", actionLabel = "Open Pharmacy", onActionClick = onOpenMedicalHub)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bestMedical, key = { it.id }) { prod ->
                        val store = medicalStores.firstOrNull { it.id == prod.storeId }
                        MedicalMiniCard(
                            product = prod,
                            onAddClick = { viewModel.addMedicalToCart(prod, store) },
                            onBuyNowClick = { viewModel.buyNowMedicalProduct(prod, store, onNavigateToCheckout) },
                            onCardClick = { inspectingMedical = prod }
                        )
                    }
                }
            }
        }

        // Recently Ordered Section
        if (orders.isNotEmpty()) {
            item {
                SectionHeader("Order Again", subtitle = "Your recent GT Service orders")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders.take(5), key = { it.id }) { order ->
                        Card(
                            modifier = Modifier.width(240.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(order.vendorName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    StatusBadge(order.status)
                                }
                                Text(
                                    "${order.items.size} items • ${GharTakConstants.formatPkr(order.totalPkr)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { viewModel.reorder(order) }, contentPadding = PaddingValues(0.dp)) {
                                    Text("Reorder Now")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val allCustomerCities = remember(settings.cities, settings.multanCityDeliveryFees) {
        (settings.cities + settings.multanCityDeliveryFees.map { it.city })
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .ifEmpty { GharTakConstants.INITIAL_PAKISTAN_CITIES }
    }

    if (showCityPicker) {
        CityPickerDialog(
            cities = allCustomerCities,
            selectedCity = selectedCity,
            includeAllCitiesOption = true,
            pendingCityRequests = myCityRequests,
            onRequestNewCity = { requestedCity -> viewModel.submitCityApprovalRequest(requestedCity) },
            onSelectCity = { viewModel.setCity(it) },
            onDismiss = { showCityPicker = false }
        )
    }

    parcelRiderDialogType?.let { reqType ->
        val approvedCities = allCustomerCities
        val defaultReqCity = selectedCity.takeIf { c ->
            !c.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) &&
                approvedCities.any { it.equals(c, ignoreCase = true) }
        } ?: approvedCities.firstOrNull() ?: GharTakConstants.DEFAULT_CITY
        CustomerParcelRiderDialog(
            requestType = reqType,
            approvedCities = approvedCities,
            defaultCity = defaultReqCity,
            defaultName = userProfile?.fullName.orEmpty(),
            defaultPhone = userProfile?.phone.orEmpty(),
            onDismiss = { parcelRiderDialogType = null },
            onSubmit = { pickup, dropoff, city, contactName, contactPhone, details, feePkr ->
                viewModel.submitCustomerParcelRiderRequest(
                    requestType = reqType,
                    pickupAddress = pickup,
                    dropoffAddress = dropoff,
                    city = city,
                    recipientName = contactName,
                    recipientPhone = contactPhone,
                    details = details,
                    offeredFeePkr = feePkr,
                    onSuccess = { parcelRiderDialogType = null }
                )
            }
        )
    }

    customizingFood?.let { food ->
        val rest = restaurants.firstOrNull { it.id == food.restaurantId }
        FoodCustomizationSheet(
            item = food,
            onDismiss = { customizingFood = null },
            onOpenRestaurant = {
                customizingFood = null
                onOpenRestaurant(food.restaurantId)
            },
            onAddToCart = { note, qty, extraPrice ->
                viewModel.addFoodToCart(food, rest, note, qty, extraPrice)
                customizingFood = null
            },
            onOrderNow = { note, qty, extraPrice ->
                customizingFood = null
                viewModel.buyNowFoodItem(food, rest, note, qty, extraPrice, onNavigateToCheckout)
            }
        )
    }

    inspectingGrocery?.let { prod ->
        val store = groceryStores.firstOrNull { it.id == prod.storeId }
        GroceryProductDetailDialog(
            product = prod,
            onDismiss = { inspectingGrocery = null },
            onOpenStore = {
                inspectingGrocery = null
                onOpenGroceryStore(prod.storeId)
            },
            onAddToCart = {
                viewModel.addGroceryToCart(prod, store)
                inspectingGrocery = null
            },
            onBuyNow = {
                inspectingGrocery = null
                viewModel.buyNowGroceryProduct(prod, store, onNavigateToCheckout)
            }
        )
    }

    inspectingMedical?.let { prod ->
        val store = medicalStores.firstOrNull { it.id == prod.storeId }
        MedicalProductDetailDialog(
            product = prod,
            onDismiss = { inspectingMedical = null },
            onOpenStore = {
                inspectingMedical = null
                onOpenMedicalStore(prod.storeId)
            },
            onAddToCart = {
                viewModel.addMedicalToCart(prod, store)
                inspectingMedical = null
            },
            onBuyNow = {
                inspectingMedical = null
                viewModel.buyNowMedicalProduct(prod, store, onNavigateToCheckout)
            }
        )
    }
}

private fun PlatformSettings.activePromoBadgeForRestaurant(restaurantId: String): String? {
    val specific = promoOffers.firstOrNull { it.restaurantId == restaurantId }
    if (specific != null) {
        val code = specific.code.trim().uppercase().ifBlank { "PROMO" }
        return "${specific.discountPercent}% OFF • $code"
    }
    if (promoBannerEnabled && promoRestaurantId == restaurantId) {
        val code = promoCode.trim().uppercase().ifBlank { "GHARTAK20" }
        return "$promoDiscountPercent% OFF • $code"
    }
    return null
}

private fun PlatformSettings.promoBannersList(filterRestaurantId: String? = null): List<HomePromoBannerModel> = buildList {
    if (promoBannerEnabled) {
        val matchesPrimary = filterRestaurantId == null ||
            promoRestaurantId.isBlank() ||
            promoRestaurantId == filterRestaurantId
        if (matchesPrimary) {
            val pCode = promoCode.trim().uppercase().ifBlank { "GHARTAK20" }
            val pBadge = promoBadgeText.ifBlank { "USE CODE: $pCode" }
            val pTitle = promoTitle.ifBlank {
                if (promoRestaurantName.isNotBlank()) {
                    "$promoDiscountPercent% OFF at $promoRestaurantName"
                } else {
                    "$promoDiscountPercent% OFF Food & Groceries"
                }
            }
            val pSub = promoSubtitle.ifBlank { "Your Favourite Food, Delivered to Your Door!" }
            add(
                HomePromoBannerModel(
                    id = "primary",
                    code = pCode,
                    badge = pBadge,
                    title = pTitle,
                    subtitle = pSub,
                    restaurantId = promoRestaurantId,
                    restaurantName = promoRestaurantName
                )
            )
        }
    }
    promoOffers.forEach { offer ->
        val matchesOffer = filterRestaurantId == null ||
            offer.restaurantId.isBlank() ||
            offer.restaurantId == filterRestaurantId
        if (matchesOffer) {
            val oCode = offer.code.trim().uppercase().ifBlank { "PROMO" }
            val oBadge = offer.badgeText.ifBlank { "USE CODE: $oCode" }
            val oTitle = offer.title.ifBlank {
                if (offer.restaurantName.isNotBlank()) "${offer.discountPercent}% OFF at ${offer.restaurantName}" else "${offer.discountPercent}% OFF"
            }
            val oSub = offer.subtitle.ifBlank { "Tap to copy code $oCode & order!" }
            add(
                HomePromoBannerModel(
                    id = offer.id.ifBlank { oCode },
                    code = oCode,
                    badge = oBadge,
                    title = oTitle,
                    subtitle = oSub,
                    restaurantId = offer.restaurantId,
                    restaurantName = offer.restaurantName
                )
            )
        }
    }
}

private data class HomePromoBannerModel(
    val id: String,
    val code: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val restaurantId: String = "",
    val restaurantName: String = ""
)

@Composable
private fun HomePromoBannerCard(
    banner: HomePromoBannerModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        GharTakImage(
            imageUrl = "res://img_banner_deals",
            contentDescription = banner.title,
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
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = GharTakEmerald, shape = RoundedCornerShape(999.dp)) {
                        Text(
                            banner.badge,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (banner.restaurantName.isNotBlank()) {
                        Surface(color = GtNavyHeader, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                "${banner.restaurantName} • Order",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Column {
                    Text(
                        banner.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        banner.subtitle,
                        color = GtMintSubtext,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerParcelRiderDialog(
    requestType: String,
    approvedCities: List<String>,
    defaultCity: String,
    defaultName: String,
    defaultPhone: String,
    onDismiss: () -> Unit,
    onSubmit: (
        pickup: String,
        dropoff: String,
        city: String,
        contactName: String,
        contactPhone: String,
        details: String,
        feePkr: Double
    ) -> Unit
) {
    val isParcel = requestType == "send_parcel"
    var selectedReqCity by remember(defaultCity) { mutableStateOf(defaultCity) }
    var pickupAddress by remember { mutableStateOf("") }
    var dropoffAddress by remember { mutableStateOf("") }
    var contactName by remember(defaultName) { mutableStateOf(defaultName) }
    var contactPhone by remember(defaultPhone) { mutableStateOf(defaultPhone.ifBlank { "+92" }) }
    var details by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf(if (isParcel) "180" else "400") }

    val feeValue = feeText.toDoubleOrNull() ?: 0.0
    val isValid = pickupAddress.trim().length >= 3 &&
        (if (isParcel) dropoffAddress.trim().length >= 3 else true) &&
        feeValue >= 50.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isParcel) "Send Parcel via GT Rider" else "Hire a Dedicated Rider")
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Requests go to Super Admin for approval before riders can accept them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Text("Approved City", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(approvedCities) { c ->
                            FilterChip(
                                selected = selectedReqCity.equals(c, ignoreCase = true),
                                onClick = { selectedReqCity = c },
                                label = { Text(c) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = pickupAddress,
                        onValueChange = { pickupAddress = it },
                        label = { Text(if (isParcel) "Pickup Address *" else "Starting / Duty Location *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_parcel_pickup_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = dropoffAddress,
                        onValueChange = { dropoffAddress = it },
                        label = { Text(if (isParcel) "Dropoff Address *" else "Destination / Duty Area") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_parcel_dropoff_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text(if (isParcel) "Recipient Name" else "Your Contact Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Contact Mobile (+92...)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text(if (isParcel) "Parcel Weight / Item Details" else "Hours / Tasks / Errands") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { next -> feeText = next.filter { it.isDigit() } },
                        label = { Text("Offered Rider Fee (PKR, min 50) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_parcel_fee_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    onSubmit(
                        pickupAddress,
                        dropoffAddress,
                        selectedReqCity,
                        contactName,
                        contactPhone,
                        details,
                        feeValue
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("customer_parcel_rider_submit_button")
            ) {
                Text("Submit for Approval")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text("Cancel") }
        }
    )
}

@Composable
private fun VerticalSplitCard(
    title: String,
    subtitle: String,
    imageRes: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GharTakImage(imageUrl = imageRes, contentDescription = title, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, GharTakCharcoal.copy(alpha = 0.88f))))
                    .padding(14.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Surface(color = accentColor, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            subtitle,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    rest: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    promoBadge: String? = null
) {
    val displayImage = restaurantImageUri(rest.name, rest.imageUrl)
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(132.dp)) {
                GharTakImage(imageUrl = displayImage, contentDescription = rest.name, modifier = Modifier.fillMaxSize())
                if (!promoBadge.isNullOrBlank()) {
                    Surface(
                        color = GharTakEmerald,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                    ) {
                        Text(
                            text = promoBadge,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
                Surface(
                    color = GharTakCharcoal.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DeliveryDining, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${rest.deliveryTimeMinutes} min • ${GharTakConstants.formatPkr(rest.deliveryFeePkr)}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(rest.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = GharTakAmber, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(String.format("%.1f", rest.rating), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text("${rest.address} (${rest.city})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                OperatingHoursBadge(
                    isOpen = rest.isOpen,
                    openingTime = rest.openingTime,
                    closingTime = rest.closingTime
                )
            }
        }
    }
}

@Composable
fun GroceryStoreCard(store: GroceryStore, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val displayImage = groceryStoreImageUri(store.name, store.imageUrl)
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(132.dp)) {
                GharTakImage(imageUrl = displayImage, contentDescription = store.name, modifier = Modifier.fillMaxSize(), isGroceryFallback = true)
                Surface(
                    color = GharTakEmerald.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                ) {
                    Text("${store.deliveryTimeMinutes} min • ${GharTakConstants.formatPkr(store.deliveryFeePkr)}", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(store.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = GharTakAmber, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(String.format("%.1f", store.rating), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text("${store.address} (${store.city})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun FoodMiniCard(
    item: FoodItem,
    onAddClick: () -> Unit,
    onOrderNowClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val canOrder = item.isAvailable && item.stockQuantity > 0L
    val displayImage = item.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_food" }
        ?: foodCategoryImageUri(item.categoryName, item.name)
    Card(
        modifier = Modifier.width(214.dp).clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(126.dp)) {
                GharTakImage(imageUrl = displayImage, contentDescription = item.name, modifier = Modifier.fillMaxSize())
                if (item.discountPercent > 0) {
                    DiscountBadge(item.discountPercent, Modifier.align(Alignment.TopStart).padding(8.dp))
                }
                Surface(
                    color = GharTakCharcoal.copy(alpha = 0.80f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                ) {
                    Text(
                        "~${item.preparationTimeMinutes} min",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.categoryName, style = MaterialTheme.typography.labelSmall, color = GharTakPink, fontWeight = FontWeight.SemiBold)
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.restaurantName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FoodDayBadges(availableDays = item.availableDays, isDailySpecial = item.isDailySpecial)
                PriceRow(item.regularPricePkr, item.discountPricePkr)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAddClick,
                        enabled = canOrder,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(0.95f).height(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add ${item.name}", modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Add", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    Button(
                        onClick = onOrderNowClick,
                        enabled = canOrder,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(36.dp)
                            .testTag("order_now_food_${item.id}")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = "Order ${item.name} now", modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(if (canOrder) "Order Now" else "Sold", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun GroceryMiniCard(
    product: GroceryProduct,
    onAddClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val canOrder = product.isAvailable && product.stockQuantity > 0L
    val displayImage = product.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_grocery" }
        ?: groceryCategoryImageUri(product.categoryName, product.name)
    Card(
        modifier = Modifier.width(214.dp).clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(126.dp)) {
                GharTakImage(imageUrl = displayImage, contentDescription = product.name, modifier = Modifier.fillMaxSize(), isGroceryFallback = true)
                if (product.discountPercent > 0) {
                    DiscountBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(8.dp))
                }
                Surface(
                    color = GharTakEmerald.copy(alpha = 0.90f),
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
                Text(product.categoryName, style = MaterialTheme.typography.labelSmall, color = GharTakEmerald, fontWeight = FontWeight.SemiBold)
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${product.storeName} • Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                        colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(36.dp)
                            .testTag("buy_now_grocery_${product.id}")
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

@Composable
fun FoodHubScreen(
    viewModel: GharTakViewModel,
    onOpenRestaurant: (String) -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val categories by viewModel.foodCategories.collectAsStateWithLifecycle()
    val restaurantsState by viewModel.restaurantsState.collectAsStateWithLifecycle()
    val foodItemsState by viewModel.foodItemsState.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDayFilter by remember { mutableStateOf("Today") }
    var customizingFood by remember { mutableStateOf<FoodItem?>(null) }

    val restaurants = (restaurantsState as? UiState.Success)?.data.orEmpty()
    val items = (foodItemsState as? UiState.Success)?.data.orEmpty().filter { it.isAvailable }
    val catNames = listOf("All") + categories.map { it.name }.ifEmpty { GharTakConstants.FOOD_CATEGORIES }
    val dayFilters = listOf("Today", "All Days") + GharTakConstants.DAYS_OF_WEEK
    val todayKey = remember { GharTakConstants.todayDayKey() }
    val filteredItems = items
        .filter { selectedCategory == "All" || it.categoryName.equals(selectedCategory, ignoreCase = true) }
        .filter { item ->
            when (selectedDayFilter) {
                "All Days" -> true
                "Today" -> item.isAvailableOnDay(todayKey)
                else -> item.isAvailableOnDay(selectedDayFilter)
            }
        }
        .let { list ->
            if (selectedDayFilter == "Today") list.sortedByDescending { it.isDailySpecial } else list
        }
    val foodPromoBanners = settings.promoBannersList()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = GtNavyHeader, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    "Food Delivery",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Browse Pakistani favourites, BBQ, Biryani, Pizza & more",
                    style = MaterialTheme.typography.bodySmall,
                    color = GtMintSubtext
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        if (foodPromoBanners.isNotEmpty()) {
            item {
                if (foodPromoBanners.size == 1) {
                    val card = foodPromoBanners.first()
                    HomePromoBannerCard(
                        banner = card,
                        onClick = {
                            clipboard.setText(AnnotatedString(card.code))
                            Toast.makeText(context, "Copied promo code: ${card.code}", Toast.LENGTH_SHORT).show()
                            if (card.restaurantId.isNotBlank()) {
                                onOpenRestaurant(card.restaurantId)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(142.dp)
                            .testTag("food_hub_promo_banner")
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(foodPromoBanners, key = { "food_${it.id}" }) { card ->
                            HomePromoBannerCard(
                                banner = card,
                                onClick = {
                                    clipboard.setText(AnnotatedString(card.code))
                                    Toast.makeText(context, "Copied promo code: ${card.code}", Toast.LENGTH_SHORT).show()
                                    if (card.restaurantId.isNotBlank()) {
                                        onOpenRestaurant(card.restaurantId)
                                    }
                                },
                                modifier = Modifier
                                    .width(304.dp)
                                    .height(142.dp)
                            )
                        }
                    }
                }
            }
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
                                    painter = painterResource(id = foodCategoryDrawableRes(cat)),
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
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dayFilters) { day ->
                    val label = if (day == "Today") "Today ($todayKey)" else day
                    FilterChip(
                        selected = selectedDayFilter == day,
                        onClick = { selectedDayFilter = day },
                        label = { Text(label) },
                        modifier = Modifier.testTag("food_day_chip_$day")
                    )
                }
            }
        }
        item {
            Text("Restaurants (${restaurants.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (restaurants.isEmpty()) {
            item { EmptyStateView("No restaurants approved yet", "Check back soon or ask Admin to approve restaurants.") }
        } else {
            items(restaurants, key = { it.id }) { rest ->
                RestaurantCard(
                    rest = rest,
                    onClick = { onOpenRestaurant(rest.id) },
                    promoBadge = settings.activePromoBadgeForRestaurant(rest.id),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("Dishes in $selectedCategory (${filteredItems.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (filteredItems.isEmpty()) {
            item { EmptyStateView("No dishes in $selectedCategory yet", "New dishes added by Admin or restaurants appear here automatically.") }
        } else {
            items(filteredItems, key = { it.id }) { food ->
                val rest = restaurants.firstOrNull { it.id == food.restaurantId }
                FoodProductRowCard(
                    food = food,
                    onCardClick = { customizingFood = food },
                    onAddClick = { customizingFood = food },
                    onOrderNowClick = { viewModel.buyNowFoodItem(food, rest, onReadyForCheckout = onNavigateToCheckout) }
                )
            }
        }
    }
    }

    customizingFood?.let { food ->
        val rest = restaurants.firstOrNull { it.id == food.restaurantId }
        FoodCustomizationSheet(
            item = food,
            onDismiss = { customizingFood = null },
            onOpenRestaurant = {
                customizingFood = null
                onOpenRestaurant(food.restaurantId)
            },
            onAddToCart = { note, qty, extraPrice ->
                viewModel.addFoodToCart(food, rest, note, qty, extraPrice)
                customizingFood = null
            },
            onOrderNow = { note, qty, extraPrice ->
                customizingFood = null
                viewModel.buyNowFoodItem(food, rest, note, qty, extraPrice, onNavigateToCheckout)
            }
        )
    }
}

@Composable
fun GroceryHubScreen(
    viewModel: GharTakViewModel,
    onOpenGroceryStore: (String) -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val categories by viewModel.groceryCategories.collectAsStateWithLifecycle()
    val storesState by viewModel.groceryStoresState.collectAsStateWithLifecycle()
    val productsState by viewModel.groceryProductsState.collectAsStateWithLifecycle()
    val groceryCart by viewModel.groceryCart.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("All") }
    var inspectingProduct by remember { mutableStateOf<GroceryProduct?>(null) }

    val stores = (storesState as? UiState.Success)?.data.orEmpty()
    val products = (productsState as? UiState.Success)?.data.orEmpty().filter { it.isAvailable }
    val catNames = listOf("All") + categories.map { it.name }.ifEmpty { GharTakConstants.GROCERY_CATEGORIES }
    val filteredProducts = if (selectedCategory == "All") products else products.filter { it.categoryName.equals(selectedCategory, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = GtNavyHeader, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    "Grocery Delivery",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Fresh fruits, dairy, meat, rice, oil & 18 daily aisles",
                    style = MaterialTheme.typography.bodySmall,
                    color = GtMintSubtext
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(catNames) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        leadingIcon = if (cat != "All") {
                            {
                                Image(
                                    painter = painterResource(id = groceryCategoryDrawableRes(cat)),
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
            Text("Grocery Stores (${stores.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (stores.isEmpty()) {
            item { EmptyStateView("No grocery stores open yet", "Approved grocery stores will appear here.") }
        } else {
            items(stores, key = { it.id }) { store ->
                GroceryStoreCard(store = store, onClick = { onOpenGroceryStore(store.id) }, modifier = Modifier.fillMaxWidth())
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("Products in $selectedCategory (${filteredProducts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (filteredProducts.isEmpty()) {
            item { EmptyStateView("No products in $selectedCategory yet", "New grocery products added by Admin or stores appear here automatically.") }
        } else {
            items(filteredProducts, key = { it.id }) { product ->
                val store = stores.firstOrNull { it.id == product.storeId }
                val inCartQty = groceryCart.firstOrNull { it.productId == product.id }?.quantity ?: 0
                GroceryProductRowCard(
                    product = product,
                    inCartQty = inCartQty,
                    onCardClick = { inspectingProduct = product },
                    onAdd = { viewModel.addGroceryToCart(product, store) },
                    onBuyNow = { viewModel.buyNowGroceryProduct(product, store, onNavigateToCheckout) },
                    onDelta = { delta -> viewModel.updateCartQuantity("grocery", product.id, delta) }
                )
            }
        }
    }
    }

    inspectingProduct?.let { prod ->
        val store = stores.firstOrNull { it.id == prod.storeId }
        GroceryProductDetailDialog(
            product = prod,
            onDismiss = { inspectingProduct = null },
            onOpenStore = {
                inspectingProduct = null
                onOpenGroceryStore(prod.storeId)
            },
            onAddToCart = {
                viewModel.addGroceryToCart(prod, store)
                inspectingProduct = null
            },
            onBuyNow = {
                inspectingProduct = null
                viewModel.buyNowGroceryProduct(prod, store, onNavigateToCheckout)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    viewModel: GharTakViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val restaurantsState by viewModel.restaurantsState.collectAsStateWithLifecycle()
    val foodItemsState by viewModel.foodItemsState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val reviews by viewModel.visibleReviews.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()

    val restaurant = (restaurantsState as? UiState.Success)?.data?.firstOrNull { it.id == restaurantId }
    val fullMenu = (foodItemsState as? UiState.Success)?.data?.filter { it.restaurantId == restaurantId }.orEmpty()
    val restReviews = reviews.filter { it.targetId == restaurantId }
    val isFav = favorites.any { it.targetId == restaurantId && it.targetType == "restaurant" }
    val restPromos = remember(settings, restaurantId) {
        settings.promoBannersList(filterRestaurantId = restaurantId)
    }
    var selectedDayFilter by remember { mutableStateOf("Today") }
    val todayKey = remember { GharTakConstants.todayDayKey() }
    val dayFilters = listOf("Today", "All Days") + GharTakConstants.DAYS_OF_WEEK
    val menu = remember(fullMenu, selectedDayFilter, todayKey) {
        fullMenu
            .filter { item ->
                when (selectedDayFilter) {
                    "All Days" -> true
                    "Today" -> item.isAvailableOnDay(todayKey)
                    else -> item.isAvailableOnDay(selectedDayFilter)
                }
            }
            .let { list ->
                if (selectedDayFilter == "Today") list.sortedByDescending { it.isDailySpecial } else list
            }
    }
    var customizingFood by remember { mutableStateOf<FoodItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurant?.name ?: "Restaurant Menu", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = gtNavyTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (restaurant != null) {
                        IconButton(onClick = {
                            viewModel.toggleFavorite(restaurant.id, "restaurant", restaurant.name, restaurant.address, restaurant.imageUrl)
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
        if (restaurant == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GharTakImage(
                        imageUrl = restaurantImageUri(restaurant.name, restaurant.imageUrl),
                        contentDescription = restaurant.name,
                        modifier = Modifier.fillMaxWidth().height(185.dp).clip(RoundedCornerShape(20.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(restaurant.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${restaurant.address}, ${restaurant.city}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge("${restaurant.rating} ★ (${restaurant.reviewCount})")
                        Text("Min: ${GharTakConstants.formatPkr(restaurant.minOrderPkr)}", style = MaterialTheme.typography.labelMedium)
                        Text("Delivery: ${GharTakConstants.formatPkr(restaurant.deliveryFeePkr)}", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    OperatingHoursBadge(
                        isOpen = restaurant.isOpen,
                        openingTime = restaurant.openingTime,
                        closingTime = restaurant.closingTime
                    )
                    if (!restaurant.isCurrentlyOpenNow()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = GharTakCoralError.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "${restaurant.name} is currently closed (${restaurant.operatingHoursLabel()}). Ordering is paused until opening.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GharTakCoralError,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    if (!restaurant.description.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(restaurant.description, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (restPromos.isNotEmpty()) {
                    items(restPromos, key = { "rest_promo_${it.id}" }) { banner ->
                        HomePromoBannerCard(
                            banner = banner,
                            onClick = {
                                clipboard.setText(AnnotatedString(banner.code))
                                Toast.makeText(context, "Copied code ${banner.code}! Apply at checkout.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(138.dp)
                                .testTag("restaurant_promo_banner_${banner.id}")
                        )
                    }
                }

                item {
                    HorizontalDivider()
                    Spacer(Modifier.height(6.dp))
                    Text("Food Menu (${menu.size})", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(dayFilters) { day ->
                            val label = if (day == "Today") "Today ($todayKey)" else day
                            FilterChip(
                                selected = selectedDayFilter == day,
                                onClick = { selectedDayFilter = day },
                                label = { Text(label) },
                                modifier = Modifier.testTag("rest_day_chip_$day")
                            )
                        }
                    }
                }

                if (menu.isEmpty()) {
                    item { EmptyStateView("Menu is empty", "No dishes available at this restaurant yet.") }
                } else {
                    items(menu, key = { it.id }) { food ->
                        FoodProductRowCard(
                            food = food,
                            onCardClick = { customizingFood = food },
                            onAddClick = { customizingFood = food },
                            onOrderNowClick = { viewModel.buyNowFoodItem(food, restaurant, onReadyForCheckout = onNavigateToCheckout) }
                        )
                    }
                }

                item {
                    HorizontalDivider()
                    Spacer(Modifier.height(6.dp))
                    Text("Customer Reviews (${restReviews.size})", style = MaterialTheme.typography.titleLarge)
                }
                if (restReviews.isEmpty()) {
                    item { Text("No reviews yet. Order and share your feedback!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(restReviews, key = { it.id }) { rev ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(rev.userName, style = MaterialTheme.typography.titleSmall)
                                    Text("${rev.rating.toInt()} ★", color = GharTakAmber, fontWeight = FontWeight.Bold)
                                }
                                Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    customizingFood?.let { food ->
        FoodCustomizationSheet(
            item = food,
            onDismiss = { customizingFood = null },
            onAddToCart = { note, qty, extraPrice ->
                viewModel.addFoodToCart(food, restaurant, note, qty, extraPrice)
                customizingFood = null
            },
            onOrderNow = { note, qty, extraPrice ->
                customizingFood = null
                viewModel.buyNowFoodItem(food, restaurant, note, qty, extraPrice, onNavigateToCheckout)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryStoreDetailScreen(
    storeId: String,
    viewModel: GharTakViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val storesState by viewModel.groceryStoresState.collectAsStateWithLifecycle()
    val productsState by viewModel.groceryProductsState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val groceryCart by viewModel.groceryCart.collectAsStateWithLifecycle()

    val store = (storesState as? UiState.Success)?.data?.firstOrNull { it.id == storeId }
    val products = (productsState as? UiState.Success)?.data?.filter { it.storeId == storeId }.orEmpty()
    val isFav = favorites.any { it.targetId == storeId && it.targetType == "groceryStore" }
    var inspectingProduct by remember { mutableStateOf<GroceryProduct?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(store?.name ?: "Grocery Store", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = gtNavyTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (store != null) {
                        IconButton(onClick = {
                            viewModel.toggleFavorite(store.id, "groceryStore", store.name, store.address, store.imageUrl)
                        }) {
                            Icon(if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Favorite", tint = if (isFav) GtMintSubtext else Color.White)
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
                        imageUrl = groceryStoreImageUri(store.name, store.imageUrl),
                        contentDescription = store.name,
                        modifier = Modifier.fillMaxWidth().height(175.dp).clip(RoundedCornerShape(20.dp)),
                        isGroceryFallback = true
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
                    Text("Available Products (${products.size})", style = MaterialTheme.typography.titleLarge)
                }
                if (products.isEmpty()) {
                    item { EmptyStateView("No products in store", "Products added for ${store.name} appear here automatically.") }
                } else {
                    items(products, key = { it.id }) { prod ->
                        val qty = groceryCart.firstOrNull { it.productId == prod.id }?.quantity ?: 0
                        GroceryProductRowCard(
                            product = prod,
                            inCartQty = qty,
                            onCardClick = { inspectingProduct = prod },
                            onAdd = { viewModel.addGroceryToCart(prod, store) },
                            onBuyNow = { viewModel.buyNowGroceryProduct(prod, store, onNavigateToCheckout) },
                            onDelta = { delta -> viewModel.updateCartQuantity("grocery", prod.id, delta) }
                        )
                    }
                }
            }
        }
    }

    inspectingProduct?.let { prod ->
        GroceryProductDetailDialog(
            product = prod,
            onDismiss = { inspectingProduct = null },
            onAddToCart = {
                viewModel.addGroceryToCart(prod, store)
                inspectingProduct = null
            },
            onBuyNow = {
                inspectingProduct = null
                viewModel.buyNowGroceryProduct(prod, store, onNavigateToCheckout)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodProductRowCard(
    food: FoodItem,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit,
    onOrderNowClick: () -> Unit
) {
    val available = food.isAvailable && food.stockQuantity > 0L
    val displayImage = food.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_food" }
        ?: foodCategoryImageUri(food.categoryName, food.name)
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
                GharTakImage(displayImage, food.name, modifier = Modifier.fillMaxSize())
                if (food.discountPercent > 0) {
                    DiscountBadge(food.discountPercent, Modifier.align(Alignment.TopStart).padding(6.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${food.categoryName} • ~${food.preparationTimeMinutes} min", style = MaterialTheme.typography.labelSmall, color = GharTakPink, fontWeight = FontWeight.SemiBold)
                Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(food.restaurantName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                FoodDayBadges(availableDays = food.availableDays, isDailySpecial = food.isDailySpecial)
                if (food.description.isNotBlank()) {
                    Text(food.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                PriceRow(food.regularPricePkr, food.discountPricePkr)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddClick,
                        enabled = available,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (available) "Add to Cart" else "Sold Out", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = onOrderNowClick,
                        enabled = available,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("order_now_food_${food.id}")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Order Now", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroceryProductRowCard(
    product: GroceryProduct,
    inCartQty: Int,
    onCardClick: () -> Unit,
    onAdd: () -> Unit,
    onBuyNow: () -> Unit,
    onDelta: (Int) -> Unit
) {
    val available = product.isAvailable && product.stockQuantity > 0L
    val displayImage = product.imageUrl?.takeIf { it.isNotBlank() && it != "res://img_hero_grocery" }
        ?: groceryCategoryImageUri(product.categoryName, product.name)
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
                GharTakImage(displayImage, product.name, modifier = Modifier.fillMaxSize(), isGroceryFallback = true)
                if (product.discountPercent > 0) {
                    DiscountBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(6.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${product.categoryName} • ${product.unit}", style = MaterialTheme.typography.labelSmall, color = GharTakEmerald, fontWeight = FontWeight.SemiBold)
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${product.storeName} • Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                            colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("buy_now_grocery_${product.id}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: GharTakViewModel,
    onBack: () -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onOpenGroceryStore: (String) -> Unit,
    onOpenMedicalStore: (String) -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val restaurantsState by viewModel.restaurantsState.collectAsStateWithLifecycle()
    val foodItemsState by viewModel.foodItemsState.collectAsStateWithLifecycle()
    val storesState by viewModel.groceryStoresState.collectAsStateWithLifecycle()
    val productsState by viewModel.groceryProductsState.collectAsStateWithLifecycle()
    val groceryCart by viewModel.groceryCart.collectAsStateWithLifecycle()
    val medStoresState by viewModel.medicalStoresState.collectAsStateWithLifecycle()
    val medProductsState by viewModel.medicalProductsState.collectAsStateWithLifecycle()
    val medicalCart by viewModel.medicalCart.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var scopeFilter by remember { mutableStateOf("All") } // All, Food, Grocery, Medical
    var maxPricePkr by remember { mutableDoubleStateOf(5000.0) }
    var onlyAvailable by remember { mutableStateOf(true) }
    var customizingFood by remember { mutableStateOf<FoodItem?>(null) }
    var inspectingGrocery by remember { mutableStateOf<GroceryProduct?>(null) }
    var inspectingMedical by remember { mutableStateOf<MedicalProduct?>(null) }

    val restaurants = (restaurantsState as? UiState.Success)?.data.orEmpty()
    val foodItems = (foodItemsState as? UiState.Success)?.data.orEmpty()
    val stores = (storesState as? UiState.Success)?.data.orEmpty()
    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val medStores = (medStoresState as? UiState.Success)?.data.orEmpty()
    val medProducts = (medProductsState as? UiState.Success)?.data.orEmpty()

    val q = query.trim().lowercase()
    val matchedRestaurants = if (scopeFilter != "All" && scopeFilter != "Food") emptyList() else restaurants.filter {
        q.isBlank() || it.name.lowercase().contains(q) || it.city.lowercase().contains(q)
    }
    val matchedFood = if (scopeFilter != "All" && scopeFilter != "Food") emptyList() else foodItems.filter {
        (q.isBlank() || it.name.lowercase().contains(q) || it.categoryName.lowercase().contains(q) || it.restaurantName.lowercase().contains(q)) &&
            it.effectivePricePkr <= maxPricePkr && (!onlyAvailable || (it.isAvailable && it.stockQuantity > 0L))
    }
    val matchedStores = if (scopeFilter != "All" && scopeFilter != "Grocery") emptyList() else stores.filter {
        q.isBlank() || it.name.lowercase().contains(q) || it.city.lowercase().contains(q)
    }
    val matchedGrocery = if (scopeFilter != "All" && scopeFilter != "Grocery") emptyList() else products.filter {
        (q.isBlank() || it.name.lowercase().contains(q) || it.categoryName.lowercase().contains(q) || it.storeName.lowercase().contains(q)) &&
            it.effectivePricePkr <= maxPricePkr && (!onlyAvailable || (it.isAvailable && it.stockQuantity > 0L))
    }
    val matchedMedStores = if (scopeFilter != "All" && scopeFilter != "Medical") emptyList() else medStores.filter {
        q.isBlank() || it.name.lowercase().contains(q) || it.city.lowercase().contains(q)
    }
    val matchedMedical = if (scopeFilter != "All" && scopeFilter != "Medical") emptyList() else medProducts.filter {
        (q.isBlank() || it.name.lowercase().contains(q) || it.categoryName.lowercase().contains(q) || it.storeName.lowercase().contains(q)) &&
            it.effectivePricePkr <= maxPricePkr && (!onlyAvailable || (it.isAvailable && it.stockQuantity > 0L))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search GT Service") },
                colors = gtNavyTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search dishes, groceries, medicines, stores...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("search_input")
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("All", "Food", "Grocery", "Medical")) { f ->
                        FilterChip(selected = scopeFilter == f, onClick = { scopeFilter = f }, label = { Text(f) })
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Max Price: ${GharTakConstants.formatPkr(maxPricePkr)}", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("In Stock Only", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(6.dp))
                        Switch(checked = onlyAvailable, onCheckedChange = { onlyAvailable = it })
                    }
                }
                Slider(
                    value = maxPricePkr.toFloat(),
                    onValueChange = { maxPricePkr = it.toDouble() },
                    valueRange = 100f..5000f
                )
            }

            if (matchedRestaurants.isNotEmpty()) {
                item { Text("Restaurants (${matchedRestaurants.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matchedRestaurants, key = { "r_${it.id}" }) { r ->
                    ListResultRow(
                        imageUrl = restaurantImageUri(r.name, r.imageUrl),
                        title = r.name,
                        subtitle = "${r.city} • ${r.deliveryTimeMinutes} min",
                        isGrocery = false,
                        onClick = { onOpenRestaurant(r.id) }
                    )
                }
            }
            if (matchedFood.isNotEmpty()) {
                item { Text("Food Dishes (${matchedFood.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matchedFood, key = { "f_${it.id}" }) { f ->
                    val rest = restaurants.firstOrNull { it.id == f.restaurantId }
                    FoodProductRowCard(
                        food = f,
                        onCardClick = { customizingFood = f },
                        onAddClick = { customizingFood = f },
                        onOrderNowClick = { viewModel.buyNowFoodItem(f, rest, onReadyForCheckout = onNavigateToCheckout) }
                    )
                }
            }
            if (matchedStores.isNotEmpty()) {
                item { Text("Grocery Stores (${matchedStores.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matchedStores, key = { "s_${it.id}" }) { s ->
                    ListResultRow(
                        imageUrl = groceryStoreImageUri(s.name, s.imageUrl),
                        title = s.name,
                        subtitle = "${s.city} • ${s.deliveryTimeMinutes} min",
                        isGrocery = true,
                        onClick = { onOpenGroceryStore(s.id) }
                    )
                }
            }
            if (matchedGrocery.isNotEmpty()) {
                item { Text("Grocery Products (${matchedGrocery.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matchedGrocery, key = { "g_${it.id}" }) { g ->
                    val store = stores.firstOrNull { it.id == g.storeId }
                    val qty = groceryCart.firstOrNull { it.productId == g.id }?.quantity ?: 0
                    GroceryProductRowCard(
                        product = g,
                        inCartQty = qty,
                        onCardClick = { inspectingGrocery = g },
                        onAdd = { viewModel.addGroceryToCart(g, store) },
                        onBuyNow = { viewModel.buyNowGroceryProduct(g, store, onNavigateToCheckout) },
                        onDelta = { delta -> viewModel.updateCartQuantity("grocery", g.id, delta) }
                    )
                }
            }
            if (matchedMedStores.isNotEmpty()) {
                item { Text("Medical Pharmacies (${matchedMedStores.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matchedMedStores, key = { "ms_${it.id}" }) { ms ->
                    ListResultRow(
                        imageUrl = medicalStoreImageUri(ms.name, ms.imageUrl),
                        title = ms.name,
                        subtitle = "${ms.city} • ${ms.deliveryTimeMinutes} min",
                        isGrocery = false,
                        isMedical = true,
                        onClick = { onOpenMedicalStore(ms.id) }
                    )
                }
            }
            if (matchedMedical.isNotEmpty()) {
                item { Text("Medicines & Healthcare (${matchedMedical.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matchedMedical, key = { "mp_${it.id}" }) { mp ->
                    val store = medStores.firstOrNull { it.id == mp.storeId }
                    val qty = medicalCart.firstOrNull { it.productId == mp.id }?.quantity ?: 0
                    MedicalProductRowCard(
                        product = mp,
                        inCartQty = qty,
                        onCardClick = { inspectingMedical = mp },
                        onAdd = { viewModel.addMedicalToCart(mp, store) },
                        onBuyNow = { viewModel.buyNowMedicalProduct(mp, store, onNavigateToCheckout) },
                        onDelta = { delta -> viewModel.updateCartQuantity("medical", mp.id, delta) }
                    )
                }
            }
        }
    }

    customizingFood?.let { food ->
        val rest = restaurants.firstOrNull { it.id == food.restaurantId }
        FoodCustomizationSheet(
            item = food,
            onDismiss = { customizingFood = null },
            onOpenRestaurant = {
                customizingFood = null
                onOpenRestaurant(food.restaurantId)
            },
            onAddToCart = { note, qty, extraPrice ->
                viewModel.addFoodToCart(food, rest, note, qty, extraPrice)
                customizingFood = null
            },
            onOrderNow = { note, qty, extraPrice ->
                customizingFood = null
                viewModel.buyNowFoodItem(food, rest, note, qty, extraPrice, onNavigateToCheckout)
            }
        )
    }

    inspectingGrocery?.let { prod ->
        val store = stores.firstOrNull { it.id == prod.storeId }
        GroceryProductDetailDialog(
            product = prod,
            onDismiss = { inspectingGrocery = null },
            onOpenStore = {
                inspectingGrocery = null
                onOpenGroceryStore(prod.storeId)
            },
            onAddToCart = {
                viewModel.addGroceryToCart(prod, store)
                inspectingGrocery = null
            },
            onBuyNow = {
                inspectingGrocery = null
                viewModel.buyNowGroceryProduct(prod, store, onNavigateToCheckout)
            }
        )
    }

    inspectingMedical?.let { prod ->
        val store = medStores.firstOrNull { it.id == prod.storeId }
        MedicalProductDetailDialog(
            product = prod,
            onDismiss = { inspectingMedical = null },
            onOpenStore = {
                inspectingMedical = null
                onOpenMedicalStore(prod.storeId)
            },
            onAddToCart = {
                viewModel.addMedicalToCart(prod, store)
                inspectingMedical = null
            },
            onBuyNow = {
                inspectingMedical = null
                viewModel.buyNowMedicalProduct(prod, store, onNavigateToCheckout)
            }
        )
    }
}

@Composable
private fun ListResultRow(
    imageUrl: String,
    title: String,
    subtitle: String,
    isGrocery: Boolean,
    isMedical: Boolean = false,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GharTakImage(
                imageUrl = imageUrl,
                contentDescription = title,
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)),
                isGroceryFallback = isGrocery,
                isMedicalFallback = isMedical
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroceryProductDetailDialog(
    product: GroceryProduct,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onOpenStore: (() -> Unit)? = null
) {
    val gallery = product.allImages
    val fallbackImage = groceryCategoryImageUri(product.categoryName, product.name)
    var activeImage by remember(product) {
        mutableStateOf(
            gallery.firstOrNull()?.takeUnless { it == "res://img_hero_grocery" } ?: fallbackImage
        )
    }
    val available = product.isAvailable && product.stockQuantity > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            LazyColumn(modifier = Modifier.height(370.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp))) {
                        GharTakImage(activeImage, product.name, modifier = Modifier.fillMaxSize(), isGroceryFallback = true)
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
                                    isGroceryFallback = true
                                )
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${product.storeName} • ${product.categoryName}", style = MaterialTheme.typography.labelLarge, color = GharTakEmerald)
                            Text("Pack/Unit: ${product.unit} • Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (onOpenStore != null) {
                            TextButton(onClick = onOpenStore) { Text("Visit Store") }
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
                    colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                    modifier = Modifier.testTag("dialog_buy_now_button")
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
