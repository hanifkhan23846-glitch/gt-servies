package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesRepository
import com.example.data.model.AppPortal
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.CelebrationItem
import com.example.data.model.CityApprovalRequest
import com.example.data.model.CouponRecord
import com.example.data.model.DeliveryAddress
import com.example.data.model.DeliveryAreaFeeRule
import com.example.data.model.FavoriteItem
import com.example.data.model.FoodItem
import com.example.data.model.GharTakConstants
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.MedicalProduct
import com.example.data.model.MedicalStore
import com.example.data.model.MultanCityFeeRule
import com.example.data.model.MultanItem
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.ParcelRiderRequest
import com.example.data.model.PlatformSettings
import com.example.data.model.Restaurant
import com.example.data.model.ReviewRecord
import com.example.data.model.SupportChatMessage
import com.example.data.model.SupportChatThread
import com.example.data.model.UserNotification
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.remote.GharTakRepository
import com.example.ui.components.calculateDistanceKm
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DeliveryFeeResolution(
    val feePkr: Double,
    val reason: String,
    val matchedRule: DeliveryAreaFeeRule? = null,
    val distanceKm: Double? = null,
    val isWithinBaseRadius: Boolean = true
)

class GharTakViewModel(
    private val repository: GharTakRepository,
    private val prefs: PreferencesRepository,
    val currentUserId: String,
    val currentUserEmail: String,
    val currentUserName: String,
    val currentUserPhoto: String?,
    val isBootstrappedAdmin: Boolean
) : ViewModel() {

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _showAdminLogin = MutableStateFlow(false)
    val showAdminLogin: StateFlow<Boolean> = _showAdminLogin.asStateFlow()

    private val _adminSessionUnlocked = MutableStateFlow(false)
    val adminSessionUnlocked: StateFlow<Boolean> = _adminSessionUnlocked.asStateFlow()

    private val _adminLoginLoading = MutableStateFlow(false)
    val adminLoginLoading: StateFlow<Boolean> = _adminLoginLoading.asStateFlow()

    private val _adminLoginError = MutableStateFlow<String?>(null)
    val adminLoginError: StateFlow<String?> = _adminLoginError.asStateFlow()

    private val _foodCart = MutableStateFlow<List<CartItem>>(emptyList())
    val foodCart: StateFlow<List<CartItem>> = _foodCart.asStateFlow()

    private val _groceryCart = MutableStateFlow<List<CartItem>>(emptyList())
    val groceryCart: StateFlow<List<CartItem>> = _groceryCart.asStateFlow()

    private val _medicalCart = MutableStateFlow<List<CartItem>>(emptyList())
    val medicalCart: StateFlow<List<CartItem>> = _medicalCart.asStateFlow()

    private val _multanCart = MutableStateFlow<List<CartItem>>(emptyList())
    val multanCart: StateFlow<List<CartItem>> = _multanCart.asStateFlow()

    private val _celebrationCart = MutableStateFlow<List<CartItem>>(emptyList())
    val celebrationCart: StateFlow<List<CartItem>> = _celebrationCart.asStateFlow()

    private val _preferredCheckoutChannel = MutableStateFlow("food")
    val preferredCheckoutChannel: StateFlow<String> = _preferredCheckoutChannel.asStateFlow()

    private val _selectedMultanDestination = MutableStateFlow<String?>(null)
    val selectedMultanDestination: StateFlow<String?> = _selectedMultanDestination.asStateFlow()

    private val _appliedFoodCoupon = MutableStateFlow<CouponRecord?>(null)
    val appliedFoodCoupon: StateFlow<CouponRecord?> = _appliedFoodCoupon.asStateFlow()

    private val _appliedGroceryCoupon = MutableStateFlow<CouponRecord?>(null)
    val appliedGroceryCoupon: StateFlow<CouponRecord?> = _appliedGroceryCoupon.asStateFlow()

    private val _appliedMedicalCoupon = MutableStateFlow<CouponRecord?>(null)
    val appliedMedicalCoupon: StateFlow<CouponRecord?> = _appliedMedicalCoupon.asStateFlow()

    private val _appliedMultanCoupon = MutableStateFlow<CouponRecord?>(null)
    val appliedMultanCoupon: StateFlow<CouponRecord?> = _appliedMultanCoupon.asStateFlow()

    private val _appliedCelebrationCoupon = MutableStateFlow<CouponRecord?>(null)
    val appliedCelebrationCoupon: StateFlow<CouponRecord?> = _appliedCelebrationCoupon.asStateFlow()

    val selectedCity: StateFlow<String> = prefs.selectedCity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), GharTakConstants.DEFAULT_CITY)

    val darkMode: StateFlow<Boolean> = prefs.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val onboardingSeen: StateFlow<Boolean> = prefs.onboardingSeen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { Log.w(TAG, "Error observing profile", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val platformSettings: StateFlow<PlatformSettings> = repository.observePlatformSettings()
        .catch { Log.w(TAG, "Error observing platform settings", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlatformSettings())

    val addresses: StateFlow<List<DeliveryAddress>> = repository.observeAddresses(currentUserId)
        .catch { Log.w(TAG, "Error observing addresses", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val favorites: StateFlow<List<FavoriteItem>> = repository.observeFavorites(currentUserId)
        .catch { Log.w(TAG, "Error observing favorites", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val notifications: StateFlow<List<UserNotification>> = repository.observeNotifications(currentUserId)
        .catch { Log.w(TAG, "Error observing notifications", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val foodCategories: StateFlow<List<Category>> = repository.observeActiveFoodCategories()
        .map { list -> list.sortedBy { it.sortOrder } }
        .catch { Log.w(TAG, "Error observing food categories", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val walletTransactions: StateFlow<List<WalletTransaction>> = repository.observeWalletTransactions(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "Error observing wallet transactions", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val restaurantsState: StateFlow<UiState<List<Restaurant>>> = repository.observeApprovedRestaurants()
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .asUiState("restaurants")

    val foodItemsState: StateFlow<UiState<List<FoodItem>>> = repository.observeAvailableFoodItems()
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .asUiState("food items")

    val groceryCategories: StateFlow<List<Category>> = repository.observeActiveGroceryCategories()
        .map { list -> list.sortedBy { it.sortOrder } }
        .catch { Log.w(TAG, "Error observing grocery categories", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val groceryStoresState: StateFlow<UiState<List<GroceryStore>>> = repository.observeApprovedGroceryStores()
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .asUiState("grocery stores")

    val groceryProductsState: StateFlow<UiState<List<GroceryProduct>>> = repository.observeAvailableGroceryProducts()
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .asUiState("grocery products")

    val medicalCategories: StateFlow<List<Category>> = repository.observeActiveMedicalCategories()
        .map { list -> list.sortedBy { it.sortOrder } }
        .catch { Log.w(TAG, "Error observing medical categories", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val medicalStoresState: StateFlow<UiState<List<MedicalStore>>> = repository.observeApprovedMedicalStores()
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .asUiState("medical stores")

    val medicalProductsState: StateFlow<UiState<List<MedicalProduct>>> = repository.observeAvailableMedicalProducts()
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .asUiState("medical products")

    val multanItemsState: StateFlow<UiState<List<MultanItem>>> = repository.observeAvailableMultanItems()
        .map { list -> list.sortedByDescending { it.isFeatured } }
        .asUiState("Multan catalog")

    val celebrationItemsState: StateFlow<UiState<List<CelebrationItem>>> = repository.observeAvailableCelebrationItems()
        .map { list -> list.sortedByDescending { it.isFeatured } }
        .asUiState("Celebration catalog")

    val customerOrdersState: StateFlow<UiState<List<OrderRecord>>> = repository.observeCustomerOrders(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
        .asUiState("orders")

    val activeCoupons: StateFlow<List<CouponRecord>> = repository.observeActiveCoupons()
        .catch { Log.w(TAG, "Error observing coupons", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val visibleReviews: StateFlow<List<ReviewRecord>> = repository.observeVisibleReviews()
        .catch { Log.w(TAG, "Error observing reviews", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val supportChatThread: StateFlow<SupportChatThread?> = repository.observeSupportChatThread(currentUserId)
        .catch { Log.w(TAG, "Error observing support thread", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val supportChatMessages: StateFlow<List<SupportChatMessage>> = repository.observeSupportChatMessages(currentUserId)
        .map { list -> list.sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "Error observing support messages", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val myCityRequests: StateFlow<List<CityApprovalRequest>> = repository.observeMyCityRequests(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "Error observing city requests", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val myParcelRiderRequests: StateFlow<List<ParcelRiderRequest>> = repository.observeMyParcelRiderRequests(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE } }
        .catch { Log.w(TAG, "Error observing parcel/rider requests", it.unwrapFirestore()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    init {
        viewModelScope.launch {
            runCatching {
                repository.ensureUserProfile(
                    userId = currentUserId,
                    fullName = currentUserName,
                    email = currentUserEmail,
                    avatarUrl = currentUserPhoto
                )
            }
        }
    }

    private fun <T> Flow<T>.asUiState(label: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = error.unwrapFirestore()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $label", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load $label"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private fun Throwable.unwrapFirestore(): FirebaseFirestoreException? =
        generateSequence(this) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showMessage(msg: String) {
        _toastMessage.value = msg
    }

    fun setCity(city: String) {
        _selectedMultanDestination.value = null
        viewModelScope.launch {
            prefs.setSelectedCity(city)
            val profile = userProfile.value
            if (profile != null) {
                runCatching {
                    repository.updateUserProfile(
                        userId = currentUserId,
                        fullName = profile.fullName,
                        phone = profile.phone,
                        city = city
                    )
                }
            }
        }
    }

    fun setMultanDestination(location: String) {
        val clean = location.trim()
        if (clean.isNotBlank()) {
            _selectedMultanDestination.value = clean
        }
    }

    fun submitCityApprovalRequest(cityName: String, onSuccess: () -> Unit = {}) {
        val clean = cityName.trim()
        if (clean.length < 2) {
            _toastMessage.value = "City name must be at least 2 characters"
            return
        }
        val approvedCities = platformSettings.value.cities
        val alreadyApproved = approvedCities.firstOrNull { it.equals(clean, ignoreCase = true) }
        if (alreadyApproved != null) {
            setCity(alreadyApproved)
            _toastMessage.value = "$alreadyApproved is already approved and selected!"
            onSuccess()
            return
        }
        val requesterName = userProfile.value?.fullName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            runCatching {
                repository.submitCityApprovalRequest(clean, requesterName)
            }.onSuccess {
                _toastMessage.value = "City '$clean' submitted for Super Admin approval!"
                onSuccess()
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Failed to submit city request"
            }
        }
    }

    fun submitCustomerParcelRiderRequest(
        requestType: String,
        pickupAddress: String,
        dropoffAddress: String,
        city: String,
        recipientName: String,
        recipientPhone: String,
        details: String,
        offeredFeePkr: Double,
        onSuccess: () -> Unit = {}
    ) {
        if (pickupAddress.trim().length < 3 || offeredFeePkr < 50.0) {
            _toastMessage.value = "Enter a valid pickup address & offered fee (min PKR 50)"
            return
        }
        val profile = userProfile.value
        val resolvedName = profile?.fullName?.ifBlank { currentUserName } ?: currentUserName
        val safeCity = city.trim()
            .takeUnless { it.isBlank() || it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            ?: platformSettings.value.cities.firstOrNull()
            ?: GharTakConstants.DEFAULT_CITY
        viewModelScope.launch {
            runCatching {
                val reqId = repository.createParcelRiderRequest(
                    ParcelRiderRequest(
                        requestType = requestType,
                        requesterId = currentUserId,
                        restaurantId = "customer_direct",
                        restaurantName = "Customer: ${resolvedName.take(80)}",
                        pickupAddress = pickupAddress.trim(),
                        dropoffAddress = dropoffAddress.trim().ifBlank { safeCity },
                        city = safeCity,
                        recipientName = recipientName.trim().ifBlank { resolvedName },
                        recipientPhone = recipientPhone.trim().ifBlank { profile?.phone ?: "+92" },
                        details = details.trim(),
                        offeredFeePkr = offeredFeePkr.coerceIn(50.0, 50000.0)
                    )
                )
                val label = if (requestType == "send_parcel") "Send Parcel" else "Hire a Rider"
                repository.createNotification(
                    userId = currentUserId,
                    title = "$label Request Submitted",
                    body = "Your $label request in $safeCity is pending Super Admin approval before dispatch to riders.",
                    type = "system"
                )
                reqId
            }.onSuccess {
                _toastMessage.value = "Request submitted! Waiting for Admin approval before rider dispatch."
                onSuccess()
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Failed to submit rider request"
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }
    }

    fun markOnboardingSeen() {
        viewModelScope.launch { prefs.setOnboardingSeen(true) }
    }

    fun openAdminLogin() {
        _adminLoginError.value = null
        _showAdminLogin.value = true
    }

    fun dismissAdminLogin() {
        _adminLoginError.value = null
        _showAdminLogin.value = false
        if (userProfile.value?.activePortal == AppPortal.ADMIN.key && !_adminSessionUnlocked.value) {
            switchPortal(AppPortal.CUSTOMER)
        }
    }

    fun loginAdmin(email: String, password: String) {
        if (email.trim().length < 2 || password.length < 4) {
            _adminLoginError.value = "Enter a valid Admin Login ID and password (min 4 characters)."
            return
        }
        viewModelScope.launch {
            _adminLoginLoading.value = true
            _adminLoginError.value = null
            runCatching {
                repository.activateAdminSession(
                    userId = currentUserId,
                    fullName = currentUserName,
                    email = currentUserEmail,
                    avatarUrl = currentUserPhoto,
                    loginEmail = email,
                    loginPassword = password,
                    isBootstrappedAdmin = isBootstrappedAdmin
                )
            }.onSuccess {
                _adminLoginLoading.value = false
                _adminSessionUnlocked.value = true
                _showAdminLogin.value = false
                _toastMessage.value = "GT Service Admin Panel unlocked!"
            }.onFailure { err ->
                _adminLoginLoading.value = false
                _adminLoginError.value = err.localizedMessage ?: "Failed to unlock Admin Panel"
            }
        }
    }

    fun exitAdminPortal() {
        _adminSessionUnlocked.value = false
        _showAdminLogin.value = false
        switchPortal(AppPortal.CUSTOMER)
    }

    fun switchPortal(portal: AppPortal) {
        if (portal == AppPortal.ADMIN && !_adminSessionUnlocked.value) {
            openAdminLogin()
            return
        }
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            runCatching {
                repository.updateUserProfile(
                    userId = currentUserId,
                    fullName = profile.fullName,
                    phone = profile.phone,
                    city = profile.city,
                    activePortal = portal.key
                )
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Failed to switch portal"
            }
        }
    }

    fun saveProfile(fullName: String, phone: String, city: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            runCatching {
                repository.updateUserProfile(currentUserId, fullName, phone, city, avatarUrl = avatarUrl)
                prefs.setSelectedCity(city)
            }.onSuccess {
                _toastMessage.value = "Profile saved"
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Could not save profile"
            }
        }
    }

    fun saveAddress(address: DeliveryAddress) {
        viewModelScope.launch {
            runCatching {
                repository.saveAddress(currentUserId, address.copy(userId = currentUserId))
            }.onSuccess {
                _toastMessage.value = "Address saved"
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Failed to save address"
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            runCatching {
                repository.deleteAddress(currentUserId, addressId)
            }.onSuccess {
                _toastMessage.value = "Address removed"
            }
        }
    }

    fun toggleFavorite(targetId: String, targetType: String, title: String, subtitle: String, imageUrl: String?) {
        val isFav = favorites.value.any { it.targetId == targetId && it.targetType == targetType }
        viewModelScope.launch {
            runCatching {
                repository.toggleFavorite(
                    currentUserId,
                    FavoriteItem(
                        userId = currentUserId,
                        targetId = targetId,
                        targetType = targetType,
                        title = title,
                        subtitle = subtitle,
                        imageUrl = imageUrl
                    ),
                    currentlyFavorited = isFav
                )
            }.onSuccess {
                _toastMessage.value = if (isFav) "Removed from favorites" else "Added to favorites"
            }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(currentUserId, notifId)
        }
    }

    fun topUpWallet(
        amountPkr: Double,
        channel: String,
        note: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (amountPkr <= 0.0 || amountPkr > 50000.0) {
            _toastMessage.value = "Enter an amount between PKR 1 and PKR 50,000"
            return
        }
        viewModelScope.launch {
            runCatching {
                val newBalance = repository.topUpWallet(currentUserId, amountPkr, channel, note)
                val (targetNumber, targetTitle) = resolvePaymentAccount(channel)
                val cleanNote = note.trim()
                val isTransferChannel = channel.equals("JazzCash", ignoreCase = true) ||
                    channel.equals("Easypaisa", ignoreCase = true) ||
                    targetNumber.isNotBlank()

                if (isTransferChannel && targetNumber.isNotBlank()) {
                    val refPart = if (cleanNote.isNotBlank()) " • Ref/TID: $cleanNote" else ""
                    val paymentMsg =
                        "Assalam-o-Alaikum, I have sent ${GharTakConstants.formatPkr(amountPkr)} via $channel to your number $targetNumber ($targetTitle) for Wallet top-up.$refPart"
                    runCatching {
                        val profile = userProfile.value
                        val name = profile?.fullName?.ifBlank { currentUserName } ?: currentUserName
                        val activeSupport = platformSettings.value.supportPhoneLocal.ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
                        val phone = profile?.phone?.ifBlank { activeSupport } ?: activeSupport
                        repository.sendSupportChatMessage(
                            threadUserId = currentUserId,
                            senderRole = "customer",
                            senderName = name,
                            customerName = name,
                            customerPhone = phone,
                            text = paymentMsg,
                            supportPhone = activeSupport
                        )
                    }
                }

                repository.createNotification(
                    userId = currentUserId,
                    title = "Wallet Top-Up Successful",
                    body = if (isTransferChannel && targetNumber.isNotBlank()) {
                        "${GharTakConstants.formatPkr(amountPkr)} added via $channel ($targetNumber). Confirmation message sent to GT Service. New balance: ${GharTakConstants.formatPkr(newBalance)}."
                    } else {
                        "${GharTakConstants.formatPkr(amountPkr)} added via $channel. New balance: ${GharTakConstants.formatPkr(newBalance)}."
                    },
                    type = "system"
                )
                newBalance to (isTransferChannel && targetNumber.isNotBlank())
            }.onSuccess { (newBalance, sentConfirm) ->
                _toastMessage.value = if (sentConfirm) {
                    "Wallet topped up & payment confirmation sent to GT Service! Balance: ${GharTakConstants.formatPkr(newBalance)}"
                } else {
                    "Wallet topped up! Balance: ${GharTakConstants.formatPkr(newBalance)}"
                }
                onSuccess()
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Wallet top-up failed"
            }
        }
    }

    private fun resolvePaymentAccount(channel: String): Pair<String, String> {
        val settings = platformSettings.value
        return when {
            channel.equals("JazzCash", ignoreCase = true) -> {
                settings.jazzCashNumber.ifBlank { GharTakConstants.DEFAULT_JAZZCASH_NUMBER } to
                    settings.jazzCashAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
            }
            channel.equals("Easypaisa", ignoreCase = true) -> {
                settings.easypaisaNumber.ifBlank { GharTakConstants.DEFAULT_EASYPAISA_NUMBER } to
                    settings.easypaisaAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
            }
            else -> {
                val matched = settings.beneficiaryAccounts.firstOrNull {
                    it.channel.equals(channel, ignoreCase = true)
                }
                if (matched != null) {
                    matched.accountNumber to matched.accountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
                } else {
                    "" to ""
                }
            }
        }
    }

    fun sendPaymentSentConfirmation(
        paymentChannel: String,
        amountPkr: Double,
        targetNumber: String,
        targetTitle: String,
        orderId: String = "",
        referenceNote: String = "",
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                val profile = userProfile.value
                val name = profile?.fullName?.ifBlank { currentUserName } ?: currentUserName
                val activeSupport = platformSettings.value.supportPhoneLocal.ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
                val phone = profile?.phone?.ifBlank { activeSupport } ?: activeSupport
                val cleanRef = referenceNote.trim()
                val orderPart = if (orderId.isNotBlank()) " for Order #${orderId.takeLast(6).uppercase()}" else ""
                val refPart = if (cleanRef.isNotBlank()) " • TID/Ref: $cleanRef" else ""
                val messageText =
                    "Assalam-o-Alaikum, I have sent ${GharTakConstants.formatPkr(amountPkr)} via $paymentChannel to your number $targetNumber ($targetTitle)$orderPart.$refPart"

                repository.sendSupportChatMessage(
                    threadUserId = currentUserId,
                    senderRole = "customer",
                    senderName = name,
                    customerName = name,
                    customerPhone = phone,
                    text = messageText,
                    orderId = orderId,
                    supportPhone = activeSupport
                )
                repository.createNotification(
                    userId = currentUserId,
                    title = "$paymentChannel Payment Message Sent",
                    body = "We notified GT Service that you sent ${GharTakConstants.formatPkr(amountPkr)} to $targetNumber.",
                    type = if (orderId.isNotBlank()) "order" else "system",
                    orderId = orderId
                )
                messageText
            }.onSuccess { sentText ->
                _toastMessage.value = "Message sent: I have sent payment to $targetNumber!"
                onSuccess(sentText)
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Failed to send payment confirmation"
            }
        }
    }

    // --- Separate Food & Grocery Cart Management ---

    private fun resolveOpenRestaurant(item: FoodItem, passed: Restaurant?): Restaurant? =
        passed ?: (restaurantsState.value as? UiState.Success)?.data?.firstOrNull { it.id == item.restaurantId }

    private fun resolveOpenGroceryStore(product: GroceryProduct, passed: GroceryStore?): GroceryStore? =
        passed ?: (groceryStoresState.value as? UiState.Success)?.data?.firstOrNull { it.id == product.storeId }

    private fun resolveOpenMedicalStore(product: MedicalProduct, passed: MedicalStore?): MedicalStore? =
        passed ?: (medicalStoresState.value as? UiState.Success)?.data?.firstOrNull { it.id == product.storeId }

    fun addFoodToCart(
        item: FoodItem,
        restaurant: Restaurant?,
        note: String = "",
        quantity: Int = 1,
        extraAddonsPricePkr: Double = 0.0
    ) {
        val resolvedRestaurant = resolveOpenRestaurant(item, restaurant)
        if (resolvedRestaurant != null && !resolvedRestaurant.isCurrentlyOpenNow()) {
            _toastMessage.value = "${resolvedRestaurant.name} is currently closed (${resolvedRestaurant.operatingHoursLabel()})."
            return
        }
        if (!item.isAvailable || item.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${item.name} is currently unavailable."
            return
        }
        val clampedQty = quantity.coerceIn(1, item.stockQuantity.toInt().coerceAtLeast(1))
        val effectiveUnitPrice = (item.effectivePricePkr + extraAddonsPricePkr.coerceAtLeast(0.0))
        val cleanNote = note.trim()
        val current = _foodCart.value
        val existingVendor = current.firstOrNull()?.vendorId
        val nextList = if (existingVendor != null && existingVendor != item.restaurantId) {
            // Single-vendor food cart per order so vendor receives only their order
            listOf(
                CartItem(
                    productId = item.id,
                    name = item.name,
                    vendorId = item.restaurantId,
                    vendorName = item.restaurantName,
                    vendorOwnerId = resolvedRestaurant?.ownerId ?: item.ownerId,
                    orderType = "food",
                    unitPricePkr = effectiveUnitPrice,
                    unit = "serving",
                    quantity = clampedQty,
                    imageUrl = item.imageUrl,
                    customizationNote = cleanNote,
                    baseUnitPricePkr = item.effectivePricePkr
                )
            )
        } else {
            val match = current.firstOrNull {
                it.productId == item.id &&
                    it.customizationNote == cleanNote &&
                    kotlin.math.abs(it.unitPricePkr - effectiveUnitPrice) < 0.01
            }
            if (match != null) {
                current.map {
                    if (it.productId == item.id && it.customizationNote == cleanNote && kotlin.math.abs(it.unitPricePkr - effectiveUnitPrice) < 0.01) {
                        it.copy(quantity = (it.quantity + clampedQty).coerceAtMost(item.stockQuantity.toInt().coerceAtLeast(1)))
                    } else it
                }
            } else {
                current + CartItem(
                    productId = item.id,
                    name = item.name,
                    vendorId = item.restaurantId,
                    vendorName = item.restaurantName,
                    vendorOwnerId = resolvedRestaurant?.ownerId ?: item.ownerId,
                    orderType = "food",
                    unitPricePkr = effectiveUnitPrice,
                    unit = "serving",
                    quantity = clampedQty,
                    imageUrl = item.imageUrl,
                    customizationNote = cleanNote,
                    baseUnitPricePkr = item.effectivePricePkr
                )
            }
        }
        _foodCart.value = nextList
        _preferredCheckoutChannel.value = "food"
        _toastMessage.value = if (existingVendor != null && existingVendor != item.restaurantId) {
            "Started new food order for ${item.restaurantName}"
        } else {
            "${clampedQty}x ${item.name} added to Cart"
        }
    }

    fun buyNowFoodItem(
        item: FoodItem,
        restaurant: Restaurant?,
        note: String = "",
        quantity: Int = 1,
        extraAddonsPricePkr: Double = 0.0,
        onReadyForCheckout: () -> Unit
    ) {
        val resolvedRestaurant = resolveOpenRestaurant(item, restaurant)
        if (resolvedRestaurant != null && !resolvedRestaurant.isCurrentlyOpenNow()) {
            _toastMessage.value = "${resolvedRestaurant.name} is currently closed (${resolvedRestaurant.operatingHoursLabel()})."
            return
        }
        if (!item.isAvailable || item.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${item.name} is currently unavailable."
            return
        }
        val clampedQty = quantity.coerceIn(1, item.stockQuantity.toInt().coerceAtLeast(1))
        val effectiveUnitPrice = (item.effectivePricePkr + extraAddonsPricePkr.coerceAtLeast(0.0))
        val cleanNote = note.trim()
        val current = _foodCart.value
        val existingVendor = current.firstOrNull()?.vendorId
        val nextList = if (existingVendor == item.restaurantId && current.any {
                it.productId == item.id &&
                    it.customizationNote == cleanNote &&
                    kotlin.math.abs(it.unitPricePkr - effectiveUnitPrice) < 0.01
            }) {
            current.map {
                if (it.productId == item.id && it.customizationNote == cleanNote && kotlin.math.abs(it.unitPricePkr - effectiveUnitPrice) < 0.01) {
                    it.copy(quantity = maxOf(it.quantity, clampedQty))
                } else it
            }
        } else if (existingVendor == item.restaurantId) {
            current + CartItem(
                productId = item.id,
                name = item.name,
                vendorId = item.restaurantId,
                vendorName = item.restaurantName,
                vendorOwnerId = resolvedRestaurant?.ownerId ?: item.ownerId,
                orderType = "food",
                unitPricePkr = effectiveUnitPrice,
                unit = "serving",
                quantity = clampedQty,
                imageUrl = item.imageUrl,
                customizationNote = cleanNote,
                baseUnitPricePkr = item.effectivePricePkr
            )
        } else {
            listOf(
                CartItem(
                    productId = item.id,
                    name = item.name,
                    vendorId = item.restaurantId,
                    vendorName = item.restaurantName,
                    vendorOwnerId = resolvedRestaurant?.ownerId ?: item.ownerId,
                    orderType = "food",
                    unitPricePkr = effectiveUnitPrice,
                    unit = "serving",
                    quantity = clampedQty,
                    imageUrl = item.imageUrl,
                    customizationNote = cleanNote,
                    baseUnitPricePkr = item.effectivePricePkr
                )
            )
        }
        _foodCart.value = nextList
        _preferredCheckoutChannel.value = "food"
        onReadyForCheckout()
    }

    fun addGroceryToCart(product: GroceryProduct, store: GroceryStore?) {
        val resolvedStore = resolveOpenGroceryStore(product, store)
        if (resolvedStore != null && !resolvedStore.isCurrentlyOpenNow()) {
            _toastMessage.value = "${resolvedStore.name} is currently closed (${resolvedStore.operatingHoursLabel()})."
            return
        }
        if (!product.isAvailable || product.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${product.name} is currently out of stock."
            return
        }
        val current = _groceryCart.value
        val existingVendor = current.firstOrNull()?.vendorId
        val nextList = if (existingVendor != null && existingVendor != product.storeId) {
            listOf(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    vendorId = product.storeId,
                    vendorName = product.storeName,
                    vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                    orderType = "grocery",
                    unitPricePkr = product.effectivePricePkr,
                    unit = product.unit,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
            )
        } else {
            val match = current.firstOrNull { it.productId == product.id }
            if (match != null) {
                if (match.quantity >= product.stockQuantity) {
                    _toastMessage.value = "Max available stock (${product.stockQuantity} ${product.unit}) reached."
                    return
                }
                current.map {
                    if (it.productId == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                current + CartItem(
                    productId = product.id,
                    name = product.name,
                    vendorId = product.storeId,
                    vendorName = product.storeName,
                    vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                    orderType = "grocery",
                    unitPricePkr = product.effectivePricePkr,
                    unit = product.unit,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
            }
        }
        _groceryCart.value = nextList
        _preferredCheckoutChannel.value = "grocery"
        _toastMessage.value = if (existingVendor != null && existingVendor != product.storeId) {
            "Started new grocery cart for ${product.storeName}"
        } else {
            "${product.name} added to Grocery Cart"
        }
    }

    fun buyNowGroceryProduct(
        product: GroceryProduct,
        store: GroceryStore?,
        onReadyForCheckout: () -> Unit
    ) {
        val resolvedStore = resolveOpenGroceryStore(product, store)
        if (resolvedStore != null && !resolvedStore.isCurrentlyOpenNow()) {
            _toastMessage.value = "${resolvedStore.name} is currently closed (${resolvedStore.operatingHoursLabel()})."
            return
        }
        if (!product.isAvailable || product.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${product.name} is currently out of stock."
            return
        }
        val current = _groceryCart.value
        val existingVendor = current.firstOrNull()?.vendorId
        val nextList = if (existingVendor == product.storeId && current.any { it.productId == product.id }) {
            current
        } else if (existingVendor == product.storeId) {
            current + CartItem(
                productId = product.id,
                name = product.name,
                vendorId = product.storeId,
                vendorName = product.storeName,
                vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                orderType = "grocery",
                unitPricePkr = product.effectivePricePkr,
                unit = product.unit,
                quantity = 1,
                imageUrl = product.imageUrl
            )
        } else {
            listOf(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    vendorId = product.storeId,
                    vendorName = product.storeName,
                    vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                    orderType = "grocery",
                    unitPricePkr = product.effectivePricePkr,
                    unit = product.unit,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
            )
        }
        _groceryCart.value = nextList
        _preferredCheckoutChannel.value = "grocery"
        onReadyForCheckout()
    }

    fun addMedicalToCart(product: MedicalProduct, store: MedicalStore?) {
        val resolvedStore = resolveOpenMedicalStore(product, store)
        if (resolvedStore != null && !resolvedStore.isCurrentlyOpenNow()) {
            _toastMessage.value = "${resolvedStore.name} is currently closed (${resolvedStore.operatingHoursLabel()})."
            return
        }
        if (!product.isAvailable || product.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${product.name} is currently out of stock."
            return
        }
        val current = _medicalCart.value
        val existingVendor = current.firstOrNull()?.vendorId
        val nextList = if (existingVendor != null && existingVendor != product.storeId) {
            listOf(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    vendorId = product.storeId,
                    vendorName = product.storeName,
                    vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                    orderType = "medical",
                    unitPricePkr = product.effectivePricePkr,
                    unit = product.unit,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
            )
        } else {
            val match = current.firstOrNull { it.productId == product.id }
            if (match != null) {
                if (match.quantity >= product.stockQuantity) {
                    _toastMessage.value = "Max available stock (${product.stockQuantity} ${product.unit}) reached."
                    return
                }
                current.map {
                    if (it.productId == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                current + CartItem(
                    productId = product.id,
                    name = product.name,
                    vendorId = product.storeId,
                    vendorName = product.storeName,
                    vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                    orderType = "medical",
                    unitPricePkr = product.effectivePricePkr,
                    unit = product.unit,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
            }
        }
        _medicalCart.value = nextList
        _preferredCheckoutChannel.value = "medical"
        _toastMessage.value = if (existingVendor != null && existingVendor != product.storeId) {
            "Started new pharmacy cart for ${product.storeName}"
        } else {
            "${product.name} added to Medical Cart"
        }
    }

    fun buyNowMedicalProduct(
        product: MedicalProduct,
        store: MedicalStore?,
        onReadyForCheckout: () -> Unit
    ) {
        val resolvedStore = resolveOpenMedicalStore(product, store)
        if (resolvedStore != null && !resolvedStore.isCurrentlyOpenNow()) {
            _toastMessage.value = "${resolvedStore.name} is currently closed (${resolvedStore.operatingHoursLabel()})."
            return
        }
        if (!product.isAvailable || product.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${product.name} is currently out of stock."
            return
        }
        val current = _medicalCart.value
        val existingVendor = current.firstOrNull()?.vendorId
        val nextList = if (existingVendor == product.storeId && current.any { it.productId == product.id }) {
            current
        } else if (existingVendor == product.storeId) {
            current + CartItem(
                productId = product.id,
                name = product.name,
                vendorId = product.storeId,
                vendorName = product.storeName,
                vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                orderType = "medical",
                unitPricePkr = product.effectivePricePkr,
                unit = product.unit,
                quantity = 1,
                imageUrl = product.imageUrl
            )
        } else {
            listOf(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    vendorId = product.storeId,
                    vendorName = product.storeName,
                    vendorOwnerId = resolvedStore?.ownerId ?: product.ownerId,
                    orderType = "medical",
                    unitPricePkr = product.effectivePricePkr,
                    unit = product.unit,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
            )
        }
        _medicalCart.value = nextList
        _preferredCheckoutChannel.value = "medical"
        onReadyForCheckout()
    }

    fun addMultanToCart(item: MultanItem, note: String = "", quantity: Int = 1) {
        if (!item.isAvailable || item.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${item.name} is currently unavailable."
            return
        }
        val clampedQty = quantity.coerceIn(1, item.stockQuantity.toInt().coerceAtLeast(1))
        val cleanNote = note.trim()
        val current = _multanCart.value
        val match = current.firstOrNull { it.productId == item.id && it.customizationNote == cleanNote }
        _multanCart.value = if (match != null) {
            current.map {
                if (it.productId == item.id && it.customizationNote == cleanNote) {
                    it.copy(quantity = (it.quantity + clampedQty).coerceAtMost(item.stockQuantity.toInt().coerceAtLeast(1)))
                } else it
            }
        } else {
            current + CartItem(
                productId = item.id,
                name = item.name,
                vendorId = "gt_multan_hub",
                vendorName = "GT Service • Multan Express",
                vendorOwnerId = "platform_admin",
                orderType = "multan",
                unitPricePkr = item.effectivePricePkr,
                unit = item.unit,
                quantity = clampedQty,
                imageUrl = item.imageUrl,
                customizationNote = cleanNote
            )
        }
        _preferredCheckoutChannel.value = "multan"
        _toastMessage.value = "${clampedQty}x ${item.name} added to Multan Cart!"
    }

    fun buyNowMultanItem(item: MultanItem, note: String = "", quantity: Int = 1, onReadyForCheckout: () -> Unit) {
        if (!item.isAvailable || item.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${item.name} is currently unavailable."
            return
        }
        val clampedQty = quantity.coerceIn(1, item.stockQuantity.toInt().coerceAtLeast(1))
        val cleanNote = note.trim()
        val current = _multanCart.value
        val match = current.firstOrNull { it.productId == item.id && it.customizationNote == cleanNote }
        _multanCart.value = if (match != null) {
            current.map {
                if (it.productId == item.id && it.customizationNote == cleanNote) {
                    it.copy(quantity = maxOf(it.quantity, clampedQty))
                } else it
            }
        } else {
            current + CartItem(
                productId = item.id,
                name = item.name,
                vendorId = "gt_multan_hub",
                vendorName = "GT Service • Multan Express",
                vendorOwnerId = "platform_admin",
                orderType = "multan",
                unitPricePkr = item.effectivePricePkr,
                unit = item.unit,
                quantity = clampedQty,
                imageUrl = item.imageUrl,
                customizationNote = cleanNote
            )
        }
        _preferredCheckoutChannel.value = "multan"
        onReadyForCheckout()
    }

    fun addCustomMultanItemToCart(
        itemName: String,
        brandOrShop: String,
        estimatedPricePkr: Double,
        unit: String = "1 item",
        quantity: Int = 1,
        note: String = "",
        buyNow: Boolean = false,
        onReadyForCheckout: () -> Unit = {}
    ) {
        val cleanName = itemName.trim()
        val cleanBrand = brandOrShop.trim()
        if (cleanName.length < 2) {
            _toastMessage.value = "Please enter the Multan item name."
            return
        }
        val safePrice = estimatedPricePkr.coerceAtLeast(1.0)
        val safeQty = quantity.coerceIn(1, 50)
        val cleanUnit = unit.trim().ifBlank { "1 item" }
        val cleanNote = note.trim().take(200)
        val displayName = if (cleanBrand.isNotBlank() && !cleanName.contains(cleanBrand, ignoreCase = true)) {
            "$cleanName ($cleanBrand)"
        } else {
            cleanName
        }
        val customCartItem = CartItem(
            productId = "custom_multan_${UUID.randomUUID().toString().replace("-", "").take(10)}",
            name = displayName,
            vendorId = "gt_multan_hub",
            vendorName = "GT Service • Multan Express",
            vendorOwnerId = "platform_admin",
            orderType = "multan",
            unitPricePkr = safePrice,
            unit = cleanUnit,
            quantity = safeQty,
            imageUrl = null,
            customizationNote = cleanNote
        )
        _multanCart.value = _multanCart.value + customCartItem
        _preferredCheckoutChannel.value = "multan"
        _toastMessage.value = "${safeQty}x $displayName added to Multan Cart!"
        if (buyNow) {
            onReadyForCheckout()
        }
    }

    fun addCelebrationToCart(item: CelebrationItem, note: String = "", quantity: Int = 1) {
        if (!item.isAvailable || item.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${item.name} is currently unavailable."
            return
        }
        val clampedQty = quantity.coerceIn(1, item.stockQuantity.toInt().coerceAtLeast(1))
        val cleanNote = note.trim()
        val current = _celebrationCart.value
        val match = current.firstOrNull { it.productId == item.id && it.customizationNote == cleanNote }
        _celebrationCart.value = if (match != null) {
            current.map {
                if (it.productId == item.id && it.customizationNote == cleanNote) {
                    it.copy(quantity = (it.quantity + clampedQty).coerceAtMost(item.stockQuantity.toInt().coerceAtLeast(1)))
                } else it
            }
        } else {
            current + CartItem(
                productId = item.id,
                name = item.name,
                vendorId = "gt_celebration_hub",
                vendorName = "GT Service • Celebration Studio",
                vendorOwnerId = "platform_admin",
                orderType = "celebration",
                unitPricePkr = item.effectivePricePkr,
                unit = item.unit,
                quantity = clampedQty,
                imageUrl = item.imageUrl,
                customizationNote = cleanNote
            )
        }
        _preferredCheckoutChannel.value = "celebration"
        _toastMessage.value = "${clampedQty}x ${item.name} added to Celebration Cart!"
    }

    fun buyNowCelebrationItem(item: CelebrationItem, note: String = "", quantity: Int = 1, onReadyForCheckout: () -> Unit) {
        if (!item.isAvailable || item.stockQuantity <= 0L) {
            _toastMessage.value = "Sorry, ${item.name} is currently unavailable."
            return
        }
        val clampedQty = quantity.coerceIn(1, item.stockQuantity.toInt().coerceAtLeast(1))
        val cleanNote = note.trim()
        val current = _celebrationCart.value
        val match = current.firstOrNull { it.productId == item.id && it.customizationNote == cleanNote }
        _celebrationCart.value = if (match != null) {
            current.map {
                if (it.productId == item.id && it.customizationNote == cleanNote) {
                    it.copy(quantity = maxOf(it.quantity, clampedQty))
                } else it
            }
        } else {
            current + CartItem(
                productId = item.id,
                name = item.name,
                vendorId = "gt_celebration_hub",
                vendorName = "GT Service • Celebration Studio",
                vendorOwnerId = "platform_admin",
                orderType = "celebration",
                unitPricePkr = item.effectivePricePkr,
                unit = item.unit,
                quantity = clampedQty,
                imageUrl = item.imageUrl,
                customizationNote = cleanNote
            )
        }
        _preferredCheckoutChannel.value = "celebration"
        onReadyForCheckout()
    }

    fun getMultanLocationsWithFees(
        settings: PlatformSettings = platformSettings.value
    ): List<Pair<String, Double>> {
        val defaultFee = settings.multanDefaultDeliveryFeePkr.takeIf { it > 0.0 } ?: 350.0
        val result = mutableListOf<Pair<String, Double>>()
        val seen = mutableSetOf<String>()
        settings.multanCityDeliveryFees.forEach { rule ->
            val clean = rule.city.trim()
            val key = clean.lowercase()
            if (clean.isNotBlank() && seen.add(key)) {
                result.add(clean to rule.feePkr)
            }
        }
        settings.cities.forEach { city ->
            val clean = city.trim()
            val key = clean.lowercase()
            if (clean.isNotBlank() &&
                !clean.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) &&
                seen.add(key)
            ) {
                result.add(clean to defaultFee)
            }
        }
        if (result.isEmpty()) {
            result.add(GharTakConstants.DEFAULT_CITY to defaultFee)
        }
        return result
    }

    fun resolveMultanLocationAndFee(
        preferredCity: String,
        addressText: String = "",
        explicitDestination: String? = _selectedMultanDestination.value,
        settings: PlatformSettings = platformSettings.value
    ): Pair<String, Double> {
        val defaultFee = settings.multanDefaultDeliveryFeePkr.takeIf { it > 0.0 } ?: 350.0
        val rules = settings.multanCityDeliveryFees.filter { it.city.isNotBlank() }

        val cleanExplicit = explicitDestination?.trim().orEmpty()
            .takeUnless { it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            .orEmpty()
        if (cleanExplicit.isNotBlank()) {
            val explicitRule = rules.firstOrNull { it.city.trim().equals(cleanExplicit, ignoreCase = true) }
            return (explicitRule?.city?.trim() ?: cleanExplicit) to (explicitRule?.feePkr ?: defaultFee)
        }

        val lowerAddress = addressText.trim().lowercase()
        if (lowerAddress.isNotBlank()) {
            val addressMatchedRule = rules
                .sortedByDescending { it.city.trim().length }
                .firstOrNull { rule ->
                    val cleanRuleCity = rule.city.trim().lowercase()
                    cleanRuleCity.length >= 2 && lowerAddress.contains(cleanRuleCity)
                }
            if (addressMatchedRule != null) {
                return addressMatchedRule.city.trim() to addressMatchedRule.feePkr
            }
        }

        val cleanPreferred = preferredCity.trim()
            .takeUnless { it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            .orEmpty()
        if (cleanPreferred.isNotBlank()) {
            val cityRule = rules.firstOrNull { it.city.trim().equals(cleanPreferred, ignoreCase = true) }
            return (cityRule?.city?.trim() ?: cleanPreferred) to (cityRule?.feePkr ?: defaultFee)
        }

        val fallbackLoc = rules.firstOrNull()?.city?.trim()
            ?: settings.cities.firstOrNull { !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            ?: GharTakConstants.DEFAULT_CITY
        val fallbackFee = rules.firstOrNull { it.city.trim().equals(fallbackLoc, ignoreCase = true) }?.feePkr ?: defaultFee
        return fallbackLoc to fallbackFee
    }

    fun resolveMultanCityDeliveryFee(
        city: String,
        settings: PlatformSettings = platformSettings.value,
        addressText: String = ""
    ): Double = resolveMultanLocationAndFee(
        preferredCity = city,
        addressText = addressText,
        explicitDestination = city.trim().takeIf { it.isNotBlank() },
        settings = settings
    ).second

    fun resolveDeliveryFee(
        orderType: String,
        deliveryCity: String,
        addressText: String,
        selectedAreaRule: DeliveryAreaFeeRule? = null,
        vendorId: String = "",
        customerLat: Double? = null,
        customerLng: Double? = null,
        manualDistanceKm: Double? = null,
        explicitMultanDestination: String? = _selectedMultanDestination.value,
        settings: PlatformSettings = platformSettings.value
    ): DeliveryFeeResolution {
        val cleanCity = deliveryCity.trim().ifBlank { GharTakConstants.DEFAULT_CITY }
        if (orderType == "multan") {
            val (resolvedLocation, fee) = resolveMultanLocationAndFee(
                preferredCity = cleanCity,
                addressText = addressText,
                explicitDestination = explicitMultanDestination,
                settings = settings
            )
            return DeliveryFeeResolution(
                feePkr = fee,
                reason = "Multan → $resolvedLocation Express Fee",
                matchedRule = null
            )
        }

        val resolvedDistanceKm = when {
            manualDistanceKm != null && manualDistanceKm >= 0.0 -> manualDistanceKm
            customerLat != null && customerLng != null -> {
                val (hubLat, hubLng) = GharTakConstants.cityHubCoordinates(cleanCity)
                calculateDistanceKm(hubLat, hubLng, customerLat, customerLng)
            }
            else -> null
        }

        val cityRules = settings.deliveryAreaFeeRules.filter { it.city.equals(cleanCity, ignoreCase = true) }
        val explicitMatch = selectedAreaRule?.takeIf { it.city.equals(cleanCity, ignoreCase = true) }
        val lowerAddr = addressText.trim().lowercase()
        val autoMatched = if (explicitMatch == null && lowerAddr.isNotBlank()) {
            cityRules.firstOrNull { rule ->
                val cleanArea = rule.areaName.substringBefore("(").trim().lowercase()
                val tokens = cleanArea.split("/", ",").map { it.trim() }.filter { it.length >= 3 }
                lowerAddr.contains(cleanArea) || tokens.any { lowerAddr.contains(it) }
            }
        } else {
            null
        }
        val activeRule = explicitMatch ?: autoMatched
        if (activeRule != null) {
            return DeliveryFeeResolution(
                feePkr = activeRule.feePkr,
                reason = "Zone/Area: ${activeRule.areaName}",
                matchedRule = activeRule,
                distanceKm = resolvedDistanceKm ?: activeRule.maxDistanceKm,
                isWithinBaseRadius = (resolvedDistanceKm ?: activeRule.maxDistanceKm) <= settings.deliveryBaseRadiusKm
            )
        }

        if (settings.radiusPricingEnabled) {
            val baseRadius = settings.deliveryBaseRadiusKm.coerceAtLeast(0.5)
            val baseRadiusLabel = if (baseRadius == baseRadius.toLong().toDouble()) {
                baseRadius.toLong().toString()
            } else {
                String.format(Locale.US, "%.1f", baseRadius)
            }
            val withinFee = settings.withinRadiusFeePkr.coerceAtLeast(0.0)
            val extraPerKm = settings.extraPerKmFeePkr.coerceAtLeast(0.0)
            val dKm = resolvedDistanceKm ?: 0.0
            return if (dKm <= baseRadius) {
                val distPart = resolvedDistanceKm?.let { " • ${String.format(Locale.US, "%.1f", it)} km" }.orEmpty()
                DeliveryFeeResolution(
                    feePkr = withinFee,
                    reason = "Within $baseRadiusLabel km radius$distPart",
                    matchedRule = null,
                    distanceKm = resolvedDistanceKm,
                    isWithinBaseRadius = true
                )
            } else {
                val extraKm = ceil(dKm - baseRadius).coerceAtLeast(1.0)
                val computedFee = withinFee + extraKm * extraPerKm
                DeliveryFeeResolution(
                    feePkr = computedFee,
                    reason = "${String.format(Locale.US, "%.1f", dKm)} km ($baseRadiusLabel km base + ${extraKm.toInt()} km extra)",
                    matchedRule = null,
                    distanceKm = dKm,
                    isWithinBaseRadius = false
                )
            }
        }

        val vendorFee = when (orderType) {
            "food" -> (restaurantsState.value as? UiState.Success)?.data?.firstOrNull { it.id == vendorId }?.deliveryFeePkr
            "grocery" -> (groceryStoresState.value as? UiState.Success)?.data?.firstOrNull { it.id == vendorId }?.deliveryFeePkr
            "medical" -> (medicalStoresState.value as? UiState.Success)?.data?.firstOrNull { it.id == vendorId }?.deliveryFeePkr
            else -> null
        }
        val fallbackFee = maxOf(settings.baseDeliveryFeePkr, vendorFee ?: 0.0)
        return DeliveryFeeResolution(
            feePkr = fallbackFee,
            reason = "Standard $cleanCity Delivery",
            matchedRule = null,
            distanceKm = resolvedDistanceKm
        )
    }

    private fun cartFlowFor(orderType: String): MutableStateFlow<List<CartItem>> = when (orderType) {
        "food" -> _foodCart
        "medical" -> _medicalCart
        "multan" -> _multanCart
        "celebration" -> _celebrationCart
        else -> _groceryCart
    }

    fun updateCartQuantity(
        orderType: String,
        productId: String,
        delta: Int,
        customizationNote: String? = null
    ) {
        val targetFlow = cartFlowFor(orderType)
        targetFlow.value = targetFlow.value.mapNotNull { item ->
            val noteMatches = customizationNote == null || item.customizationNote == customizationNote
            if (item.productId != productId || !noteMatches) item
            else {
                val next = item.quantity + delta
                if (next <= 0) null else item.copy(quantity = next)
            }
        }
    }

    fun updateCartItemNoteInline(
        orderType: String,
        cartLineId: String,
        newNote: String
    ) {
        val clampedNote = newNote.take(200)
        val targetFlow = cartFlowFor(orderType)
        targetFlow.value = targetFlow.value.map { current ->
            if (current.cartLineId == cartLineId) {
                current.copy(customizationNote = clampedNote)
            } else {
                current
            }
        }
    }

    fun updateCartItemCustomization(
        orderType: String,
        targetItem: CartItem,
        newNote: String,
        newUnitPricePkr: Double = targetItem.unitPricePkr
    ) {
        val cleanNote = newNote.trim().take(200)
        val safePrice = newUnitPricePkr.coerceAtLeast(targetItem.baseUnitPricePkr)
        val targetFlow = cartFlowFor(orderType)
        val updated = targetFlow.value.map { current ->
            if (current.cartLineId == targetItem.cartLineId) {
                current.copy(customizationNote = cleanNote, unitPricePkr = safePrice)
            } else {
                current
            }
        }
        // Merge duplicate rows if updated customization matches an existing entry
        val merged = mutableListOf<CartItem>()
        updated.forEach { candidate ->
            val idx = merged.indexOfFirst {
                it.productId == candidate.productId &&
                    it.customizationNote == candidate.customizationNote &&
                    kotlin.math.abs(it.unitPricePkr - candidate.unitPricePkr) < 0.01
            }
            if (idx >= 0) {
                val existing = merged[idx]
                merged[idx] = existing.copy(quantity = existing.quantity + candidate.quantity)
            } else {
                merged += candidate
            }
        }
        targetFlow.value = merged
        _toastMessage.value = "Updated extras for ${targetItem.name}"
    }

    fun clearCart(orderType: String) {
        when (orderType) {
            "food" -> {
                _foodCart.value = emptyList()
                _appliedFoodCoupon.value = null
            }
            "medical" -> {
                _medicalCart.value = emptyList()
                _appliedMedicalCoupon.value = null
            }
            "multan" -> {
                _multanCart.value = emptyList()
                _appliedMultanCoupon.value = null
            }
            "celebration" -> {
                _celebrationCart.value = emptyList()
                _appliedCelebrationCoupon.value = null
            }
            else -> {
                _groceryCart.value = emptyList()
                _appliedGroceryCoupon.value = null
            }
        }
    }

    fun applyCoupon(orderType: String, rawCode: String, subtotalPkr: Double) {
        val code = rawCode.trim().uppercase()
        if (code.isBlank()) {
            _toastMessage.value = "Enter a coupon code"
            return
        }
        val coupon = activeCoupons.value.firstOrNull { it.code.equals(code, ignoreCase = true) }
        if (coupon == null || !coupon.isActive) {
            _toastMessage.value = "Invalid or inactive coupon code"
            return
        }
        if (subtotalPkr < coupon.minOrderPkr) {
            _toastMessage.value = "Minimum order ${GharTakConstants.formatPkr(coupon.minOrderPkr)} required for $code"
            return
        }
        when (orderType) {
            "food" -> _appliedFoodCoupon.value = coupon
            "medical" -> _appliedMedicalCoupon.value = coupon
            "multan" -> _appliedMultanCoupon.value = coupon
            "celebration" -> _appliedCelebrationCoupon.value = coupon
            else -> _appliedGroceryCoupon.value = coupon
        }
        _toastMessage.value = "Coupon $code applied!"
    }

    fun calculateDiscount(coupon: CouponRecord?, subtotalPkr: Double): Double {
        if (coupon == null || subtotalPkr < coupon.minOrderPkr) return 0.0
        return if (coupon.discountType == "percentage") {
            val raw = subtotalPkr * (coupon.discountValue / 100.0)
            if (coupon.maxDiscountPkr > 0.0) raw.coerceAtMost(coupon.maxDiscountPkr) else raw
        } else {
            coupon.discountValue.coerceAtMost(subtotalPkr)
        }
    }

    fun placeOrder(
        orderType: String,
        address: DeliveryAddress?,
        manualAddressLine: String,
        customerPhone: String,
        paymentMethod: String,
        orderInstructions: String = "",
        paymentReference: String = "",
        notifyPaymentSent: Boolean = true,
        selectedAreaRule: DeliveryAreaFeeRule? = null,
        manualDistanceKm: Double? = null,
        explicitMultanDestination: String? = _selectedMultanDestination.value,
        onSuccess: (String) -> Unit
    ) {
        val cart = cartFlowFor(orderType).value
        if (cart.isEmpty()) {
            _toastMessage.value = "Your cart is empty"
            return
        }
        val addressLine = address?.formattedLine()?.ifBlank { manualAddressLine } ?: manualAddressLine.trim()
        if (addressLine.length < 4) {
            _toastMessage.value = "Please select or enter a delivery address"
            return
        }

        val settings = platformSettings.value
        val profile = userProfile.value
        val baseDeliveryCity = address?.city?.takeIf { it.isNotBlank() }
            ?: selectedCity.value.takeUnless { it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            ?: profile?.city?.ifBlank { GharTakConstants.DEFAULT_CITY }
            ?: GharTakConstants.DEFAULT_CITY
        val deliveryCity = if (orderType == "multan") {
            resolveMultanLocationAndFee(
                preferredCity = baseDeliveryCity,
                addressText = addressLine,
                explicitDestination = explicitMultanDestination,
                settings = settings
            ).first
        } else {
            baseDeliveryCity
        }

        val first = cart.first()
        val subtotal = cart.sumOf { it.unitPricePkr * it.quantity }
        val coupon = when (orderType) {
            "food" -> _appliedFoodCoupon.value
            "medical" -> _appliedMedicalCoupon.value
            "multan" -> _appliedMultanCoupon.value
            "celebration" -> _appliedCelebrationCoupon.value
            else -> _appliedGroceryCoupon.value
        }
        val discount = calculateDiscount(coupon, subtotal)
        val (hubLat, hubLng) = GharTakConstants.cityHubCoordinates(deliveryCity)
        val feeResolution = resolveDeliveryFee(
            orderType = orderType,
            deliveryCity = deliveryCity,
            addressText = addressLine,
            selectedAreaRule = selectedAreaRule,
            vendorId = first.vendorId,
            customerLat = address?.latitude,
            customerLng = address?.longitude,
            manualDistanceKm = manualDistanceKm,
            explicitMultanDestination = explicitMultanDestination,
            settings = settings
        )
        val deliveryFee = feeResolution.feePkr
        val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)
        val commissionRate = when (orderType) {
            "food" -> settings.restaurantCommissionPercent
            "grocery", "medical" -> settings.groceryCommissionPercent
            else -> 0.0
        }
        val commissionPkr = (subtotal * commissionRate) / 100.0
        val vendorEarnings = (subtotal - commissionPkr).coerceAtLeast(0.0)
        val riderEarnings = settings.riderPayoutPkr

        val initialPaymentStatus = when (paymentMethod) {
            "Wallet" -> "paid"
            "COD" -> "pending"
            else -> "awaiting_verification"
        }

        if (paymentMethod == "Wallet") {
            val currentWallet = userProfile.value?.walletBalancePkr ?: 0.0
            if (currentWallet < total) {
                _toastMessage.value = "Insufficient Wallet balance (${GharTakConstants.formatPkr(currentWallet)}). Please top up your Wallet."
                return
            }
        }

        val cleanOrderNote = orderInstructions.trim()
        val cleanPaymentRef = paymentReference.trim()
        val lineItems = cart.mapIndexed { idx, it ->
            val baseItemName = if (it.customizationNote.isNotBlank()) "${it.name} (${it.customizationNote})" else it.name
            val finalName = if (idx == 0 && (cleanOrderNote.isNotBlank() || cleanPaymentRef.isNotBlank())) {
                buildString {
                    append(baseItemName)
                    if (cleanOrderNote.isNotBlank()) append(" • Request: $cleanOrderNote")
                    if (cleanPaymentRef.isNotBlank()) append(" • Payment Ref: $cleanPaymentRef")
                }
            } else {
                baseItemName
            }
            OrderLineItem(
                productId = it.productId,
                name = finalName.take(220),
                quantity = it.quantity.toLong(),
                unitPricePkr = it.unitPricePkr,
                unit = it.unit
            )
        }

        val resolvedName = profile?.fullName?.ifBlank { currentUserName } ?: currentUserName
        val resolvedPhone = customerPhone.ifBlank { profile?.phone ?: "+92" }
        val order = OrderRecord(
            orderType = orderType,
            customerId = currentUserId,
            customerName = resolvedName,
            customerPhone = resolvedPhone,
            vendorId = first.vendorId,
            vendorName = first.vendorName,
            vendorOwnerId = first.vendorOwnerId,
            deliveryAddress = addressLine,
            city = deliveryCity,
            deliveryLat = address?.latitude ?: hubLat,
            deliveryLng = address?.longitude ?: hubLng,
            items = lineItems,
            subtotalPkr = subtotal,
            deliveryFeePkr = deliveryFee,
            discountPkr = discount,
            couponCode = coupon?.code ?: "",
            totalPkr = total,
            commissionPkr = commissionPkr,
            vendorEarningsPkr = vendorEarnings,
            riderEarningsPkr = riderEarnings,
            paymentMethod = paymentMethod,
            paymentStatus = initialPaymentStatus,
            status = "Pending"
        )

        viewModelScope.launch {
            runCatching {
                val orderId = if (paymentMethod == "Wallet") {
                    repository.placeOrderWithWallet(order)
                } else {
                    repository.placeOrder(order)
                }

                val isMobileWalletTransfer = paymentMethod == "JazzCash" || paymentMethod == "Easypaisa"
                if (isMobileWalletTransfer && notifyPaymentSent) {
                    val (targetNumber, targetTitle) = resolvePaymentAccount(paymentMethod)
                    val refPart = if (cleanPaymentRef.isNotBlank()) " • TID/Ref: $cleanPaymentRef" else ""
                    val paymentConfirmMsg =
                        "Assalam-o-Alaikum, I have sent ${GharTakConstants.formatPkr(total)} via $paymentMethod to your number $targetNumber ($targetTitle) for Order #${orderId.takeLast(6).uppercase()}.$refPart"
                    runCatching {
                        val activeSupport = settings.supportPhoneLocal.ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
                        repository.sendSupportChatMessage(
                            threadUserId = currentUserId,
                            senderRole = "customer",
                            senderName = resolvedName,
                            customerName = resolvedName,
                            customerPhone = resolvedPhone.ifBlank { activeSupport },
                            text = paymentConfirmMsg,
                            orderId = orderId,
                            supportPhone = activeSupport
                        )
                    }
                }

                repository.createNotification(
                    userId = currentUserId,
                    title = "Order Placed (#${orderId.takeLast(6).uppercase()})",
                    body = if (isMobileWalletTransfer && notifyPaymentSent) {
                        val (targetNumber, _) = resolvePaymentAccount(paymentMethod)
                        "${first.vendorName} received your $orderType order (${GharTakConstants.formatPkr(total)}). Message sent to GT Service: payment sent to $paymentMethod $targetNumber."
                    } else {
                        "${first.vendorName} received your $orderType order for ${GharTakConstants.formatPkr(total)}."
                    },
                    type = "order",
                    orderId = orderId
                )
                orderId
            }.onSuccess { orderId ->
                clearCart(orderType)
                _toastMessage.value = when {
                    paymentMethod == "Wallet" -> "Order placed & paid from Wallet!"
                    (paymentMethod == "JazzCash" || paymentMethod == "Easypaisa") && notifyPaymentSent ->
                        "Order placed & '$paymentMethod payment sent' message delivered to Admin!"
                    else -> "Order placed successfully!"
                }
                onSuccess(orderId)
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Failed to place order"
            }
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            runCatching {
                val refunded = repository.cancelOrderByCustomer(orderId, reason.ifBlank { "Cancelled by customer" })
                repository.createNotification(
                    userId = currentUserId,
                    title = if (refunded) "Order Cancelled & Refunded" else "Order Cancelled",
                    body = if (refunded) {
                        "Your order #${orderId.takeLast(6).uppercase()} was cancelled and refunded to your GT Service Wallet."
                    } else {
                        "Your order #${orderId.takeLast(6).uppercase()} has been cancelled."
                    },
                    type = "order",
                    orderId = orderId
                )
                refunded
            }.onSuccess { refunded ->
                _toastMessage.value = if (refunded) "Order cancelled & amount refunded to Wallet" else "Order cancelled"
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Order cannot be cancelled at this stage"
            }
        }
    }

    fun reorder(order: OrderRecord) {
        val cartItems = order.items.map {
            CartItem(
                productId = it.productId,
                name = it.name,
                vendorId = order.vendorId,
                vendorName = order.vendorName,
                vendorOwnerId = order.vendorOwnerId,
                orderType = order.orderType,
                unitPricePkr = it.unitPricePkr,
                unit = it.unit,
                quantity = it.quantity.toInt().coerceAtLeast(1)
            )
        }
        when (order.orderType) {
            "food" -> _foodCart.value = cartItems
            "medical" -> _medicalCart.value = cartItems
            "multan" -> _multanCart.value = cartItems
            "celebration" -> _celebrationCart.value = cartItems
            else -> _groceryCart.value = cartItems
        }
        _preferredCheckoutChannel.value = when (order.orderType) {
            "food" -> "food"
            "medical" -> "medical"
            "multan" -> "multan"
            "celebration" -> "celebration"
            else -> "grocery"
        }
        _toastMessage.value = "Items added to ${order.orderType} cart!"
    }

    fun submitReview(orderId: String, targetId: String, targetType: String, targetName: String, rating: Double, comment: String) {
        viewModelScope.launch {
            val name = userProfile.value?.fullName?.ifBlank { currentUserName } ?: currentUserName
            runCatching {
                repository.submitReview(
                    ReviewRecord(
                        userId = currentUserId,
                        userName = name,
                        orderId = orderId,
                        targetId = targetId,
                        targetType = targetType,
                        targetName = targetName,
                        rating = rating,
                        comment = comment
                    )
                )
            }.onSuccess {
                _toastMessage.value = "Thank you for your review!"
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Failed to submit review"
            }
        }
    }

    fun sendSupportMessage(
        text: String,
        orderId: String = "",
        imageUrl: String? = null,
        audioBase64: String? = null,
        audioDurationSec: Long? = null
    ) {
        val clean = text.trim()
        if (clean.isBlank() && imageUrl.isNullOrBlank() && audioBase64.isNullOrBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value
            val custName = profile?.fullName?.ifBlank { currentUserName } ?: currentUserName
            val activeSupport = platformSettings.value.supportPhoneLocal.ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
            val custPhone = profile?.phone?.takeIf { it.isNotBlank() && it != "+92" }
                ?: activeSupport
            runCatching {
                repository.sendSupportChatMessage(
                    threadUserId = currentUserId,
                    customerName = custName,
                    customerPhone = custPhone,
                    senderName = custName,
                    senderRole = "customer",
                    text = clean,
                    orderId = orderId,
                    supportPhone = activeSupport,
                    imageUrl = imageUrl,
                    audioBase64 = audioBase64,
                    audioDurationSec = audioDurationSec
                )
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Failed to send message"
            }
        }
    }

    fun markSupportChatRead() {
        viewModelScope.launch {
            runCatching {
                repository.markSupportChatRead(currentUserId, "customer")
            }
        }
    }

    companion object {
        private const val TAG = "GharTakVM"
        private const val STOP_TIMEOUT_MS = 5000L
    }
}
