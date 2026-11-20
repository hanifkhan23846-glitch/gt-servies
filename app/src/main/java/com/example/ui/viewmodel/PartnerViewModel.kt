package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FoodItem
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.OrderRecord
import com.example.data.model.ParcelRiderRequest
import com.example.data.model.Restaurant
import com.example.data.model.RiderProfile
import com.example.data.remote.GharTakRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PartnerViewModel(
    private val repository: GharTakRepository,
    val currentUserId: String,
    val currentUserEmail: String,
    val currentUserName: String
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _staffLoginLoading = MutableStateFlow(false)
    val staffLoginLoading: StateFlow<Boolean> = _staffLoginLoading.asStateFlow()

    private val _selectedRestaurantId = MutableStateFlow<String?>(null)
    val selectedRestaurantId: StateFlow<String?> = _selectedRestaurantId.asStateFlow()

    val staffSessionRestaurantIds: StateFlow<List<String>> = repository.observeStaffSessionRestaurantIds(currentUserId)
        .catch { Log.w(TAG, "observeStaffSessionRestaurantIds error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val ownedRestaurantsFlow = repository.observeMyRestaurants(currentUserId)
        .catch { Log.w(TAG, "observeMyRestaurants error", it); emit(emptyList()) }

    private val staffRestaurantsFlow = staffSessionRestaurantIds
        .flatMapLatest { ids -> repository.observeRestaurantsByIds(ids) }
        .catch { Log.w(TAG, "observeRestaurantsByIds error", it); emit(emptyList()) }

    val myRestaurants: StateFlow<List<Restaurant>> = combine(ownedRestaurantsFlow, staffRestaurantsFlow) { owned, staff ->
        (staff + owned).distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val ownedFoodItemsFlow = repository.observeMyFoodItems(currentUserId)
        .catch { Log.w(TAG, "observeMyFoodItems error", it); emit(emptyList()) }

    private val staffFoodItemsFlow = staffSessionRestaurantIds
        .flatMapLatest { ids -> repository.observeFoodItemsByRestaurantIds(ids) }
        .catch { Log.w(TAG, "observeFoodItemsByRestaurantIds error", it); emit(emptyList()) }

    val myFoodItems: StateFlow<List<FoodItem>> = combine(ownedFoodItemsFlow, staffFoodItemsFlow) { owned, staff ->
        (staff + owned).distinctBy { it.id }
            .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val myGroceryStores: StateFlow<List<GroceryStore>> = repository.observeMyGroceryStores(currentUserId)
        .catch { Log.w(TAG, "observeMyGroceryStores error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val myGroceryProducts: StateFlow<List<GroceryProduct>> = repository.observeMyGroceryProducts(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "observeMyGroceryProducts error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val ownedVendorOrdersFlow = repository.observeVendorOrders(currentUserId)
        .catch { Log.w(TAG, "observeVendorOrders error", it); emit(emptyList()) }

    private val staffVendorOrdersFlow = staffSessionRestaurantIds
        .flatMapLatest { ids -> repository.observeOrdersByVendorIds(ids) }
        .catch { Log.w(TAG, "observeOrdersByVendorIds error", it); emit(emptyList()) }

    val myVendorOrders: StateFlow<List<OrderRecord>> = combine(ownedVendorOrdersFlow, staffVendorOrdersFlow) { owned, staff ->
        (staff + owned).distinctBy { it.id }
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val riderProfile: StateFlow<RiderProfile?> = repository.observeRiderProfile(currentUserId)
        .catch { Log.w(TAG, "observeRiderProfile error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val availableRiderOrders: StateFlow<List<OrderRecord>> = repository.observeAvailableRiderOrders()
        .catch { Log.w(TAG, "observeAvailableRiderOrders error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val assignedRiderOrders: StateFlow<List<OrderRecord>> = repository.observeAssignedRiderOrders(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
        .catch { Log.w(TAG, "observeAssignedRiderOrders error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val ownedParcelRequestsFlow = repository.observeMyParcelRiderRequests(currentUserId)
        .catch { Log.w(TAG, "observeMyParcelRiderRequests error", it); emit(emptyList()) }

    private val staffParcelRequestsFlow = staffSessionRestaurantIds
        .flatMapLatest { ids -> repository.observeParcelRequestsByRestaurantIds(ids) }
        .catch { Log.w(TAG, "observeParcelRequestsByRestaurantIds error", it); emit(emptyList()) }

    val myParcelRiderRequests: StateFlow<List<ParcelRiderRequest>> = combine(ownedParcelRequestsFlow, staffParcelRequestsFlow) { owned, staff ->
        (staff + owned).distinctBy { it.id }
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val availableParcelRiderJobs: StateFlow<List<ParcelRiderRequest>> = repository.observeApprovedParcelRiderJobs()
        .map { list -> list.filter { it.riderId.isBlank() }.sortedByDescending { it.createdAt?.seconds ?: 0L } }
        .catch { Log.w(TAG, "observeApprovedParcelRiderJobs error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val assignedParcelRiderJobs: StateFlow<List<ParcelRiderRequest>> = repository.observeAssignedParcelRiderJobs(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
        .catch { Log.w(TAG, "observeAssignedParcelRiderJobs error", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun clearMessage() {
        _message.value = null
    }

    fun selectActiveRestaurant(restaurantId: String) {
        _selectedRestaurantId.value = restaurantId
    }

    fun loginToRestaurantPortal(loginId: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _staffLoginLoading.value = true
            runCatching {
                repository.loginRestaurantWithCredentials(loginId, password)
            }.onSuccess { rest ->
                _staffLoginLoading.value = false
                _selectedRestaurantId.value = rest.id
                _message.value = "Signed in to ${rest.name}!"
                onSuccess()
            }.onFailure { err ->
                _staffLoginLoading.value = false
                _message.value = err.localizedMessage ?: "Invalid restaurant Login ID or password"
            }
        }
    }

    fun logoutFromRestaurantPortal(restaurantId: String) {
        viewModelScope.launch {
            runCatching {
                repository.logoutRestaurantStaffSession(currentUserId, restaurantId)
            }.onSuccess {
                if (_selectedRestaurantId.value == restaurantId) {
                    _selectedRestaurantId.value = null
                }
                _message.value = "Signed out of restaurant portal"
            }.onFailure { err ->
                _message.value = err.localizedMessage ?: "Could not log out"
            }
        }
    }

    // --- Restaurant Owner / Staff Actions ---

    fun registerOrUpdateRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            runCatching {
                val payload = if (restaurant.id.isBlank()) {
                    restaurant.copy(ownerId = currentUserId, email = currentUserEmail)
                } else {
                    restaurant
                }
                repository.saveRestaurant(payload, isAdminOverride = false)
            }.onSuccess {
                _message.value = if (restaurant.id.isBlank()) "Restaurant submitted for Admin approval!" else "Restaurant profile updated"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to save restaurant"
            }
        }
    }

    fun saveFoodItem(item: FoodItem) {
        if (item.id.isBlank()) {
            _message.value = "Only Super Admin can add new food products."
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.saveFoodItem(item)
            }.onSuccess {
                _message.value = "Food item updated & synced to all customer devices!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to update food item"
            }
        }
    }

    fun deleteFoodItem(itemId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteFoodItem(itemId) }
                .onSuccess { _message.value = "Food item deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Delete failed" }
        }
    }

    // --- Grocery Store Owner Actions ---

    fun registerOrUpdateGroceryStore(store: GroceryStore) {
        viewModelScope.launch {
            runCatching {
                repository.saveGroceryStore(
                    store.copy(ownerId = currentUserId, email = currentUserEmail),
                    isAdminOverride = false
                )
            }.onSuccess {
                _message.value = if (store.id.isBlank()) "Grocery store submitted for Admin approval!" else "Store profile updated"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to save grocery store"
            }
        }
    }

    fun saveGroceryProduct(product: GroceryProduct) {
        viewModelScope.launch {
            runCatching {
                repository.saveGroceryProduct(product.copy(ownerId = currentUserId))
            }.onSuccess {
                _message.value = "Grocery product saved & synced to all customer devices!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to save product"
            }
        }
    }

    fun deleteGroceryProduct(productId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteGroceryProduct(productId) }
                .onSuccess { _message.value = "Grocery product deleted" }
                .onFailure { _message.value = it.localizedMessage ?: "Delete failed" }
        }
    }

    fun updateRestaurantHoursAndOpen(restaurant: Restaurant, isOpen: Boolean, openingTime: String, closingTime: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateVendorHoursAndOpenStatus("restaurants", restaurant.id, isOpen, openingTime, closingTime)
            }.onSuccess {
                _message.value = "${restaurant.name} hours updated!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to update restaurant timings"
            }
        }
    }

    fun updateGroceryStoreHoursAndOpen(store: GroceryStore, isOpen: Boolean, openingTime: String, closingTime: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateVendorHoursAndOpenStatus("groceryStores", store.id, isOpen, openingTime, closingTime)
            }.onSuccess {
                _message.value = "${store.name} hours updated!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to update store timings"
            }
        }
    }

    // --- Vendor Order Actions ---

    fun updateVendorOrderStatus(order: OrderRecord, status: String, reason: String = "") {
        viewModelScope.launch {
            runCatching {
                repository.updateOrderStatusByVendor(order.id, status, reason)
                runCatching {
                    repository.createNotification(
                        userId = order.customerId,
                        title = "Order $status",
                        body = "${order.vendorName} updated your order to $status.",
                        type = "order",
                        orderId = order.id
                    )
                }
            }.onSuccess {
                _message.value = "Order marked $status"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to update order"
            }
        }
    }

    // --- Rider Actions ---

    fun registerRider(rider: RiderProfile) {
        viewModelScope.launch {
            runCatching {
                repository.registerRider(rider.copy(userId = currentUserId, email = currentUserEmail))
            }.onSuccess {
                _message.value = "Rider application submitted for Admin verification!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to submit rider application"
            }
        }
    }

    fun toggleRiderOnline(isOnline: Boolean, lat: Double = 29.3956, lng: Double = 71.6836) {
        viewModelScope.launch {
            runCatching {
                repository.updateRiderOnlineAndLocation(currentUserId, isOnline, lat, lng)
            }.onSuccess {
                _message.value = if (isOnline) "You are now Online" else "You are now Offline"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to update rider status"
            }
        }
    }

    fun acceptDelivery(order: OrderRecord) {
        val rider = riderProfile.value
        if (rider == null || rider.approvalStatus != "approved") {
            _message.value = "Admin approval is required before accepting deliveries."
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.claimOrderByRider(order.id, currentUserId, rider.fullName)
                runCatching {
                    repository.createNotification(
                        userId = order.customerId,
                        title = "Rider Assigned",
                        body = "${rider.fullName} is heading to ${order.vendorName} to pick up your order.",
                        type = "order",
                        orderId = order.id
                    )
                }
            }.onSuccess {
                _message.value = "Delivery accepted!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Order could not be claimed"
            }
        }
    }

    fun advanceDelivery(order: OrderRecord, nextStatus: String) {
        viewModelScope.launch {
            val paymentStatus = if (nextStatus == "Delivered" && order.paymentMethod == "COD") "paid" else order.paymentStatus
            runCatching {
                repository.updateOrderDeliveryByRider(order.id, nextStatus, paymentStatus)
                runCatching {
                    repository.createNotification(
                        userId = order.customerId,
                        title = "Order $nextStatus",
                        body = "Your GT Service order #${order.id.takeLast(6).uppercase()} is now $nextStatus.",
                        type = "order",
                        orderId = order.id
                    )
                }
            }.onSuccess {
                _message.value = "Delivery updated to $nextStatus"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to update delivery"
            }
        }
    }

    // --- Restaurant Parcel & Hire Rider Dispatch (Requires Admin Approval) ---

    fun submitParcelRiderRequest(
        requestType: String,
        restaurant: Restaurant,
        pickupAddress: String,
        dropoffAddress: String,
        recipientName: String,
        recipientPhone: String,
        details: String,
        offeredFeePkr: Double
    ) {
        viewModelScope.launch {
            runCatching {
                repository.createParcelRiderRequest(
                    ParcelRiderRequest(
                        requestType = requestType,
                        requesterId = currentUserId,
                        restaurantId = restaurant.id,
                        restaurantName = restaurant.name,
                        pickupAddress = pickupAddress,
                        dropoffAddress = dropoffAddress,
                        city = restaurant.city,
                        recipientName = recipientName,
                        recipientPhone = recipientPhone,
                        details = details,
                        offeredFeePkr = offeredFeePkr
                    )
                )
            }.onSuccess {
                val label = if (requestType == "send_parcel") "Send Parcel" else "Hire Rider"
                _message.value = "$label request submitted for Super Admin approval!"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to submit request"
            }
        }
    }

    fun claimParcelRiderJob(req: ParcelRiderRequest) {
        val rider = riderProfile.value
        if (rider == null || rider.approvalStatus != "approved") {
            _message.value = "Admin approval is required before claiming jobs."
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.claimParcelRiderJobByRider(req.id, currentUserId, rider.fullName)
                if (req.requesterId.isNotBlank()) {
                    runCatching {
                        repository.createNotification(
                            userId = req.requesterId,
                            title = "Rider Assigned to Your Request",
                            body = "${rider.fullName} accepted your ${if (req.requestType == "send_parcel") "parcel" else "hire rider"} job.",
                            type = "system"
                        )
                    }
                }
            }.onSuccess {
                _message.value = "Job accepted! Head to ${req.pickupAddress}."
            }.onFailure {
                _message.value = it.localizedMessage ?: "Could not claim job"
            }
        }
    }

    fun advanceParcelRiderJob(req: ParcelRiderRequest, nextStatus: String) {
        viewModelScope.launch {
            runCatching {
                repository.advanceParcelRiderJobByRider(req.id, nextStatus)
                if (req.requesterId.isNotBlank()) {
                    runCatching {
                        repository.createNotification(
                            userId = req.requesterId,
                            title = "Parcel / Rider Job Updated",
                            body = "Your job from ${req.restaurantName} is now ${nextStatus.replace('_', ' ')}.",
                            type = "system"
                        )
                    }
                }
            }.onSuccess {
                _message.value = "Job marked ${nextStatus.replace('_', ' ')}"
            }.onFailure {
                _message.value = it.localizedMessage ?: "Failed to update job status"
            }
        }
    }

    companion object {
        private const val TAG = "PartnerVM"
        private const val STOP_TIMEOUT_MS = 5000L
    }
}

