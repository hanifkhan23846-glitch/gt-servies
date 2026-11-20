package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.OrderRecord
import com.example.data.model.RiderProfile
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GtNavyHeader
import com.example.ui.theme.GharTakPink
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private val MyLocationBlue = Color(0xFF2563EB)

/**
 * Computes real great-circle (Haversine) distance in kilometers between two GPS coordinates.
 */
fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    if (lat1.isNaN() || lng1.isNaN() || lat2.isNaN() || lng2.isNaN()) return 0.0
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val radLat1 = Math.toRadians(lat1)
    val radLat2 = Math.toRadians(lat2)
    val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
        cos(radLat1) * cos(radLat2) * sin(dLng / 2.0) * sin(dLng / 2.0)
    val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
    return (earthRadiusKm * c).coerceAtLeast(0.0)
}

fun formatDistanceLabel(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
        val meters = (distanceKm * 1000.0).roundToInt().coerceAtLeast(0)
        "$meters m"
    } else {
        String.format(Locale.US, "%.1f km", distanceKm)
    }
}

fun estimateTravelMinutes(distanceKm: Double, avgSpeedKmh: Double = 28.0): Int {
    if (distanceKm <= 0.03) return 1
    return ((distanceKm / avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)
}

fun formatDistanceWithEta(distanceKm: Double): String =
    "${formatDistanceLabel(distanceKm)} • ~${estimateTravelMinutes(distanceKm)} min"

fun buildGoogleMapsDirectionsUrl(
    originLat: Double?,
    originLng: Double?,
    destLat: Double,
    destLng: Double
): String {
    val base = "https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng&travelmode=driving"
    return if (originLat != null && originLng != null) {
        "$base&origin=$originLat,$originLng"
    } else {
        base
    }
}

fun openGoogleMapsDirections(
    context: Context,
    originLat: Double?,
    originLng: Double?,
    destLat: Double,
    destLng: Double
) {
    val uri = Uri.parse(buildGoogleMapsDirectionsUrl(originLat, originLng, destLat, destLng))
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    runCatching { context.startActivity(mapsIntent) }.onFailure {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        runCatching { context.startActivity(browserIntent) }
    }
}

@Stable
data class DeviceLocationState(
    val myLocation: Pair<Double, Double>?,
    val hasPermission: Boolean,
    val isFetching: Boolean,
    val refreshOrRequestLocation: () -> Unit
)

@SuppressLint("MissingPermission")
@Composable
fun rememberDeviceLocationState(): DeviceLocationState {
    val context = LocalContext.current
    val fusedClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }

    fun checkPerm(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    var hasPermission by remember { mutableStateOf(checkPerm()) }
    var myLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isFetching by remember { mutableStateOf(false) }

    fun fetchLocationNow() {
        if (!checkPerm()) return
        isFetching = true
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    myLocation = loc.latitude to loc.longitude
                    isFetching = false
                } else {
                    fusedClient.lastLocation
                        .addOnSuccessListener { last ->
                            if (last != null) {
                                myLocation = last.latitude to last.longitude
                            }
                            isFetching = false
                        }
                        .addOnFailureListener { isFetching = false }
                }
            }
            .addOnFailureListener {
                fusedClient.lastLocation
                    .addOnSuccessListener { last ->
                        if (last != null) myLocation = last.latitude to last.longitude
                        isFetching = false
                    }
                    .addOnFailureListener { isFetching = false }
            }
    }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasPermission = granted
        if (granted) {
            fetchLocationNow()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && myLocation == null) {
            fetchLocationNow()
        }
    }

    return remember(myLocation, hasPermission, isFetching) {
        DeviceLocationState(
            myLocation = myLocation,
            hasPermission = hasPermission,
            isFetching = isFetching,
            refreshOrRequestLocation = {
                if (checkPerm()) {
                    hasPermission = true
                    fetchLocationNow()
                } else {
                    permLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        )
    }
}

private data class MapPin(
    val id: String,
    val label: String,
    val lat: Double,
    val lng: Double,
    val color: Color,
    val isPulsing: Boolean = false
)

@Composable
fun TrackingRoutePathCanvas(
    dropoffLat: Double,
    dropoffLng: Double,
    dropoffLabel: String,
    riderLat: Double? = null,
    riderLng: Double? = null,
    riderLabel: String = "Rider",
    riderOnline: Boolean = false,
    myLat: Double? = null,
    myLng: Double? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pins = remember(dropoffLat, dropoffLng, dropoffLabel, riderLat, riderLng, riderLabel, riderOnline, myLat, myLng) {
        buildList {
            if (myLat != null && myLng != null) {
                add(MapPin("me", "You", myLat, myLng, MyLocationBlue, isPulsing = true))
            }
            if (riderLat != null && riderLng != null) {
                add(MapPin("rider", riderLabel, riderLat, riderLng, GharTakEmerald, isPulsing = riderOnline))
            }
            add(MapPin("dropoff", dropoffLabel, dropoffLat, dropoffLng, GtNavyHeader, isPulsing = false))
        }
    }

    val surfaceBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val roadColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceBg)
            .testTag("tracking_route_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle street-grid lines for map spatial context
            val cols = 6
            val rows = 5
            for (i in 1 until cols) {
                val x = size.width * (i.toFloat() / cols)
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
            }
            for (j in 1 until rows) {
                val y = size.height * (j.toFloat() / rows)
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
            }
            // Diagonal arterial guideline
            drawLine(
                roadColor,
                Offset(0f, size.height * 0.78f),
                Offset(size.width, size.height * 0.22f),
                strokeWidth = 3f
            )

            val padX = size.width * 0.16f
            val padY = size.height * 0.22f
            val usableW = (size.width - 2 * padX).coerceAtLeast(1f)
            val usableH = (size.height - 2 * padY).coerceAtLeast(1f)

            val minLat = pins.minOf { it.lat }
            val maxLat = pins.maxOf { it.lat }
            val minLng = pins.minOf { it.lng }
            val maxLng = pins.maxOf { it.lng }
            val spanLat = (maxLat - minLat).takeIf { it > 0.0005 } ?: 0.01
            val spanLng = (maxLng - minLng).takeIf { it > 0.0005 } ?: 0.01

            fun toOffset(index: Int, pin: MapPin): Offset {
                if (pins.size == 1) {
                    return Offset(size.width / 2f, size.height / 2f)
                }
                val identicalCoords = (maxLat - minLat) <= 0.0001 && (maxLng - minLng) <= 0.0001
                if (identicalCoords) {
                    val frac = index.toFloat() / (pins.size - 1).coerceAtLeast(1)
                    return Offset(padX + frac * usableW, padY + (1f - frac * 0.5f) * usableH * 0.75f)
                }
                val normX = ((pin.lng - minLng) / spanLng).toFloat().coerceIn(0f, 1f)
                val normY = (1f - ((pin.lat - minLat) / spanLat).toFloat()).coerceIn(0f, 1f)
                return Offset(padX + normX * usableW, padY + normY * usableH)
            }

            val offsets = pins.mapIndexed { idx, p -> p.id to toOffset(idx, p) }.toMap()
            val dropoffOffset = offsets["dropoff"]
            val riderOffset = offsets["rider"]
            val myOffset = offsets["me"]

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)

            // Route Path 1: My GPS -> Dropoff (dashed blue)
            if (myOffset != null && dropoffOffset != null) {
                drawLine(
                    color = MyLocationBlue.copy(alpha = 0.75f),
                    start = myOffset,
                    end = dropoffOffset,
                    strokeWidth = 5f,
                    pathEffect = dashEffect,
                    cap = StrokeCap.Round
                )
            }

            // Route Path 2: My GPS -> Rider (dashed amber if rider present)
            if (myOffset != null && riderOffset != null) {
                drawLine(
                    color = GharTakAmber.copy(alpha = 0.75f),
                    start = myOffset,
                    end = riderOffset,
                    strokeWidth = 4.5f,
                    pathEffect = dashEffect,
                    cap = StrokeCap.Round
                )
            }

            // Route Path 3: Rider -> Dropoff (primary glowing route line)
            if (riderOffset != null && dropoffOffset != null) {
                drawLine(
                    color = GharTakEmerald.copy(alpha = 0.28f),
                    start = riderOffset,
                    end = dropoffOffset,
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = GharTakEmerald,
                    start = riderOffset,
                    end = dropoffOffset,
                    strokeWidth = 6.5f,
                    cap = StrokeCap.Round
                )
            }

            // Draw Markers
            pins.forEach { pin ->
                val center = offsets[pin.id] ?: return@forEach
                if (pin.isPulsing) {
                    drawCircle(
                        color = pin.color.copy(alpha = 0.22f),
                        radius = 16.dp.toPx() * pulseScale * 0.75f,
                        center = center
                    )
                }
                drawCircle(color = Color.White, radius = 12.dp.toPx(), center = center)
                drawCircle(color = pin.color, radius = 9.dp.toPx(), center = center)
            }
        }

        // Top-left live route legend pills
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (myLat != null && myLng != null) {
                MapLegendChip("You", MyLocationBlue)
            }
            if (riderLat != null && riderLng != null) {
                MapLegendChip("Rider", GharTakEmerald)
            }
            MapLegendChip("Address", GtNavyHeader)
        }

        // Bottom distance pill inside map
        val primaryRouteKm = when {
            riderLat != null && riderLng != null -> calculateDistanceKm(riderLat, riderLng, dropoffLat, dropoffLng)
            myLat != null && myLng != null -> calculateDistanceKm(myLat, myLng, dropoffLat, dropoffLng)
            else -> null
        }
        if (primaryRouteKm != null) {
            val prefix = if (riderLat != null) "Rider → Dropoff" else "You → Dropoff"
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(999.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Route, contentDescription = null, tint = GharTakEmerald, modifier = Modifier.size(15.dp))
                    Text(
                        text = "$prefix: ${formatDistanceWithEta(primaryRouteKm)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLegendChip(label: String, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackingRouteAndDistanceDialog(
    order: OrderRecord,
    matchedRider: RiderProfile?,
    locationState: DeviceLocationState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val myLoc = locationState.myLocation
    val riderLat = matchedRider?.latitude
    val riderLng = matchedRider?.longitude

    val riderToDropoffKm = remember(riderLat, riderLng, order.deliveryLat, order.deliveryLng) {
        if (riderLat != null && riderLng != null) {
            calculateDistanceKm(riderLat, riderLng, order.deliveryLat, order.deliveryLng)
        } else null
    }
    val myToDropoffKm = remember(myLoc, order.deliveryLat, order.deliveryLng) {
        myLoc?.let { (lat, lng) -> calculateDistanceKm(lat, lng, order.deliveryLat, order.deliveryLng) }
    }
    val myToRiderKm = remember(myLoc, riderLat, riderLng) {
        if (myLoc != null && riderLat != null && riderLng != null) {
            calculateDistanceKm(myLoc.first, myLoc.second, riderLat, riderLng)
        } else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tracking_route_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Route & Distance • #${order.id.takeLast(6).uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${order.vendorName} • ${order.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("route_dialog_close")) {
                        Icon(Icons.Filled.Close, contentDescription = "Close route view")
                    }
                }

                // Live Route Map Visualization
                TrackingRoutePathCanvas(
                    dropoffLat = order.deliveryLat,
                    dropoffLng = order.deliveryLng,
                    dropoffLabel = order.customerName,
                    riderLat = riderLat,
                    riderLng = riderLng,
                    riderLabel = matchedRider?.fullName ?: "Rider",
                    riderOnline = matchedRider?.isOnline ?: false,
                    myLat = myLoc?.first,
                    myLng = myLoc?.second
                )

                // Delivery Address Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GtNavyHeader, modifier = Modifier.size(18.dp))
                            Text("Delivery Address", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${order.customerName} (${order.customerPhone})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${order.deliveryAddress}, ${order.city}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Coordinates: ${String.format(Locale.US, "%.4f, %.4f", order.deliveryLat, order.deliveryLng)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Distance Breakdown Section
                Surface(
                    color = GharTakEmerald.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Live Distances & ETA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                        if (riderToDropoffKm != null) {
                            val riderDisplay = matchedRider?.fullName?.ifBlank { null }
                                ?: order.riderName.ifBlank { "Rider" }
                            DistanceMetricRow(
                                icon = Icons.Filled.DeliveryDining,
                                iconColor = GharTakEmerald,
                                label = "Rider ($riderDisplay) → Address",
                                value = formatDistanceWithEta(riderToDropoffKm)
                            )
                        } else if (order.riderName.isNotBlank()) {
                            Text(
                                text = "Assigned Rider: ${order.riderName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (myToDropoffKm != null) {
                            DistanceMetricRow(
                                icon = Icons.Filled.MyLocation,
                                iconColor = MyLocationBlue,
                                label = "My Location → Address",
                                value = formatDistanceWithEta(myToDropoffKm)
                            )
                        }

                        if (myToRiderKm != null) {
                            DistanceMetricRow(
                                icon = Icons.Filled.NearMe,
                                iconColor = GharTakAmber,
                                label = "My Location → Rider",
                                value = formatDistanceWithEta(myToRiderKm)
                            )
                        }

                        if (myLoc == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (locationState.isFetching) "Getting your GPS..." else "Enable GPS for distance from you",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { locationState.refreshOrRequestLocation() },
                                    modifier = Modifier.testTag("route_enable_my_gps")
                                ) {
                                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Use My GPS")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Google Maps Directions Launchers (draws full road path)
                Text("Open Turn-by-Turn Path in Google Maps", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (riderLat != null && riderLng != null) {
                        Button(
                            onClick = {
                                openGoogleMapsDirections(context, riderLat, riderLng, order.deliveryLat, order.deliveryLng)
                            },
                            modifier = Modifier.testTag("route_gmaps_rider_to_dropoff")
                        ) {
                            Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Rider → Address")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            openGoogleMapsDirections(context, myLoc?.first, myLoc?.second, order.deliveryLat, order.deliveryLng)
                        },
                        modifier = Modifier.testTag("route_gmaps_my_to_dropoff")
                    ) {
                        Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("My Location → Address")
                    }
                    if (riderLat != null && riderLng != null) {
                        OutlinedButton(
                            onClick = {
                                openGoogleMapsDirections(context, myLoc?.first, myLoc?.second, riderLat, riderLng)
                            },
                            modifier = Modifier.testTag("route_gmaps_my_to_rider")
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("My Location → Rider")
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun DistanceMetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = iconColor
        )
    }
}
