package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GharTakConstants
import com.example.data.model.OrderRecord
import com.example.data.model.RiderProfile
import com.example.ui.components.DeviceLocationState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrackingRouteAndDistanceDialog
import com.example.ui.components.calculateDistanceKm
import com.example.ui.components.formatDistanceLabel
import com.example.ui.components.formatDistanceWithEta
import com.example.ui.components.openGoogleMapsDirections
import com.example.ui.components.rememberDeviceLocationState
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakPink
import com.example.ui.viewmodel.AdminViewModel
import java.util.Locale

private val TRACKING_PIPELINE = listOf(
    "Pending",
    "Accepted",
    "Preparing",
    "Ready for Pickup",
    "Rider Assigned",
    "Picked Up",
    "On the Way",
    "Delivered"
)

private val KITCHEN_STAGES = setOf("Pending", "Accepted", "Preparing", "Ready for Pickup")
private val IN_TRANSIT_STAGES = setOf("Rider Assigned", "Picked Up", "On the Way")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminTrackingSection(adminVm: AdminViewModel) {
    val context = LocalContext.current
    val orders by adminVm.orders.collectAsStateWithLifecycle()
    val riders by adminVm.riders.collectAsStateWithLifecycle()
    val locationState = rememberDeviceLocationState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Orders, 1 = Rider Fleet
    var searchQuery by remember { mutableStateOf("") }
    var stageFilter by remember { mutableStateOf("All Active") }
    var assignTarget by remember { mutableStateOf<OrderRecord?>(null) }
    var statusTarget by remember { mutableStateOf<OrderRecord?>(null) }
    var routeDialogOrder by remember { mutableStateOf<OrderRecord?>(null) }

    val activeOrders = remember(orders) {
        orders.filter { it.status !in GharTakConstants.TERMINAL_ORDER_STATUSES }
    }
    val inTransitOrders = remember(orders) {
        orders.filter { it.status in IN_TRANSIT_STAGES }
    }
    val approvedRiders = remember(riders) {
        riders.filter { it.approvalStatus == "approved" }
            .sortedWith(compareByDescending<RiderProfile> { it.isOnline }.thenBy { it.fullName })
    }
    val onlineApprovedRiders = remember(approvedRiders) {
        approvedRiders.filter { it.isOnline }
    }

    val stageFilteredOrders = remember(orders, activeOrders, stageFilter) {
        when (stageFilter) {
            "All Active" -> activeOrders
            "Kitchen / Store" -> orders.filter { it.status in KITCHEN_STAGES }
            "In Transit" -> orders.filter { it.status in IN_TRANSIT_STAGES }
            "Delivered" -> orders.filter { it.status == "Delivered" }
            else -> orders
        }
    }

    val query = searchQuery.trim().lowercase()
    val filteredOrders = remember(stageFilteredOrders, query) {
        if (query.isBlank()) {
            stageFilteredOrders
        } else {
            stageFilteredOrders.filter { o ->
                o.id.lowercase().contains(query) ||
                    o.vendorName.lowercase().contains(query) ||
                    o.customerName.lowercase().contains(query) ||
                    o.riderName.lowercase().contains(query) ||
                    o.deliveryAddress.lowercase().contains(query) ||
                    o.city.lowercase().contains(query)
            }
        }
    }

    val filteredRiders = remember(riders, query) {
        val sorted = riders.sortedWith(
            compareByDescending<RiderProfile> { it.approvalStatus == "approved" }
                .thenByDescending { it.isOnline }
                .thenBy { it.fullName }
        )
        if (query.isBlank()) {
            sorted
        } else {
            sorted.filter { r ->
                r.fullName.lowercase().contains(query) ||
                    r.city.lowercase().contains(query) ||
                    r.vehicleNumber.lowercase().contains(query) ||
                    r.phone.lowercase().contains(query)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Order Tracking (${activeOrders.size})") },
                modifier = Modifier.testTag("tracking_tab_orders")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Rider Fleet (${riders.size})") },
                modifier = Modifier.testTag("tracking_tab_riders")
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live summary KPI cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "Active Orders",
                        value = "${activeOrders.size}",
                        modifier = Modifier.weight(1f),
                        highlightColor = GharTakPink
                    )
                    MetricCard(
                        label = "In Transit",
                        value = "${inTransitOrders.size}",
                        modifier = Modifier.weight(1f),
                        highlightColor = GharTakAmber
                    )
                    MetricCard(
                        label = "Online Riders",
                        value = "${onlineApprovedRiders.size}",
                        modifier = Modifier.weight(1f),
                        highlightColor = GharTakEmerald
                    )
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = {
                        Text(
                            if (selectedTab == 0) "Search order #, customer, vendor, rider, or city"
                            else "Search rider name, city, phone, or plate"
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search tracking") },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tracking_search_input")
                )
            }

            // My GPS Distance Bar
            item {
                val myLoc = locationState.myLocation
                Surface(
                    color = if (myLoc != null) GharTakEmerald.copy(alpha = 0.09f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.MyLocation,
                                contentDescription = "My GPS Location",
                                tint = if (myLoc != null) GharTakEmerald else GharTakPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (myLoc != null) {
                                    "Your GPS Active (${formatCoords(myLoc.first, myLoc.second)}) • Tap any order for map path & distance"
                                } else {
                                    "Tap Enable GPS to measure distance from you & open route paths"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(
                            onClick = { locationState.refreshOrRequestLocation() },
                            modifier = Modifier.testTag("tracking_enable_my_gps")
                        ) {
                            Text(if (myLoc != null) "Refresh" else "Enable GPS")
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                item {
                    val filterOptions = listOf("All Active", "Kitchen / Store", "In Transit", "Delivered", "All Orders")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filterOptions) { option ->
                            FilterChip(
                                selected = stageFilter == option,
                                onClick = { stageFilter = option },
                                label = { Text(option) },
                                modifier = Modifier.testTag("tracking_filter_${option.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                if (filteredOrders.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = if (orders.isEmpty()) "No orders placed yet" else "No matching orders in '$stageFilter'",
                            subtitle = if (orders.isEmpty()) {
                                "Incoming customer food and grocery deliveries will appear here for real-time dispatch tracking."
                            } else {
                                "Try switching the stage filter or clearing your search query."
                            },
                            actionLabel = if (stageFilter != "All Orders" || searchQuery.isNotBlank()) "Show All Orders" else null,
                            onAction = if (stageFilter != "All Orders" || searchQuery.isNotBlank()) {
                                {
                                    stageFilter = "All Orders"
                                    searchQuery = ""
                                }
                            } else null
                        )
                    }
                } else {
                    items(filteredOrders, key = { it.id }) { order ->
                        val matchedRider = riders.firstOrNull {
                            order.riderId.isNotBlank() && (it.userId == order.riderId || it.id == order.riderId)
                        }
                        AdminOrderTrackingCard(
                            order = order,
                            matchedRider = matchedRider,
                            myLocation = locationState.myLocation,
                            hasApprovedRiders = approvedRiders.isNotEmpty(),
                            onOpenRouteDialog = { routeDialogOrder = order },
                            onOpenDirectionsPath = {
                                openGoogleMapsDirections(
                                    context = context,
                                    originLat = matchedRider?.latitude ?: locationState.myLocation?.first,
                                    originLng = matchedRider?.longitude ?: locationState.myLocation?.second,
                                    destLat = order.deliveryLat,
                                    destLng = order.deliveryLng
                                )
                            },
                            onAssignRider = { assignTarget = order },
                            onAdvanceStage = { nextStatus ->
                                val nextPayment = if (nextStatus == "Delivered") "paid" else null
                                adminVm.updateOrderAdmin(order, nextStatus, paymentStatus = nextPayment)
                            },
                            onChangeStatus = { statusTarget = order }
                        )
                    }
                }
            } else {
                if (filteredRiders.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No riders found",
                            subtitle = "Registered delivery riders will show their live GPS coordinates and dispatch status here."
                        )
                    }
                } else {
                    items(filteredRiders, key = { it.id }) { rider ->
                        val riderActiveOrders = activeOrders.filter {
                            it.riderId == rider.userId || it.riderId == rider.id
                        }
                        AdminRiderFleetCard(
                            rider = rider,
                            myLocation = locationState.myLocation,
                            activeOrders = riderActiveOrders,
                            onOpenRoutePath = {
                                val myLoc = locationState.myLocation
                                openGoogleMapsDirections(
                                    context = context,
                                    originLat = myLoc?.first,
                                    originLng = myLoc?.second,
                                    destLat = rider.latitude,
                                    destLng = rider.longitude
                                )
                            },
                            onInspectOrder = { order -> routeDialogOrder = order }
                        )
                    }
                }
            }
        }
    }

    assignTarget?.let { order ->
        AlertDialog(
            onDismissRequest = { assignTarget = null },
            title = {
                Text("Dispatch Rider • #${order.id.takeLast(6).uppercase()}")
            },
            text = {
                if (approvedRiders.isEmpty()) {
                    Text("No approved riders are currently available. Approve a rider in the Riders section first.")
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(approvedRiders, key = { it.id }) { rider ->
                            val activeCount = activeOrders.count { it.riderId == rider.userId || it.riderId == rider.id }
                            OutlinedButton(
                                onClick = {
                                    val targetStatus = if (order.status in setOf("Picked Up", "On the Way")) {
                                        order.status
                                    } else {
                                        "Rider Assigned"
                                    }
                                    adminVm.updateOrderAdmin(order, targetStatus, rider = rider)
                                    assignTarget = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tracking_pick_rider_${rider.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (rider.isOnline) GharTakEmerald else Color.Gray)
                                            )
                                            Text(
                                                text = rider.fullName,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${rider.city} • ${rider.vehicleType} • $activeCount active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = if (rider.isOnline) "ONLINE" else "OFFLINE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (rider.isOnline) GharTakEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { assignTarget = null }) { Text("Close") }
            }
        )
    }

    statusTarget?.let { order ->
        AlertDialog(
            onDismissRequest = { statusTarget = null },
            title = { Text("Update Tracking Stage • #${order.id.takeLast(6).uppercase()}") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(GharTakConstants.ORDER_STATUSES) { statusOption ->
                        val isCurrent = statusOption == order.status
                        if (isCurrent) {
                            Button(
                                onClick = { statusTarget = null },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("$statusOption (Current)") }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val payStatus = if (statusOption == "Delivered") "paid" else null
                                    adminVm.updateOrderAdmin(order, statusOption, paymentStatus = payStatus)
                                    statusTarget = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(statusOption) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { statusTarget = null }) { Text("Cancel") }
            }
        )
    }

    routeDialogOrder?.let { order ->
        val matchedRider = riders.firstOrNull {
            order.riderId.isNotBlank() && (it.userId == order.riderId || it.id == order.riderId)
        }
        TrackingRouteAndDistanceDialog(
            order = order,
            matchedRider = matchedRider,
            locationState = locationState,
            onDismiss = { routeDialogOrder = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminOrderTrackingCard(
    order: OrderRecord,
    matchedRider: RiderProfile?,
    myLocation: Pair<Double, Double>?,
    hasApprovedRiders: Boolean,
    onOpenRouteDialog: () -> Unit,
    onOpenDirectionsPath: () -> Unit,
    onAssignRider: () -> Unit,
    onAdvanceStage: (String) -> Unit,
    onChangeStatus: () -> Unit
) {
    val nextStage = remember(order.status) { nextTrackingStatus(order.status) }
    val isCancelledOrRejected = order.status == "Cancelled" || order.status == "Rejected"
    val riderToDropoffKm = remember(matchedRider?.latitude, matchedRider?.longitude, order.deliveryLat, order.deliveryLng) {
        if (matchedRider != null) {
            calculateDistanceKm(matchedRider.latitude, matchedRider.longitude, order.deliveryLat, order.deliveryLng)
        } else null
    }
    val myToDropoffKm = remember(myLocation, order.deliveryLat, order.deliveryLng) {
        myLocation?.let { (lat, lng) -> calculateDistanceKm(lat, lng, order.deliveryLat, order.deliveryLng) }
    }
    val myToRiderKm = remember(myLocation, matchedRider?.latitude, matchedRider?.longitude) {
        if (myLocation != null && matchedRider != null) {
            calculateDistanceKm(myLocation.first, myLocation.second, matchedRider.latitude, matchedRider.longitude)
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenRouteDialog() }
            .testTag("tracking_order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "#${order.id.takeLast(6).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = if (order.orderType == "grocery") GharTakEmerald.copy(alpha = 0.14f) else GharTakPink.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.orderType.uppercase(),
                            color = if (order.orderType == "grocery") GharTakEmerald else GharTakPink,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                StatusBadge(order.status)
            }

            // Progress stepper
            AdminTrackingStepper(currentStatus = order.status)

            // Pickup -> Dropoff details
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Storefront,
                            contentDescription = "Pickup vendor",
                            tint = GharTakPink,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Pickup: ${order.vendorName} (${order.city})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "Customer dropoff",
                            tint = GharTakEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dropoff: ${order.customerName} (${order.customerPhone})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${order.deliveryAddress}, ${order.city}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "GPS: ${formatCoords(order.deliveryLat, order.deliveryLng)} • ${GharTakConstants.formatPkr(order.totalPkr)} (${order.paymentMethod})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (myToDropoffKm != null) {
                                Text(
                                    text = "From You → Address: ${formatDistanceWithEta(myToDropoffKm)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GharTakPink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Rider Telemetry Strip
            Surface(
                color = if (order.riderId.isNotBlank()) GharTakEmerald.copy(alpha = 0.08f) else GharTakAmber.copy(alpha = 0.09f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.DeliveryDining,
                            contentDescription = "Assigned rider",
                            tint = if (order.riderId.isNotBlank()) GharTakEmerald else GharTakAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        if (order.riderId.isNotBlank()) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = order.riderName.ifBlank { matchedRider?.fullName ?: "Assigned Rider" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (matchedRider != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (matchedRider.isOnline) GharTakEmerald else Color.Gray)
                                        )
                                        Text(
                                            text = if (matchedRider.isOnline) "Live" else "Offline",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (matchedRider.isOnline) GharTakEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (matchedRider != null) {
                                    Text(
                                        text = "${matchedRider.vehicleType} (${matchedRider.vehicleNumber}) • GPS ${formatCoords(matchedRider.latitude, matchedRider.longitude)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (riderToDropoffKm != null) {
                                    Text(
                                        text = "Rider → Dropoff: ${formatDistanceWithEta(riderToDropoffKm)}" +
                                            if (myToRiderKm != null) " • From You: ${formatDistanceLabel(myToRiderKm)}" else "",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GharTakEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Awaiting rider assignment",
                                style = MaterialTheme.typography.bodySmall,
                                color = GharTakAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    TextButton(
                        onClick = onOpenRouteDialog,
                        modifier = Modifier.testTag("tracking_rider_map_${order.id}")
                    ) {
                        Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Route")
                    }
                }
            }

            if (isCancelledOrRejected && order.cancellationReason.isNotBlank()) {
                Text(
                    text = "Reason: ${order.cancellationReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GharTakCoralError
                )
            }

            HorizontalDivider()

            // Actions
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onOpenRouteDialog,
                    modifier = Modifier.testTag("tracking_route_map_${order.id}")
                ) {
                    Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Route & Distance")
                }

                OutlinedButton(
                    onClick = onOpenDirectionsPath,
                    modifier = Modifier.testTag("tracking_dropoff_map_${order.id}")
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Map Path")
                }

                if (!isCancelledOrRejected && order.status != "Delivered" && hasApprovedRiders) {
                    OutlinedButton(
                        onClick = onAssignRider,
                        modifier = Modifier.testTag("tracking_assign_rider_${order.id}")
                    ) {
                        Text(if (order.riderId.isBlank()) "Assign Rider" else "Reassign")
                    }
                }

                if (nextStage != null) {
                    Button(
                        onClick = { onAdvanceStage(nextStage) },
                        modifier = Modifier.testTag("tracking_advance_${order.id}")
                    ) {
                        Text("Next: $nextStage")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                TextButton(
                    onClick = onChangeStatus,
                    modifier = Modifier.testTag("tracking_status_${order.id}")
                ) {
                    Text("All Stages")
                }
            }
        }
    }
}

@Composable
private fun AdminTrackingStepper(currentStatus: String) {
    if (currentStatus == "Cancelled" || currentStatus == "Rejected") {
        Surface(
            color = GharTakCoralError.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Order $currentStatus",
                color = GharTakCoralError,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        return
    }

    val currentIndex = TRACKING_PIPELINE.indexOf(currentStatus).coerceAtLeast(0)
    val targetFraction = (currentIndex + 1).toFloat() / TRACKING_PIPELINE.size.toFloat()
    val animatedFraction by animateFloatAsState(targetValue = targetFraction, label = "tracking_progress")
    val barColor = if (currentStatus == "Delivered") GharTakEmerald else GharTakPink

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(
            progress = { animatedFraction },
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
        )
        val milestones = listOf(
            "Placed" to 0,
            "Preparing" to 2,
            "Rider" to 4,
            "On Way" to 6,
            "Delivered" to 7
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            milestones.forEach { (label, stepIdx) ->
                val reached = currentIndex >= stepIdx
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                    color = if (reached) barColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdminRiderFleetCard(
    rider: RiderProfile,
    myLocation: Pair<Double, Double>?,
    activeOrders: List<OrderRecord>,
    onOpenRoutePath: () -> Unit,
    onInspectOrder: (OrderRecord) -> Unit
) {
    val myToRiderKm = remember(myLocation, rider.latitude, rider.longitude) {
        myLocation?.let { (lat, lng) -> calculateDistanceKm(lat, lng, rider.latitude, rider.longitude) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (activeOrders.isNotEmpty()) onInspectOrder(activeOrders.first()) else onOpenRoutePath()
            }
            .testTag("tracking_fleet_card_${rider.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (rider.isOnline) GharTakEmerald else Color.Gray)
                    )
                    Column {
                        Text(rider.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${rider.vehicleType} (${rider.vehicleNumber}) • ${rider.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(rider.approvalStatus)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (rider.isOnline) "Online • Live GPS Active" else "Offline • Last Known GPS",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (rider.isOnline) GharTakEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Coords: ${formatCoords(rider.latitude, rider.longitude)} • ${rider.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (myToRiderKm != null) {
                        Text(
                            text = "Distance from You: ${formatDistanceWithEta(myToRiderKm)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GharTakPink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Active Orders: ${activeOrders.size} • Completed: ${rider.completedDeliveries}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onOpenRoutePath,
                    modifier = Modifier.testTag("tracking_fleet_map_${rider.id}")
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Route Path")
                }
            }

            if (activeOrders.isNotEmpty()) {
                HorizontalDivider()
                activeOrders.forEach { order ->
                    val riderToOrderKm = calculateDistanceKm(rider.latitude, rider.longitude, order.deliveryLat, order.deliveryLng)
                    Text(
                        text = "• #${order.id.takeLast(6).uppercase()} ${order.vendorName} → ${order.customerName} (${formatDistanceLabel(riderToOrderKm)}) • Tap to track",
                        style = MaterialTheme.typography.labelMedium,
                        color = GharTakPink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInspectOrder(order) }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun nextTrackingStatus(current: String): String? {
    val idx = TRACKING_PIPELINE.indexOf(current)
    return if (idx in 0 until TRACKING_PIPELINE.lastIndex) TRACKING_PIPELINE[idx + 1] else null
}

private fun formatCoords(lat: Double, lng: Double): String =
    String.format(Locale.US, "%.4f, %.4f", lat, lng)
