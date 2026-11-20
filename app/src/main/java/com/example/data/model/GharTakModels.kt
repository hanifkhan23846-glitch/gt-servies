package com.example.data.model

import android.util.Base64
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.security.MessageDigest
import java.util.Calendar

object GharTakConstants {
    const val APP_NAME = "GT Service"
    const val BOOTSTRAPPED_ADMIN_EMAIL = "hanifkhan23846@gmail.com"
    const val ADMIN_LOGIN_EMAIL = "admin"
    const val ADMIN_LOGIN_PASSWORD = "admin123"
    const val LEGACY_ADMIN_KEY_HASH = "bf6b5bdb74c79ece9fc0ad0ac9fb0359f9555d4f35a83b2e6ec69ae99e09603d"
    const val DEFAULT_CITY = "Bahawalpur"
    const val ALL_CITIES = "All Cities"
    const val COUNTRY_CODE = "+92"
    const val CURRENCY = "PKR"
    const val SUPPORT_PHONE_LOCAL = "03036767152"
    const val SUPPORT_PHONE_E164 = "+923036767152"
    const val SUPPORT_WHATSAPP_NUMBER = "923036767152"
    const val SUPPORT_DISPLAY_NAME = "GT Service Support"
    const val DEFAULT_JAZZCASH_NUMBER = "03298580188"
    const val DEFAULT_EASYPAISA_NUMBER = "03298580188"
    const val DEFAULT_BENEFICIARY_TITLE = "GT Service"

    fun toWhatsappDigits(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.startsWith("92") && digits.length >= 11 -> digits
            digits.startsWith("0") && digits.length >= 10 -> "92" + digits.drop(1)
            digits.length >= 10 -> digits
            else -> SUPPORT_WHATSAPP_NUMBER
        }
    }

    fun sha256Hex(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun normalizeCredentialKey(loginId: String, password: String): String {
        val normalizedId = loginId.trim().lowercase()
        val effectiveId = if (normalizedId == BOOTSTRAPPED_ADMIN_EMAIL) ADMIN_LOGIN_EMAIL else normalizedId
        return "$effectiveId:$password"
    }

    fun isValidAdminCredentials(email: String, password: String): Boolean {
        val clean = email.trim()
        return (clean.equals(ADMIN_LOGIN_EMAIL, ignoreCase = true) ||
            clean.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)) &&
            password == ADMIN_LOGIN_PASSWORD
    }

    val INITIAL_PAKISTAN_CITIES = listOf(
        "Bahawalpur",
        "Karachi",
        "Lahore",
        "Islamabad",
        "Rawalpindi",
        "Faisalabad",
        "Multan",
        "Peshawar",
        "Quetta",
        "Sialkot",
        "Gujranwala",
        "Hyderabad",
        "Sukkur",
        "Abbottabad",
        "Sargodha",
        "Rahim Yar Khan",
        "Gujrat",
        "Sahiwal",
        "Mardan",
        "Larkana"
    )

    val FOOD_CATEGORIES = listOf(
        "Pizza",
        "Burger",
        "Biryani",
        "BBQ",
        "Chinese",
        "Fast Food",
        "Pakistani Food",
        "Shawarma",
        "Desserts",
        "Drinks"
    )

    val GROCERY_CATEGORIES = listOf(
        "Fruits",
        "Vegetables",
        "Dairy Products",
        "Milk and Eggs",
        "Meat",
        "Chicken",
        "Fish",
        "Rice and Flour",
        "Cooking Oil and Ghee",
        "Spices",
        "Bakery Items",
        "Beverages",
        "Snacks",
        "Frozen Foods",
        "Personal Care",
        "Household Cleaning",
        "Baby Products",
        "Breakfast Items"
    )

    val GROCERY_UNITS = listOf(
        "1 kg",
        "500 gram",
        "250 gram",
        "1 litre",
        "500 ml",
        "250 ml",
        "1 packet",
        "1 piece"
    )

    val MEDICAL_CATEGORIES = listOf(
        "Tablets & Capsules",
        "Syrups & Drops",
        "First Aid & Bandages",
        "Pain & Fever",
        "Vitamins & Supplements",
        "Baby & Mother Care",
        "Diabetes Care",
        "Heart & BP Care",
        "Skin & Derma",
        "Surgical & Devices"
    )

    val MEDICAL_UNITS = listOf(
        "1 strip",
        "1 box",
        "1 bottle",
        "1 pack",
        "1 tube",
        "1 piece",
        "60 ml",
        "120 ml"
    )

    val DAYS_OF_WEEK = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val MULTAN_CATEGORIES = listOf(
        "Cookies & Bakery",
        "Fast Food",
        "Sohan Halwa & Sweets",
        "Restaurants & BBQ",
        "Drinks & Cafes",
        "Gifts & Specials"
    )

    val MULTAN_BRANDS = listOf(
        "Crumble",
        "KFC",
        "Hafiz Sohan Halwa",
        "Rewari Sweets",
        "Zanzibar",
        "Multan Special"
    )

    val CELEBRATION_CATEGORIES = listOf(
        "Birthday Cakes",
        "Flowers & Bouquets",
        "Balloons & Decor",
        "Gift Boxes",
        "Party Combos",
        "Anniversary Specials"
    )

    fun todayDayKey(calendar: Calendar = Calendar.getInstance()): String = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        Calendar.SUNDAY -> "Sun"
        else -> "Mon"
    }

    fun parseMinutesOfDay(hhmm: String): Int? {
        val parts = hhmm.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    fun isWithinOperatingHours(
        isOpen: Boolean,
        openingTime: String,
        closingTime: String,
        calendar: Calendar = Calendar.getInstance()
    ): Boolean {
        if (!isOpen) return false
        val openMin = parseMinutesOfDay(openingTime) ?: return true
        val closeMin = parseMinutesOfDay(closingTime) ?: return true
        if ((openMin == 0 && closeMin >= 23 * 60 + 59) || openMin == closeMin) return true
        val nowMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        return if (openMin < closeMin) {
            nowMin in openMin..closeMin
        } else {
            // Cross-midnight window e.g. 18:00 to 02:00
            nowMin >= openMin || nowMin <= closeMin
        }
    }

    fun formatTime12h(hhmm: String): String {
        val mins = parseMinutesOfDay(hhmm) ?: return hhmm.ifBlank { "12:00 AM" }
        val h24 = mins / 60
        val m = mins % 60
        val amPm = if (h24 < 12) "AM" else "PM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return "%d:%02d %s".format(h12, m, amPm)
    }

    fun formatOperatingHours(openingTime: String, closingTime: String): String {
        val openMin = parseMinutesOfDay(openingTime)
        val closeMin = parseMinutesOfDay(closingTime)
        if (openMin == null || closeMin == null || (openMin == 0 && closeMin >= 23 * 60 + 59) || openMin == closeMin) {
            return "Open 24 Hours"
        }
        return "${formatTime12h(openingTime)} – ${formatTime12h(closingTime)}"
    }

    fun calculateDiscountPercent(regularPricePkr: Double, discountPricePkr: Double): Int {
        if (regularPricePkr <= 0.0 || discountPricePkr <= 0.0 || discountPricePkr >= regularPricePkr) return 0
        return (((regularPricePkr - discountPricePkr) / regularPricePkr) * 100.0).toInt().coerceIn(1, 99)
    }

    val ORDER_STATUSES = listOf(
        "Pending",
        "Accepted",
        "Preparing",
        "Ready for Pickup",
        "Rider Assigned",
        "Picked Up",
        "On the Way",
        "Delivered",
        "Cancelled",
        "Rejected"
    )

    val TERMINAL_ORDER_STATUSES = setOf("Delivered", "Cancelled", "Rejected")
    val CANCELLABLE_ORDER_STATUSES = setOf("Pending", "Accepted")

    fun cityHubCoordinates(city: String): Pair<Double, Double> = when (city.trim().lowercase()) {
        "bahawalpur" -> 29.3956 to 71.6836
        "multan" -> 30.1575 to 71.5249
        "lahore" -> 31.5204 to 74.3587
        "karachi" -> 24.8607 to 67.0011
        "islamabad" -> 33.6844 to 73.0479
        "rawalpindi" -> 33.5651 to 73.0169
        "faisalabad" -> 31.4504 to 73.1350
        "peshawar" -> 34.0151 to 71.5249
        "quetta" -> 30.1798 to 66.9750
        "sialkot" -> 32.4945 to 74.5229
        "gujranwala" -> 32.1877 to 74.1945
        "hyderabad" -> 25.3960 to 68.3578
        "sukkur" -> 27.7131 to 68.8492
        "abbottabad" -> 34.1688 to 73.2215
        "sargodha" -> 32.0740 to 72.6861
        "rahim yar khan" -> 28.4212 to 70.2989
        "gujrat" -> 32.5731 to 74.0750
        "sahiwal" -> 30.6682 to 73.1114
        "mardan" -> 34.1986 to 72.0404
        "larkana" -> 27.5590 to 68.2120
        else -> 29.3956 to 71.6836
    }

    fun formatPkr(amount: Double): String {
        val rounded = amount.toLong()
        return if (amount == rounded.toDouble()) {
            "PKR %,d".format(rounded)
        } else {
            "PKR %,.0f".format(amount)
        }
    }
}

enum class AppPortal(val key: String, val label: String) {
    CUSTOMER("customer", "Customer"),
    RESTAURANT("restaurant", "Restaurant"),
    GROCERY("grocery", "Grocery Store"),
    RIDER("rider", "Rider"),
    ADMIN("admin", "Super Admin");

    companion object {
        fun fromKey(key: String): AppPortal = entries.firstOrNull { it.key == key } ?: CUSTOMER
    }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val activePortal: String = AppPortal.CUSTOMER.key,
    val avatarUrl: String? = null,
    val walletBalancePkr: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "topup", // "topup", "payment", "refund", "admin_credit"
    val amountPkr: Double = 0.0,
    val balanceAfterPkr: Double = 0.0,
    val paymentChannel: String = "JazzCash",
    val referenceId: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DeliveryAddress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val label: String = "Home",
    val houseNumber: String = "",
    val street: String = "",
    val area: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val latitude: Double = 29.3956,
    val longitude: Double = 71.6836,
    val isDefault: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun formattedLine(): String = listOf(houseNumber, street, area, city)
        .filter { it.isNotBlank() }
        .joinToString(", ")
}

@IgnoreExtraProperties
data class FavoriteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val targetId: String = "",
    val targetType: String = "restaurant",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "order",
    val orderId: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Restaurant(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val imageUrl: String? = null,
    val description: String? = null,
    val deliveryTimeMinutes: Long = 30L,
    val deliveryFeePkr: Double = 80.0,
    val minOrderPkr: Double = 300.0,
    val rating: Double = 4.8,
    val reviewCount: Long = 0L,
    val approvalStatus: String = "pending",
    val isOpen: Boolean = true,
    val openingTime: String = "00:00",
    val closingTime: String = "23:59",
    val commissionPercent: Double = 15.0,
    val portalLoginId: String = "",
    val portalPassHash: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isCurrentlyOpenNow(): Boolean =
        GharTakConstants.isWithinOperatingHours(isOpen, openingTime, closingTime)

    fun operatingHoursLabel(): String =
        GharTakConstants.formatOperatingHours(openingTime, closingTime)
}

@IgnoreExtraProperties
data class Category(
    @DocumentId val id: String = "",
    val name: String = "",
    val iconKey: String = "restaurant",
    val isActive: Boolean = true,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FoodItem(
    @DocumentId val id: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val ownerId: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val name: String = "",
    val description: String = "",
    val regularPricePkr: Double = 0.0,
    val discountPricePkr: Double = 0.0,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
    val preparationTimeMinutes: Long = 20L,
    val stockQuantity: Long = 50L,
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val availableDays: List<String> = emptyList(),
    @get:PropertyName("isDailySpecial") @PropertyName("isDailySpecial")
    val isDailySpecial: Boolean = false,
    val city: String = GharTakConstants.DEFAULT_CITY,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectivePricePkr: Double
        get() = if (discountPricePkr > 0.0 && discountPricePkr < regularPricePkr) discountPricePkr else regularPricePkr

    val discountPercent: Int
        get() = GharTakConstants.calculateDiscountPercent(regularPricePkr, discountPricePkr)

    val allImages: List<String>
        get() = (listOfNotNull(imageUrl?.takeIf { it.isNotBlank() }) + additionalImages.filter { it.isNotBlank() }).distinct()

    fun isAvailableOnDay(dayKey: String = GharTakConstants.todayDayKey()): Boolean =
        availableDays.isEmpty() || availableDays.any { it.equals(dayKey, ignoreCase = true) }

    fun dayScheduleLabel(): String =
        if (availableDays.isEmpty()) "All Days" else availableDays.joinToString(", ")
}

@IgnoreExtraProperties
data class GroceryStore(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val imageUrl: String? = null,
    val description: String? = null,
    val deliveryTimeMinutes: Long = 25L,
    val deliveryFeePkr: Double = 70.0,
    val minOrderPkr: Double = 400.0,
    val rating: Double = 4.8,
    val reviewCount: Long = 0L,
    val approvalStatus: String = "pending",
    val isOpen: Boolean = true,
    val openingTime: String = "00:00",
    val closingTime: String = "23:59",
    val commissionPercent: Double = 10.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isCurrentlyOpenNow(): Boolean =
        GharTakConstants.isWithinOperatingHours(isOpen, openingTime, closingTime)

    fun operatingHoursLabel(): String =
        GharTakConstants.formatOperatingHours(openingTime, closingTime)
}

@IgnoreExtraProperties
data class GroceryProduct(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val ownerId: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val name: String = "",
    val description: String = "",
    val regularPricePkr: Double = 0.0,
    val discountPricePkr: Double = 0.0,
    val unit: String = "1 kg",
    val stockQuantity: Long = 20L,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val city: String = GharTakConstants.DEFAULT_CITY,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectivePricePkr: Double
        get() = if (discountPricePkr > 0.0 && discountPricePkr < regularPricePkr) discountPricePkr else regularPricePkr

    val discountPercent: Int
        get() = GharTakConstants.calculateDiscountPercent(regularPricePkr, discountPricePkr)

    val allImages: List<String>
        get() = (listOfNotNull(imageUrl?.takeIf { it.isNotBlank() }) + additionalImages.filter { it.isNotBlank() }).distinct()
}

@IgnoreExtraProperties
data class MultanItem(
    @DocumentId val id: String = "",
    val brandName: String = "Multan Special",
    val categoryName: String = "Cookies & Bakery",
    val name: String = "",
    val description: String = "",
    val regularPricePkr: Double = 0.0,
    val discountPricePkr: Double = 0.0,
    val unit: String = "1 box",
    val stockQuantity: Long = 30L,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
    val originCity: String = "Multan",
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val city: String = GharTakConstants.ALL_CITIES,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectivePricePkr: Double
        get() = if (discountPricePkr > 0.0 && discountPricePkr < regularPricePkr) discountPricePkr else regularPricePkr

    val discountPercent: Int
        get() = GharTakConstants.calculateDiscountPercent(regularPricePkr, discountPricePkr)

    val allImages: List<String>
        get() = (listOfNotNull(imageUrl?.takeIf { it.isNotBlank() }) + additionalImages.filter { it.isNotBlank() }).distinct()
}

@IgnoreExtraProperties
data class CelebrationItem(
    @DocumentId val id: String = "",
    val categoryName: String = "Birthday Cakes",
    val name: String = "",
    val description: String = "",
    val regularPricePkr: Double = 0.0,
    val discountPricePkr: Double = 0.0,
    val unit: String = "1 item",
    val stockQuantity: Long = 30L,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val city: String = GharTakConstants.ALL_CITIES,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectivePricePkr: Double
        get() = if (discountPricePkr > 0.0 && discountPricePkr < regularPricePkr) discountPricePkr else regularPricePkr

    val discountPercent: Int
        get() = GharTakConstants.calculateDiscountPercent(regularPricePkr, discountPricePkr)

    val allImages: List<String>
        get() = (listOfNotNull(imageUrl?.takeIf { it.isNotBlank() }) + additionalImages.filter { it.isNotBlank() }).distinct()
}

@IgnoreExtraProperties
data class MedicalStore(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val imageUrl: String? = null,
    val description: String? = null,
    val deliveryTimeMinutes: Long = 20L,
    val deliveryFeePkr: Double = 60.0,
    val minOrderPkr: Double = 250.0,
    val rating: Double = 4.9,
    val reviewCount: Long = 0L,
    val approvalStatus: String = "pending",
    val isOpen: Boolean = true,
    val openingTime: String = "00:00",
    val closingTime: String = "23:59",
    val commissionPercent: Double = 10.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isCurrentlyOpenNow(): Boolean =
        GharTakConstants.isWithinOperatingHours(isOpen, openingTime, closingTime)

    fun operatingHoursLabel(): String =
        GharTakConstants.formatOperatingHours(openingTime, closingTime)
}

@IgnoreExtraProperties
data class MedicalProduct(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val ownerId: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val name: String = "",
    val description: String = "",
    val regularPricePkr: Double = 0.0,
    val discountPricePkr: Double = 0.0,
    val unit: String = "1 strip",
    val stockQuantity: Long = 30L,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val city: String = GharTakConstants.DEFAULT_CITY,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectivePricePkr: Double
        get() = if (discountPricePkr > 0.0 && discountPricePkr < regularPricePkr) discountPricePkr else regularPricePkr

    val discountPercent: Int
        get() = GharTakConstants.calculateDiscountPercent(regularPricePkr, discountPricePkr)

    val allImages: List<String>
        get() = (listOfNotNull(imageUrl?.takeIf { it.isNotBlank() }) + additionalImages.filter { it.isNotBlank() }).distinct()
}

@IgnoreExtraProperties
data class ParcelRiderRequest(
    @DocumentId val id: String = "",
    val requestType: String = "send_parcel", // "send_parcel" or "hire_rider"
    val requesterId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val recipientName: String = "",
    val recipientPhone: String = "",
    val details: String = "",
    val offeredFeePkr: Double = 150.0,
    val approvalStatus: String = "pending", // "pending", "approved", "rejected", "rider_assigned", "picked_up", "completed"
    val riderId: String = "",
    val riderName: String = "",
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RiderProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val cnicNumber: String = "",
    val licenseNumber: String = "",
    val vehicleType: String = "Motorbike",
    val vehicleNumber: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val approvalStatus: String = "pending",
    val isOnline: Boolean = false,
    val latitude: Double = 29.3956,
    val longitude: Double = 71.6836,
    val totalEarningsPkr: Double = 0.0,
    val completedDeliveries: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class CartItem(
    val productId: String,
    val name: String,
    val vendorId: String,
    val vendorName: String,
    val vendorOwnerId: String,
    val orderType: String, // "food", "grocery", or "medical"
    val unitPricePkr: Double,
    val unit: String,
    val quantity: Int,
    val imageUrl: String? = null,
    val customizationNote: String = "",
    val baseUnitPricePkr: Double = unitPricePkr,
    val cartLineId: String = java.util.UUID.randomUUID().toString()
)

@IgnoreExtraProperties
data class OrderLineItem(
    val productId: String = "",
    val name: String = "",
    val quantity: Long = 1L,
    val unitPricePkr: Double = 0.0,
    val unit: String = "item"
)

@IgnoreExtraProperties
data class OrderRecord(
    @DocumentId val id: String = "",
    val orderType: String = "food",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val vendorOwnerId: String = "",
    val riderId: String = "",
    val riderName: String = "",
    val deliveryAddress: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val deliveryLat: Double = 29.3956,
    val deliveryLng: Double = 71.6836,
    val items: List<OrderLineItem> = emptyList(),
    val subtotalPkr: Double = 0.0,
    val deliveryFeePkr: Double = 0.0,
    val discountPkr: Double = 0.0,
    val couponCode: String = "",
    val totalPkr: Double = 0.0,
    val commissionPkr: Double = 0.0,
    val vendorEarningsPkr: Double = 0.0,
    val riderEarningsPkr: Double = 0.0,
    val paymentMethod: String = "COD",
    val paymentStatus: String = "pending",
    val status: String = "Pending",
    val cancellationReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ReviewRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val orderId: String = "",
    val targetId: String = "",
    val targetType: String = "restaurant",
    val targetName: String = "",
    val rating: Double = 5.0,
    val comment: String = "",
    val isHidden: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CouponRecord(
    @DocumentId val id: String = "",
    val code: String = "",
    val discountType: String = "percentage",
    val discountValue: Double = 10.0,
    val minOrderPkr: Double = 500.0,
    val maxDiscountPkr: Double = 300.0,
    val usageLimit: Long = 100L,
    val usedCount: Long = 0L,
    val isActive: Boolean = true,
    val expiresAtEpochMs: Long = 1900000000000L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BeneficiaryAccount(
    val id: String = "",
    val channel: String = "",
    val accountTitle: String = "",
    val accountNumber: String = ""
)

@IgnoreExtraProperties
data class PromoOffer(
    val id: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val code: String = "",
    val badgeText: String = "",
    val title: String = "",
    val subtitle: String = "",
    val discountPercent: Int = 20
)

@IgnoreExtraProperties
data class CityApprovalRequest(
    @DocumentId val id: String = "",
    val cityName: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DeliveryAreaFeeRule(
    val id: String = "",
    val city: String = GharTakConstants.DEFAULT_CITY,
    val areaName: String = "",
    val feePkr: Double = 150.0,
    val maxDistanceKm: Double = 5.0
)

@IgnoreExtraProperties
data class MultanCityFeeRule(
    val city: String = "",
    val feePkr: Double = 350.0
)

@IgnoreExtraProperties
data class PlatformSettings(
    @DocumentId val id: String = "",
    val cities: List<String> = GharTakConstants.INITIAL_PAKISTAN_CITIES,
    val restaurantCommissionPercent: Double = 15.0,
    val groceryCommissionPercent: Double = 10.0,
    val baseDeliveryFeePkr: Double = 80.0,
    val riderPayoutPkr: Double = 70.0,
    val jazzCashConfigured: Boolean = true,
    val easypaisaConfigured: Boolean = true,
    val jazzCashNumber: String = GharTakConstants.DEFAULT_JAZZCASH_NUMBER,
    val jazzCashAccountTitle: String = GharTakConstants.DEFAULT_BENEFICIARY_TITLE,
    val easypaisaNumber: String = GharTakConstants.DEFAULT_EASYPAISA_NUMBER,
    val easypaisaAccountTitle: String = GharTakConstants.DEFAULT_BENEFICIARY_TITLE,
    val beneficiaryAccounts: List<BeneficiaryAccount> = emptyList(),
    val promoBannerEnabled: Boolean = true,
    val promoCode: String = "GHARTAK20",
    val promoDiscountPercent: Int = 20,
    val promoBadgeText: String = "USE CODE: GHARTAK20",
    val promoTitle: String = "20% OFF Food & Groceries",
    val promoSubtitle: String = "Your Favourite Food, Delivered to Your Door!",
    val promoRestaurantId: String = "",
    val promoRestaurantName: String = "",
    val promoOffers: List<PromoOffer> = emptyList(),
    val deliveryAreaFeeRules: List<DeliveryAreaFeeRule> = emptyList(),
    val multanCityDeliveryFees: List<MultanCityFeeRule> = emptyList(),
    val multanDefaultDeliveryFeePkr: Double = 350.0,
    val radiusPricingEnabled: Boolean = true,
    val deliveryBaseRadiusKm: Double = 5.0,
    val withinRadiusFeePkr: Double = 100.0,
    val extraPerKmFeePkr: Double = 25.0,
    val supportPhoneLocal: String = GharTakConstants.SUPPORT_PHONE_LOCAL,
    val supportWhatsappNumber: String = GharTakConstants.SUPPORT_WHATSAPP_NUMBER,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RestaurantCredentialRecord(
    @DocumentId val id: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val loginId: String = "",
    val password: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RestaurantAccessRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val loginId: String = "",
    val loginKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminAuthConfig(
    @DocumentId val id: String = "",
    val loginEmail: String = GharTakConstants.ADMIN_LOGIN_EMAIL,
    val adminKeyHash: String = GharTakConstants.LEGACY_ADMIN_KEY_HASH,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminActivityLog(
    @DocumentId val id: String = "",
    val adminId: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SupportChatThread(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val supportPhone: String = GharTakConstants.SUPPORT_PHONE_LOCAL,
    val lastMessage: String = "",
    val lastSenderRole: String = "customer",
    val unreadByAdmin: Long = 0L,
    val unreadByCustomer: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SupportChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "customer",
    val text: String = "",
    val orderId: String = "",
    val supportPhone: String = GharTakConstants.SUPPORT_PHONE_LOCAL,
    val imageUrl: String? = null,
    val audioBase64: String? = null,
    val audioDurationSec: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)
