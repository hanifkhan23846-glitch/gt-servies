package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.AdminActivityLog
import com.example.data.model.AdminAuthConfig
import com.example.data.model.AppPortal
import com.example.data.model.BeneficiaryAccount
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
import com.example.data.model.RestaurantCredentialRecord
import com.example.data.model.ReviewRecord
import com.example.data.model.RiderProfile
import com.example.data.model.SupportChatMessage
import com.example.data.model.SupportChatThread
import com.example.data.model.UserNotification
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GharTakRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun <T> Flow<T>.wrapFirestoreErrors(
        operationType: OperationType,
        path: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    private inline fun <reified T> Query.observeList(path: String): Flow<List<T>> = flow {
        requireUserId()
        emitAll(
            snapshots().map { snapshot ->
                snapshot.toObjects(T::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }.wrapFirestoreErrors(OperationType.LIST, path)
        )
    }

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        requireUserId()
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .wrapFirestoreErrors(OperationType.GET, path)
        )
    }

    suspend fun ensureUserProfile(
        userId: String,
        fullName: String,
        email: String,
        avatarUrl: String?
    ) {
        val docRef = db.collection("users").document(userId)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to userId,
                    "fullName" to fullName.ifBlank { "GT Service User" }.take(80),
                    "email" to email.take(120),
                    "phone" to "+92",
                    "city" to GharTakConstants.DEFAULT_CITY,
                    "activePortal" to AppPortal.CUSTOMER.key,
                    "avatarUrl" to avatarUrl,
                    "walletBalancePkr" to 0.0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        phone: String,
        city: String,
        activePortal: String? = null,
        avatarUrl: String? = null
    ) {
        val docRef = db.collection("users").document(userId)
        try {
            val updates = mutableMapOf<String, Any>(
                "fullName" to fullName.trim().ifBlank { "GT Service User" }.take(80),
                "phone" to phone.trim().take(24),
                "city" to city.trim().ifBlank { GharTakConstants.DEFAULT_CITY }.take(60),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (activePortal != null) updates["activePortal"] = activePortal
            if (avatarUrl != null) updates["avatarUrl"] = avatarUrl.take(200000)
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeAllUsersAdmin(): Flow<List<UserProfile>> =
        db.collection("users").observeList("users")

    fun observeAdminAuthConfig(): Flow<AdminAuthConfig> = flow {
        requireUserId()
        emitAll(
            db.collection("platformConfig").document("adminAuth")
                .snapshots()
                .map { snap ->
                    snap.toObject(AdminAuthConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: AdminAuthConfig()
                }
                .wrapFirestoreErrors(OperationType.GET, "platformConfig/adminAuth")
        )
    }

    suspend fun updateAdminAuthCredentials(newLoginEmail: String, newPassword: String) {
        val uid = requireUserId()
        val cleanId = newLoginEmail.trim()
        require(cleanId.length in 2..120) { "Admin Login ID must be 2 to 120 characters." }
        require(newPassword.length in 4..64) { "Admin Password must be 4 to 64 characters." }

        val normalizedKey = GharTakConstants.normalizeCredentialKey(cleanId, newPassword)
        val newHash = GharTakConstants.sha256Hex(normalizedKey)
        val configRef = db.collection("platformConfig").document("adminAuth")
        try {
            configRef.set(
                mapOf(
                    "loginEmail" to cleanId,
                    "adminKeyHash" to newHash,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val adminRef = db.collection("admins").document(uid)
            val adminSnap = adminRef.get().await()
            if (adminSnap.exists()) {
                adminRef.update(mapOf("adminKey" to normalizedKey)).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, configRef.path)
            throw e
        }
    }

    suspend fun activateAdminSession(
        userId: String,
        fullName: String,
        email: String,
        avatarUrl: String?,
        loginEmail: String,
        loginPassword: String,
        isBootstrappedAdmin: Boolean = false
    ) {
        require(loginEmail.trim().isNotBlank() && loginPassword.isNotBlank()) {
            "Enter your Admin Login ID and Password."
        }
        val uid = requireUserId()
        require(uid == userId) { "Authenticated user mismatch" }

        ensureUserProfile(userId, fullName, email, avatarUrl)

        val adminKey = GharTakConstants.normalizeCredentialKey(loginEmail, loginPassword)
        val enteredHash = GharTakConstants.sha256Hex(adminKey)

        if (isBootstrappedAdmin) {
            val configSnap = runCatching {
                db.collection("platformConfig").document("adminAuth").get().await()
            }.getOrNull()
            val expectedHash = configSnap?.takeIf { it.exists() }?.getString("adminKeyHash")
                ?: GharTakConstants.LEGACY_ADMIN_KEY_HASH
            if (enteredHash != expectedHash) {
                throw IllegalArgumentException("Invalid Admin Login ID or Password.")
            }
        }

        val adminRef = db.collection("admins").document(userId)
        try {
            val adminSnap = adminRef.get().await()
            val safeEmail = email.ifBlank { "admin@gtservice.pk" }.take(120)
            if (!adminSnap.exists()) {
                adminRef.set(
                    mapOf(
                        "userId" to userId,
                        "email" to safeEmail,
                        "adminKey" to adminKey,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                adminRef.update(
                    mapOf(
                        "email" to safeEmail,
                        "adminKey" to adminKey
                    )
                ).await()
            }
            db.collection("users").document(userId).update(
                mapOf(
                    "activePortal" to AppPortal.ADMIN.key,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                throw IllegalArgumentException("Invalid Admin Login ID or Password.")
            }
            handleFirestoreError(e, OperationType.WRITE, adminRef.path)
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, adminRef.path)
            throw e
        }
    }

    // --- GT Service Wallet ---

    fun observeWalletTransactions(userId: String): Flow<List<WalletTransaction>> =
        db.collection("users").document(userId).collection("walletTransactions")
            .observeList("users/$userId/walletTransactions")

    suspend fun topUpWallet(
        userId: String,
        amountPkr: Double,
        channel: String,
        note: String = ""
    ): Double {
        val uid = requireUserId()
        require(uid == userId) { "Authenticated user mismatch" }
        require(amountPkr > 0.0 && amountPkr <= 50000.0) { "Top-up amount must be between 1 and 50,000 PKR." }

        val userRef = db.collection("users").document(userId)
        val txId = "wtx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val txRef = userRef.collection("walletTransactions").document(txId)
        val cleanChannel = channel.trim().ifBlank { "JazzCash" }.take(60)
        val cleanNote = note.trim().ifBlank { "Added via $cleanChannel" }.take(300)

        try {
            return db.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val currentBalance = userSnap.getDouble("walletBalancePkr") ?: 0.0
                val nextBalance = (currentBalance + amountPkr).coerceAtMost(1_000_000.0)
                tx.update(
                    userRef,
                    mapOf(
                        "walletBalancePkr" to nextBalance,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    txRef,
                    mapOf(
                        "userId" to userId,
                        "type" to "topup",
                        "amountPkr" to amountPkr,
                        "balanceAfterPkr" to nextBalance,
                        "paymentChannel" to cleanChannel,
                        "referenceId" to txId,
                        "note" to cleanNote,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                nextBalance
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path)
            throw e
        }
    }

    suspend fun adminCreditCustomerWallet(
        targetUserId: String,
        amountPkr: Double,
        reason: String
    ): Double {
        requireUserId()
        require(amountPkr > 0.0 && amountPkr <= 500000.0) { "Credit amount must be between 1 and 500,000 PKR." }

        val userRef = db.collection("users").document(targetUserId)
        val txId = "wtx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val txRef = userRef.collection("walletTransactions").document(txId)
        val cleanNote = reason.trim().ifBlank { "Admin promotional wallet credit" }.take(300)

        try {
            return db.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val currentBalance = userSnap.getDouble("walletBalancePkr") ?: 0.0
                val nextBalance = (currentBalance + amountPkr).coerceAtMost(1_000_000.0)
                tx.update(
                    userRef,
                    mapOf(
                        "walletBalancePkr" to nextBalance,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    txRef,
                    mapOf(
                        "userId" to targetUserId,
                        "type" to "admin_credit",
                        "amountPkr" to amountPkr,
                        "balanceAfterPkr" to nextBalance,
                        "paymentChannel" to "Admin Credit",
                        "referenceId" to txId,
                        "note" to cleanNote,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                nextBalance
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path)
            throw e
        }
    }

    // --- Addresses ---

    fun observeAddresses(userId: String): Flow<List<DeliveryAddress>> =
        db.collection("users").document(userId).collection("addresses")
            .observeList("users/$userId/addresses")

    suspend fun saveAddress(userId: String, address: DeliveryAddress): String {
        val id = address.id.ifBlank { UUID.randomUUID().toString().replace("-", "").take(20) }
        val docRef = db.collection("users").document(userId).collection("addresses").document(id)
        try {
            if (address.id.isBlank()) {
                val payload = mapOf(
                    "userId" to userId,
                    "label" to address.label,
                    "houseNumber" to address.houseNumber.take(40),
                    "street" to address.street.take(80),
                    "area" to address.area.take(80),
                    "city" to address.city.take(60),
                    "latitude" to address.latitude,
                    "longitude" to address.longitude,
                    "isDefault" to address.isDefault,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "label" to address.label,
                    "houseNumber" to address.houseNumber.take(40),
                    "street" to address.street.take(80),
                    "area" to address.area.take(80),
                    "city" to address.city.take(60),
                    "latitude" to address.latitude,
                    "longitude" to address.longitude,
                    "isDefault" to address.isDefault,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAddress(userId: String, addressId: String) {
        val docRef = db.collection("users").document(userId).collection("addresses").document(addressId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Favorites & Notifications ---

    fun observeFavorites(userId: String): Flow<List<FavoriteItem>> =
        db.collection("users").document(userId).collection("favorites")
            .observeList("users/$userId/favorites")

    suspend fun toggleFavorite(userId: String, fav: FavoriteItem, currentlyFavorited: Boolean) {
        val docId = "${fav.targetType}_${fav.targetId}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val docRef = db.collection("users").document(userId).collection("favorites").document(docId)
        try {
            if (currentlyFavorited) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to userId,
                    "targetId" to fav.targetId,
                    "targetType" to fav.targetType,
                    "title" to fav.title.take(100),
                    "subtitle" to fav.subtitle.take(150),
                    "imageUrl" to fav.imageUrl?.take(200000),
                    "createdAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeNotifications(userId: String): Flow<List<UserNotification>> =
        db.collection("users").document(userId).collection("notifications")
            .observeList("users/$userId/notifications")

    suspend fun createNotification(userId: String, title: String, body: String, type: String, orderId: String = "") {
        val id = "notif_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(userId).collection("notifications").document(id)
        try {
            docRef.set(
                mapOf(
                    "userId" to userId,
                    "title" to title.take(120),
                    "body" to body.take(400),
                    "type" to type,
                    "orderId" to orderId.take(128),
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    suspend fun markNotificationRead(userId: String, notifId: String) {
        val docRef = db.collection("users").document(userId).collection("notifications").document(notifId)
        try {
            docRef.update(mapOf("isRead" to true)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    // --- Categories ---

    fun observeActiveFoodCategories(): Flow<List<Category>> =
        db.collection("foodCategories")
            .whereEqualTo("isActive", true)
            .observeList("foodCategories")

    fun observeAllFoodCategoriesAdmin(): Flow<List<Category>> =
        db.collection("foodCategories").observeList("foodCategories")

    fun observeActiveGroceryCategories(): Flow<List<Category>> =
        db.collection("groceryCategories")
            .whereEqualTo("isActive", true)
            .observeList("groceryCategories")

    fun observeAllGroceryCategoriesAdmin(): Flow<List<Category>> =
        db.collection("groceryCategories").observeList("groceryCategories")

    fun observeActiveMedicalCategories(): Flow<List<Category>> =
        db.collection("medicalCategories")
            .whereEqualTo("isActive", true)
            .observeList("medicalCategories")

    fun observeAllMedicalCategoriesAdmin(): Flow<List<Category>> =
        db.collection("medicalCategories").observeList("medicalCategories")

    suspend fun saveCategory(collection: String, category: Category): String {
        val id = category.id.ifBlank { "cat_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection(collection).document(id)
        try {
            if (category.id.isBlank()) {
                docRef.set(
                    mapOf(
                        "name" to category.name.trim().take(60),
                        "iconKey" to category.iconKey.take(40),
                        "isActive" to category.isActive,
                        "sortOrder" to category.sortOrder,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "name" to category.name.trim().take(60),
                        "iconKey" to category.iconKey.take(40),
                        "isActive" to category.isActive,
                        "sortOrder" to category.sortOrder,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCategory(collection: String, categoryId: String) {
        val docRef = db.collection(collection).document(categoryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Restaurants & Food Items ---

    fun observeApprovedRestaurants(): Flow<List<Restaurant>> =
        db.collection("restaurants")
            .whereEqualTo("approvalStatus", "approved")
            .observeList("restaurants")

    fun observeMyRestaurants(ownerId: String): Flow<List<Restaurant>> =
        db.collection("restaurants")
            .whereEqualTo("ownerId", ownerId)
            .observeList("restaurants")

    fun observeAllRestaurantsAdmin(): Flow<List<Restaurant>> =
        db.collection("restaurants").observeList("restaurants")

    suspend fun saveRestaurant(restaurant: Restaurant, isAdminOverride: Boolean = false): String {
        val uid = requireUserId()
        val id = restaurant.id.ifBlank { "rest_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("restaurants").document(id)
        try {
            if (restaurant.id.isBlank()) {
                val payload = mapOf(
                    "ownerId" to (restaurant.ownerId.ifBlank { uid }),
                    "name" to restaurant.name.trim().take(100),
                    "ownerName" to restaurant.ownerName.trim().take(80),
                    "phone" to restaurant.phone.trim().take(24),
                    "email" to restaurant.email.trim().take(120),
                    "address" to restaurant.address.trim().take(200),
                    "city" to restaurant.city.trim().take(60),
                    "imageUrl" to restaurant.imageUrl?.take(200000),
                    "description" to restaurant.description?.take(500),
                    "deliveryTimeMinutes" to restaurant.deliveryTimeMinutes,
                    "deliveryFeePkr" to restaurant.deliveryFeePkr,
                    "minOrderPkr" to restaurant.minOrderPkr,
                    "rating" to if (isAdminOverride) restaurant.rating else 5.0,
                    "reviewCount" to if (isAdminOverride) restaurant.reviewCount else 0L,
                    "approvalStatus" to if (isAdminOverride) restaurant.approvalStatus else "pending",
                    "isOpen" to restaurant.isOpen,
                    "openingTime" to restaurant.openingTime.trim().ifBlank { "00:00" }.take(5),
                    "closingTime" to restaurant.closingTime.trim().ifBlank { "23:59" }.take(5),
                    "commissionPercent" to restaurant.commissionPercent,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "name" to restaurant.name.trim().take(100),
                    "phone" to restaurant.phone.trim().take(24),
                    "address" to restaurant.address.trim().take(200),
                    "city" to restaurant.city.trim().take(60),
                    "deliveryTimeMinutes" to restaurant.deliveryTimeMinutes,
                    "deliveryFeePkr" to restaurant.deliveryFeePkr,
                    "minOrderPkr" to restaurant.minOrderPkr,
                    "isOpen" to restaurant.isOpen,
                    "openingTime" to restaurant.openingTime.trim().ifBlank { "00:00" }.take(5),
                    "closingTime" to restaurant.closingTime.trim().ifBlank { "23:59" }.take(5),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                restaurant.imageUrl?.let { updates["imageUrl"] = it.take(200000) }
                restaurant.description?.let { updates["description"] = it.take(500) }
                if (isAdminOverride) {
                    updates["approvalStatus"] = restaurant.approvalStatus
                    updates["commissionPercent"] = restaurant.commissionPercent
                }
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateRestaurantApproval(restaurantId: String, status: String, commissionPercent: Double? = null) {
        val docRef = db.collection("restaurants").document(restaurantId)
        try {
            val updates = mutableMapOf<String, Any>(
                "approvalStatus" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (commissionPercent != null) updates["commissionPercent"] = commissionPercent
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Per-Restaurant Portal Credentials & Scoped Staff Sessions ---

    fun observeAdminRestaurantCredentials(): Flow<List<RestaurantCredentialRecord>> =
        db.collection("adminRestaurantCredentials").observeList("adminRestaurantCredentials")

    suspend fun saveRestaurantCredentialsAdmin(
        restaurantId: String,
        restaurantName: String,
        loginId: String,
        password: String
    ) {
        val cleanLoginId = loginId.trim().lowercase()
        require(cleanLoginId.length in 2..80) { "Restaurant Login ID must be 2 to 80 characters." }
        require(password.length in 4..64) { "Restaurant Password must be 4 to 64 characters." }

        val passHash = GharTakConstants.sha256Hex("$cleanLoginId:$password")
        val restRef = db.collection("restaurants").document(restaurantId)
        val credRef = db.collection("adminRestaurantCredentials").document(restaurantId)
        try {
            val batch = db.batch()
            batch.update(
                restRef,
                mapOf(
                    "portalLoginId" to cleanLoginId,
                    "portalPassHash" to passHash,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                credRef,
                mapOf(
                    "restaurantId" to restaurantId,
                    "restaurantName" to restaurantName.trim().take(100),
                    "loginId" to cleanLoginId,
                    "password" to password,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, credRef.path)
            throw e
        }
    }

    fun observeStaffSessionRestaurantIds(userId: String): Flow<List<String>> = flow {
        requireUserId()
        val path = "staffSessions/$userId"
        emitAll(
            db.collection("staffSessions").document(userId)
                .snapshots()
                .map { snap ->
                    if (!snap.exists()) emptyList()
                    else (snap.get("restaurantIds") as? List<*>)?.filterIsInstance<String>().orEmpty()
                }
                .wrapFirestoreErrors(OperationType.GET, path)
        )
    }

    fun observeRestaurantsByIds(ids: List<String>): Flow<List<Restaurant>> {
        val cleanIds = ids.filter { it.isNotBlank() }.distinct().take(10)
        if (cleanIds.isEmpty()) return flowOf(emptyList())
        return db.collection("restaurants")
            .whereIn(FieldPath.documentId(), cleanIds)
            .observeList("restaurants")
    }

    fun observeFoodItemsByRestaurantIds(ids: List<String>): Flow<List<FoodItem>> {
        val cleanIds = ids.filter { it.isNotBlank() }.distinct().take(10)
        if (cleanIds.isEmpty()) return flowOf(emptyList())
        return db.collection("foodItems")
            .whereIn("restaurantId", cleanIds)
            .observeList("foodItems")
    }

    fun observeOrdersByVendorIds(ids: List<String>): Flow<List<OrderRecord>> {
        val cleanIds = ids.filter { it.isNotBlank() }.distinct().take(10)
        if (cleanIds.isEmpty()) return flowOf(emptyList())
        return db.collection("orders")
            .whereIn("vendorId", cleanIds)
            .observeList("orders")
    }

    fun observeParcelRequestsByRestaurantIds(ids: List<String>): Flow<List<ParcelRiderRequest>> {
        val cleanIds = ids.filter { it.isNotBlank() }.distinct().take(10)
        if (cleanIds.isEmpty()) return flowOf(emptyList())
        return db.collection("parcelRiderRequests")
            .whereIn("restaurantId", cleanIds)
            .observeList("parcelRiderRequests")
    }

    suspend fun loginRestaurantWithCredentials(loginId: String, password: String): Restaurant {
        val uid = requireUserId()
        val cleanLoginId = loginId.trim().lowercase()
        require(cleanLoginId.length in 2..80 && password.length in 4..64) {
            "Enter a valid Restaurant Login ID and password (min 4 chars)."
        }

        val matching = db.collection("restaurants")
            .whereEqualTo("approvalStatus", "approved")
            .whereEqualTo("portalLoginId", cleanLoginId)
            .get()
            .await()
            .toObjects(Restaurant::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)

        val restaurant = matching.firstOrNull()
            ?: throw IllegalArgumentException("No approved restaurant found for Login ID '$cleanLoginId'.")

        val loginKey = "$cleanLoginId:$password"
        if (GharTakConstants.sha256Hex(loginKey) != restaurant.portalPassHash) {
            throw IllegalArgumentException("Incorrect password for ${restaurant.name}.")
        }

        val accessRef = db.collection("restaurantAccess").document("${restaurant.id}_$uid")
        val sessionRef = db.collection("staffSessions").document(uid)
        try {
            accessRef.set(
                mapOf(
                    "userId" to uid,
                    "restaurantId" to restaurant.id,
                    "loginId" to cleanLoginId,
                    "loginKey" to loginKey,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val sessSnap = sessionRef.get().await()
            if (!sessSnap.exists()) {
                sessionRef.set(
                    mapOf(
                        "userId" to uid,
                        "lastRestaurantId" to restaurant.id,
                        "restaurantIds" to listOf(restaurant.id),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val existingIds = (sessSnap.get("restaurantIds") as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()
                val updatedIds = (listOf(restaurant.id) + existingIds).distinct().take(20)
                sessionRef.update(
                    mapOf(
                        "userId" to uid,
                        "lastRestaurantId" to restaurant.id,
                        "restaurantIds" to updatedIds,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            return restaurant
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, sessionRef.path)
            throw e
        }
    }

    suspend fun logoutRestaurantStaffSession(userId: String, restaurantId: String) {
        val sessionRef = db.collection("staffSessions").document(userId)
        val accessRef = db.collection("restaurantAccess").document("${restaurantId}_$userId")
        try {
            val sessSnap = sessionRef.get().await()
            if (sessSnap.exists()) {
                val remaining = (sessSnap.get("restaurantIds") as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()
                    .filter { it != restaurantId }
                if (remaining.isEmpty()) {
                    sessionRef.delete().await()
                } else {
                    sessionRef.update(
                        mapOf(
                            "userId" to userId,
                            "lastRestaurantId" to remaining.first(),
                            "restaurantIds" to remaining,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            runCatching { accessRef.delete().await() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, sessionRef.path)
            throw e
        }
    }

    fun observeAvailableFoodItems(): Flow<List<FoodItem>> =
        db.collection("foodItems")
            .whereEqualTo("isAvailable", true)
            .observeList("foodItems")

    fun observeMyFoodItems(ownerId: String): Flow<List<FoodItem>> =
        db.collection("foodItems")
            .whereEqualTo("ownerId", ownerId)
            .observeList("foodItems")

    fun observeAllFoodItemsAdmin(): Flow<List<FoodItem>> =
        db.collection("foodItems").observeList("foodItems")

    suspend fun saveFoodItem(item: FoodItem, isAdminOverride: Boolean = false): String {
        val uid = requireUserId()
        val id = item.id.ifBlank { "food_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("foodItems").document(id)
        val cleanAdditionalImages = item.additionalImages.filter { it.isNotBlank() }.take(4).map { it.take(200000) }
        val cleanPrepTime = item.preparationTimeMinutes.coerceIn(1L, 300L)
        val cleanImageUrl = item.imageUrl?.takeIf { it.isNotBlank() }?.take(200000)
        val cleanDays = item.availableDays
            .filter { day -> GharTakConstants.DAYS_OF_WEEK.any { it.equals(day, ignoreCase = true) } }
            .map { day -> GharTakConstants.DAYS_OF_WEEK.first { it.equals(day, ignoreCase = true) } }
            .distinct()
            .take(7)
        try {
            if (item.id.isBlank()) {
                val payload = mapOf(
                    "restaurantId" to item.restaurantId,
                    "restaurantName" to item.restaurantName.take(100),
                    "ownerId" to item.ownerId.ifBlank { uid },
                    "categoryId" to item.categoryId,
                    "categoryName" to item.categoryName.take(60),
                    "name" to item.name.trim().take(100),
                    "description" to item.description.trim().take(500),
                    "regularPricePkr" to item.regularPricePkr,
                    "discountPricePkr" to item.discountPricePkr,
                    "imageUrl" to cleanImageUrl,
                    "additionalImages" to cleanAdditionalImages,
                    "preparationTimeMinutes" to cleanPrepTime,
                    "stockQuantity" to item.stockQuantity,
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "availableDays" to cleanDays,
                    "isDailySpecial" to item.isDailySpecial,
                    "city" to item.city.take(60),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "categoryId" to item.categoryId,
                    "categoryName" to item.categoryName.take(60),
                    "name" to item.name.trim().take(100),
                    "description" to item.description.trim().take(500),
                    "regularPricePkr" to item.regularPricePkr,
                    "discountPricePkr" to item.discountPricePkr,
                    "imageUrl" to (cleanImageUrl ?: FieldValue.delete()),
                    "additionalImages" to cleanAdditionalImages,
                    "preparationTimeMinutes" to cleanPrepTime,
                    "stockQuantity" to item.stockQuantity,
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "availableDays" to cleanDays,
                    "isDailySpecial" to item.isDailySpecial,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (isAdminOverride) {
                    updates["restaurantId"] = item.restaurantId
                    updates["restaurantName"] = item.restaurantName.take(100)
                    updates["city"] = item.city.take(60)
                }
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteFoodItem(itemId: String) {
        val docRef = db.collection("foodItems").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Grocery Stores & Products ---

    fun observeApprovedGroceryStores(): Flow<List<GroceryStore>> =
        db.collection("groceryStores")
            .whereEqualTo("approvalStatus", "approved")
            .observeList("groceryStores")

    fun observeMyGroceryStores(ownerId: String): Flow<List<GroceryStore>> =
        db.collection("groceryStores")
            .whereEqualTo("ownerId", ownerId)
            .observeList("groceryStores")

    fun observeAllGroceryStoresAdmin(): Flow<List<GroceryStore>> =
        db.collection("groceryStores").observeList("groceryStores")

    suspend fun saveGroceryStore(store: GroceryStore, isAdminOverride: Boolean = false): String {
        val uid = requireUserId()
        val id = store.id.ifBlank { "store_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("groceryStores").document(id)
        try {
            if (store.id.isBlank()) {
                val payload = mapOf(
                    "ownerId" to (store.ownerId.ifBlank { uid }),
                    "name" to store.name.trim().take(100),
                    "ownerName" to store.ownerName.trim().take(80),
                    "phone" to store.phone.trim().take(24),
                    "email" to store.email.trim().take(120),
                    "address" to store.address.trim().take(200),
                    "city" to store.city.trim().take(60),
                    "imageUrl" to store.imageUrl?.take(200000),
                    "description" to store.description?.take(500),
                    "deliveryTimeMinutes" to store.deliveryTimeMinutes,
                    "deliveryFeePkr" to store.deliveryFeePkr,
                    "minOrderPkr" to store.minOrderPkr,
                    "rating" to if (isAdminOverride) store.rating else 5.0,
                    "reviewCount" to if (isAdminOverride) store.reviewCount else 0L,
                    "approvalStatus" to if (isAdminOverride) store.approvalStatus else "pending",
                    "isOpen" to store.isOpen,
                    "openingTime" to store.openingTime.trim().ifBlank { "00:00" }.take(5),
                    "closingTime" to store.closingTime.trim().ifBlank { "23:59" }.take(5),
                    "commissionPercent" to store.commissionPercent,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "name" to store.name.trim().take(100),
                    "phone" to store.phone.trim().take(24),
                    "address" to store.address.trim().take(200),
                    "city" to store.city.trim().take(60),
                    "deliveryTimeMinutes" to store.deliveryTimeMinutes,
                    "deliveryFeePkr" to store.deliveryFeePkr,
                    "minOrderPkr" to store.minOrderPkr,
                    "isOpen" to store.isOpen,
                    "openingTime" to store.openingTime.trim().ifBlank { "00:00" }.take(5),
                    "closingTime" to store.closingTime.trim().ifBlank { "23:59" }.take(5),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                store.imageUrl?.let { updates["imageUrl"] = it.take(200000) }
                store.description?.let { updates["description"] = it.take(500) }
                if (isAdminOverride) {
                    updates["approvalStatus"] = store.approvalStatus
                    updates["commissionPercent"] = store.commissionPercent
                }
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateGroceryStoreApproval(storeId: String, status: String, commissionPercent: Double? = null) {
        val docRef = db.collection("groceryStores").document(storeId)
        try {
            val updates = mutableMapOf<String, Any>(
                "approvalStatus" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (commissionPercent != null) updates["commissionPercent"] = commissionPercent
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeAvailableGroceryProducts(): Flow<List<GroceryProduct>> =
        db.collection("groceryProducts")
            .whereEqualTo("isAvailable", true)
            .observeList("groceryProducts")

    fun observeMyGroceryProducts(ownerId: String): Flow<List<GroceryProduct>> =
        db.collection("groceryProducts")
            .whereEqualTo("ownerId", ownerId)
            .observeList("groceryProducts")

    fun observeAllGroceryProductsAdmin(): Flow<List<GroceryProduct>> =
        db.collection("groceryProducts").observeList("groceryProducts")

    suspend fun saveGroceryProduct(product: GroceryProduct, isAdminOverride: Boolean = false): String {
        val uid = requireUserId()
        val id = product.id.ifBlank { "groc_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("groceryProducts").document(id)
        val cleanAdditionalImages = product.additionalImages.filter { it.isNotBlank() }.take(4).map { it.take(200000) }
        val cleanUnit = product.unit.trim().ifBlank { "1 kg" }.take(30)
        val cleanImageUrl = product.imageUrl?.takeIf { it.isNotBlank() }?.take(200000)
        try {
            if (product.id.isBlank()) {
                val payload = mapOf(
                    "storeId" to product.storeId,
                    "storeName" to product.storeName.take(100),
                    "ownerId" to product.ownerId.ifBlank { uid },
                    "categoryId" to product.categoryId,
                    "categoryName" to product.categoryName.take(60),
                    "name" to product.name.trim().take(100),
                    "description" to product.description.trim().take(500),
                    "regularPricePkr" to product.regularPricePkr,
                    "discountPricePkr" to product.discountPricePkr,
                    "unit" to cleanUnit,
                    "stockQuantity" to product.stockQuantity,
                    "imageUrl" to cleanImageUrl,
                    "additionalImages" to cleanAdditionalImages,
                    "isAvailable" to product.isAvailable,
                    "isFeatured" to product.isFeatured,
                    "city" to product.city.take(60),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "categoryId" to product.categoryId,
                    "categoryName" to product.categoryName.take(60),
                    "name" to product.name.trim().take(100),
                    "description" to product.description.trim().take(500),
                    "regularPricePkr" to product.regularPricePkr,
                    "discountPricePkr" to product.discountPricePkr,
                    "unit" to cleanUnit,
                    "stockQuantity" to product.stockQuantity,
                    "imageUrl" to (cleanImageUrl ?: FieldValue.delete()),
                    "additionalImages" to cleanAdditionalImages,
                    "isAvailable" to product.isAvailable,
                    "isFeatured" to product.isFeatured,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (isAdminOverride) {
                    updates["storeId"] = product.storeId
                    updates["storeName"] = product.storeName.take(100)
                    updates["city"] = product.city.take(60)
                }
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteGroceryProduct(productId: String) {
        val docRef = db.collection("groceryProducts").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Medical Stores & Products ---

    fun observeApprovedMedicalStores(): Flow<List<MedicalStore>> =
        db.collection("medicalStores")
            .whereEqualTo("approvalStatus", "approved")
            .observeList("medicalStores")

    fun observeMyMedicalStores(ownerId: String): Flow<List<MedicalStore>> =
        db.collection("medicalStores")
            .whereEqualTo("ownerId", ownerId)
            .observeList("medicalStores")

    fun observeAllMedicalStoresAdmin(): Flow<List<MedicalStore>> =
        db.collection("medicalStores").observeList("medicalStores")

    suspend fun saveMedicalStore(store: MedicalStore, isAdminOverride: Boolean = false): String {
        val uid = requireUserId()
        val id = store.id.ifBlank { "med_store_${UUID.randomUUID().toString().replace("-", "").take(14)}" }
        val docRef = db.collection("medicalStores").document(id)
        try {
            if (store.id.isBlank()) {
                val payload = mapOf(
                    "ownerId" to (store.ownerId.ifBlank { uid }),
                    "name" to store.name.trim().take(100),
                    "ownerName" to store.ownerName.trim().take(80),
                    "phone" to store.phone.trim().take(24),
                    "email" to store.email.trim().take(120),
                    "address" to store.address.trim().take(200),
                    "city" to store.city.trim().take(60),
                    "imageUrl" to store.imageUrl?.take(200000),
                    "description" to store.description?.take(500),
                    "deliveryTimeMinutes" to store.deliveryTimeMinutes,
                    "deliveryFeePkr" to store.deliveryFeePkr,
                    "minOrderPkr" to store.minOrderPkr,
                    "rating" to if (isAdminOverride) store.rating else 5.0,
                    "reviewCount" to if (isAdminOverride) store.reviewCount else 0L,
                    "approvalStatus" to if (isAdminOverride) store.approvalStatus else "pending",
                    "isOpen" to store.isOpen,
                    "openingTime" to store.openingTime.trim().ifBlank { "00:00" }.take(5),
                    "closingTime" to store.closingTime.trim().ifBlank { "23:59" }.take(5),
                    "commissionPercent" to store.commissionPercent,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "name" to store.name.trim().take(100),
                    "phone" to store.phone.trim().take(24),
                    "address" to store.address.trim().take(200),
                    "city" to store.city.trim().take(60),
                    "deliveryTimeMinutes" to store.deliveryTimeMinutes,
                    "deliveryFeePkr" to store.deliveryFeePkr,
                    "minOrderPkr" to store.minOrderPkr,
                    "isOpen" to store.isOpen,
                    "openingTime" to store.openingTime.trim().ifBlank { "00:00" }.take(5),
                    "closingTime" to store.closingTime.trim().ifBlank { "23:59" }.take(5),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                store.imageUrl?.let { updates["imageUrl"] = it.take(200000) }
                store.description?.let { updates["description"] = it.take(500) }
                if (isAdminOverride) {
                    updates["approvalStatus"] = store.approvalStatus
                    updates["commissionPercent"] = store.commissionPercent
                }
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateMedicalStoreApproval(storeId: String, status: String, commissionPercent: Double? = null) {
        val docRef = db.collection("medicalStores").document(storeId)
        try {
            val updates = mutableMapOf<String, Any>(
                "approvalStatus" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (commissionPercent != null) updates["commissionPercent"] = commissionPercent
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeAvailableMedicalProducts(): Flow<List<MedicalProduct>> =
        db.collection("medicalProducts")
            .whereEqualTo("isAvailable", true)
            .observeList("medicalProducts")

    fun observeAllMedicalProductsAdmin(): Flow<List<MedicalProduct>> =
        db.collection("medicalProducts").observeList("medicalProducts")

    suspend fun saveMedicalProduct(product: MedicalProduct, isAdminOverride: Boolean = false): String {
        val uid = requireUserId()
        val id = product.id.ifBlank { "med_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("medicalProducts").document(id)
        val cleanAdditionalImages = product.additionalImages.filter { it.isNotBlank() }.take(4).map { it.take(200000) }
        val cleanUnit = product.unit.trim().ifBlank { "1 strip" }.take(30)
        val cleanImageUrl = product.imageUrl?.takeIf { it.isNotBlank() }?.take(200000)
        try {
            if (product.id.isBlank()) {
                val payload = mapOf(
                    "storeId" to product.storeId,
                    "storeName" to product.storeName.take(100),
                    "ownerId" to product.ownerId.ifBlank { uid },
                    "categoryId" to product.categoryId,
                    "categoryName" to product.categoryName.take(60),
                    "name" to product.name.trim().take(100),
                    "description" to product.description.trim().take(500),
                    "regularPricePkr" to product.regularPricePkr,
                    "discountPricePkr" to product.discountPricePkr,
                    "unit" to cleanUnit,
                    "stockQuantity" to product.stockQuantity,
                    "imageUrl" to cleanImageUrl,
                    "additionalImages" to cleanAdditionalImages,
                    "isAvailable" to product.isAvailable,
                    "isFeatured" to product.isFeatured,
                    "city" to product.city.take(60),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "categoryId" to product.categoryId,
                    "categoryName" to product.categoryName.take(60),
                    "name" to product.name.trim().take(100),
                    "description" to product.description.trim().take(500),
                    "regularPricePkr" to product.regularPricePkr,
                    "discountPricePkr" to product.discountPricePkr,
                    "unit" to cleanUnit,
                    "stockQuantity" to product.stockQuantity,
                    "imageUrl" to (cleanImageUrl ?: FieldValue.delete()),
                    "additionalImages" to cleanAdditionalImages,
                    "isAvailable" to product.isAvailable,
                    "isFeatured" to product.isFeatured,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (isAdminOverride) {
                    updates["storeId"] = product.storeId
                    updates["storeName"] = product.storeName.take(100)
                    updates["city"] = product.city.take(60)
                }
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMedicalProduct(productId: String) {
        val docRef = db.collection("medicalProducts").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun updateVendorHoursAndOpenStatus(
        collection: String,
        vendorId: String,
        isOpen: Boolean,
        openingTime: String,
        closingTime: String
    ) {
        require(collection in setOf("restaurants", "groceryStores", "medicalStores")) {
            "Invalid vendor collection: $collection"
        }
        val docRef = db.collection(collection).document(vendorId)
        try {
            docRef.update(
                mapOf(
                    "isOpen" to isOpen,
                    "openingTime" to openingTime.trim().ifBlank { "00:00" }.take(5),
                    "closingTime" to closingTime.trim().ifBlank { "23:59" }.take(5),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Anything You Want from Multan ---

    fun observeAvailableMultanItems(): Flow<List<MultanItem>> =
        db.collection("multanItems")
            .whereEqualTo("isAvailable", true)
            .observeList("multanItems")

    fun observeAllMultanItemsAdmin(): Flow<List<MultanItem>> =
        db.collection("multanItems").observeList("multanItems")

    suspend fun saveMultanItemAdmin(item: MultanItem): String {
        val id = item.id.ifBlank { "mlt_${UUID.randomUUID().toString().replace("-", "").take(14)}" }
        val docRef = db.collection("multanItems").document(id)
        val cleanAdditionalImages = item.additionalImages.filter { it.isNotBlank() }.take(4).map { it.take(200000) }
        val cleanUnit = item.unit.trim().ifBlank { "1 box" }.take(30)
        val cleanImageUrl = item.imageUrl?.takeIf { it.isNotBlank() }?.take(200000)
        try {
            if (item.id.isBlank()) {
                val payload = mapOf(
                    "brandName" to item.brandName.trim().ifBlank { "Multan Special" }.take(80),
                    "categoryName" to item.categoryName.trim().ifBlank { "Cookies & Bakery" }.take(60),
                    "name" to item.name.trim().take(100),
                    "description" to item.description.trim().take(500),
                    "regularPricePkr" to item.regularPricePkr.coerceIn(1.0, 200000.0),
                    "discountPricePkr" to item.discountPricePkr.coerceIn(0.0, item.regularPricePkr),
                    "unit" to cleanUnit,
                    "stockQuantity" to item.stockQuantity.coerceIn(0L, 100000L),
                    "imageUrl" to cleanImageUrl,
                    "additionalImages" to cleanAdditionalImages,
                    "originCity" to item.originCity.trim().ifBlank { "Multan" }.take(60),
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "city" to item.city.trim().ifBlank { GharTakConstants.ALL_CITIES }.take(60),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "brandName" to item.brandName.trim().ifBlank { "Multan Special" }.take(80),
                    "categoryName" to item.categoryName.trim().ifBlank { "Cookies & Bakery" }.take(60),
                    "name" to item.name.trim().take(100),
                    "description" to item.description.trim().take(500),
                    "regularPricePkr" to item.regularPricePkr.coerceIn(1.0, 200000.0),
                    "discountPricePkr" to item.discountPricePkr.coerceIn(0.0, item.regularPricePkr),
                    "unit" to cleanUnit,
                    "stockQuantity" to item.stockQuantity.coerceIn(0L, 100000L),
                    "imageUrl" to (cleanImageUrl ?: FieldValue.delete()),
                    "additionalImages" to cleanAdditionalImages,
                    "originCity" to item.originCity.trim().ifBlank { "Multan" }.take(60),
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "city" to item.city.trim().ifBlank { GharTakConstants.ALL_CITIES }.take(60),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMultanItemAdmin(itemId: String) {
        val docRef = db.collection("multanItems").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Celebration (Cakes, Flowers, Balloons, Gifts) ---

    fun observeAvailableCelebrationItems(): Flow<List<CelebrationItem>> =
        db.collection("celebrationItems")
            .whereEqualTo("isAvailable", true)
            .observeList("celebrationItems")

    fun observeAllCelebrationItemsAdmin(): Flow<List<CelebrationItem>> =
        db.collection("celebrationItems").observeList("celebrationItems")

    suspend fun saveCelebrationItemAdmin(item: CelebrationItem): String {
        val id = item.id.ifBlank { "clb_${UUID.randomUUID().toString().replace("-", "").take(14)}" }
        val docRef = db.collection("celebrationItems").document(id)
        val cleanAdditionalImages = item.additionalImages.filter { it.isNotBlank() }.take(4).map { it.take(200000) }
        val cleanUnit = item.unit.trim().ifBlank { "1 item" }.take(30)
        val cleanImageUrl = item.imageUrl?.takeIf { it.isNotBlank() }?.take(200000)
        try {
            if (item.id.isBlank()) {
                val payload = mapOf(
                    "categoryName" to item.categoryName.trim().ifBlank { "Birthday Cakes" }.take(60),
                    "name" to item.name.trim().take(100),
                    "description" to item.description.trim().take(500),
                    "regularPricePkr" to item.regularPricePkr.coerceIn(1.0, 200000.0),
                    "discountPricePkr" to item.discountPricePkr.coerceIn(0.0, item.regularPricePkr),
                    "unit" to cleanUnit,
                    "stockQuantity" to item.stockQuantity.coerceIn(0L, 100000L),
                    "imageUrl" to cleanImageUrl,
                    "additionalImages" to cleanAdditionalImages,
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "city" to item.city.trim().ifBlank { GharTakConstants.ALL_CITIES }.take(60),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "categoryName" to item.categoryName.trim().ifBlank { "Birthday Cakes" }.take(60),
                    "name" to item.name.trim().take(100),
                    "description" to item.description.trim().take(500),
                    "regularPricePkr" to item.regularPricePkr.coerceIn(1.0, 200000.0),
                    "discountPricePkr" to item.discountPricePkr.coerceIn(0.0, item.regularPricePkr),
                    "unit" to cleanUnit,
                    "stockQuantity" to item.stockQuantity.coerceIn(0L, 100000L),
                    "imageUrl" to (cleanImageUrl ?: FieldValue.delete()),
                    "additionalImages" to cleanAdditionalImages,
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "city" to item.city.trim().ifBlank { GharTakConstants.ALL_CITIES }.take(60),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCelebrationItemAdmin(itemId: String) {
        val docRef = db.collection("celebrationItems").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Parcel & Rider Requests (Restaurant -> Admin Approval -> Rider) ---

    fun observeMyParcelRiderRequests(requesterId: String): Flow<List<ParcelRiderRequest>> =
        db.collection("parcelRiderRequests")
            .whereEqualTo("requesterId", requesterId)
            .observeList("parcelRiderRequests")

    fun observeAllParcelRiderRequestsAdmin(): Flow<List<ParcelRiderRequest>> =
        db.collection("parcelRiderRequests").observeList("parcelRiderRequests")

    fun observeApprovedParcelRiderJobs(): Flow<List<ParcelRiderRequest>> =
        db.collection("parcelRiderRequests")
            .whereEqualTo("approvalStatus", "approved")
            .observeList("parcelRiderRequests")

    fun observeAssignedParcelRiderJobs(riderId: String): Flow<List<ParcelRiderRequest>> =
        db.collection("parcelRiderRequests")
            .whereEqualTo("riderId", riderId)
            .observeList("parcelRiderRequests")

    suspend fun createParcelRiderRequest(req: ParcelRiderRequest): String {
        val uid = requireUserId()
        val id = "prq_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val docRef = db.collection("parcelRiderRequests").document(id)
        try {
            val payload = mapOf(
                "requestType" to req.requestType,
                "requesterId" to uid,
                "restaurantId" to req.restaurantId,
                "restaurantName" to req.restaurantName.trim().take(100),
                "pickupAddress" to req.pickupAddress.trim().take(250),
                "dropoffAddress" to req.dropoffAddress.trim().take(250),
                "city" to req.city.trim().ifBlank { GharTakConstants.DEFAULT_CITY }.take(60),
                "recipientName" to req.recipientName.trim().take(80),
                "recipientPhone" to req.recipientPhone.trim().take(24),
                "details" to req.details.trim().take(400),
                "offeredFeePkr" to req.offeredFeePkr.coerceIn(50.0, 50000.0),
                "approvalStatus" to "pending",
                "riderId" to "",
                "riderName" to "",
                "adminNote" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun reviewParcelRiderRequestAdmin(requestId: String, status: String, adminNote: String) {
        val docRef = db.collection("parcelRiderRequests").document(requestId)
        try {
            docRef.update(
                mapOf(
                    "approvalStatus" to status,
                    "adminNote" to adminNote.trim().take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun claimParcelRiderJobByRider(requestId: String, riderId: String, riderName: String) {
        val docRef = db.collection("parcelRiderRequests").document(requestId)
        try {
            docRef.update(
                mapOf(
                    "riderId" to riderId,
                    "riderName" to riderName.trim().take(80),
                    "approvalStatus" to "rider_assigned",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun advanceParcelRiderJobByRider(requestId: String, nextStatus: String) {
        val docRef = db.collection("parcelRiderRequests").document(requestId)
        try {
            docRef.update(
                mapOf(
                    "approvalStatus" to nextStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Riders ---

    fun observeRiderProfile(userId: String): Flow<RiderProfile?> = flow {
        requireUserId()
        emitAll(
            db.collection("riders").document(userId)
                .snapshots()
                .map { it.toObject(RiderProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .wrapFirestoreErrors(OperationType.GET, "riders/$userId")
        )
    }

    fun observeAllRidersAdmin(): Flow<List<RiderProfile>> =
        db.collection("riders").observeList("riders")

    suspend fun registerRider(rider: RiderProfile) {
        val uid = requireUserId()
        val docRef = db.collection("riders").document(uid)
        try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "fullName" to rider.fullName.trim().take(80),
                    "phone" to rider.phone.trim().take(24),
                    "email" to rider.email.trim().take(120),
                    "cnicNumber" to rider.cnicNumber.trim().take(25),
                    "licenseNumber" to rider.licenseNumber.trim().take(30),
                    "vehicleType" to rider.vehicleType.trim().take(40),
                    "vehicleNumber" to rider.vehicleNumber.trim().take(30),
                    "city" to rider.city.trim().take(60),
                    "approvalStatus" to "pending",
                    "isOnline" to false,
                    "latitude" to rider.latitude,
                    "longitude" to rider.longitude,
                    "totalEarningsPkr" to 0.0,
                    "completedDeliveries" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateRiderOnlineAndLocation(riderId: String, isOnline: Boolean, lat: Double, lng: Double) {
        val docRef = db.collection("riders").document(riderId)
        try {
            docRef.update(
                mapOf(
                    "isOnline" to isOnline,
                    "latitude" to lat,
                    "longitude" to lng,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateRiderApprovalAdmin(riderId: String, status: String) {
        val docRef = db.collection("riders").document(riderId)
        try {
            docRef.update(
                mapOf(
                    "approvalStatus" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Orders ---

    fun observeCustomerOrders(customerId: String): Flow<List<OrderRecord>> =
        db.collection("orders")
            .whereEqualTo("customerId", customerId)
            .observeList("orders")

    fun observeVendorOrders(vendorOwnerId: String): Flow<List<OrderRecord>> =
        db.collection("orders")
            .whereEqualTo("vendorOwnerId", vendorOwnerId)
            .observeList("orders")

    fun observeAvailableRiderOrders(): Flow<List<OrderRecord>> =
        db.collection("orders")
            .whereEqualTo("status", "Ready for Pickup")
            .whereEqualTo("riderId", "")
            .observeList("orders")

    fun observeAssignedRiderOrders(riderId: String): Flow<List<OrderRecord>> =
        db.collection("orders")
            .whereEqualTo("riderId", riderId)
            .observeList("orders")

    fun observeAllOrdersAdmin(): Flow<List<OrderRecord>> =
        db.collection("orders").observeList("orders")

    suspend fun placeOrder(order: OrderRecord): String {
        val uid = requireUserId()
        val id = "ord_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("orders").document(id)
        try {
            val serializedItems = order.items.map {
                mapOf(
                    "productId" to it.productId,
                    "name" to it.name,
                    "quantity" to it.quantity,
                    "unitPricePkr" to it.unitPricePkr,
                    "unit" to it.unit
                )
            }
            val payload = mapOf(
                "orderType" to order.orderType,
                "customerId" to uid,
                "customerName" to order.customerName.take(80),
                "customerPhone" to order.customerPhone.take(24),
                "vendorId" to order.vendorId,
                "vendorName" to order.vendorName.take(100),
                "vendorOwnerId" to order.vendorOwnerId,
                "riderId" to "",
                "riderName" to "",
                "deliveryAddress" to order.deliveryAddress.take(250),
                "city" to order.city.take(60),
                "deliveryLat" to order.deliveryLat,
                "deliveryLng" to order.deliveryLng,
                "items" to serializedItems,
                "subtotalPkr" to order.subtotalPkr,
                "deliveryFeePkr" to order.deliveryFeePkr,
                "discountPkr" to order.discountPkr,
                "couponCode" to order.couponCode.take(40),
                "totalPkr" to order.totalPkr,
                "commissionPkr" to order.commissionPkr,
                "vendorEarningsPkr" to order.vendorEarningsPkr,
                "riderEarningsPkr" to order.riderEarningsPkr,
                "paymentMethod" to order.paymentMethod,
                "paymentStatus" to order.paymentStatus,
                "status" to "Pending",
                "cancellationReason" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun placeOrderWithWallet(order: OrderRecord): String {
        val uid = requireUserId()
        val id = "ord_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val orderRef = db.collection("orders").document(id)
        val userRef = db.collection("users").document(uid)
        val txId = "wtx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val txRef = userRef.collection("walletTransactions").document(txId)

        try {
            val serializedItems = order.items.map {
                mapOf(
                    "productId" to it.productId,
                    "name" to it.name,
                    "quantity" to it.quantity,
                    "unitPricePkr" to it.unitPricePkr,
                    "unit" to it.unit
                )
            }
            val payload = mapOf(
                "orderType" to order.orderType,
                "customerId" to uid,
                "customerName" to order.customerName.take(80),
                "customerPhone" to order.customerPhone.take(24),
                "vendorId" to order.vendorId,
                "vendorName" to order.vendorName.take(100),
                "vendorOwnerId" to order.vendorOwnerId,
                "riderId" to "",
                "riderName" to "",
                "deliveryAddress" to order.deliveryAddress.take(250),
                "city" to order.city.take(60),
                "deliveryLat" to order.deliveryLat,
                "deliveryLng" to order.deliveryLng,
                "items" to serializedItems,
                "subtotalPkr" to order.subtotalPkr,
                "deliveryFeePkr" to order.deliveryFeePkr,
                "discountPkr" to order.discountPkr,
                "couponCode" to order.couponCode.take(40),
                "totalPkr" to order.totalPkr,
                "commissionPkr" to order.commissionPkr,
                "vendorEarningsPkr" to order.vendorEarningsPkr,
                "riderEarningsPkr" to order.riderEarningsPkr,
                "paymentMethod" to "Wallet",
                "paymentStatus" to "paid",
                "status" to "Pending",
                "cancellationReason" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val currentBalance = userSnap.getDouble("walletBalancePkr") ?: 0.0
                if (currentBalance < order.totalPkr) {
                    throw IllegalStateException(
                        "Insufficient Wallet balance (${GharTakConstants.formatPkr(currentBalance)} available). Top up your Wallet first."
                    )
                }
                val nextBalance = (currentBalance - order.totalPkr).coerceAtLeast(0.0)
                tx.update(
                    userRef,
                    mapOf(
                        "walletBalancePkr" to nextBalance,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(orderRef, payload)
                if (order.totalPkr > 0.0) {
                    tx.set(
                        txRef,
                        mapOf(
                            "userId" to uid,
                            "type" to "payment",
                            "amountPkr" to order.totalPkr,
                            "balanceAfterPkr" to nextBalance,
                            "paymentChannel" to "GT Service Wallet",
                            "referenceId" to id,
                            "note" to "Paid for ${order.vendorName} order".take(300),
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, orderRef.path)
            throw e
        }
    }

    suspend fun cancelOrderByCustomer(orderId: String, reason: String): Boolean {
        val docRef = db.collection("orders").document(orderId)
        try {
            return db.runTransaction { tx ->
                val orderSnap = tx.get(docRef)
                val customerId = orderSnap.getString("customerId").orEmpty()
                val paymentMethod = orderSnap.getString("paymentMethod").orEmpty()
                val paymentStatus = orderSnap.getString("paymentStatus").orEmpty()
                val status = orderSnap.getString("status").orEmpty()
                val totalPkr = orderSnap.getDouble("totalPkr") ?: 0.0
                val vendorName = orderSnap.getString("vendorName").orEmpty()

                val shouldRefund = paymentMethod == "Wallet" &&
                    paymentStatus == "paid" &&
                    totalPkr > 0.0 &&
                    customerId.isNotBlank() &&
                    status in GharTakConstants.CANCELLABLE_ORDER_STATUSES

                if (shouldRefund) {
                    val userRef = db.collection("users").document(customerId)
                    val userSnap = tx.get(userRef)
                    val currentBalance = userSnap.getDouble("walletBalancePkr") ?: 0.0
                    val nextBalance = (currentBalance + totalPkr).coerceAtMost(1_000_000.0)
                    val refundTxId = "wtx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
                    val refundTxRef = userRef.collection("walletTransactions").document(refundTxId)

                    tx.update(
                        userRef,
                        mapOf(
                            "walletBalancePkr" to nextBalance,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    tx.set(
                        refundTxRef,
                        mapOf(
                            "userId" to customerId,
                            "type" to "refund",
                            "amountPkr" to totalPkr,
                            "balanceAfterPkr" to nextBalance,
                            "paymentChannel" to "Order Refund",
                            "referenceId" to orderId,
                            "note" to "Refund for cancelled $vendorName order".take(300),
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                tx.update(
                    docRef,
                    mapOf(
                        "status" to "Cancelled",
                        "cancellationReason" to reason.take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                shouldRefund
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateOrderStatusByVendor(orderId: String, status: String, reason: String = "") {
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "cancellationReason" to reason.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun claimOrderByRider(orderId: String, riderId: String, riderName: String) {
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "riderId" to riderId,
                    "riderName" to riderName.take(80),
                    "status" to "Rider Assigned",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateOrderDeliveryByRider(orderId: String, status: String, paymentStatus: String) {
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "paymentStatus" to paymentStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateOrderAdmin(
        orderId: String,
        status: String,
        paymentStatus: String? = null,
        riderId: String? = null,
        riderName: String? = null
    ) {
        val docRef = db.collection("orders").document(orderId)
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (paymentStatus != null) updates["paymentStatus"] = paymentStatus
            if (riderId != null) updates["riderId"] = riderId
            if (riderName != null) updates["riderName"] = riderName.take(80)
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Reviews ---

    fun observeVisibleReviews(): Flow<List<ReviewRecord>> =
        db.collection("reviews")
            .whereEqualTo("isHidden", false)
            .observeList("reviews")

    fun observeAllReviewsAdmin(): Flow<List<ReviewRecord>> =
        db.collection("reviews").observeList("reviews")

    suspend fun submitReview(review: ReviewRecord) {
        val uid = requireUserId()
        val id = "rev_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("reviews").document(id)
        try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "userName" to review.userName.take(80),
                    "orderId" to review.orderId.take(128),
                    "targetId" to review.targetId.take(128),
                    "targetType" to review.targetType,
                    "targetName" to review.targetName.take(100),
                    "rating" to review.rating.coerceIn(1.0, 5.0),
                    "comment" to review.comment.take(500),
                    "isHidden" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun setReviewHiddenAdmin(reviewId: String, hidden: Boolean) {
        val docRef = db.collection("reviews").document(reviewId)
        try {
            docRef.update(
                mapOf(
                    "isHidden" to hidden,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Coupons, Platform Settings, Admin Logs ---

    fun observeActiveCoupons(): Flow<List<CouponRecord>> =
        db.collection("coupons")
            .whereEqualTo("isActive", true)
            .observeList("coupons")

    fun observeAllCouponsAdmin(): Flow<List<CouponRecord>> =
        db.collection("coupons").observeList("coupons")

    suspend fun saveCouponAdmin(coupon: CouponRecord): String {
        val id = coupon.id.ifBlank { "cpn_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val docRef = db.collection("coupons").document(id)
        try {
            if (coupon.id.isBlank()) {
                docRef.set(
                    mapOf(
                        "code" to coupon.code.trim().uppercase().take(30),
                        "discountType" to coupon.discountType,
                        "discountValue" to coupon.discountValue,
                        "minOrderPkr" to coupon.minOrderPkr,
                        "maxDiscountPkr" to coupon.maxDiscountPkr,
                        "usageLimit" to coupon.usageLimit,
                        "usedCount" to 0L,
                        "isActive" to coupon.isActive,
                        "expiresAtEpochMs" to coupon.expiresAtEpochMs,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "code" to coupon.code.trim().uppercase().take(30),
                        "discountType" to coupon.discountType,
                        "discountValue" to coupon.discountValue,
                        "minOrderPkr" to coupon.minOrderPkr,
                        "maxDiscountPkr" to coupon.maxDiscountPkr,
                        "usageLimit" to coupon.usageLimit,
                        "isActive" to coupon.isActive,
                        "expiresAtEpochMs" to coupon.expiresAtEpochMs,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCouponAdmin(couponId: String) {
        val docRef = db.collection("coupons").document(couponId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observePlatformSettings(): Flow<PlatformSettings> = flow {
        requireUserId()
        emitAll(
            db.collection("platformSettings").document("global")
                .snapshots()
                .map { snap ->
                    snap.toObject(PlatformSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: PlatformSettings()
                }
                .wrapFirestoreErrors(OperationType.GET, "platformSettings/global")
        )
    }

    suspend fun savePlatformSettingsAdmin(settings: PlatformSettings) {
        val docRef = db.collection("platformSettings").document("global")
        try {
            val serializedBeneficiaries = settings.beneficiaryAccounts
                .filter { it.channel.isNotBlank() && it.accountNumber.isNotBlank() }
                .take(20)
                .map { acc ->
                    mapOf(
                        "id" to acc.id.ifBlank { "ben_${UUID.randomUUID().toString().replace("-", "").take(10)}" },
                        "channel" to acc.channel.trim().take(60),
                        "accountTitle" to acc.accountTitle.trim().ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }.take(80),
                        "accountNumber" to acc.accountNumber.trim().take(60)
                    )
                }
            val serializedPromos = settings.promoOffers
                .filter { it.title.isNotBlank() || it.code.isNotBlank() }
                .take(20)
                .map { promo ->
                    mapOf(
                        "id" to promo.id.ifBlank { "prm_${UUID.randomUUID().toString().replace("-", "").take(10)}" },
                        "restaurantId" to promo.restaurantId.trim().take(128),
                        "restaurantName" to promo.restaurantName.trim().take(100),
                        "code" to promo.code.trim().uppercase().take(30),
                        "badgeText" to promo.badgeText.trim().take(60),
                        "title" to promo.title.trim().take(100),
                        "subtitle" to promo.subtitle.trim().take(160),
                        "discountPercent" to promo.discountPercent.coerceIn(0, 90)
                    )
                }
            val serializedAreaRules = settings.deliveryAreaFeeRules
                .filter { it.city.isNotBlank() && it.areaName.isNotBlank() }
                .take(100)
                .map { rule ->
                    mapOf(
                        "id" to rule.id.ifBlank { "area_${UUID.randomUUID().toString().replace("-", "").take(10)}" },
                        "city" to rule.city.trim().take(60),
                        "areaName" to rule.areaName.trim().take(80),
                        "feePkr" to rule.feePkr.coerceIn(0.0, 10000.0),
                        "maxDistanceKm" to rule.maxDistanceKm.coerceIn(0.5, 200.0)
                    )
                }
            val serializedMultanFees = settings.multanCityDeliveryFees
                .filter { it.city.isNotBlank() }
                .distinctBy { it.city.trim().lowercase() }
                .take(50)
                .map { row ->
                    mapOf(
                        "city" to row.city.trim().take(60),
                        "feePkr" to row.feePkr.coerceIn(0.0, 50000.0)
                    )
                }
            val cleanSupportLocal = settings.supportPhoneLocal.trim()
                .ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }.take(24)
            val cleanWhatsapp = settings.supportWhatsappNumber.trim()
                .ifBlank { GharTakConstants.toWhatsappDigits(cleanSupportLocal) }.take(24)
            docRef.set(
                mapOf(
                    "cities" to settings.cities.take(50),
                    "restaurantCommissionPercent" to settings.restaurantCommissionPercent,
                    "groceryCommissionPercent" to settings.groceryCommissionPercent,
                    "baseDeliveryFeePkr" to settings.baseDeliveryFeePkr,
                    "riderPayoutPkr" to settings.riderPayoutPkr,
                    "jazzCashConfigured" to settings.jazzCashConfigured,
                    "easypaisaConfigured" to settings.easypaisaConfigured,
                    "jazzCashNumber" to settings.jazzCashNumber.trim().ifBlank { GharTakConstants.DEFAULT_JAZZCASH_NUMBER }.take(24),
                    "jazzCashAccountTitle" to settings.jazzCashAccountTitle.trim().ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }.take(80),
                    "easypaisaNumber" to settings.easypaisaNumber.trim().ifBlank { GharTakConstants.DEFAULT_EASYPAISA_NUMBER }.take(24),
                    "easypaisaAccountTitle" to settings.easypaisaAccountTitle.trim().ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }.take(80),
                    "beneficiaryAccounts" to serializedBeneficiaries,
                    "promoBannerEnabled" to settings.promoBannerEnabled,
                    "promoCode" to settings.promoCode.trim().uppercase().ifBlank { "GHARTAK20" }.take(30),
                    "promoDiscountPercent" to settings.promoDiscountPercent.coerceIn(0, 90),
                    "promoBadgeText" to settings.promoBadgeText.trim().take(60),
                    "promoTitle" to settings.promoTitle.trim().take(100),
                    "promoSubtitle" to settings.promoSubtitle.trim().take(160),
                    "promoRestaurantId" to settings.promoRestaurantId.trim().take(128),
                    "promoRestaurantName" to settings.promoRestaurantName.trim().take(100),
                    "promoOffers" to serializedPromos,
                    "deliveryAreaFeeRules" to serializedAreaRules,
                    "multanCityDeliveryFees" to serializedMultanFees,
                    "multanDefaultDeliveryFeePkr" to settings.multanDefaultDeliveryFeePkr.coerceIn(0.0, 10000.0),
                    "radiusPricingEnabled" to settings.radiusPricingEnabled,
                    "deliveryBaseRadiusKm" to settings.deliveryBaseRadiusKm.coerceIn(0.5, 100.0),
                    "withinRadiusFeePkr" to settings.withinRadiusFeePkr.coerceIn(0.0, 5000.0),
                    "extraPerKmFeePkr" to settings.extraPerKmFeePkr.coerceIn(0.0, 1000.0),
                    "supportPhoneLocal" to cleanSupportLocal,
                    "supportWhatsappNumber" to cleanWhatsapp,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeMyCityRequests(userId: String): Flow<List<CityApprovalRequest>> =
        db.collection("cityRequests")
            .whereEqualTo("requesterId", userId)
            .observeList("cityRequests")

    fun observeAllCityRequestsAdmin(): Flow<List<CityApprovalRequest>> =
        db.collection("cityRequests").observeList("cityRequests")

    suspend fun submitCityApprovalRequest(cityName: String, requesterName: String): String {
        val uid = requireUserId()
        val cleanCity = cityName.trim().take(60)
        require(cleanCity.length >= 2) { "City name must be at least 2 characters" }
        val id = "city_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("cityRequests").document(id)
        try {
            docRef.set(
                mapOf(
                    "cityName" to cleanCity,
                    "requesterId" to uid,
                    "requesterName" to requesterName.trim().ifBlank { "GT Service User" }.take(80),
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun reviewCityRequestAdmin(req: CityApprovalRequest, approve: Boolean, currentSettings: PlatformSettings) {
        val docRef = db.collection("cityRequests").document(req.id)
        try {
            if (approve) {
                val updatedCities = (currentSettings.cities + req.cityName.trim())
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }
                    .take(50)
                savePlatformSettingsAdmin(currentSettings.copy(cities = updatedCities))
            }
            docRef.update(
                mapOf(
                    "status" to if (approve) "approved" else "rejected",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeAdminLogs(): Flow<List<AdminActivityLog>> =
        db.collection("adminActivityLogs").observeList("adminActivityLogs")

    suspend fun logAdminAction(
        adminId: String,
        adminEmail: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String
    ) {
        val id = "log_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("adminActivityLogs").document(id)
        try {
            docRef.set(
                mapOf(
                    "adminId" to adminId,
                    "adminEmail" to adminEmail.take(120),
                    "action" to action.take(80),
                    "targetType" to targetType.take(60),
                    "targetId" to targetId.take(128),
                    "details" to details.take(500),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    // --- Live Customer <-> Admin Support Chat (Helpline 03036767152) ---

    fun observeSupportChatThread(userId: String): Flow<SupportChatThread?> = flow {
        requireUserId()
        val path = "supportChats/$userId"
        emitAll(
            db.collection("supportChats").document(userId)
                .snapshots()
                .map { snap ->
                    snap.toObject(SupportChatThread::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .wrapFirestoreErrors(OperationType.GET, path)
        )
    }

    fun observeAllSupportChatThreadsAdmin(): Flow<List<SupportChatThread>> =
        db.collection("supportChats").observeList("supportChats")

    fun observeSupportChatMessages(userId: String): Flow<List<SupportChatMessage>> =
        db.collection("supportChats").document(userId).collection("messages")
            .observeList("supportChats/$userId/messages")

    suspend fun sendSupportChatMessage(
        threadUserId: String,
        customerName: String,
        customerPhone: String,
        senderName: String,
        senderRole: String,
        text: String,
        orderId: String = "",
        supportPhone: String = GharTakConstants.SUPPORT_PHONE_LOCAL,
        imageUrl: String? = null,
        audioBase64: String? = null,
        audioDurationSec: Long? = null
    ): String {
        val uid = requireUserId()
        val cleanText = text.trim().take(1000)
        val cleanImageUrl = imageUrl?.takeIf { it.isNotBlank() }?.take(200000)
        val cleanAudio = audioBase64?.takeIf { it.isNotBlank() }?.take(200000)
        val safeDuration = audioDurationSec?.coerceIn(0L, 120L)
        require(cleanText.isNotEmpty() || cleanImageUrl != null || cleanAudio != null) {
            "Message, photo, or voice note is required"
        }
        require(senderRole == "customer" || senderRole == "admin") { "Invalid sender role" }
        if (senderRole == "customer") {
            require(uid == threadUserId) { "Customer can only post to their own support chat" }
        }

        val effectiveText = cleanText.ifBlank {
            when {
                cleanImageUrl != null && cleanAudio != null -> "Photo & Voice Note"
                cleanImageUrl != null -> "Photo"
                else -> "Voice Note (${safeDuration ?: 1}s)"
            }
        }

        val threadRef = db.collection("supportChats").document(threadUserId)
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val msgRef = threadRef.collection("messages").document(msgId)
        val activeSupportPhone = supportPhone.trim().ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }.take(24)
        val safeCustomerName = customerName.trim().ifBlank { "GT Service Customer" }.take(80)
        val safeCustomerPhone = customerPhone.trim().ifBlank { activeSupportPhone }.take(24)
        val safeSenderName = senderName.trim().ifBlank {
            if (senderRole == "admin") "GT Service Support ($activeSupportPhone)" else safeCustomerName
        }.take(80)
        val safeOrderId = orderId.trim().take(128)

        try {
            db.runTransaction { tx ->
                val threadSnap = tx.get(threadRef)
                val existingUnreadAdmin = threadSnap.getLong("unreadByAdmin") ?: 0L
                val existingUnreadCust = threadSnap.getLong("unreadByCustomer") ?: 0L

                val nextUnreadAdmin = if (senderRole == "customer") (existingUnreadAdmin + 1L).coerceAtMost(10000L) else 0L
                val nextUnreadCust = if (senderRole == "admin") (existingUnreadCust + 1L).coerceAtMost(10000L) else 0L

                if (!threadSnap.exists()) {
                    tx.set(
                        threadRef,
                        mapOf(
                            "userId" to threadUserId,
                            "customerName" to safeCustomerName,
                            "customerPhone" to safeCustomerPhone,
                            "supportPhone" to activeSupportPhone,
                            "lastMessage" to effectiveText,
                            "lastSenderRole" to senderRole,
                            "unreadByAdmin" to nextUnreadAdmin,
                            "unreadByCustomer" to nextUnreadCust,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    val threadUpdates = mutableMapOf<String, Any>(
                        "supportPhone" to activeSupportPhone,
                        "lastMessage" to effectiveText,
                        "lastSenderRole" to senderRole,
                        "unreadByAdmin" to nextUnreadAdmin,
                        "unreadByCustomer" to nextUnreadCust,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    if (senderRole == "customer") {
                        threadUpdates["customerName"] = safeCustomerName
                        if (safeCustomerPhone.isNotBlank()) threadUpdates["customerPhone"] = safeCustomerPhone
                    }
                    tx.update(threadRef, threadUpdates)
                }

                val msgPayload = mutableMapOf<String, Any>(
                    "userId" to threadUserId,
                    "senderId" to uid,
                    "senderName" to safeSenderName,
                    "senderRole" to senderRole,
                    "text" to effectiveText,
                    "orderId" to safeOrderId,
                    "supportPhone" to activeSupportPhone,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                if (cleanImageUrl != null) msgPayload["imageUrl"] = cleanImageUrl
                if (cleanAudio != null) {
                    msgPayload["audioBase64"] = cleanAudio
                    if (safeDuration != null) msgPayload["audioDurationSec"] = safeDuration
                }
                tx.set(msgRef, msgPayload)
            }.await()
            return msgId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, msgRef.path)
            throw e
        }
    }

    suspend fun markSupportChatRead(threadUserId: String, readerRole: String) {
        val uid = requireUserId()
        if (readerRole == "customer" && uid != threadUserId) return
        val threadRef = db.collection("supportChats").document(threadUserId)
        try {
            val snap = threadRef.get().await()
            if (!snap.exists()) return
            val field = if (readerRole == "admin") "unreadByAdmin" else "unreadByCustomer"
            val currentUnread = snap.getLong(field) ?: 0L
            if (currentUnread > 0L) {
                threadRef.update(
                    mapOf(
                        field to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, threadRef.path)
        }
    }
}
