package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AppPortal
import com.example.data.model.Category
import com.example.data.model.DeliveryAddress
import com.example.data.model.FoodItem
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.Restaurant
import com.example.data.model.RiderProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class GharTakRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_and_saveAddress_succeedsForOwnerAndIsolatesOthers() = runBlocking {
        val aliceUid = signInTestUser("alice_${UUID.randomUUID()}@test.com")
        val repo = GharTakRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile(aliceUid, "Alice Bahawalpur", "alice@test.com", null)
            repo.saveAddress(
                aliceUid,
                DeliveryAddress(
                    label = "Home",
                    houseNumber = "14-A",
                    street = "Baghdad Road",
                    area = "Model Town A",
                    city = "Bahawalpur",
                    latitude = 29.3956,
                    longitude = 71.6836,
                    isDefault = true
                )
            )
        }

        val addresses = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAddresses(aliceUid).first { it.isNotEmpty() }
        }
        assertEquals(1, addresses.size)
        assertEquals("Bahawalpur", addresses.first().city)

        // Cross-user isolation: Bob cannot read Alice's addresses
        signInTestUser("bob_${UUID.randomUUID()}@test.com")
        val bobEx = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) { repo.observeAddresses(aliceUid).first() }
        }.exceptionOrNull()
        val firestoreEx = checkNotNull(bobEx?.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $bobEx" }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    @Test
    fun placeOrder_and_vendorStatusUpdate_succeedsWithRoleEnforcement() = runBlocking {
        val vendorUid = signInTestUser("vendor_${UUID.randomUUID()}@test.com")
        val repo = GharTakRepository(firestore)

        val restaurantId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveRestaurant(
                Restaurant(
                    ownerId = vendorUid,
                    name = "Chaman Biryani ${UUID.randomUUID().toString().take(4)}",
                    ownerName = "Tariq",
                    phone = "+923001112233",
                    email = "vendor@test.com",
                    address = "Farid Gate, Bahawalpur",
                    city = "Bahawalpur"
                )
            )
        }
        assertTrue(restaurantId.isNotBlank())

        val customerUid = signInTestUser("cust_${UUID.randomUUID()}@test.com")
        val orderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.placeOrder(
                OrderRecord(
                    orderType = "food",
                    customerId = customerUid,
                    customerName = "Zainab",
                    customerPhone = "+923019998877",
                    vendorId = restaurantId,
                    vendorName = "Chaman Biryani",
                    vendorOwnerId = vendorUid,
                    deliveryAddress = "Model Town B, Bahawalpur",
                    city = "Bahawalpur",
                    items = listOf(OrderLineItem("item_1", "Chicken Biryani", 2, 450.0, "plate")),
                    subtotalPkr = 900.0,
                    deliveryFeePkr = 80.0,
                    totalPkr = 980.0,
                    commissionPkr = 135.0,
                    vendorEarningsPkr = 765.0,
                    riderEarningsPkr = 70.0,
                    paymentMethod = "COD",
                    paymentStatus = "pending"
                )
            )
        }
        assertTrue(orderId.isNotBlank())

        val custOrders = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomerOrders(customerUid).first { list -> list.any { it.id == orderId } }
        }
        assertEquals("Pending", custOrders.first { it.id == orderId }.status)

        // Customer cannot accept their own order (only vendor/admin can)
        val customerTamperEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) { repo.updateOrderStatusByVendor(orderId, "Accepted") }
        }.exceptionOrNull()
        val tamperFirestoreEx = checkNotNull(customerTamperEx?.unwrapFirestoreException()) {
            "Expected PERMISSION_DENIED for customer accepting own order, got $customerTamperEx"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, tamperFirestoreEx.code)
    }

    @Test
    fun observeCustomerOrders_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = GharTakRepository(firestore)
        val ex = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) { repo.observeCustomerOrders("some_uid").first() }
        }.exceptionOrNull()
        assertTrue(ex is IllegalStateException || ex?.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }

    @Test
    fun registerRider_startsPending() = runBlocking {
        val riderUid = signInTestUser("rider_${UUID.randomUUID()}@test.com")
        val repo = GharTakRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.registerRider(
                RiderProfile(
                    fullName = "Hamza Ali",
                    phone = "+923217776655",
                    email = "rider@test.com",
                    cnicNumber = "31202-1122334-5",
                    licenseNumber = "BWP-7788",
                    vehicleType = "Motorbike",
                    vehicleNumber = "BWP-2026",
                    city = "Bahawalpur"
                )
            )
        }
        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeRiderProfile(riderUid).first { it != null }
        }
        assertEquals("pending", profile?.approvalStatus)
    }

    @Test
    fun saveFoodAndGroceryProducts_roundTripsImagesPrepTimeUnitsAndEnforcesOwnerDelete() = runBlocking {
        val vendorEmail = "prodvendor_${UUID.randomUUID()}@test.com"
        val vendorUid = signInTestUser(vendorEmail)
        val repo = GharTakRepository(firestore)

        val restaurantId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveRestaurant(
                Restaurant(
                    ownerId = vendorUid,
                    name = "Desi Zaika ${UUID.randomUUID().toString().take(4)}",
                    ownerName = "Salman",
                    phone = "+923001112244",
                    email = "salman@test.com",
                    address = "Circular Road, Bahawalpur",
                    city = "Bahawalpur"
                )
            )
        }

        val storeId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveGroceryStore(
                GroceryStore(
                    ownerId = vendorUid,
                    name = "BWP Fresh Mart ${UUID.randomUUID().toString().take(4)}",
                    ownerName = "Salman",
                    phone = "+923001112244",
                    email = "salman@test.com",
                    address = "Model Town B, Bahawalpur",
                    city = "Bahawalpur"
                )
            )
        }

        val initialFood = FoodItem(
            restaurantId = restaurantId,
            restaurantName = "Desi Zaika",
            ownerId = vendorUid,
            categoryId = "cat_bbq",
            categoryName = "BBQ",
            name = "Chicken Malai Boti",
            description = "Charcoal grilled boneless cubes",
            regularPricePkr = 850.0,
            discountPricePkr = 740.0,
            imageUrl = "data:image/jpeg;base64,/9j/4AAQ",
            additionalImages = listOf("data:image/png;base64,iVBORw0KGgo="),
            preparationTimeMinutes = 25L,
            stockQuantity = 18L,
            isAvailable = true,
            isFeatured = true,
            city = "Bahawalpur"
        )
        // Restaurant owner cannot create a new FoodItem directly
        val vendorCreateEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) { repo.saveFoodItem(initialFood) }
        }.exceptionOrNull()
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            checkNotNull(vendorCreateEx?.unwrapFirestoreException()).code
        )

        // Admin creates the FoodItem assigned to vendorUid
        val adminUid = signInTestUser("rule_admin_${UUID.randomUUID()}@test.com")
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.activateAdminSession(
                userId = adminUid,
                fullName = "Admin Rule Test",
                email = "ruleadmin@test.com",
                avatarUrl = null,
                loginEmail = "admin",
                loginPassword = "admin123"
            )
        }
        val foodId = withTimeout(DEFAULT_TIMEOUT_MS) { repo.saveFoodItem(initialFood, isAdminOverride = true) }

        // Switch back to restaurant owner to read & update existing menu item
        signInTestUser(vendorEmail)
        val savedFood = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeMyFoodItems(vendorUid).first { list -> list.any { it.id == foodId } }
                .first { it.id == foodId }
        }
        assertEquals(25L, savedFood.preparationTimeMinutes)
        assertEquals(1, savedFood.additionalImages.size)
        assertEquals("data:image/jpeg;base64,/9j/4AAQ", savedFood.imageUrl)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveFoodItem(savedFood.copy(imageUrl = null, preparationTimeMinutes = 30L))
        }
        val updatedFood = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeMyFoodItems(vendorUid).first { list ->
                list.any { it.id == foodId && it.imageUrl == null && it.preparationTimeMinutes == 30L }
            }.first { it.id == foodId }
        }
        assertNull(updatedFood.imageUrl)
        assertEquals(30L, updatedFood.preparationTimeMinutes)

        val groceryId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveGroceryProduct(
                GroceryProduct(
                    storeId = storeId,
                    storeName = "BWP Fresh Mart",
                    ownerId = vendorUid,
                    categoryId = "cat_dairy",
                    categoryName = "Dairy Products",
                    name = "Farm Fresh Yogurt",
                    description = "Pure thick curd",
                    regularPricePkr = 320.0,
                    discountPricePkr = 280.0,
                    unit = "500 gram",
                    stockQuantity = 40L,
                    imageUrl = "data:image/webp;base64,UklGR",
                    additionalImages = listOf("data:image/jpeg;base64,/9j/4AAQ"),
                    isAvailable = true,
                    isFeatured = true,
                    city = "Bahawalpur"
                )
            )
        }
        val savedGrocery = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeMyGroceryProducts(vendorUid).first { list -> list.any { it.id == groceryId } }
                .first { it.id == groceryId }
        }
        assertEquals("500 gram", savedGrocery.unit)
        assertEquals(1, savedGrocery.additionalImages.size)

        // Another user receives PERMISSION_DENIED trying to delete vendor's products
        signInTestUser("stranger_${UUID.randomUUID()}@test.com")
        val deleteFoodEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) { repo.deleteFoodItem(foodId) }
        }.exceptionOrNull()
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            checkNotNull(deleteFoodEx?.unwrapFirestoreException()).code
        )

        val deleteGroceryEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) { repo.deleteGroceryProduct(groceryId) }
        }.exceptionOrNull()
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            checkNotNull(deleteGroceryEx?.unwrapFirestoreException()).code
        )
    }

    @Test
    fun activateAdminSession_withValidCredentials_grantsAdminProductCreationAndWrongCredentialsFail() = runBlocking {
        val adminCandidateUid = signInTestUser("newadmin_${UUID.randomUUID()}@test.com")
        val repo = GharTakRepository(firestore)

        // Wrong credentials are rejected immediately
        val wrongEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repo.activateAdminSession(
                    userId = adminCandidateUid,
                    fullName = "Admin User",
                    email = "newadmin@test.com",
                    avatarUrl = null,
                    loginEmail = "admin",
                    loginPassword = "wrongpassword"
                )
            }
        }.exceptionOrNull()
        assertTrue(wrongEx is IllegalArgumentException)

        // Valid admin / admin123 unlocks /admins/{uid} and switches portal to admin
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.activateAdminSession(
                userId = adminCandidateUid,
                fullName = "Admin User",
                email = "newadmin@test.com",
                avatarUrl = null,
                loginEmail = "admin",
                loginPassword = "admin123"
            )
        }

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(adminCandidateUid).first { it?.activePortal == AppPortal.ADMIN.key }
        }
        assertEquals(AppPortal.ADMIN.key, profile?.activePortal)

        // Activated admin can create an approved restaurant & add a food product with image
        val restId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveRestaurant(
                Restaurant(
                    ownerId = adminCandidateUid,
                    name = "Admin Kitchen ${UUID.randomUUID().toString().take(4)}",
                    ownerName = "Admin User",
                    phone = "+923001234567",
                    email = "newadmin@test.com",
                    address = "Model Town A, Bahawalpur",
                    city = "Bahawalpur",
                    approvalStatus = "approved"
                ),
                isAdminOverride = true
            )
        }
        val catId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveCategory("foodCategories", Category(name = "Chef Specials", sortOrder = 1))
        }
        val foodId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveFoodItem(
                FoodItem(
                    restaurantId = restId,
                    restaurantName = "Admin Kitchen",
                    ownerId = adminCandidateUid,
                    categoryId = catId,
                    categoryName = "Chef Specials",
                    name = "Admin Zinger Deal",
                    description = "Uploaded via Admin Panel",
                    regularPricePkr = 650.0,
                    discountPricePkr = 550.0,
                    imageUrl = "data:image/jpeg;base64,/9j/4AAQ",
                    isAvailable = true,
                    city = "Bahawalpur"
                ),
                isAdminOverride = true
            )
        }
        val adminFoods = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAllFoodItemsAdmin().first { list -> list.any { it.id == foodId } }
        }
        assertTrue(adminFoods.any { it.id == foodId && it.imageUrl == "data:image/jpeg;base64,/9j/4AAQ" })

        // Cross-device visibility: another customer on a second mobile observes newly added food & grocery products
        val grocStoreId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveGroceryStore(
                GroceryStore(
                    ownerId = adminCandidateUid,
                    name = "Admin Fresh Mart ${UUID.randomUUID().toString().take(4)}",
                    ownerName = "Admin User",
                    phone = "+923001234567",
                    email = "newadmin@test.com",
                    address = "Circular Road, Bahawalpur",
                    city = "Bahawalpur",
                    approvalStatus = "approved"
                ),
                isAdminOverride = true
            )
        }
        val grocId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveGroceryProduct(
                GroceryProduct(
                    storeId = grocStoreId,
                    storeName = "Admin Fresh Mart",
                    ownerId = adminCandidateUid,
                    categoryId = "cat_fruits",
                    categoryName = "Fruits",
                    name = "Admin Mango Crate",
                    description = "Synced across customer devices",
                    regularPricePkr = 1200.0,
                    discountPricePkr = 990.0,
                    unit = "1 kg",
                    stockQuantity = 15L,
                    imageUrl = "res://img_grocery_fruits",
                    isAvailable = true,
                    isFeatured = false,
                    city = "Bahawalpur"
                ),
                isAdminOverride = true
            )
        }

        // Switch auth user to a completely separate customer device account
        signInTestUser("second_device_${UUID.randomUUID()}@test.com")
        val secondDeviceFoods = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAvailableFoodItems().first { list -> list.any { it.id == foodId } }
        }
        val secondDeviceGroceries = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAvailableGroceryProducts().first { list -> list.any { it.id == grocId } }
        }
        assertTrue(secondDeviceFoods.any { it.id == foodId && it.name == "Admin Zinger Deal" })
        assertTrue(secondDeviceGroceries.any { it.id == grocId && it.name == "Admin Mango Crate" })
    }

    @Test
    fun walletTopUp_placeOrderWithWallet_andCancelRefund_updatesBalanceAndLedgerAtomically() = runBlocking {
        val custUid = signInTestUser("wallet_cust_${UUID.randomUUID()}@test.com")
        val repo = GharTakRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile(custUid, "Bilal Wallet", "bilal@test.com", null)
        }

        // Paying with 0 PKR balance throws IllegalStateException
        val insufficientEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repo.placeOrderWithWallet(
                    OrderRecord(
                        orderType = "food",
                        customerId = custUid,
                        customerName = "Bilal Wallet",
                        customerPhone = "+923001112233",
                        vendorId = "rest_w_1",
                        vendorName = "Khan BBQ",
                        vendorOwnerId = "owner_w_1",
                        deliveryAddress = "Model Town A, Bahawalpur",
                        city = "Bahawalpur",
                        items = listOf(OrderLineItem("f1", "Tikka", 1, 500.0, "piece")),
                        subtotalPkr = 500.0,
                        deliveryFeePkr = 80.0,
                        totalPkr = 580.0,
                        commissionPkr = 75.0,
                        vendorEarningsPkr = 425.0,
                        riderEarningsPkr = 70.0
                    )
                )
            }
        }.exceptionOrNull()
        assertTrue(insufficientEx is IllegalStateException)

        // Top up 1,500 PKR via JazzCash
        val newBalance = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.topUpWallet(custUid, 1500.0, "JazzCash", "Mobile wallet topup")
        }
        assertEquals(1500.0, newBalance, 0.01)

        // Place Wallet order of 580 PKR -> balance becomes 920 PKR
        val orderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.placeOrderWithWallet(
                OrderRecord(
                    orderType = "food",
                    customerId = custUid,
                    customerName = "Bilal Wallet",
                    customerPhone = "+923001112233",
                    vendorId = "rest_w_1",
                    vendorName = "Khan BBQ",
                    vendorOwnerId = "owner_w_1",
                    deliveryAddress = "Model Town A, Bahawalpur",
                    city = "Bahawalpur",
                    items = listOf(OrderLineItem("f1", "Tikka", 1, 500.0, "piece")),
                    subtotalPkr = 500.0,
                    deliveryFeePkr = 80.0,
                    totalPkr = 580.0,
                    commissionPkr = 75.0,
                    vendorEarningsPkr = 425.0,
                    riderEarningsPkr = 70.0
                )
            )
        }
        val profileAfterPay = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(custUid).first { it != null && it.walletBalancePkr == 920.0 }
        }
        assertEquals(920.0, profileAfterPay?.walletBalancePkr ?: -1.0, 0.01)

        // Cancel Wallet order -> atomically refunds 580 PKR back to 1,500 PKR and records topup/payment/refund ledger
        val refunded = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.cancelOrderByCustomer(orderId, "Changed my mind")
        }
        assertTrue(refunded)

        val profileAfterRefund = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(custUid).first { it != null && it.walletBalancePkr == 1500.0 }
        }
        assertEquals(1500.0, profileAfterRefund?.walletBalancePkr ?: -1.0, 0.01)

        val ledger = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeWalletTransactions(custUid).first { list -> list.size >= 3 }
        }
        assertTrue(ledger.any { it.type == "topup" && it.amountPkr == 1500.0 })
        assertTrue(ledger.any { it.type == "payment" && it.referenceId == orderId && it.amountPkr == 580.0 })
        assertTrue(ledger.any { it.type == "refund" && it.referenceId == orderId && it.amountPkr == 580.0 })
    }

    @Test
    fun sendSupportChatMessage_roundTripsWithSupportPhoneAndIsolatesOtherUsers() = runBlocking {
        val aliceUid = signInTestUser("chatalice_${UUID.randomUUID()}@test.com")
        val repo = GharTakRepository(firestore)

        val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.sendSupportChatMessage(
                threadUserId = aliceUid,
                customerName = "Ayesha Noor",
                customerPhone = "+923001122334",
                senderName = "Ayesha Noor",
                senderRole = "customer",
                text = "Assalam-o-Alaikum, please confirm my grocery order ETA.",
                orderId = "ord_test_chat"
            )
        }
        assertTrue(msgId.isNotBlank())

        val thread = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSupportChatThread(aliceUid).first { it != null && it.unreadByAdmin == 1L }
        }
        assertEquals("03036767152", thread?.supportPhone)
        assertEquals("customer", thread?.lastSenderRole)

        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSupportChatMessages(aliceUid).first { list -> list.any { it.id == msgId } }
        }
        assertEquals(1, messages.size)
        assertEquals("03036767152", messages.first().supportPhone)
        assertEquals("ord_test_chat", messages.first().orderId)

        // Cross-user isolation: another customer cannot read Alice's chat messages
        signInTestUser("chatbob_${UUID.randomUUID()}@test.com")
        val bobEx = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) { repo.observeSupportChatMessages(aliceUid).first() }
        }.exceptionOrNull()
        val firestoreEx = checkNotNull(bobEx?.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $bobEx" }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}

