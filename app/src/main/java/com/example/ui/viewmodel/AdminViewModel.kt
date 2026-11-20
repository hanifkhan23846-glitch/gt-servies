package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminActivityLog
import com.example.data.model.AdminAuthConfig
import com.example.data.model.Category
import com.example.data.model.CelebrationItem
import com.example.data.model.CityApprovalRequest
import com.example.data.model.CouponRecord
import com.example.data.model.FoodItem
import com.example.data.model.GharTakConstants
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.MedicalProduct
import com.example.data.model.MedicalStore
import com.example.data.model.MultanItem
import com.example.data.model.OrderRecord
import com.example.data.model.ParcelRiderRequest
import com.example.data.model.PlatformSettings
import com.example.data.model.PromoOffer
import com.example.data.model.Restaurant
import com.example.data.model.RestaurantCredentialRecord
import com.example.data.model.ReviewRecord
import com.example.data.model.RiderProfile
import com.example.data.model.SupportChatMessage
import com.example.data.model.SupportChatThread
import com.example.data.model.UserProfile
import com.example.data.remote.CatalogSeedHelper
import com.example.data.remote.GharTakRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val db: FirebaseFirestore,
    private val repository: GharTakRepository,
    val adminId: String,
    val adminEmail: String
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val users: StateFlow<List<UserProfile>> = repository.observeAllUsersAdmin()
        .catch { Log.w(TAG, "users error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val restaurants: StateFlow<List<Restaurant>> = repository.observeAllRestaurantsAdmin()
        .map { it.sortedByDescending { r -> r.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "restaurants error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val foodItems: StateFlow<List<FoodItem>> = repository.observeAllFoodItemsAdmin()
        .map { it.sortedByDescending { f -> f.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "foodItems error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val foodCategories: StateFlow<List<Category>> = repository.observeAllFoodCategoriesAdmin()
        .map { it.sortedBy { c -> c.sortOrder } }
        .catch { Log.w(TAG, "foodCategories error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val groceryStores: StateFlow<List<GroceryStore>> = repository.observeAllGroceryStoresAdmin()
        .map { it.sortedByDescending { s -> s.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "groceryStores error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val groceryProducts: StateFlow<List<GroceryProduct>> = repository.observeAllGroceryProductsAdmin()
        .map { it.sortedByDescending { g -> g.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "groceryProducts error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val groceryCategories: StateFlow<List<Category>> = repository.observeAllGroceryCategoriesAdmin()
        .map { it.sortedBy { c -> c.sortOrder } }
        .catch { Log.w(TAG, "groceryCategories error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val medicalStores: StateFlow<List<MedicalStore>> = repository.observeAllMedicalStoresAdmin()
        .map { it.sortedByDescending { s -> s.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "medicalStores error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val medicalProducts: StateFlow<List<MedicalProduct>> = repository.observeAllMedicalProductsAdmin()
        .map { it.sortedByDescending { m -> m.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "medicalProducts error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val medicalCategories: StateFlow<List<Category>> = repository.observeAllMedicalCategoriesAdmin()
        .map { it.sortedBy { c -> c.sortOrder } }
        .catch { Log.w(TAG, "medicalCategories error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val multanItems: StateFlow<List<MultanItem>> = repository.observeAllMultanItemsAdmin()
        .map { it.sortedByDescending { m -> m.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "multanItems error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val celebrationItems: StateFlow<List<CelebrationItem>> = repository.observeAllCelebrationItemsAdmin()
        .map { it.sortedByDescending { c -> c.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "celebrationItems error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val parcelRiderRequests: StateFlow<List<ParcelRiderRequest>> = repository.observeAllParcelRiderRequestsAdmin()
        .map { it.sortedByDescending { req -> req.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "parcelRiderRequests error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val riders: StateFlow<List<RiderProfile>> = repository.observeAllRidersAdmin()
        .catch { Log.w(TAG, "riders error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val orders: StateFlow<List<OrderRecord>> = repository.observeAllOrdersAdmin()
        .map { it.sortedByDescending { o -> o.createdAt?.seconds ?: 0L } }
        .catch { Log.w(TAG, "orders error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val coupons: StateFlow<List<CouponRecord>> = repository.observeAllCouponsAdmin()
        .catch { Log.w(TAG, "coupons error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val reviews: StateFlow<List<ReviewRecord>> = repository.observeAllReviewsAdmin()
        .catch { Log.w(TAG, "reviews error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val logs: StateFlow<List<AdminActivityLog>> = repository.observeAdminLogs()
        .map { it.sortedByDescending { l -> l.createdAt?.seconds ?: 0L } }
        .catch { Log.w(TAG, "logs error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val settings: StateFlow<PlatformSettings> = repository.observePlatformSettings()
        .catch { Log.w(TAG, "settings error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlatformSettings())

    val cityRequests: StateFlow<List<CityApprovalRequest>> = repository.observeAllCityRequestsAdmin()
        .map { it.sortedByDescending { req -> req.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "cityRequests error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val adminAuthConfig: StateFlow<AdminAuthConfig> = repository.observeAdminAuthConfig()
        .catch { Log.w(TAG, "adminAuthConfig error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AdminAuthConfig())

    val restaurantCredentials: StateFlow<List<RestaurantCredentialRecord>> =
        repository.observeAdminRestaurantCredentials()
            .catch { Log.w(TAG, "restaurantCredentials error", it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val supportChatThreads: StateFlow<List<SupportChatThread>> = repository.observeAllSupportChatThreadsAdmin()
        .map { list -> list.sortedByDescending { it.updatedAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "supportChatThreads error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _selectedSupportUserId = MutableStateFlow<String?>(null)
    val selectedSupportUserId: StateFlow<String?> = _selectedSupportUserId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSupportMessages: StateFlow<List<SupportChatMessage>> = _selectedSupportUserId
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeSupportChatMessages(uid)
                    .map { list -> list.sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
                    .catch { Log.w(TAG, "supportChatMessages error", it) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun clearMessage() {
        _message.value = null
    }

    fun updateAdminAuthCredentials(newLoginId: String, newPassword: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateAdminAuthCredentials(newLoginId, newPassword)
                repository.logAdminAction(
                    adminId = adminId,
                    adminEmail = adminEmail,
                    action = "UPDATE_ADMIN_AUTH",
                    targetType = "platformConfig",
                    targetId = "adminAuth",
                    details = "Updated Super Admin login ID to '${newLoginId.trim()}'"
                )
            }.onSuccess {
                _message.value = "Admin login ID & password updated!"
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to update Admin credentials"
            }
        }
    }

    fun saveRestaurantCredentials(restaurant: Restaurant, loginId: String, password: String) {
        viewModelScope.launch {
            runCatching {
                repository.saveRestaurantCredentialsAdmin(
                    restaurantId = restaurant.id,
                    restaurantName = restaurant.name,
                    loginId = loginId,
                    password = password
                )
                repository.logAdminAction(
                    adminId = adminId,
                    adminEmail = adminEmail,
                    action = "SET_RESTAURANT_CREDENTIALS",
                    targetType = "restaurants",
                    targetId = restaurant.id,
                    details = "Updated portal credentials for ${restaurant.name} (ID: ${loginId.trim().lowercase()})"
                )
            }.onSuccess {
                _message.value = "Portal login saved for ${restaurant.name}"
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to save restaurant credentials"
            }
        }
    }

    fun seedStarterCatalog() {
        viewModelScope.launch {
            runCatching {
                CatalogSeedHelper.seedStarterCatalog(db, adminId, adminEmail)
                repository.logAdminAction(
                    adminId = adminId,
                    adminEmail = adminEmail,
                    action = "SEED_CATALOG",
                    targetType = "platform",
                    targetId = "global",
                    details = "Initialized Bahawalpur food & grocery starter catalog, 28 categories, and coupons."
                )
            }.onSuccess {
                _message.value = "Bahawalpur starter catalog populated in Firestore!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to seed catalog"
            }
        }
    }

    fun saveCategory(collection: String, category: Category) {
        viewModelScope.launch {
            runCatching {
                val id = repository.saveCategory(collection, category)
                repository.logAdminAction(adminId, adminEmail, "SAVE_CATEGORY", collection, id, "Saved category ${category.name}")
            }.onSuccess { _message.value = "Category saved" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save category" }
        }
    }

    fun deleteCategory(collection: String, category: Category) {
        viewModelScope.launch {
            runCatching {
                repository.deleteCategory(collection, category.id)
                repository.logAdminAction(adminId, adminEmail, "DELETE_CATEGORY", collection, category.id, "Deleted ${category.name}")
            }.onSuccess { _message.value = "Category deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to delete category" }
        }
    }

    fun saveFoodItem(item: FoodItem) {
        viewModelScope.launch {
            runCatching {
                val id = repository.saveFoodItem(item, isAdminOverride = true)
                repository.logAdminAction(adminId, adminEmail, "SAVE_FOOD_ITEM", "foodItems", id, "Saved ${item.name} (${item.regularPricePkr} PKR)")
            }.onSuccess { _message.value = "Food product saved & live on all devices!" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save food product" }
        }
    }

    fun deleteFoodItem(item: FoodItem) {
        viewModelScope.launch {
            runCatching {
                repository.deleteFoodItem(item.id)
                repository.logAdminAction(adminId, adminEmail, "DELETE_FOOD_ITEM", "foodItems", item.id, "Deleted ${item.name}")
            }.onSuccess { _message.value = "Food product deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to delete food product" }
        }
    }

    fun saveGroceryProduct(product: GroceryProduct) {
        viewModelScope.launch {
            runCatching {
                val id = repository.saveGroceryProduct(product, isAdminOverride = true)
                repository.logAdminAction(adminId, adminEmail, "SAVE_GROCERY_PRODUCT", "groceryProducts", id, "Saved ${product.name}")
            }.onSuccess { _message.value = "Grocery product saved & live on all devices!" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save grocery product" }
        }
    }

    fun deleteGroceryProduct(product: GroceryProduct) {
        viewModelScope.launch {
            runCatching {
                repository.deleteGroceryProduct(product.id)
                repository.logAdminAction(adminId, adminEmail, "DELETE_GROCERY_PRODUCT", "groceryProducts", product.id, "Deleted ${product.name}")
            }.onSuccess { _message.value = "Grocery product deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to delete grocery product" }
        }
    }

    fun saveRestaurantAdmin(restaurant: Restaurant, onCreated: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            runCatching {
                val resolvedImg = com.example.ui.components.restaurantImageUri(restaurant.name, restaurant.imageUrl)
                val id = repository.saveRestaurant(
                    restaurant.copy(
                        ownerId = restaurant.ownerId.ifBlank { adminId },
                        email = restaurant.email.ifBlank { adminEmail },
                        imageUrl = resolvedImg,
                        approvalStatus = "approved",
                        isOpen = restaurant.isOpen
                    ),
                    isAdminOverride = true
                )
                repository.logAdminAction(adminId, adminEmail, "SAVE_RESTAURANT", "restaurants", id, "Saved ${restaurant.name}")
                id
            }.onSuccess { id ->
                _message.value = "Restaurant ${restaurant.name} saved & approved"
                onCreated?.invoke(id)
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to save restaurant"
            }
        }
    }

    fun saveGroceryStoreAdmin(store: GroceryStore, onCreated: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            runCatching {
                val resolvedImg = com.example.ui.components.groceryStoreImageUri(store.name, store.imageUrl)
                val id = repository.saveGroceryStore(
                    store.copy(
                        ownerId = store.ownerId.ifBlank { adminId },
                        email = store.email.ifBlank { adminEmail },
                        imageUrl = resolvedImg,
                        approvalStatus = "approved",
                        isOpen = store.isOpen
                    ),
                    isAdminOverride = true
                )
                repository.logAdminAction(adminId, adminEmail, "SAVE_GROCERY_STORE", "groceryStores", id, "Saved ${store.name}")
                id
            }.onSuccess { id ->
                _message.value = "Grocery store ${store.name} saved & approved"
                onCreated?.invoke(id)
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to save grocery store"
            }
        }
    }

    fun updateRestaurantApproval(restaurant: Restaurant, status: String, commissionPercent: Double? = null) {
        viewModelScope.launch {
            runCatching {
                repository.updateRestaurantApproval(restaurant.id, status, commissionPercent)
                repository.logAdminAction(adminId, adminEmail, "UPDATE_RESTAURANT", "restaurants", restaurant.id, "${restaurant.name} -> $status")
            }.onSuccess { _message.value = "${restaurant.name} marked $status" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to update restaurant" }
        }
    }

    fun updateGroceryStoreApproval(store: GroceryStore, status: String, commissionPercent: Double? = null) {
        viewModelScope.launch {
            runCatching {
                repository.updateGroceryStoreApproval(store.id, status, commissionPercent)
                repository.logAdminAction(adminId, adminEmail, "UPDATE_GROCERY_STORE", "groceryStores", store.id, "${store.name} -> $status")
            }.onSuccess { _message.value = "${store.name} marked $status" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to update grocery store" }
        }
    }

    fun saveMedicalProduct(product: MedicalProduct) {
        viewModelScope.launch {
            runCatching {
                val id = repository.saveMedicalProduct(product, isAdminOverride = true)
                repository.logAdminAction(adminId, adminEmail, "SAVE_MEDICAL_PRODUCT", "medicalProducts", id, "Saved ${product.name}")
            }.onSuccess { _message.value = "Medical product saved & live on all devices!" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save medical product" }
        }
    }

    fun deleteMedicalProduct(product: MedicalProduct) {
        viewModelScope.launch {
            runCatching {
                repository.deleteMedicalProduct(product.id)
                repository.logAdminAction(adminId, adminEmail, "DELETE_MEDICAL_PRODUCT", "medicalProducts", product.id, "Deleted ${product.name}")
            }.onSuccess { _message.value = "Medical product deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to delete medical product" }
        }
    }

    fun saveMedicalStoreAdmin(store: MedicalStore, onCreated: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            runCatching {
                val resolvedImg = com.example.ui.components.medicalStoreImageUri(store.name, store.imageUrl)
                val id = repository.saveMedicalStore(
                    store.copy(
                        ownerId = store.ownerId.ifBlank { adminId },
                        email = store.email.ifBlank { adminEmail },
                        imageUrl = resolvedImg,
                        approvalStatus = "approved",
                        isOpen = store.isOpen
                    ),
                    isAdminOverride = true
                )
                repository.logAdminAction(adminId, adminEmail, "SAVE_MEDICAL_STORE", "medicalStores", id, "Saved ${store.name}")
                id
            }.onSuccess { id ->
                _message.value = "Pharmacy ${store.name} saved & approved"
                onCreated?.invoke(id)
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to save pharmacy"
            }
        }
    }

    fun updateMedicalStoreApproval(store: MedicalStore, status: String, commissionPercent: Double? = null) {
        viewModelScope.launch {
            runCatching {
                repository.updateMedicalStoreApproval(store.id, status, commissionPercent)
                repository.logAdminAction(adminId, adminEmail, "UPDATE_MEDICAL_STORE", "medicalStores", store.id, "${store.name} -> $status")
            }.onSuccess { _message.value = "${store.name} marked $status" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to update pharmacy" }
        }
    }

    fun updateVendorHoursAndOpen(
        collection: String,
        vendorId: String,
        vendorName: String,
        isOpen: Boolean,
        openingTime: String,
        closingTime: String
    ) {
        viewModelScope.launch {
            runCatching {
                repository.updateVendorHoursAndOpenStatus(collection, vendorId, isOpen, openingTime, closingTime)
                repository.logAdminAction(
                    adminId,
                    adminEmail,
                    "UPDATE_VENDOR_HOURS",
                    collection,
                    vendorId,
                    "$vendorName open=$isOpen ($openingTime - $closingTime)"
                )
            }.onSuccess {
                val hoursText = GharTakConstants.formatOperatingHours(openingTime, closingTime)
                _message.value = "$vendorName updated: ${if (isOpen) "Open" else "Closed"} ($hoursText)"
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to update store timings"
            }
        }
    }

    fun saveMultanItem(item: MultanItem) {
        viewModelScope.launch {
            runCatching {
                val id = repository.saveMultanItemAdmin(item)
                repository.logAdminAction(adminId, adminEmail, "SAVE_MULTAN_ITEM", "multanItems", id, "Saved ${item.brandName} • ${item.name}")
            }.onSuccess { _message.value = "Multan item '${item.name}' saved & live!" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save Multan item" }
        }
    }

    fun deleteMultanItem(item: MultanItem) {
        viewModelScope.launch {
            runCatching {
                repository.deleteMultanItemAdmin(item.id)
                repository.logAdminAction(adminId, adminEmail, "DELETE_MULTAN_ITEM", "multanItems", item.id, "Deleted ${item.name}")
            }.onSuccess { _message.value = "Multan item deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to delete Multan item" }
        }
    }

    fun saveCelebrationItem(item: CelebrationItem) {
        viewModelScope.launch {
            runCatching {
                val id = repository.saveCelebrationItemAdmin(item)
                repository.logAdminAction(adminId, adminEmail, "SAVE_CELEBRATION_ITEM", "celebrationItems", id, "Saved ${item.name}")
            }.onSuccess { _message.value = "Celebration item '${item.name}' saved & live!" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save Celebration item" }
        }
    }

    fun deleteCelebrationItem(item: CelebrationItem) {
        viewModelScope.launch {
            runCatching {
                repository.deleteCelebrationItemAdmin(item.id)
                repository.logAdminAction(adminId, adminEmail, "DELETE_CELEBRATION_ITEM", "celebrationItems", item.id, "Deleted ${item.name}")
            }.onSuccess { _message.value = "Celebration item deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to delete Celebration item" }
        }
    }

    fun reviewParcelRiderRequest(req: ParcelRiderRequest, status: String, adminNote: String) {
        viewModelScope.launch {
            runCatching {
                repository.reviewParcelRiderRequestAdmin(req.id, status, adminNote)
                repository.logAdminAction(
                    adminId, adminEmail, "REVIEW_PARCEL_RIDER_REQUEST", "parcelRiderRequests", req.id,
                    "${req.restaurantName} (${req.requestType}) -> $status"
                )
                if (req.requesterId.isNotBlank()) {
                    val label = if (req.requestType == "send_parcel") "Send Parcel" else "Hire Rider"
                    repository.createNotification(
                        userId = req.requesterId,
                        title = "$label Request ${status.replaceFirstChar { it.uppercase() }}",
                        body = if (adminNote.isNotBlank()) "Admin: $adminNote" else "Your $label request from ${req.restaurantName} was $status.",
                        type = "system"
                    )
                }
            }.onSuccess { _message.value = "Request marked $status" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to review request" }
        }
    }

    fun updateRiderApproval(rider: RiderProfile, status: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateRiderApprovalAdmin(rider.id, status)
                repository.logAdminAction(adminId, adminEmail, "UPDATE_RIDER", "riders", rider.id, "${rider.fullName} -> $status")
            }.onSuccess { _message.value = "${rider.fullName} marked $status" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to update rider" }
        }
    }

    fun updateOrderAdmin(order: OrderRecord, status: String, paymentStatus: String? = null, rider: RiderProfile? = null) {
        viewModelScope.launch {
            runCatching {
                repository.updateOrderAdmin(
                    orderId = order.id,
                    status = status,
                    paymentStatus = paymentStatus,
                    riderId = rider?.userId,
                    riderName = rider?.fullName
                )
                repository.logAdminAction(
                    adminId, adminEmail, "UPDATE_ORDER", "orders", order.id,
                    "Status=$status Payment=${paymentStatus ?: order.paymentStatus} Rider=${rider?.fullName ?: order.riderName}"
                )
                repository.createNotification(
                    userId = order.customerId,
                    title = "Order $status",
                    body = "Admin updated order #${order.id.takeLast(6).uppercase()} to $status.",
                    type = "order",
                    orderId = order.id
                )
            }.onSuccess { _message.value = "Order updated to $status" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to update order" }
        }
    }

    fun saveCoupon(coupon: CouponRecord) {
        viewModelScope.launch {
            runCatching {
                val id = repository.saveCouponAdmin(coupon)
                repository.logAdminAction(adminId, adminEmail, "SAVE_COUPON", "coupons", id, "Coupon ${coupon.code}")
            }.onSuccess { _message.value = "Coupon ${coupon.code.uppercase()} saved" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save coupon" }
        }
    }

    fun deleteCoupon(coupon: CouponRecord) {
        viewModelScope.launch {
            runCatching {
                repository.deleteCouponAdmin(coupon.id)
                repository.logAdminAction(adminId, adminEmail, "DELETE_COUPON", "coupons", coupon.id, "Deleted ${coupon.code}")
            }.onSuccess { _message.value = "Coupon deleted" }
        }
    }

    fun toggleReviewHidden(review: ReviewRecord) {
        viewModelScope.launch {
            runCatching {
                repository.setReviewHiddenAdmin(review.id, !review.isHidden)
                repository.logAdminAction(adminId, adminEmail, "MODERATE_REVIEW", "reviews", review.id, "Hidden=${!review.isHidden}")
            }.onSuccess { _message.value = if (review.isHidden) "Review restored" else "Review hidden" }
        }
    }

    fun saveSettings(updated: PlatformSettings) {
        viewModelScope.launch {
            runCatching {
                repository.savePlatformSettingsAdmin(updated)
                repository.logAdminAction(adminId, adminEmail, "UPDATE_SETTINGS", "platformSettings", "global", "Updated commissions/cities/gateways")
            }.onSuccess { _message.value = "Platform settings saved" }
                .onFailure { _message.value = it.localizedMessage ?: "Failed to save settings" }
        }
    }

    fun reviewCityRequest(req: CityApprovalRequest, approve: Boolean) {
        viewModelScope.launch {
            runCatching {
                repository.reviewCityRequestAdmin(req, approve, settings.value)
                repository.logAdminAction(
                    adminId, adminEmail, "REVIEW_CITY_REQUEST", "cityRequests", req.id,
                    "${req.cityName} -> ${if (approve) "approved" else "rejected"}"
                )
                if (req.requesterId.isNotBlank()) {
                    repository.createNotification(
                        userId = req.requesterId,
                        title = if (approve) "Location Approved: ${req.cityName}" else "Location Request Declined",
                        body = if (approve) "${req.cityName} has been added to GT Service approved cities!" else "Your request for ${req.cityName} was declined by Admin.",
                        type = "system"
                    )
                }
            }.onSuccess {
                _message.value = if (approve) "Approved & added ${req.cityName}!" else "Rejected ${req.cityName}"
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to review city request"
            }
        }
    }

    fun addRestaurantPromoOffer(offer: PromoOffer, syncCoupon: Boolean) {
        val cleanCode = offer.code.trim().uppercase().ifBlank { "PROMO20" }
        val pct = offer.discountPercent.coerceIn(1, 90)
        val isEditing = offer.id.isNotBlank() && settings.value.promoOffers.any { it.id == offer.id }
        val normalized = offer.copy(
            id = offer.id.ifBlank { "promo_${System.currentTimeMillis()}" },
            code = cleanCode,
            discountPercent = pct,
            badgeText = offer.badgeText.trim().ifBlank { "USE CODE: $cleanCode" },
            title = offer.title.trim().ifBlank {
                if (offer.restaurantName.isNotBlank()) "$pct% OFF at ${offer.restaurantName}" else "$pct% OFF"
            },
            subtitle = offer.subtitle.trim().ifBlank { "Tap to claim code $cleanCode & open menu!" }
        )
        val updatedPromos = if (isEditing) {
            settings.value.promoOffers.map { if (it.id == normalized.id) normalized else it }
        } else {
            (settings.value.promoOffers + normalized).take(20)
        }
        viewModelScope.launch {
            runCatching {
                repository.savePlatformSettingsAdmin(settings.value.copy(promoOffers = updatedPromos))
                if (syncCoupon) {
                    val existing = coupons.value.firstOrNull { it.code.equals(cleanCode, ignoreCase = true) }
                    repository.saveCouponAdmin(
                        CouponRecord(
                            id = existing?.id ?: "",
                            code = cleanCode,
                            discountType = "percentage",
                            discountValue = pct.toDouble(),
                            minOrderPkr = existing?.minOrderPkr ?: 400.0,
                            maxDiscountPkr = existing?.maxDiscountPkr ?: 300.0,
                            usageLimit = existing?.usageLimit ?: 500L,
                            usedCount = existing?.usedCount ?: 0L,
                            isActive = true,
                            expiresAtEpochMs = existing?.expiresAtEpochMs ?: 1900000000000L
                        )
                    )
                }
                repository.logAdminAction(
                    adminId,
                    adminEmail,
                    if (isEditing) "UPDATE_RESTAURANT_PROMO" else "ADD_RESTAURANT_PROMO",
                    "platformSettings",
                    "global",
                    "${normalized.restaurantName.ifBlank { "All" }} • ${normalized.title} ($cleanCode)"
                )
            }.onSuccess {
                _message.value = if (isEditing) {
                    "Restaurant promo updated & live on Home!"
                } else {
                    "Restaurant promo added & live on Home!"
                }
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to save restaurant promo"
            }
        }
    }

    fun removeRestaurantPromoOffer(offerId: String) {
        val updatedPromos = settings.value.promoOffers.filterNot { it.id == offerId }
        saveSettings(settings.value.copy(promoOffers = updatedPromos))
    }

    fun savePromoSettingsAndSyncCoupon(updated: PlatformSettings, syncCoupon: Boolean) {
        val cleanCode = updated.promoCode.trim().uppercase().ifBlank { "GHARTAK20" }
        val pct = updated.promoDiscountPercent.coerceIn(0, 90)
        val normalized = updated.copy(
            promoCode = cleanCode,
            promoDiscountPercent = pct,
            promoBadgeText = updated.promoBadgeText.trim().ifBlank { "USE CODE: $cleanCode" },
            promoTitle = updated.promoTitle.trim().ifBlank {
                if (updated.promoRestaurantName.isNotBlank()) "$pct% OFF at ${updated.promoRestaurantName}" else "$pct% OFF Food & Groceries"
            },
            promoSubtitle = updated.promoSubtitle.trim().ifBlank { "Your Favourite Food, Delivered to Your Door!" }
        )
        viewModelScope.launch {
            runCatching {
                repository.savePlatformSettingsAdmin(normalized)
                if (syncCoupon && cleanCode.length >= 2 && pct > 0) {
                    val existing = coupons.value.firstOrNull {
                        it.code.equals(cleanCode, ignoreCase = true) || it.id == "cpn_ghartak20"
                    }
                    repository.saveCouponAdmin(
                        CouponRecord(
                            id = existing?.id ?: "",
                            code = cleanCode,
                            discountType = "percentage",
                            discountValue = pct.toDouble(),
                            minOrderPkr = existing?.minOrderPkr ?: 500.0,
                            maxDiscountPkr = existing?.maxDiscountPkr ?: 250.0,
                            usageLimit = existing?.usageLimit ?: 500L,
                            usedCount = existing?.usedCount ?: 0L,
                            isActive = normalized.promoBannerEnabled,
                            expiresAtEpochMs = existing?.expiresAtEpochMs ?: 1900000000000L
                        )
                    )
                }
                repository.logAdminAction(
                    adminId,
                    adminEmail,
                    "UPDATE_PROMO_BANNER",
                    "platformSettings",
                    "global",
                    "Promo enabled=${normalized.promoBannerEnabled} rest=${normalized.promoRestaurantName.ifBlank { "All" }} code=$cleanCode pct=$pct%"
                )
            }.onSuccess {
                _message.value = if (syncCoupon) "Promo banner & coupon $cleanCode updated!" else "Promo banner settings saved!"
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to save promo settings"
            }
        }
    }

    fun sendNotificationToUser(targetUserId: String, title: String, body: String) {
        viewModelScope.launch {
            runCatching {
                repository.createNotification(targetUserId, title, body, "promo")
                repository.logAdminAction(adminId, adminEmail, "SEND_NOTIFICATION", "users", targetUserId, title)
            }.onSuccess { _message.value = "Notification sent" }
        }
    }

    fun creditCustomerWallet(user: UserProfile, amountPkr: Double, reason: String) {
        viewModelScope.launch {
            runCatching {
                val nextBal = repository.adminCreditCustomerWallet(user.userId, amountPkr, reason)
                repository.createNotification(
                    userId = user.userId,
                    title = "Wallet Credited",
                    body = "Admin credited ${GharTakConstants.formatPkr(amountPkr)} to your GT Service Wallet. Reason: $reason",
                    type = "promo"
                )
                repository.logAdminAction(
                    adminId,
                    adminEmail,
                    "WALLET_CREDIT",
                    "users",
                    user.userId,
                    "Credited $amountPkr PKR to ${user.fullName}: $reason"
                )
                nextBal
            }.onSuccess { nextBal ->
                _message.value = "${user.fullName}'s Wallet credited! New balance: ${com.example.data.model.GharTakConstants.formatPkr(nextBal)}"
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to credit wallet"
            }
        }
    }

    fun selectSupportThread(userId: String?) {
        _selectedSupportUserId.value = userId
        if (!userId.isNullOrBlank()) {
            viewModelScope.launch {
                runCatching { repository.markSupportChatRead(userId, "admin") }
            }
        }
    }

    fun sendAdminSupportReply(
        thread: SupportChatThread,
        text: String,
        imageUrl: String? = null,
        audioBase64: String? = null,
        audioDurationSec: Long? = null
    ) {
        val clean = text.trim()
        if (clean.isBlank() && imageUrl.isNullOrBlank() && audioBase64.isNullOrBlank()) return
        viewModelScope.launch {
            val activeSupport = settings.value.supportPhoneLocal.ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
            runCatching {
                repository.sendSupportChatMessage(
                    threadUserId = thread.userId,
                    customerName = thread.customerName,
                    customerPhone = thread.customerPhone,
                    senderName = "GT Service Support ($activeSupport)",
                    senderRole = "admin",
                    text = clean,
                    orderId = "",
                    supportPhone = activeSupport,
                    imageUrl = imageUrl,
                    audioBase64 = audioBase64,
                    audioDurationSec = audioDurationSec
                )
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Failed to send support reply"
            }
        }
    }

    companion object {
        private const val TAG = "AdminVM"
        private const val STOP_TIMEOUT_MS = 5000L
    }
}
