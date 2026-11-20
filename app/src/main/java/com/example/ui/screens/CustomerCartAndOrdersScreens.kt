package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppPortal
import com.example.data.model.CartItem
import com.example.data.model.DeliveryAddress
import com.example.data.model.DeliveryAreaFeeRule
import com.example.data.model.GharTakConstants
import com.example.data.model.OrderRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GharTakImage
import com.example.ui.components.PhotoPickerUploadRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrackingRouteAndDistanceDialog
import com.example.ui.components.calculateDistanceKm
import com.example.ui.components.formatDistanceWithEta
import com.example.ui.components.openGoogleMapsDirections
import com.example.ui.components.openGoogleMapsLocation
import com.example.ui.components.openWhatsAppSupport
import com.example.ui.components.rememberDeviceLocationState
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.GharTakViewModel
import com.example.ui.viewmodel.UiState
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartAndCheckoutScreen(
    viewModel: GharTakViewModel,
    onOrderPlaced: (String) -> Unit,
    onBrowseFood: () -> Unit,
    onBrowseGrocery: () -> Unit,
    onBrowseMedical: () -> Unit,
    onBrowseMultan: () -> Unit = {},
    onBrowseCelebration: () -> Unit = {},
    onOpenWallet: () -> Unit
) {
    val foodCart by viewModel.foodCart.collectAsStateWithLifecycle()
    val groceryCart by viewModel.groceryCart.collectAsStateWithLifecycle()
    val medicalCart by viewModel.medicalCart.collectAsStateWithLifecycle()
    val multanCart by viewModel.multanCart.collectAsStateWithLifecycle()
    val celebrationCart by viewModel.celebrationCart.collectAsStateWithLifecycle()
    val appliedFoodCoupon by viewModel.appliedFoodCoupon.collectAsStateWithLifecycle()
    val appliedGroceryCoupon by viewModel.appliedGroceryCoupon.collectAsStateWithLifecycle()
    val appliedMedicalCoupon by viewModel.appliedMedicalCoupon.collectAsStateWithLifecycle()
    val appliedMultanCoupon by viewModel.appliedMultanCoupon.collectAsStateWithLifecycle()
    val appliedCelebrationCoupon by viewModel.appliedCelebrationCoupon.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val preferredChannel by viewModel.preferredCheckoutChannel.collectAsStateWithLifecycle()
    val selectedMultanDestination by viewModel.selectedMultanDestination.collectAsStateWithLifecycle()

    var activeCartType by remember {
        mutableStateOf(
            when {
                preferredChannel == "multan" && multanCart.isNotEmpty() -> "multan"
                preferredChannel == "celebration" && celebrationCart.isNotEmpty() -> "celebration"
                preferredChannel == "medical" && medicalCart.isNotEmpty() -> "medical"
                preferredChannel == "grocery" && groceryCart.isNotEmpty() -> "grocery"
                preferredChannel == "food" && foodCart.isNotEmpty() -> "food"
                foodCart.isNotEmpty() -> "food"
                groceryCart.isNotEmpty() -> "grocery"
                medicalCart.isNotEmpty() -> "medical"
                multanCart.isNotEmpty() -> "multan"
                celebrationCart.isNotEmpty() -> "celebration"
                else -> "food"
            }
        )
    }
    LaunchedEffect(preferredChannel) {
        if ((preferredChannel == "food" && foodCart.isNotEmpty()) ||
            (preferredChannel == "grocery" && groceryCart.isNotEmpty()) ||
            (preferredChannel == "medical" && medicalCart.isNotEmpty()) ||
            (preferredChannel == "multan" && multanCart.isNotEmpty()) ||
            (preferredChannel == "celebration" && celebrationCart.isNotEmpty())
        ) {
            activeCartType = preferredChannel
        }
    }
    val cartItems = when (activeCartType) {
        "food" -> foodCart
        "medical" -> medicalCart
        "multan" -> multanCart
        "celebration" -> celebrationCart
        else -> groceryCart
    }
    val coupon = when (activeCartType) {
        "food" -> appliedFoodCoupon
        "medical" -> appliedMedicalCoupon
        "multan" -> appliedMultanCoupon
        "celebration" -> appliedCelebrationCoupon
        else -> appliedGroceryCoupon
    }

    var couponInput by remember { mutableStateOf("") }
    var foodOrderNote by remember { mutableStateOf("") }
    var groceryOrderNote by remember { mutableStateOf("") }
    var medicalOrderNote by remember { mutableStateOf("") }
    var multanOrderNote by remember { mutableStateOf("") }
    var celebrationOrderNote by remember { mutableStateOf("") }
    var editingCartItem by remember { mutableStateOf<CartItem?>(null) }
    var selectedAddressId by remember(addresses) { mutableStateOf(addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id ?: "") }
    var manualAddress by remember { mutableStateOf("") }
    var phoneInput by remember(userProfile) { mutableStateOf(userProfile?.phone ?: "+92") }
    var paymentMethod by remember { mutableStateOf("COD") }
    var paymentReference by remember { mutableStateOf("") }
    var autoNotifyPaymentSent by remember { mutableStateOf(true) }
    var paymentSentConfirmBanner by remember { mutableStateOf<String?>(null) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    val currentOrderNote = when (activeCartType) {
        "food" -> foodOrderNote
        "medical" -> medicalOrderNote
        "multan" -> multanOrderNote
        "celebration" -> celebrationOrderNote
        else -> groceryOrderNote
    }
    val selectedAddress = addresses.firstOrNull { it.id == selectedAddressId }
    val effectiveCity = selectedAddress?.city?.takeIf { it.isNotBlank() }
        ?: selectedCity.takeUnless { it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
        ?: userProfile?.city?.takeIf { it.isNotBlank() && !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
        ?: settings.cities.firstOrNull { !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
        ?: GharTakConstants.DEFAULT_CITY
    val cityAreaRules = remember(settings.deliveryAreaFeeRules, effectiveCity) {
        settings.deliveryAreaFeeRules.filter { it.city.equals(effectiveCity, ignoreCase = true) }
    }
    var selectedAreaRuleId by remember(effectiveCity) { mutableStateOf("") }
    var manualDistanceKm by remember(effectiveCity) { mutableStateOf<Double?>(null) }
    val selectedAreaRule = cityAreaRules.firstOrNull { it.id == selectedAreaRuleId }
    val fullAddrText = selectedAddress?.formattedLine()?.ifBlank { manualAddress } ?: manualAddress

    val multanLocationOptions = remember(settings) {
        viewModel.getMultanLocationsWithFees(settings)
    }
    val resolvedMultanDest = remember(activeCartType, selectedMultanDestination, effectiveCity, fullAddrText, settings) {
        if (activeCartType == "multan") {
            viewModel.resolveMultanLocationAndFee(
                preferredCity = effectiveCity,
                addressText = fullAddrText,
                explicitDestination = selectedMultanDestination,
                settings = settings
            )
        } else {
            effectiveCity to 0.0
        }
    }

    val subtotal = cartItems.sumOf { it.unitPricePkr * it.quantity }
    val discount = viewModel.calculateDiscount(coupon, subtotal)
    val feeResolution = if (cartItems.isEmpty()) {
        com.example.ui.viewmodel.DeliveryFeeResolution(0.0, "")
    } else {
        viewModel.resolveDeliveryFee(
            orderType = activeCartType,
            deliveryCity = if (activeCartType == "multan") resolvedMultanDest.first else effectiveCity,
            addressText = fullAddrText,
            selectedAreaRule = selectedAreaRule,
            vendorId = cartItems.firstOrNull()?.vendorId.orEmpty(),
            customerLat = selectedAddress?.latitude,
            customerLng = selectedAddress?.longitude,
            manualDistanceKm = manualDistanceKm,
            explicitMultanDestination = if (activeCartType == "multan") resolvedMultanDest.first else selectedMultanDestination,
            settings = settings
        )
    }
    val deliveryFee = feeResolution.feePkr
    val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Your Cart", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Food, Grocery, Medical, Multan Express & Celebration orders are checked out separately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            val cartTabs = listOf(
                Triple("food", "Food (${foodCart.sumOf { it.quantity }})", "cart_tab_food"),
                Triple("grocery", "Grocery (${groceryCart.sumOf { it.quantity }})", "cart_tab_grocery"),
                Triple("medical", "Medical (${medicalCart.sumOf { it.quantity }})", "cart_tab_medical"),
                Triple("multan", "Multan (${multanCart.sumOf { it.quantity }})", "cart_tab_multan"),
                Triple("celebration", "Celebration (${celebrationCart.sumOf { it.quantity }})", "cart_tab_celebration")
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cartTabs) { (key, label, tag) ->
                    FilterChip(
                        selected = activeCartType == key,
                        onClick = { activeCartType = key },
                        label = { Text(label, maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GharTakEmerald,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag(tag)
                    )
                }
            }
        }

        if (cartItems.isEmpty()) {
            item {
                val (emptyTitle, emptySubtitle, emptyActionLabel, emptyOnAction) = when (activeCartType) {
                    "food" -> listOf("Food Cart is empty", "Explore restaurants & add delicious dishes.", "Browse Restaurants") to onBrowseFood
                    "medical" -> listOf("Medical Cart is empty", "Order medicines, vitamins & first aid supplies.", "Browse Pharmacy") to onBrowseMedical
                    "multan" -> listOf("Multan Cart is empty", "Order Crumble, KFC, Hafiz Sohan Halwa & more from Multan.", "Browse Multan Hub") to onBrowseMultan
                    "celebration" -> listOf("Celebration Cart is empty", "Explore birthday cakes, bouquets, balloons & surprise gifts.", "Browse Celebration") to onBrowseCelebration
                    else -> listOf("Grocery Cart is empty", "Stock up on fresh fruits, dairy & pantry staples.", "Browse Grocery Mart") to onBrowseGrocery
                }.let { (texts, action) -> Quad(texts[0], texts[1], texts[2], action) }
                EmptyStateView(
                    title = emptyTitle,
                    subtitle = emptySubtitle,
                    actionLabel = emptyActionLabel,
                    onAction = emptyOnAction
                )
            }
        } else {
            item {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ordering from", style = MaterialTheme.typography.labelSmall)
                            Text(cartItems.first().vendorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.clearCart(activeCartType) }) {
                            Text("Clear")
                        }
                    }
                }
            }

            items(cartItems, key = { it.cartLineId }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GharTakImage(
                                imageUrl = item.imageUrl,
                                contentDescription = item.name,
                                modifier = Modifier.size(58.dp).clip(RoundedCornerShape(10.dp)),
                                isGroceryFallback = activeCartType == "grocery",
                                isMedicalFallback = activeCartType == "medical"
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${GharTakConstants.formatPkr(item.unitPricePkr)} x ${item.quantity} (${item.unit})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.updateCartQuantity(activeCartType, item.productId, -1, item.customizationNote) }) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                                }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.updateCartQuantity(activeCartType, item.productId, 1, item.customizationNote) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase")
                                }
                            }
                        }

                        // Direct inline message / customization box right inside the Cart item card
                        OutlinedTextField(
                            value = item.customizationNote,
                            onValueChange = { next ->
                                viewModel.updateCartItemNoteInline(activeCartType, item.cartLineId, next)
                            },
                            label = {
                                Text(
                                    if (activeCartType == "food") "Type message for ${item.name} (e.g. add mayonnaise)"
                                    else "Type message / note for ${item.name}"
                                )
                            },
                            placeholder = {
                                Text(
                                    if (activeCartType == "food") "e.g. Add mayonnaise, extra ketchup, less spicy..."
                                    else "e.g. Fresh / ripe pick, pack separately..."
                                )
                            },
                            minLines = 1,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inline_item_note_${item.productId}")
                        )

                        // Quick button to also open priced dips/add-ons & preset chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.customizationNote.length}/200",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { editingCartItem = item },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("edit_cart_item_${item.productId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Add dips or presets for ${item.name}",
                                    tint = GharTakPink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (activeCartType == "food") "Quick Tags & Paid Add-ons" else "Quick Packing Tags",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GharTakPink
                                )
                            }
                        }
                    }
                }
            }

            // What to Add / Order Instructions Card right in Cart
            item {
                val quickTags = when (activeCartType) {
                    "food" -> listOf("Add Mayonnaise", "Extra Ketchup", "Garlic Dip", "Less Spicy", "No Onion", "Cutlery & Napkins")
                    "medical" -> listOf("Seal-Packed Safely", "Keep Cool / Dry", "Call on Arrival")
                    "multan" -> listOf("Fresh Box", "Bubble Wrap", "Include Invoice", "Call Before Dispatch")
                    "celebration" -> listOf("Happy Birthday Card", "Write Name on Cake", "Surprise Delivery", "Gift Wrap")
                    else -> listOf("Fresh / Ripe Pick", "Separate Bags", "Call on Arrival")
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_order_instructions_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (activeCartType) {
                                "food" -> "What to Add / Kitchen Request"
                                "medical" -> "Pharmacy & Handling Instructions"
                                "multan" -> "Multan Express Instructions"
                                "celebration" -> "Cake Writing & Gift Message"
                                else -> "Order & Packing Instructions"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (activeCartType) {
                                "food" -> "Want mayonnaise, sauce, extra dip, or special prep? Tap or write below so the kitchen adds it."
                                "medical" -> "Add pharmacy packing, dosage strip, or delivery instructions below."
                                "multan" -> "Specify branch flavor, sweetness, or Multan pickup preference."
                                "celebration" -> "Write the exact birthday/anniversary message for the cake or greeting card."
                                else -> "Tell the grocery store any packing or freshness preference."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            quickTags.forEach { tag ->
                                val active = currentOrderNote.contains(tag, ignoreCase = true)
                                FilterChip(
                                    selected = active,
                                    onClick = {
                                        val updated = toggleQuickTagInNote(currentOrderNote, tag, 180)
                                        when (activeCartType) {
                                            "food" -> foodOrderNote = updated
                                            "medical" -> medicalOrderNote = updated
                                            "multan" -> multanOrderNote = updated
                                            "celebration" -> celebrationOrderNote = updated
                                            else -> groceryOrderNote = updated
                                        }
                                    },
                                    label = { Text(tag, style = MaterialTheme.typography.labelMedium) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = currentOrderNote,
                            onValueChange = { next ->
                                if (next.length <= 180) {
                                    when (activeCartType) {
                                        "food" -> foodOrderNote = next
                                        "medical" -> medicalOrderNote = next
                                        "multan" -> multanOrderNote = next
                                        "celebration" -> celebrationOrderNote = next
                                        else -> groceryOrderNote = next
                                    }
                                }
                            },
                            label = { Text(if (activeCartType == "food") "Add extras or instructions (e.g. Add mayonnaise)" else "Special instructions / message") },
                            placeholder = { Text(if (activeCartType == "food") "e.g. Please add mayonnaise, extra ketchup, less spicy" else "e.g. Write name on cake or pack safely") },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cart_order_note_input")
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${currentOrderNote.length}/180",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }

            // Coupon Box
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = { couponInput = it },
                        label = { Text("Coupon Code (e.g. GHARTAK20)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("coupon_input")
                    )
                    Button(
                        onClick = { viewModel.applyCoupon(activeCartType, couponInput, subtotal) },
                        modifier = Modifier.testTag("apply_coupon_button")
                    ) {
                        Text("Apply")
                    }
                }
            }

            // Delivery Address & Phone
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showAddAddressDialog = true }) { Text("+ New Address") }
                        }
                        if (addresses.isNotEmpty()) {
                            addresses.forEach { addr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedAddressId = addr.id
                                            if (activeCartType == "multan") {
                                                val matched = viewModel.resolveMultanLocationAndFee(
                                                    preferredCity = addr.city,
                                                    addressText = addr.formattedLine(),
                                                    explicitDestination = null,
                                                    settings = settings
                                                ).first
                                                viewModel.setMultanDestination(matched)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedAddressId == addr.id,
                                        onClick = {
                                            selectedAddressId = addr.id
                                            if (activeCartType == "multan") {
                                                val matched = viewModel.resolveMultanLocationAndFee(
                                                    preferredCity = addr.city,
                                                    addressText = addr.formattedLine(),
                                                    explicitDestination = null,
                                                    settings = settings
                                                ).first
                                                viewModel.setMultanDestination(matched)
                                            }
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${addr.label} • ${addr.city}", style = MaterialTheme.typography.labelLarge)
                                        Text(addr.formattedLine(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = manualAddress,
                                onValueChange = { next ->
                                    manualAddress = next
                                    if (activeCartType == "multan") {
                                        val rules = settings.multanCityDeliveryFees
                                            .filter { it.city.isNotBlank() }
                                            .sortedByDescending { it.city.trim().length }
                                        val lower = next.lowercase()
                                        rules.firstOrNull { it.city.trim().length >= 2 && lower.contains(it.city.trim().lowercase()) }?.let { rule ->
                                            viewModel.setMultanDestination(rule.city.trim())
                                        }
                                    }
                                },
                                label = { Text("House #, Street, Area, City") },
                                modifier = Modifier.fillMaxWidth().testTag("manual_address_input")
                            )
                        }
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Contact Mobile (+92...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (activeCartType == "multan") {
                            Surface(
                                color = GharTakEmerald.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Multan → ${resolvedMultanDest.first} Express Fee: ${GharTakConstants.formatPkr(deliveryFee)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = GharTakEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Select Multan Delivery Location (Admin-updated rates):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(multanLocationOptions, key = { "checkout_multan_${it.first.lowercase()}" }) { (locName, locFee) ->
                                            FilterChip(
                                                selected = resolvedMultanDest.first.equals(locName, ignoreCase = true),
                                                onClick = { viewModel.setMultanDestination(locName) },
                                                label = { Text("$locName • ${GharTakConstants.formatPkr(locFee)}") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = GharTakEmerald,
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.testTag("checkout_multan_loc_$locName")
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            if (settings.radiusPricingEnabled) {
                                val baseRadius = settings.deliveryBaseRadiusKm.toInt().coerceAtLeast(1)
                                Surface(
                                    color = GharTakEmerald.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "Radius Rate ($effectiveCity): Up to $baseRadius km = ${GharTakConstants.formatPkr(settings.withinRadiusFeePkr)} • +${GharTakConstants.formatPkr(settings.extraPerKmFeePkr)}/km extra",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = GharTakEmerald,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Applied: ${feeResolution.reason} → ${GharTakConstants.formatPkr(deliveryFee)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                if (selectedAreaRuleId.isBlank()) {
                                    val distanceOptions: List<Pair<String, Double?>> = listOf(
                                        "Address Auto" to null,
                                        "Within $baseRadius km" to baseRadius.toDouble(),
                                        "7 km" to 7.0,
                                        "10 km" to 10.0,
                                        "15 km" to 15.0
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(distanceOptions) { (label, kmVal) ->
                                            FilterChip(
                                                selected = manualDistanceKm == kmVal,
                                                onClick = { manualDistanceKm = kmVal },
                                                label = { Text(label) },
                                                modifier = Modifier.testTag("checkout_radius_${kmVal?.toInt() ?: "auto"}")
                                            )
                                        }
                                    }
                                }
                            }
                            if (cityAreaRules.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    "Optional Area / Zone Override in $effectiveCity",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        FilterChip(
                                            selected = selectedAreaRuleId.isBlank(),
                                            onClick = { selectedAreaRuleId = "" },
                                            label = { Text("Use Radius / Auto") },
                                            modifier = Modifier.testTag("area_rule_auto")
                                        )
                                    }
                                    items(cityAreaRules, key = { it.id }) { rule ->
                                        FilterChip(
                                            selected = selectedAreaRuleId == rule.id,
                                            onClick = { selectedAreaRuleId = rule.id },
                                            label = {
                                                Text("${rule.areaName} (${GharTakConstants.formatPkr(rule.feePkr)})")
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GharTakEmerald,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("area_rule_${rule.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Payment Method Selection
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                val walletBal = userProfile?.walletBalancePkr ?: 0.0
                val walletEnough = walletBal >= total && total > 0.0
                val jcNum = settings.jazzCashNumber.ifBlank { GharTakConstants.DEFAULT_JAZZCASH_NUMBER }
                val jcTitle = settings.jazzCashAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
                val epNum = settings.easypaisaNumber.ifBlank { GharTakConstants.DEFAULT_EASYPAISA_NUMBER }
                val epTitle = settings.easypaisaAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        PaymentOptionRow(
                            title = "Cash on Delivery (COD)",
                            subtitle = "Pay cash to the GT Service rider at your doorstep",
                            selected = paymentMethod == "COD",
                            enabled = true,
                            onSelect = { paymentMethod = "COD" }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PaymentOptionRow(
                                    title = "GT Service Wallet (${GharTakConstants.formatPkr(walletBal)})",
                                    subtitle = if (walletEnough) "Instant pay • Auto-refund if cancelled" else "Balance too low for ${GharTakConstants.formatPkr(total)}",
                                    selected = paymentMethod == "Wallet",
                                    enabled = walletEnough,
                                    onSelect = { paymentMethod = "Wallet" }
                                )
                            }
                            TextButton(
                                onClick = onOpenWallet,
                                modifier = Modifier.testTag("checkout_topup_wallet_button")
                            ) {
                                Text("Top Up")
                            }
                        }
                        PaymentOptionRow(
                            title = "JazzCash Mobile Wallet",
                            subtitle = if (settings.jazzCashConfigured) {
                                "Send to $jcNum ($jcTitle)"
                            } else {
                                "Disabled by Admin Settings"
                            },
                            selected = paymentMethod == "JazzCash",
                            enabled = settings.jazzCashConfigured,
                            onSelect = { paymentMethod = "JazzCash" }
                        )
                        PaymentOptionRow(
                            title = "Easypaisa Mobile Account",
                            subtitle = if (settings.easypaisaConfigured) {
                                "Send to $epNum ($epTitle)"
                            } else {
                                "Disabled by Admin Settings"
                            },
                            selected = paymentMethod == "Easypaisa",
                            enabled = settings.easypaisaConfigured,
                            onSelect = { paymentMethod = "Easypaisa" }
                        )

                        if (paymentMethod == "JazzCash" || paymentMethod == "Easypaisa") {
                            val activeNum = if (paymentMethod == "JazzCash") jcNum else epNum
                            val activeTitle = if (paymentMethod == "JazzCash") jcTitle else epTitle
                            Surface(
                                color = GharTakEmerald.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "Send ${GharTakConstants.formatPkr(total)} via $paymentMethod",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Account: $activeNum • Title: $activeTitle",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GharTakEmerald,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(activeNum))
                                                Toast.makeText(context, "Copied $activeNum", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("checkout_copy_payment_number_button")
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy account number", tint = GharTakPink)
                                        }
                                    }
                                    if (settings.beneficiaryAccounts.isNotEmpty()) {
                                        HorizontalDivider()
                                        Text("Other Beneficiary Accounts:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        settings.beneficiaryAccounts.forEach { acc ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "${acc.channel} (${acc.accountTitle}): ${acc.accountNumber}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                TextButton(onClick = {
                                                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(acc.accountNumber))
                                                    Toast.makeText(context, "Copied ${acc.accountNumber}", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Text("Copy")
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                    OutlinedTextField(
                                        value = paymentReference,
                                        onValueChange = { paymentReference = it },
                                        label = { Text("Transaction ID / Sender Mobile (Optional)") },
                                        placeholder = { Text("e.g. TID 123456 or 03XX...") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("checkout_payment_tid_input")
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = autoNotifyPaymentSent,
                                            onCheckedChange = { autoNotifyPaymentSent = it },
                                            colors = CheckboxDefaults.colors(checkedColor = GharTakEmerald)
                                        )
                                        Text(
                                            "Auto-send message on order: 'I have sent ${GharTakConstants.formatPkr(total)} to $activeNum'",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.sendPaymentSentConfirmation(
                                                    paymentChannel = paymentMethod,
                                                    amountPkr = total,
                                                    targetNumber = activeNum,
                                                    targetTitle = activeTitle,
                                                    referenceNote = paymentReference,
                                                    onSuccess = { sentMsg ->
                                                        paymentSentConfirmBanner = sentMsg
                                                    }
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("checkout_notify_payment_sent_button")
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("I Have Sent Payment")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val waMsg =
                                                    "Assalam-o-Alaikum GT Service, maine aap ke $paymentMethod number $activeNum ($activeTitle) per ${GharTakConstants.formatPkr(total)} send kar diye hain." +
                                                        if (paymentReference.isNotBlank()) " TID: ${paymentReference.trim()}" else ""
                                                val waDigits = settings.supportWhatsappNumber.trim()
                                                    .ifBlank { GharTakConstants.toWhatsappDigits(settings.supportPhoneLocal) }
                                                openWhatsAppSupport(context, phoneDigits = waDigits, prefillMessage = waMsg)
                                            },
                                            modifier = Modifier.testTag("checkout_whatsapp_payment_button")
                                        ) {
                                            Text("WhatsApp")
                                        }
                                    }
                                    paymentSentConfirmBanner?.let { banner ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GharTakEmerald, modifier = Modifier.size(16.dp))
                                            Text(
                                                "Sent to Admin: \"$banner\"",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GharTakEmerald,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Order Summary & Place Order
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SummaryLine("Subtotal", GharTakConstants.formatPkr(subtotal))
                        SummaryLine(
                            label = if (feeResolution.reason.isNotBlank()) "Delivery (${feeResolution.reason})" else "Delivery Fee",
                            value = GharTakConstants.formatPkr(deliveryFee)
                        )
                        if (discount > 0.0) {
                            SummaryLine("Coupon Discount (${coupon?.code})", "- ${GharTakConstants.formatPkr(discount)}", GharTakEmerald)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        SummaryLine("Total Payable", GharTakConstants.formatPkr(total), GharTakPink, bold = true)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.placeOrder(
                                    orderType = activeCartType,
                                    address = selectedAddress,
                                    manualAddressLine = manualAddress,
                                    customerPhone = phoneInput,
                                    paymentMethod = paymentMethod,
                                    orderInstructions = currentOrderNote,
                                    paymentReference = paymentReference,
                                    notifyPaymentSent = autoNotifyPaymentSent,
                                    selectedAreaRule = selectedAreaRule,
                                    manualDistanceKm = manualDistanceKm,
                                    explicitMultanDestination = if (activeCartType == "multan") resolvedMultanDest.first else selectedMultanDestination,
                                    onSuccess = { orderId ->
                                        when (activeCartType) {
                                            "food" -> foodOrderNote = ""
                                            "medical" -> medicalOrderNote = ""
                                            "multan" -> multanOrderNote = ""
                                            "celebration" -> celebrationOrderNote = ""
                                            else -> groceryOrderNote = ""
                                        }
                                        paymentReference = ""
                                        paymentSentConfirmBanner = null
                                        onOrderPlaced(orderId)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("place_order_button")
                        ) {
                            Text("Place Order • ${GharTakConstants.formatPkr(total)}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    editingCartItem?.let { targetItem ->
        CartItemExtrasDialog(
            item = targetItem,
            isFood = activeCartType == "food",
            onDismiss = { editingCartItem = null },
            onSave = { updatedNote, updatedPrice ->
                viewModel.updateCartItemCustomization(
                    orderType = activeCartType,
                    targetItem = targetItem,
                    newNote = updatedNote,
                    newUnitPricePkr = updatedPrice
                )
                editingCartItem = null
            }
        )
    }

    if (showAddAddressDialog) {
        val mergedAddressCities = (settings.cities + settings.multanCityDeliveryFees.map { it.city })
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .ifEmpty { GharTakConstants.INITIAL_PAKISTAN_CITIES }
        AddAddressDialog(
            defaultCity = if (activeCartType == "multan") {
                resolvedMultanDest.first
            } else {
                userProfile?.city ?: mergedAddressCities.firstOrNull() ?: GharTakConstants.DEFAULT_CITY
            },
            cities = mergedAddressCities,
            onDismiss = { showAddAddressDialog = false },
            onSave = { addr ->
                viewModel.saveAddress(addr)
                if (activeCartType == "multan") {
                    viewModel.setMultanDestination(addr.city)
                }
                showAddAddressDialog = false
            }
        )
    }
}

private data class CartQuickAddon(
    val id: String,
    val label: String,
    val extraPricePkr: Double
)

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun toggleQuickTagInNote(current: String, tag: String, maxLen: Int): String {
    val parts = current.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toMutableList()
    val existingIdx = parts.indexOfFirst { it.equals(tag, ignoreCase = true) }
    if (existingIdx >= 0) {
        parts.removeAt(existingIdx)
    } else {
        parts.add(tag)
    }
    return parts.joinToString(", ").take(maxLen)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CartItemExtrasDialog(
    item: CartItem,
    isFood: Boolean,
    onDismiss: () -> Unit,
    onSave: (updatedNote: String, updatedUnitPricePkr: Double) -> Unit
) {
    val quickTags = remember(isFood) {
        if (isFood) {
            listOf(
                "Add Mayonnaise",
                "Extra Ketchup",
                "Garlic Sauce",
                "Less Spicy",
                "Extra Spicy",
                "No Mayo",
                "No Onion"
            )
        } else {
            listOf("Select Fresh / Ripe", "Separate Bag", "Fragile / Careful", "Call Before Packing")
        }
    }
    val pricedExtras = remember(isFood) {
        if (isFood) {
            listOf(
                CartQuickAddon("extra_mayo", "Extra Creamy Mayonnaise Cup", 50.0),
                CartQuickAddon("garlic_mayo_dip", "Garlic Mayo Dip", 70.0),
                CartQuickAddon("chili_garlic_cup", "Chili Garlic Sauce Cup", 40.0),
                CartQuickAddon("extra_cheese_slice", "Extra Mozzarella Cheese", 150.0),
                CartQuickAddon("regular_fries_cart", "Crispy Regular Fries", 299.0)
            )
        } else {
            emptyList()
        }
    }

    var noteText by remember(item) { mutableStateOf(item.customizationNote) }
    var selectedAddonIds by remember(item) { mutableStateOf(setOf<String>()) }

    val addedExtrasTotal = pricedExtras
        .filter { it.id in selectedAddonIds }
        .sumOf { it.extraPricePkr }
    val updatedUnitPrice = item.unitPricePkr + addedExtrasTotal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(if (isFood) "Customize ${item.name}" else "Note for ${item.name}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "Unit price: ${GharTakConstants.formatPkr(updatedUnitPrice)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = GharTakPink
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Quick Requests (Tap to add/remove)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickTags.forEach { tag ->
                        val active = noteText.contains(tag, ignoreCase = true)
                        FilterChip(
                            selected = active,
                            onClick = { noteText = toggleQuickTagInNote(noteText, tag, 180) },
                            label = { Text(tag) }
                        )
                    }
                }

                if (pricedExtras.isNotEmpty()) {
                    HorizontalDivider()
                    Text("Add Extra Dips & Sides", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    pricedExtras.forEach { extra ->
                        val checked = extra.id in selectedAddonIds
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (checked) GharTakPink else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedAddonIds = if (checked) selectedAddonIds - extra.id else selectedAddonIds + extra.id
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        selectedAddonIds = if (isChecked) selectedAddonIds + extra.id else selectedAddonIds - extra.id
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = GharTakPink)
                                )
                                Text(extra.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("+ ${GharTakConstants.formatPkr(extra.extraPricePkr)}", style = MaterialTheme.typography.labelMedium, color = GharTakPink, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= 180) noteText = it },
                    label = { Text("What would you like added?") },
                    placeholder = { Text(if (isFood) "e.g. Add mayonnaise, extra ketchup, less spicy" else "e.g. Fresh ripe pick, pack separately") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_item_note_input")
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${noteText.length}/180",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedExtrasLabels = pricedExtras
                        .filter { it.id in selectedAddonIds }
                        .joinToString(", ") { it.label }
                    val finalNote = listOfNotNull(
                        noteText.trim().takeIf { it.isNotBlank() },
                        selectedExtrasLabels.takeIf { it.isNotBlank() }?.let { "Added: $it" }
                    ).joinToString(" • ")
                    onSave(finalNote, updatedUnitPrice)
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("save_cart_item_extras_button")
            ) {
                Text("Save to Cart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PaymentOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = if (enabled) onSelect else null, enabled = enabled)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold, color = color)
    }
}

@Composable
fun CustomerOrdersScreen(
    viewModel: GharTakViewModel,
    onOpenSupportChat: (String) -> Unit = {}
) {
    val ordersState by viewModel.customerOrdersState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationState = rememberDeviceLocationState()
    var cancelTarget by remember { mutableStateOf<OrderRecord?>(null) }
    var reviewTarget by remember { mutableStateOf<OrderRecord?>(null) }
    var routeDialogOrder by remember { mutableStateOf<OrderRecord?>(null) }
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    val orders = (ordersState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("My Orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Live status tracking, delivery route path & distance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(
                    onClick = { onOpenSupportChat("") },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("orders_top_chat_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Message Support", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Message")
                }
            }
        }
        if (orders.isEmpty()) {
            item { EmptyStateView("No orders yet", "Place a food or grocery order to track it live here.") }
        } else {
            items(orders, key = { it.id }) { order ->
                val isExpanded = expandedOrderId == order.id
                val myLoc = locationState.myLocation
                val myToDropoffKm = remember(myLoc, order.deliveryLat, order.deliveryLng) {
                    myLoc?.let { (lat, lng) -> calculateDistanceKm(lat, lng, order.deliveryLat, order.deliveryLng) }
                }
                Card(modifier = Modifier.fillMaxWidth().clickable { expandedOrderId = if (isExpanded) null else order.id }) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(order.vendorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("#${order.id.takeLast(6).uppercase()} • ${order.orderType.uppercase()} • ${order.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(order.status)
                        }
                        Text(
                            order.items.joinToString(", ") { "${it.quantity}x ${it.name}" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(GharTakConstants.formatPkr(order.totalPkr), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GharTakPink)
                            Text(if (isExpanded) "Hide Timeline ▲" else "Track Order ▼", style = MaterialTheme.typography.labelMedium, color = GharTakPink)
                        }

                        if (isExpanded) {
                            HorizontalDivider()
                            OrderTimeline(order.status)
                            Text("Delivering to: ${order.deliveryAddress}, ${order.city}", style = MaterialTheme.typography.bodySmall)
                            if (myToDropoffKm != null) {
                                Text(
                                    text = "Distance from You → Address: ${formatDistanceWithEta(myToDropoffKm)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GharTakPink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (order.riderName.isNotBlank()) {
                                Text("Rider: ${order.riderName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { routeDialogOrder = order },
                                    modifier = Modifier.testTag("customer_route_map_${order.id}")
                                ) {
                                    Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Route & Distance")
                                }
                                OutlinedButton(onClick = {
                                    openGoogleMapsDirections(context, myLoc?.first, myLoc?.second, order.deliveryLat, order.deliveryLng)
                                }) {
                                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Map Path")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (order.status in GharTakConstants.CANCELLABLE_ORDER_STATUSES) {
                                    OutlinedButton(onClick = { cancelTarget = order }) { Text("Cancel") }
                                }
                                if (order.status == "Delivered" && order.orderType in setOf("food", "grocery", "medical")) {
                                    Button(onClick = { reviewTarget = order }) { Text("Rate & Review") }
                                }
                                TextButton(onClick = { viewModel.reorder(order) }) { Text("Reorder") }
                                OutlinedButton(
                                    onClick = { onOpenSupportChat(order.id) },
                                    modifier = Modifier.testTag("order_chat_support_${order.id}")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Chat Help")
                                }
                            }
                            if ((order.paymentMethod == "JazzCash" || order.paymentMethod == "Easypaisa") && order.paymentStatus == "awaiting_verification") {
                                val settingsSnapshot = viewModel.platformSettings.collectAsStateWithLifecycle().value
                                val payNum = if (order.paymentMethod == "JazzCash") {
                                    settingsSnapshot.jazzCashNumber.ifBlank { GharTakConstants.DEFAULT_JAZZCASH_NUMBER }
                                } else {
                                    settingsSnapshot.easypaisaNumber.ifBlank { GharTakConstants.DEFAULT_EASYPAISA_NUMBER }
                                }
                                val payTitle = if (order.paymentMethod == "JazzCash") {
                                    settingsSnapshot.jazzCashAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
                                } else {
                                    settingsSnapshot.easypaisaAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
                                }
                                Button(
                                    onClick = {
                                        viewModel.sendPaymentSentConfirmation(
                                            paymentChannel = order.paymentMethod,
                                            amountPkr = order.totalPkr,
                                            targetNumber = payNum,
                                            targetTitle = payTitle,
                                            orderId = order.id
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("order_notify_payment_sent_${order.id}")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Notify Admin: I Have Sent ${order.paymentMethod} Payment")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    routeDialogOrder?.let { order ->
        TrackingRouteAndDistanceDialog(
            order = order,
            matchedRider = null,
            locationState = locationState,
            onDismiss = { routeDialogOrder = null }
        )
    }

    cancelTarget?.let { order ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text("Cancel Order?") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for cancellation") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.cancelOrder(order.id, reason)
                    cancelTarget = null
                }) { Text("Confirm Cancel") }
            },
            dismissButton = { TextButton(onClick = { cancelTarget = null }) { Text("Keep Order") } }
        )
    }

    reviewTarget?.let { order ->
        var rating by remember { mutableDoubleStateOf(5.0) }
        var comment by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reviewTarget = null },
            title = { Text("Review ${order.vendorName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star.toDouble() }) {
                                Icon(Icons.Filled.Star, contentDescription = "$star stars", tint = if (star <= rating) GharTakAmber else Color.Gray)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Write your experience...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val targetType = when (order.orderType) {
                        "food" -> "restaurant"
                        "medical" -> "medicalStore"
                        else -> "groceryStore"
                    }
                    viewModel.submitReview(order.id, order.vendorId, targetType, order.vendorName, rating, comment)
                    reviewTarget = null
                }) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { reviewTarget = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun OrderTimeline(currentStatus: String) {
    val steps = if (currentStatus == "Cancelled" || currentStatus == "Rejected") {
        listOf("Pending", currentStatus)
    } else {
        listOf("Pending", "Accepted", "Preparing", "Ready for Pickup", "Rider Assigned", "Picked Up", "On the Way", "Delivered")
    }
    val isNegativeTerminal = currentStatus == "Cancelled" || currentStatus == "Rejected"
    val currentIndex = steps.indexOf(currentStatus).coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        steps.forEachIndexed { idx, step ->
            val done = idx <= currentIndex
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (done) {
                        if (isNegativeTerminal && idx == currentIndex) GharTakCoralError else GharTakEmerald
                    } else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    step,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (idx == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileAndPortalsScreen(
    viewModel: GharTakViewModel,
    onOpenNotifications: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenSupportChat: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val supportThread by viewModel.supportChatThread.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    var showEditProfile by remember { mutableStateOf(false) }
    var showAddAddress by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    GharTakImage(profile?.avatarUrl ?: viewModel.currentUserPhoto, "Profile photo", modifier = Modifier.size(64.dp).clip(CircleShape))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile?.fullName ?: viewModel.currentUserName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(viewModel.currentUserEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${profile?.phone ?: "+92"} • ${profile?.city ?: GharTakConstants.DEFAULT_CITY}", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { showEditProfile = true }) { Text("Edit") }
                }
            }
        }

        // GharTak Wallet Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenWallet)
                    .testTag("profile_wallet_card"),
                colors = CardDefaults.cardColors(containerColor = GharTakEmerald.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "GT Service Wallet", tint = GharTakEmerald, modifier = Modifier.size(28.dp))
                        Column {
                            Text("GT Service Wallet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Balance: ${GharTakConstants.formatPkr(profile?.walletBalancePkr ?: 0.0)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GharTakEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Button(onClick = onOpenWallet, modifier = Modifier.testTag("profile_open_wallet_button")) {
                        Text("Top Up")
                    }
                }
            }
        }

        // Helpline & Live Support Chat Card
        item {
            SupportHelplineCard(
                onOpenLiveChat = onOpenSupportChat,
                unreadCount = supportThread?.unreadByCustomer ?: 0L,
                supportPhoneLocal = settings.supportPhoneLocal,
                supportWhatsappNumber = settings.supportWhatsappNumber
            )
        }

        // Portal Switcher: Customer, Restaurant Owner, Grocery Store Owner, Rider, Super Admin
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Switch GT Service Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Access Customer, Vendor Dashboards, Rider App, or Admin Login from your account.", style = MaterialTheme.typography.bodySmall)
                    AppPortal.entries.forEach { portal ->
                        OutlinedButton(
                            onClick = { viewModel.switchPortal(portal) },
                            modifier = Modifier.fillMaxWidth().testTag("switch_portal_${portal.key}")
                        ) {
                            Icon(
                                when (portal) {
                                    AppPortal.CUSTOMER -> Icons.Filled.Person
                                    AppPortal.RESTAURANT -> Icons.Filled.Restaurant
                                    AppPortal.GROCERY -> Icons.Filled.Store
                                    AppPortal.RIDER -> Icons.Filled.DeliveryDining
                                    AppPortal.ADMIN -> Icons.Filled.AdminPanelSettings
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (portal == AppPortal.ADMIN) "Admin Login (Add Products)" else "Open ${portal.label} Portal")
                        }
                    }
                }
            }
        }

        // Saved Addresses
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Saved Addresses (${addresses.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showAddAddress = true }) { Text("+ Add") }
                    }
                    if (addresses.isEmpty()) {
                        Text("No saved addresses yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        addresses.forEach { addr ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${addr.label} • ${addr.city}", style = MaterialTheme.typography.labelLarge)
                                    Text(addr.formattedLine(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { viewModel.deleteAddress(addr.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete address")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favorites
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Favorites (${favorites.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (favorites.isEmpty()) {
                        Text("Tap the heart icon on any restaurant or grocery store to save it here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        favorites.forEach { fav ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Favorite, contentDescription = null, tint = GharTakPink, modifier = Modifier.size(16.dp))
                                Text("${fav.title} (${fav.targetType})", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // App Preferences & Logout
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Dark Theme", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Order Notifications", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = notificationsEnabled, onCheckedChange = { viewModel.setNotificationsEnabled(it) })
                    }
                    OutlinedButton(onClick = onOpenNotifications, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("View Notifications Inbox")
                    }
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().testTag("logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }

    if (showEditProfile) {
        var name by remember { mutableStateOf(profile?.fullName ?: viewModel.currentUserName) }
        var phone by remember { mutableStateOf(profile?.phone ?: "+92") }
        var city by remember { mutableStateOf(profile?.city ?: GharTakConstants.DEFAULT_CITY) }
        var avatar by remember { mutableStateOf(profile?.avatarUrl ?: "") }
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhotoPickerUploadRow(imageUrl = avatar, onImageChanged = { avatar = it }, label = "Profile Picture")
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile (+92...)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveProfile(name, phone, city, avatar.ifBlank { null })
                    showEditProfile = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditProfile = false }) { Text("Cancel") } }
        )
    }

    if (showAddAddress) {
        val approvedCities = (settings.cities + settings.multanCityDeliveryFees.map { it.city })
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .ifEmpty { GharTakConstants.INITIAL_PAKISTAN_CITIES }
        AddAddressDialog(
            defaultCity = profile?.city?.takeIf { c -> approvedCities.any { it.equals(c, ignoreCase = true) } }
                ?: approvedCities.firstOrNull()
                ?: GharTakConstants.DEFAULT_CITY,
            cities = approvedCities,
            onDismiss = { showAddAddress = false },
            onSave = {
                viewModel.saveAddress(it)
                showAddAddress = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: GharTakViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                colors = gtNavyTopAppBarColors(),
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateView("All caught up!", "Order updates and GT Service offers appear here.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.markNotificationRead(notif.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(notif.body, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AddAddressDialog(
    defaultCity: String,
    cities: List<String>,
    onDismiss: () -> Unit,
    onSave: (DeliveryAddress) -> Unit
) {
    val context = LocalContext.current
    val initialCityCoords = remember(defaultCity) { GharTakConstants.cityHubCoordinates(defaultCity) }
    var label by remember { mutableStateOf("Home") }
    var house by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(defaultCity) }
    var lat by remember(initialCityCoords) { mutableDoubleStateOf(initialCityCoords.first) }
    var lng by remember(initialCityCoords) { mutableDoubleStateOf(initialCityCoords.second) }
    var hasCapturedGps by remember { mutableStateOf(false) }
    var gpsStatus by remember(defaultCity, initialCityCoords) {
        mutableStateOf("Default $defaultCity Center Pin (%.4f, %.4f)".format(initialCityCoords.first, initialCityCoords.second))
    }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    lat = loc.latitude
                    lng = loc.longitude
                    hasCapturedGps = true
                    gpsStatus = "GPS Pin: %.4f, %.4f".format(lat, lng)
                } else {
                    gpsStatus = "GPS ready; using city center pin"
                }
            }
        } else {
            gpsStatus = "Location permission denied; using city pin"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Address") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Office", "Other").forEach { l ->
                        FilterChip(selected = label == l, onClick = { label = l }, label = { Text(l) })
                    }
                }
                OutlinedTextField(value = house, onValueChange = { house = it }, label = { Text("House / Flat #") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street / Road") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area / Colony") }, modifier = Modifier.fillMaxWidth())
                if (cities.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cities, key = { "addr_city_${it.lowercase()}" }) { option ->
                            FilterChip(
                                selected = city.trim().equals(option, ignoreCase = true),
                                onClick = {
                                    city = option
                                    if (!hasCapturedGps) {
                                        val (cLat, cLng) = GharTakConstants.cityHubCoordinates(option)
                                        lat = cLat
                                        lng = cLng
                                        gpsStatus = "Default $option Center Pin (%.4f, %.4f)".format(cLat, cLng)
                                    }
                                },
                                label = { Text(option) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = city,
                    onValueChange = { nextCity ->
                        city = nextCity
                        if (!hasCapturedGps) {
                            val (cLat, cLng) = GharTakConstants.cityHubCoordinates(nextCity)
                            lat = cLat
                            lng = cLng
                            gpsStatus = "Default ${nextCity.ifBlank { GharTakConstants.DEFAULT_CITY }} Center Pin (%.4f, %.4f)".format(cLat, cLng)
                        }
                    },
                    label = { Text("City / Town (${cities.take(3).joinToString("/")})") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            fusedClient.lastLocation.addOnSuccessListener { loc ->
                                if (loc != null) {
                                    lat = loc.latitude
                                    lng = loc.longitude
                                    hasCapturedGps = true
                                    gpsStatus = "GPS Pin: %.4f, %.4f".format(lat, lng)
                                }
                            }
                        } else {
                            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Capture GPS Coordinates")
                }
                Text(gpsStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                enabled = house.isNotBlank() && street.isNotBlank() && area.isNotBlank(),
                onClick = {
                    onSave(
                        DeliveryAddress(
                            label = label,
                            houseNumber = house,
                            street = street,
                            area = area,
                            city = city.ifBlank { GharTakConstants.DEFAULT_CITY },
                            latitude = lat,
                            longitude = lng,
                            isDefault = true
                        )
                    )
                }
            ) { Text("Save Address") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
