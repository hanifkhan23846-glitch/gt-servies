package com.example.data.remote

import com.example.data.model.CelebrationItem
import com.example.data.model.CouponRecord
import com.example.data.model.FoodItem
import com.example.data.model.GharTakConstants
import com.example.data.model.GroceryProduct
import com.example.data.model.GroceryStore
import com.example.data.model.MedicalProduct
import com.example.data.model.MultanItem
import com.example.data.model.PlatformSettings
import com.example.data.model.Restaurant
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CatalogSeedHelper {

    suspend fun seedStarterCatalog(
        db: FirebaseFirestore,
        adminId: String,
        adminEmail: String
    ) {
        val batch = db.batch()

        // 1. Platform Settings (preserve existing Admin edits if already configured)
        val settingsRef = db.collection("platformSettings").document("global")
        val existingSettingsSnap = runCatching { settingsRef.get().await() }.getOrNull()
        if (existingSettingsSnap == null || !existingSettingsSnap.exists()) {
            batch.set(
                settingsRef,
                mapOf(
                    "cities" to GharTakConstants.INITIAL_PAKISTAN_CITIES,
                    "restaurantCommissionPercent" to 15.0,
                    "groceryCommissionPercent" to 10.0,
                    "baseDeliveryFeePkr" to 80.0,
                    "riderPayoutPkr" to 70.0,
                    "jazzCashConfigured" to true,
                    "easypaisaConfigured" to true,
                    "jazzCashNumber" to GharTakConstants.DEFAULT_JAZZCASH_NUMBER,
                    "jazzCashAccountTitle" to GharTakConstants.DEFAULT_BENEFICIARY_TITLE,
                    "easypaisaNumber" to GharTakConstants.DEFAULT_EASYPAISA_NUMBER,
                    "easypaisaAccountTitle" to GharTakConstants.DEFAULT_BENEFICIARY_TITLE,
                    "beneficiaryAccounts" to emptyList<Map<String, String>>(),
                    "promoBannerEnabled" to true,
                    "promoCode" to "GHARTAK20",
                    "promoDiscountPercent" to 20,
                    "promoBadgeText" to "USE CODE: GHARTAK20",
                    "promoTitle" to "20% OFF Food & Groceries",
                    "promoSubtitle" to "Your Favourite Food, Delivered to Your Door!",
                    "promoRestaurantId" to "",
                    "promoRestaurantName" to "",
                    "promoOffers" to emptyList<Map<String, Any>>(),
                    "deliveryAreaFeeRules" to listOf(
                        mapOf("id" to "area_bwp_city", "city" to "Bahawalpur", "areaName" to "Model Town / Circular Road (City)", "feePkr" to 150.0, "maxDistanceKm" to 5.0),
                        mapOf("id" to "area_bwp_sat", "city" to "Bahawalpur", "areaName" to "Satellite Town", "feePkr" to 200.0, "maxDistanceKm" to 8.0),
                        mapOf("id" to "area_bwp_ibrahim", "city" to "Bahawalpur", "areaName" to "Ibrahim City (Outer Area)", "feePkr" to 300.0, "maxDistanceKm" to 15.0),
                        mapOf("id" to "area_bwp_yazman", "city" to "Bahawalpur", "areaName" to "Yazman Road / Baghdad Campus", "feePkr" to 280.0, "maxDistanceKm" to 14.0),
                        mapOf("id" to "area_mlt_cantt", "city" to "Multan", "areaName" to "Cantt / Gulgasht (City)", "feePkr" to 120.0, "maxDistanceKm" to 5.0),
                        mapOf("id" to "area_mlt_outer", "city" to "Multan", "areaName" to "Bosan Road / DHA Outer", "feePkr" to 250.0, "maxDistanceKm" to 12.0)
                    ),
                    "multanCityDeliveryFees" to listOf(
                        mapOf("city" to "Multan", "feePkr" to 120.0),
                        mapOf("city" to "Bahawalpur", "feePkr" to 350.0),
                        mapOf("city" to "Lodhran", "feePkr" to 300.0),
                        mapOf("city" to "Khanewal", "feePkr" to 280.0),
                        mapOf("city" to "Lahore", "feePkr" to 450.0),
                        mapOf("city" to "Islamabad", "feePkr" to 550.0)
                    ),
                    "multanDefaultDeliveryFeePkr" to 350.0,
                    "supportPhoneLocal" to GharTakConstants.SUPPORT_PHONE_LOCAL,
                    "supportWhatsappNumber" to GharTakConstants.SUPPORT_WHATSAPP_NUMBER,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 2. Food Categories (All 10 required)
        val foodIconKeys = listOf("pizza", "burger", "biryani", "bbq", "chinese", "fastfood", "pakistani", "shawarma", "desserts", "drinks")
        GharTakConstants.FOOD_CATEGORIES.forEachIndexed { idx, name ->
            val id = "fcat_${idx + 1}"
            batch.set(
                db.collection("foodCategories").document(id),
                mapOf(
                    "name" to name,
                    "iconKey" to foodIconKeys.getOrElse(idx) { "restaurant" },
                    "isActive" to true,
                    "sortOrder" to idx + 1L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 3. Grocery Categories (All 18 required)
        GharTakConstants.GROCERY_CATEGORIES.forEachIndexed { idx, name ->
            val id = "gcat_${idx + 1}"
            batch.set(
                db.collection("groceryCategories").document(id),
                mapOf(
                    "name" to name,
                    "iconKey" to "grocery_${idx + 1}",
                    "isActive" to true,
                    "sortOrder" to idx + 1L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 4. Sample Approved Bahawalpur Restaurants (Each with real restaurant photography + separate login)
        val restaurants = listOf(
            listOf("rest_royal_biryani", "Royal Biryani & BBQ", "Circular Road, Near Farid Gate, Bahawalpur", "res://img_rest_royal_biryani", "royal_biryani", "Royal1234"),
            listOf("rest_baghdad_grill", "Baghdad Grill & Pizza", "Model Town A, Bahawalpur", "res://img_rest_baghdad_grill", "baghdad_grill", "Baghdad1234"),
            listOf("rest_sadiq_spice", "Sadiq Garh Spice Kitchen", "University Chowk, Bahawalpur", "res://img_rest_sadiq_spice", "sadiq_spice", "Sadiq1234"),
            listOf("rest_noor_mahal", "Noor Mahal Karahi & Cafe", "Model Town B Main Boulevard, Bahawalpur", "res://img_rest_noor_mahal", "noor_mahal", "Noor1234")
        )
        restaurants.forEachIndexed { idx, row ->
            val id = row[0]
            val name = row[1]
            val addr = row[2]
            val imgUri = row[3]
            val loginId = row[4]
            val portalPass = row[5]
            val passHash = GharTakConstants.sha256Hex("$loginId:$portalPass")
            batch.set(
                db.collection("restaurants").document(id),
                mapOf(
                    "ownerId" to adminId,
                    "name" to name,
                    "ownerName" to "GT Service Partner ${idx + 1}",
                    "phone" to "+92300111220${idx + 1}",
                    "email" to adminEmail,
                    "address" to addr,
                    "city" to "Bahawalpur",
                    "imageUrl" to imgUri,
                    "description" to "Authentic Bahawalpur flavours prepared fresh on order with hygienic packaging.",
                    "deliveryTimeMinutes" to (25L + idx * 4L),
                    "deliveryFeePkr" to (70.0 + idx * 10.0),
                    "minOrderPkr" to 300.0,
                    "rating" to (4.7 + (idx % 3) * 0.1).coerceAtMost(4.9),
                    "reviewCount" to (42L + idx * 18L),
                    "approvalStatus" to "approved",
                    "isOpen" to true,
                    "openingTime" to when (id) {
                        "rest_noor_mahal" -> "18:00"
                        "rest_baghdad_grill" -> "11:00"
                        else -> "00:00"
                    },
                    "closingTime" to when (id) {
                        "rest_noor_mahal" -> "02:00"
                        "rest_baghdad_grill" -> "23:59"
                        else -> "23:59"
                    },
                    "commissionPercent" to 15.0,
                    "portalLoginId" to loginId,
                    "portalPassHash" to passHash,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("adminRestaurantCredentials").document(id),
                mapOf(
                    "restaurantId" to id,
                    "restaurantName" to name,
                    "loginId" to loginId,
                    "password" to portalPass,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 5. Sample Food Items (All 10 food categories with real dish images)
        val foodItems = listOf(
            FoodItem(
                id = "food_1",
                restaurantId = "rest_royal_biryani",
                restaurantName = "Royal Biryani & BBQ",
                ownerId = adminId,
                categoryId = "fcat_3",
                categoryName = "Biryani",
                name = "Special Sindhi Chicken Biryani",
                description = "Aromatic basmati rice layered with spicy chicken masala, boiled egg & mint raita.",
                regularPricePkr = 520.0,
                discountPricePkr = 450.0,
                imageUrl = "res://img_food_biryani",
                stockQuantity = 100L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_2",
                restaurantId = "rest_royal_biryani",
                restaurantName = "Royal Biryani & BBQ",
                ownerId = adminId,
                categoryId = "fcat_4",
                categoryName = "BBQ",
                name = "Seekh Kabab & Malai Boti Platter",
                description = "Charcoal-grilled beef & chicken seekh kababs served with tamarind chutney & naan.",
                regularPricePkr = 780.0,
                discountPricePkr = 690.0,
                imageUrl = "res://img_food_bbq",
                stockQuantity = 60L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_3",
                restaurantId = "rest_baghdad_grill",
                restaurantName = "Baghdad Grill & Pizza",
                ownerId = adminId,
                categoryId = "fcat_1",
                categoryName = "Pizza",
                name = "Chicken Tikka Crown Pizza (Large)",
                description = "Smoked chicken tikka chunks, mozzarella, jalapenos & creamy garlic crust.",
                regularPricePkr = 1450.0,
                discountPricePkr = 1250.0,
                imageUrl = "res://img_food_pizza",
                stockQuantity = 40L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_4",
                restaurantId = "rest_baghdad_grill",
                restaurantName = "Baghdad Grill & Pizza",
                ownerId = adminId,
                categoryId = "fcat_2",
                categoryName = "Burger",
                name = "Crispy Zinger Crunch Burger",
                description = "Golden crispy chicken fillet with cheddar, iceberg lettuce & signature sauce.",
                regularPricePkr = 490.0,
                discountPricePkr = 420.0,
                imageUrl = "res://img_food_burger",
                stockQuantity = 80L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_5",
                restaurantId = "rest_sadiq_spice",
                restaurantName = "Sadiq Garh Spice Kitchen",
                ownerId = adminId,
                categoryId = "fcat_7",
                categoryName = "Pakistani Food",
                name = "Lahori Chicken Karahi (Half)",
                description = "Wok-seared chicken in fresh tomatoes, ginger juliennes & green chilies.",
                regularPricePkr = 1150.0,
                discountPricePkr = 990.0,
                imageUrl = "res://img_food_pakistani",
                stockQuantity = 35L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_6",
                restaurantId = "rest_sadiq_spice",
                restaurantName = "Sadiq Garh Spice Kitchen",
                ownerId = adminId,
                categoryId = "fcat_5",
                categoryName = "Chinese",
                name = "Chicken Manchurian & Egg Fried Rice",
                description = "Savory glazed chicken bites in garlic chili sauce with wok-tossed fried rice.",
                regularPricePkr = 720.0,
                discountPricePkr = 640.0,
                imageUrl = "res://img_food_chinese",
                stockQuantity = 50L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_7",
                restaurantId = "rest_baghdad_grill",
                restaurantName = "Baghdad Grill & Pizza",
                ownerId = adminId,
                categoryId = "fcat_6",
                categoryName = "Fast Food",
                name = "Crispy Chicken Broast & Fries Combo",
                description = "Two golden spiced fried chicken pieces with crinkle fries & garlic mayo dip.",
                regularPricePkr = 680.0,
                discountPricePkr = 590.0,
                imageUrl = "res://img_food_fastfood",
                stockQuantity = 65L,
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_8",
                restaurantId = "rest_royal_biryani",
                restaurantName = "Royal Biryani & BBQ",
                ownerId = adminId,
                categoryId = "fcat_8",
                categoryName = "Shawarma",
                name = "Arabic Garlic Chicken Shawarma",
                description = "Char-grilled spiced chicken strips wrapped in warm pita with pickled veggies & tahini.",
                regularPricePkr = 360.0,
                discountPricePkr = 310.0,
                imageUrl = "res://img_food_shawarma",
                stockQuantity = 90L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_9",
                restaurantId = "rest_sadiq_spice",
                restaurantName = "Sadiq Garh Spice Kitchen",
                ownerId = adminId,
                categoryId = "fcat_9",
                categoryName = "Desserts",
                name = "Warm Gulab Jamun & Pistachio Kheer",
                description = "Soft syrup-soaked gulab jamun paired with chilled cardamom pistachio kheer.",
                regularPricePkr = 420.0,
                discountPricePkr = 360.0,
                imageUrl = "res://img_food_desserts",
                stockQuantity = 45L,
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_10",
                restaurantId = "rest_noor_mahal",
                restaurantName = "Noor Mahal Karahi & Cafe",
                ownerId = adminId,
                categoryId = "fcat_10",
                categoryName = "Drinks",
                name = "Fresh Mint Margarita & Mango Lassi",
                description = "Chilled lime-mint cooler and creamy Bahawalpur mango lassi served ice cold.",
                regularPricePkr = 280.0,
                discountPricePkr = 240.0,
                imageUrl = "res://img_food_drinks",
                stockQuantity = 100L,
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            FoodItem(
                id = "food_11",
                restaurantId = "rest_noor_mahal",
                restaurantName = "Noor Mahal Karahi & Cafe",
                ownerId = adminId,
                categoryId = "fcat_7",
                categoryName = "Pakistani Food",
                name = "Noor Mahal Special Mutton Karahi",
                description = "Slow-cooked tender mutton in fresh desi ghee, black pepper & green chilies.",
                regularPricePkr = 2450.0,
                discountPricePkr = 2190.0,
                imageUrl = "res://img_food_pakistani",
                stockQuantity = 30L,
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            )
        )
        foodItems.forEachIndexed { idx, item ->
            val availableDays = when (item.id) {
                "food_1" -> listOf("Mon", "Wed", "Fri", "Sun")
                "food_5" -> listOf("Tue", "Thu", "Sat")
                "food_11" -> listOf("Fri", "Sat", "Sun")
                else -> emptyList()
            }
            val isDailySpecial = idx in listOf(0, 1, 4, 10)
            batch.set(
                db.collection("foodItems").document(item.id),
                mapOf(
                    "restaurantId" to item.restaurantId,
                    "restaurantName" to item.restaurantName,
                    "ownerId" to item.ownerId,
                    "categoryId" to item.categoryId,
                    "categoryName" to item.categoryName,
                    "name" to item.name,
                    "description" to item.description,
                    "regularPricePkr" to item.regularPricePkr,
                    "discountPricePkr" to item.discountPricePkr,
                    "imageUrl" to item.imageUrl,
                    "additionalImages" to item.additionalImages,
                    "preparationTimeMinutes" to item.preparationTimeMinutes,
                    "stockQuantity" to item.stockQuantity,
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "availableDays" to availableDays,
                    "isDailySpecial" to isDailySpecial,
                    "city" to item.city,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 6. Sample Approved Grocery Stores (5 stores with dedicated real grocery photography)
        val stores = listOf(
            listOf("store_almadina", "Al-Madina Fresh Mart", "Satellite Town Main Market, Bahawalpur", "res://img_store_almadina", "Daily farm-fresh fruits, vegetables, dairy, meats & pantry staples."),
            listOf("store_victoria", "Victoria Bazaar Grocers", "Victoria Hospital Road, Bahawalpur", "res://img_store_victoria", "Trusted rice, chakki atta, desi ghee, spices & dry grocery hub."),
            listOf("store_mega_mart", "Bahawalpur Mega Mart", "Model Town A Main Boulevard, Bahawalpur", "res://img_store_mega_mart", "Chilled dairy, frozen items, fresh bakery & express supermarket delivery."),
            listOf("store_green_basket", "Green Basket Organic Store", "University Chowk, Bahawalpur", "res://img_store_green_basket", "Handpicked organic produce, farm eggs, honey & halal poultry."),
            listOf("store_chenab", "Chenab Cash & Carry", "Dubai Chowk, Bahawalpur", "res://img_store_chenab", "Wholesale & retail beverages, snacks, personal care & home cleaning.")
        )
        stores.forEachIndexed { idx, row ->
            val id = row[0]
            val name = row[1]
            val addr = row[2]
            val imgUri = row[3]
            val desc = row[4]
            batch.set(
                db.collection("groceryStores").document(id),
                mapOf(
                    "ownerId" to adminId,
                    "name" to name,
                    "ownerName" to "GT Service Mart Partner ${idx + 1}",
                    "phone" to "+92321555660${idx + 1}",
                    "email" to adminEmail,
                    "address" to addr,
                    "city" to "Bahawalpur",
                    "imageUrl" to imgUri,
                    "description" to desc,
                    "deliveryTimeMinutes" to (18L + idx * 4L),
                    "deliveryFeePkr" to (50.0 + (idx % 3) * 10.0),
                    "minOrderPkr" to 350.0,
                    "rating" to (4.7 + (idx % 3) * 0.1).coerceAtMost(4.9),
                    "reviewCount" to (31L + idx * 12L),
                    "approvalStatus" to "approved",
                    "isOpen" to true,
                    "openingTime" to "00:00",
                    "closingTime" to "23:59",
                    "commissionPercent" to 10.0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 7. Sample Grocery Products (All 17 categories/essentials with real product images)
        val groceryProducts = listOf(
            GroceryProduct(
                id = "groc_1",
                storeId = "store_almadina",
                storeName = "Al-Madina Fresh Mart",
                ownerId = adminId,
                categoryId = "gcat_1",
                categoryName = "Fruits",
                name = "Bahawalpur Sindhri Mangoes",
                description = "Sweet handpicked export-quality Sindhri mangoes & seasonal fruits.",
                regularPricePkr = 380.0,
                discountPricePkr = 340.0,
                unit = "1 kg",
                stockQuantity = 60L,
                imageUrl = "res://img_groc_fruits",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_2",
                storeId = "store_almadina",
                storeName = "Al-Madina Fresh Mart",
                ownerId = adminId,
                categoryId = "gcat_2",
                categoryName = "Vegetables",
                name = "Farm Fresh Tomatoes & Onions Basket",
                description = "Daily harvested red tomatoes, golden onions, potatoes & green chilies.",
                regularPricePkr = 260.0,
                discountPricePkr = 220.0,
                unit = "1 kg",
                stockQuantity = 85L,
                imageUrl = "res://img_groc_vegetables",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_3",
                storeId = "store_almadina",
                storeName = "Al-Madina Fresh Mart",
                ownerId = adminId,
                categoryId = "gcat_4",
                categoryName = "Milk and Eggs",
                name = "Olper's Full Cream Milk & Farm Eggs",
                description = "Pure pasteurized full cream milk 1L with farm-fresh brown eggs.",
                regularPricePkr = 320.0,
                discountPricePkr = 295.0,
                unit = "1 litre",
                stockQuantity = 120L,
                imageUrl = "res://img_groc_milk",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_4",
                storeId = "store_almadina",
                storeName = "Al-Madina Fresh Mart",
                ownerId = adminId,
                categoryId = "gcat_3",
                categoryName = "Dairy Products",
                name = "Creamy Dahi Yogurt & Cheddar Cheese",
                description = "Thick homemade-style yogurt, salted butter & pure dairy cheddar.",
                regularPricePkr = 480.0,
                discountPricePkr = 435.0,
                unit = "500 gram",
                stockQuantity = 65L,
                imageUrl = "res://img_groc_dairy",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_5",
                storeId = "store_almadina",
                storeName = "Al-Madina Fresh Mart",
                ownerId = adminId,
                categoryId = "gcat_5",
                categoryName = "Meat",
                name = "Fresh Halal Mutton & Beef Curry Cut",
                description = "Hygienically dressed fresh halal mutton and boneless beef cuts.",
                regularPricePkr = 1950.0,
                discountPricePkr = 1790.0,
                unit = "1 kg",
                stockQuantity = 40L,
                imageUrl = "res://img_groc_meat",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_6",
                storeId = "store_almadina",
                storeName = "Al-Madina Fresh Mart",
                ownerId = adminId,
                categoryId = "gcat_6",
                categoryName = "Chicken",
                name = "Fresh Halal Chicken Breast & Drumsticks",
                description = "Chilled tender chicken boneless fillet & karahi cut pieces.",
                regularPricePkr = 780.0,
                discountPricePkr = 720.0,
                unit = "1 kg",
                stockQuantity = 75L,
                imageUrl = "res://img_groc_chicken",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_7",
                storeId = "store_victoria",
                storeName = "Victoria Bazaar Grocers",
                ownerId = adminId,
                categoryId = "gcat_8",
                categoryName = "Rice and Flour",
                name = "Guard Super Kernel Basmati Rice",
                description = "Aged extra-long grain aromatic Pakistani basmati rice.",
                regularPricePkr = 420.0,
                discountPricePkr = 385.0,
                unit = "1 kg",
                stockQuantity = 90L,
                imageUrl = "res://img_groc_rice",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_8",
                storeId = "store_victoria",
                storeName = "Victoria Bazaar Grocers",
                ownerId = adminId,
                categoryId = "gcat_8",
                categoryName = "Rice and Flour",
                name = "Fine Chakki Whole Wheat Flour (Atta)",
                description = "Stone-ground whole wheat chakki atta for soft fresh rotis.",
                regularPricePkr = 1450.0,
                discountPricePkr = 1360.0,
                unit = "10 kg bag",
                stockQuantity = 50L,
                imageUrl = "res://img_groc_flour",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_9",
                storeId = "store_victoria",
                storeName = "Victoria Bazaar Grocers",
                ownerId = adminId,
                categoryId = "gcat_9",
                categoryName = "Cooking Oil and Ghee",
                name = "Dalda Fortified Cooking Oil",
                description = "Cholesterol-free vitamin A & D fortified sunflower cooking oil.",
                regularPricePkr = 560.0,
                discountPricePkr = 530.0,
                unit = "1 litre",
                stockQuantity = 75L,
                imageUrl = "res://img_groc_oil",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_10",
                storeId = "store_victoria",
                storeName = "Victoria Bazaar Grocers",
                ownerId = adminId,
                categoryId = "gcat_9",
                categoryName = "Cooking Oil and Ghee",
                name = "Pure Desi Ghee Jar",
                description = "Traditional rich golden pure cow & buffalo desi ghee.",
                regularPricePkr = 2400.0,
                discountPricePkr = 2190.0,
                unit = "1 kg",
                stockQuantity = 35L,
                imageUrl = "res://img_groc_ghee",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_11",
                storeId = "store_victoria",
                storeName = "Victoria Bazaar Grocers",
                ownerId = adminId,
                categoryId = "gcat_10",
                categoryName = "Spices",
                name = "Royal Pakistani Garam Masala & Spices",
                description = "Aromatic haldi, red chili, cumin, green cardamom & whole spices.",
                regularPricePkr = 340.0,
                discountPricePkr = 299.0,
                unit = "250 gram",
                stockQuantity = 95L,
                imageUrl = "res://img_groc_spices",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_12",
                storeId = "store_mega_mart",
                storeName = "Bahawalpur Mega Mart",
                ownerId = adminId,
                categoryId = "gcat_11",
                categoryName = "Bakery Items",
                name = "Fresh Milky Bread & Croissants Pack",
                description = "Oven-fresh sandwich bread loaf, butter croissants & crispy rusks.",
                regularPricePkr = 290.0,
                discountPricePkr = 260.0,
                unit = "1 packet",
                stockQuantity = 55L,
                imageUrl = "res://img_groc_bakery",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_13",
                storeId = "store_chenab",
                storeName = "Chenab Cash & Carry",
                ownerId = adminId,
                categoryId = "gcat_12",
                categoryName = "Beverages",
                name = "Chilled Mango & Citrus Beverages Pack",
                description = "Refreshing chilled fruit juices, sparkling drinks & mineral water.",
                regularPricePkr = 360.0,
                discountPricePkr = 320.0,
                unit = "1 litre",
                stockQuantity = 100L,
                imageUrl = "res://img_groc_beverages",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_14",
                storeId = "store_chenab",
                storeName = "Chenab Cash & Carry",
                ownerId = adminId,
                categoryId = "gcat_13",
                categoryName = "Snacks",
                name = "Crispy Chips, Nimko & Roasted Nuts",
                description = "Crunchy golden potato chips, Lahori nimko mix & salted almonds.",
                regularPricePkr = 280.0,
                discountPricePkr = 240.0,
                unit = "1 packet",
                stockQuantity = 110L,
                imageUrl = "res://img_groc_snacks",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_15",
                storeId = "store_mega_mart",
                storeName = "Bahawalpur Mega Mart",
                ownerId = adminId,
                categoryId = "gcat_14",
                categoryName = "Frozen Foods",
                name = "K&N's Frozen Nuggets & Parathas",
                description = "Quick-fry halal chicken nuggets, flaky parathas & crispy samosas.",
                regularPricePkr = 890.0,
                discountPricePkr = 799.0,
                unit = "1 packet",
                stockQuantity = 45L,
                imageUrl = "res://img_groc_frozen",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_16",
                storeId = "store_chenab",
                storeName = "Chenab Cash & Carry",
                ownerId = adminId,
                categoryId = "gcat_15",
                categoryName = "Personal Care",
                name = "Herbal Shampoo, Soap & Skincare Set",
                description = "Nourishing hair shampoo, moisturizing cream & herbal bath soap.",
                regularPricePkr = 950.0,
                discountPricePkr = 850.0,
                unit = "1 packet",
                stockQuantity = 60L,
                imageUrl = "res://img_groc_personal_care",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_17",
                storeId = "store_chenab",
                storeName = "Chenab Cash & Carry",
                ownerId = adminId,
                categoryId = "gcat_16",
                categoryName = "Household Cleaning",
                name = "Dishwash Liquid, Detergent & Cleaner",
                description = "Lemon dishwashing liquid, laundry detergent & multi-surface cleaner.",
                regularPricePkr = 740.0,
                discountPricePkr = 660.0,
                unit = "1 litre",
                stockQuantity = 70L,
                imageUrl = "res://img_groc_household",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_18",
                storeId = "store_green_basket",
                storeName = "Green Basket Organic Store",
                ownerId = adminId,
                categoryId = "gcat_1",
                categoryName = "Fruits",
                name = "Organic Kinnow Oranges & Bananas Pack",
                description = "Farm-picked sweet Sargodha kinnow & fresh Cavendish bananas.",
                regularPricePkr = 320.0,
                discountPricePkr = 280.0,
                unit = "1 kg",
                stockQuantity = 70L,
                imageUrl = "res://img_groc_fruits",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_19",
                storeId = "store_green_basket",
                storeName = "Green Basket Organic Store",
                ownerId = adminId,
                categoryId = "gcat_4",
                categoryName = "Milk and Eggs",
                name = "Organic Brown Farm Eggs (12 pcs) & Pure Milk",
                description = "Free-range desi eggs paired with chilled whole farm milk.",
                regularPricePkr = 460.0,
                discountPricePkr = 415.0,
                unit = "1 packet",
                stockQuantity = 50L,
                imageUrl = "res://img_groc_milk",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            GroceryProduct(
                id = "groc_20",
                storeId = "store_mega_mart",
                storeName = "Bahawalpur Mega Mart",
                ownerId = adminId,
                categoryId = "gcat_3",
                categoryName = "Dairy Products",
                name = "Nurpur Unsalted Butter & Mozzarella Block",
                description = "Rich creamy pasteurized butter and stretchy pizza mozzarella block.",
                regularPricePkr = 780.0,
                discountPricePkr = 710.0,
                unit = "500 gram",
                stockQuantity = 45L,
                imageUrl = "res://img_groc_dairy",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            )
        )
        groceryProducts.forEach { prod ->
            batch.set(
                db.collection("groceryProducts").document(prod.id),
                mapOf(
                    "storeId" to prod.storeId,
                    "storeName" to prod.storeName,
                    "ownerId" to prod.ownerId,
                    "categoryId" to prod.categoryId,
                    "categoryName" to prod.categoryName,
                    "name" to prod.name,
                    "description" to prod.description,
                    "regularPricePkr" to prod.regularPricePkr,
                    "discountPricePkr" to prod.discountPricePkr,
                    "unit" to prod.unit,
                    "stockQuantity" to prod.stockQuantity,
                    "imageUrl" to prod.imageUrl,
                    "additionalImages" to prod.additionalImages,
                    "isAvailable" to prod.isAvailable,
                    "isFeatured" to prod.isFeatured,
                    "city" to prod.city,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 8. Medical Categories, Pharmacies & Products
        GharTakConstants.MEDICAL_CATEGORIES.forEachIndexed { idx, name ->
            val id = "mcat_${idx + 1}"
            batch.set(
                db.collection("medicalCategories").document(id),
                mapOf(
                    "name" to name,
                    "iconKey" to "medical_${idx + 1}",
                    "isActive" to true,
                    "sortOrder" to idx + 1L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val medicalStores = listOf(
            listOf("med_store_victoria", "Victoria City Pharmacy", "Circular Road, Opposite BVH Hospital, Bahawalpur", "res://img_store_pharmacy"),
            listOf("med_store_alshifa", "Al-Shifa Care Pharmacy", "University Chowk, Bahawalpur", "res://img_store_pharmacy"),
            listOf("med_store_model", "Model Town Wellness Pharmacy", "Model Town A Main Market, Bahawalpur", "res://img_store_pharmacy")
        )
        medicalStores.forEachIndexed { idx, row ->
            val id = row[0]
            val name = row[1]
            val addr = row[2]
            val imgUri = row[3]
            batch.set(
                db.collection("medicalStores").document(id),
                mapOf(
                    "ownerId" to adminId,
                    "name" to name,
                    "ownerName" to "Licensed Pharmacist ${idx + 1}",
                    "phone" to "+92300881120${idx + 1}",
                    "email" to adminEmail,
                    "address" to addr,
                    "city" to "Bahawalpur",
                    "imageUrl" to imgUri,
                    "description" to "DRAP-compliant 24/7 pharmacy providing authentic medicines, first-aid & wellness essentials.",
                    "deliveryTimeMinutes" to (18L + idx * 3L),
                    "deliveryFeePkr" to (60.0 + idx * 10.0),
                    "minOrderPkr" to 250.0,
                    "rating" to (4.8 + (idx % 2) * 0.1),
                    "reviewCount" to (38L + idx * 14L),
                    "approvalStatus" to "approved",
                    "isOpen" to true,
                    "openingTime" to "00:00",
                    "closingTime" to "23:59",
                    "commissionPercent" to 10.0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val medicalProducts = listOf(
            MedicalProduct(
                id = "med_1",
                storeId = "med_store_victoria",
                storeName = "Victoria City Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_4",
                categoryName = "Pain & Fever",
                name = "Panadol Extra 500mg Strip",
                description = "Fast-acting paracetamol & caffeine tablets for headache, fever and body ache relief.",
                regularPricePkr = 120.0,
                discountPricePkr = 105.0,
                unit = "1 strip",
                stockQuantity = 120L,
                imageUrl = "res://img_med_medicines",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_2",
                storeId = "med_store_victoria",
                storeName = "Victoria City Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_1",
                categoryName = "Tablets & Capsules",
                name = "Brufen 400mg Pain Relief Pack",
                description = "Ibuprofen 400mg film-coated tablets for joint pain, muscle inflammation and dental pain.",
                regularPricePkr = 160.0,
                discountPricePkr = 140.0,
                unit = "1 strip",
                stockQuantity = 95L,
                imageUrl = "res://img_med_medicines",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_3",
                storeId = "med_store_alshifa",
                storeName = "Al-Shifa Care Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_1",
                categoryName = "Tablets & Capsules",
                name = "Augmentin 625mg Tablets Box",
                description = "Co-amoxiclav 625mg broad-spectrum tablets (dispensed against valid prescription).",
                regularPricePkr = 460.0,
                discountPricePkr = 425.0,
                unit = "1 box",
                stockQuantity = 60L,
                imageUrl = "res://img_med_medicines",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_4",
                storeId = "med_store_victoria",
                storeName = "Victoria City Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_2",
                categoryName = "Syrups & Drops",
                name = "Hydralyte / ORS Electral Pack (5 Sachets)",
                description = "WHO-recommended oral rehydration salts for rapid recovery from dehydration.",
                regularPricePkr = 190.0,
                discountPricePkr = 165.0,
                unit = "1 pack",
                stockQuantity = 110L,
                imageUrl = "res://img_med_vitamins",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_5",
                storeId = "med_store_alshifa",
                storeName = "Al-Shifa Care Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_3",
                categoryName = "First Aid & Bandages",
                name = "Dettol Antiseptic Liquid & Gauze Combo",
                description = "500ml antiseptic disinfectant bottle with sterile cotton bandage and dressing pads.",
                regularPricePkr = 590.0,
                discountPricePkr = 530.0,
                unit = "1 pack",
                stockQuantity = 75L,
                imageUrl = "res://img_med_firstaid",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_6",
                storeId = "med_store_model",
                storeName = "Model Town Wellness Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_10",
                categoryName = "Surgical & Devices",
                name = "Digital Fever Thermometer",
                description = "Fast 10-second clinical accuracy waterproof digital thermometer with memory recall.",
                regularPricePkr = 550.0,
                discountPricePkr = 470.0,
                unit = "1 piece",
                stockQuantity = 45L,
                imageUrl = "res://img_med_firstaid",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_7",
                storeId = "med_store_model",
                storeName = "Model Town Wellness Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_8",
                categoryName = "Heart & BP Care",
                name = "Automatic Digital BP Monitor",
                description = "Upper-arm digital blood pressure monitor with pulse rate and irregular heartbeat detector.",
                regularPricePkr = 5800.0,
                discountPricePkr = 4950.0,
                unit = "1 box",
                stockQuantity = 20L,
                imageUrl = "res://img_med_firstaid",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_8",
                storeId = "med_store_model",
                storeName = "Model Town Wellness Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_5",
                categoryName = "Vitamins & Supplements",
                name = "Centrum Daily Multivitamin & Calcium",
                description = "Complete A to Zinc daily immunity, Vitamin D3 & bone health formula.",
                regularPricePkr = 1650.0,
                discountPricePkr = 1480.0,
                unit = "1 bottle",
                stockQuantity = 50L,
                imageUrl = "res://img_med_vitamins",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_9",
                storeId = "med_store_alshifa",
                storeName = "Al-Shifa Care Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_7",
                categoryName = "Diabetes Care",
                name = "Glucometer Strips & Lancets Pack (50 Strips)",
                description = "Accurate blood glucose test strips and sterile fine-gauge lancets.",
                regularPricePkr = 2200.0,
                discountPricePkr = 1980.0,
                unit = "1 box",
                stockQuantity = 40L,
                imageUrl = "res://img_med_medicines",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_10",
                storeId = "med_store_victoria",
                storeName = "Victoria City Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_6",
                categoryName = "Baby & Mother Care",
                name = "Gentle Baby Gripe Water Syrup",
                description = "Alcohol-free herbal colic and tummy comfort relief for infants.",
                regularPricePkr = 320.0,
                discountPricePkr = 285.0,
                unit = "120 ml",
                stockQuantity = 65L,
                imageUrl = "res://img_med_vitamins",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_11",
                storeId = "med_store_victoria",
                storeName = "Victoria City Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_3",
                categoryName = "First Aid & Bandages",
                name = "Complete Home First Aid Emergency Kit",
                description = "24-piece compact medical box with bandages, antiseptic wipes, scissors & medical tape.",
                regularPricePkr = 1250.0,
                discountPricePkr = 1090.0,
                unit = "1 box",
                stockQuantity = 35L,
                imageUrl = "res://img_med_firstaid",
                isAvailable = true,
                isFeatured = true,
                city = "Bahawalpur"
            ),
            MedicalProduct(
                id = "med_12",
                storeId = "med_store_model",
                storeName = "Model Town Wellness Pharmacy",
                ownerId = adminId,
                categoryId = "mcat_9",
                categoryName = "Skin & Derma",
                name = "Soothing Calamine & Aloe Derma Lotion",
                description = "Dermatologist-tested cooling relief for rashes, sunburn and dry skin irritation.",
                regularPricePkr = 420.0,
                discountPricePkr = 370.0,
                unit = "1 bottle",
                stockQuantity = 55L,
                imageUrl = "res://img_med_medicines",
                isAvailable = true,
                isFeatured = false,
                city = "Bahawalpur"
            )
        )
        medicalProducts.forEach { prod ->
            batch.set(
                db.collection("medicalProducts").document(prod.id),
                mapOf(
                    "storeId" to prod.storeId,
                    "storeName" to prod.storeName,
                    "ownerId" to prod.ownerId,
                    "categoryId" to prod.categoryId,
                    "categoryName" to prod.categoryName,
                    "name" to prod.name,
                    "description" to prod.description,
                    "regularPricePkr" to prod.regularPricePkr,
                    "discountPricePkr" to prod.discountPricePkr,
                    "unit" to prod.unit,
                    "stockQuantity" to prod.stockQuantity,
                    "imageUrl" to prod.imageUrl,
                    "additionalImages" to prod.additionalImages,
                    "isAvailable" to prod.isAvailable,
                    "isFeatured" to prod.isFeatured,
                    "city" to prod.city,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 9. Starter Coupons
        batch.set(
            db.collection("coupons").document("cpn_ghartak20"),
            mapOf(
                "code" to "GHARTAK20",
                "discountType" to "percentage",
                "discountValue" to 20.0,
                "minOrderPkr" to 500.0,
                "maxDiscountPkr" to 250.0,
                "usageLimit" to 500L,
                "usedCount" to 0L,
                "isActive" to true,
                "expiresAtEpochMs" to 1900000000000L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            db.collection("coupons").document("cpn_bwp100"),
            mapOf(
                "code" to "BWP100",
                "discountType" to "fixed",
                "discountValue" to 100.0,
                "minOrderPkr" to 600.0,
                "maxDiscountPkr" to 100.0,
                "usageLimit" to 500L,
                "usedCount" to 0L,
                "isActive" to true,
                "expiresAtEpochMs" to 1900000000000L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        // 10. Anything You Want from Multan (Crumble, KFC, Sohan Halwa, etc.)
        val multanItems = listOf(
            MultanItem(
                id = "mlt_crumble_box6",
                brandName = "Crumble",
                categoryName = "Cookies & Bakery",
                name = "Crumble Multan Assorted Cookies (Box of 6)",
                description = "Freshly baked thick New York-style stuffed cookies straight from Crumble Multan (Lotus, Double Chocolate, Classic Choco Chunk & Red Velvet).",
                regularPricePkr = 2150.0,
                discountPricePkr = 1950.0,
                unit = "Box of 6",
                stockQuantity = 40L,
                imageUrl = "res://img_food_desserts",
                originCity = "Multan",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            MultanItem(
                id = "mlt_crumble_brookie",
                brandName = "Crumble",
                categoryName = "Cookies & Bakery",
                name = "Crumble Fudge Brookie & Cookie Duo",
                description = "Gooey Belgian chocolate brownie baked inside a crisp-edge chocolate chip cookie from Crumble Multan.",
                regularPricePkr = 1350.0,
                discountPricePkr = 1220.0,
                unit = "Box of 4",
                stockQuantity = 35L,
                imageUrl = "res://img_food_desserts",
                originCity = "Multan",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            MultanItem(
                id = "mlt_kfc_mighty",
                brandName = "KFC",
                categoryName = "Fast Food",
                name = "KFC Multan Mighty Zinger Combo",
                description = "Double crispy spicy zinger fillet burger with regular fries and chilled drink delivered hot from KFC Multan.",
                regularPricePkr = 1090.0,
                discountPricePkr = 990.0,
                unit = "1 meal",
                stockQuantity = 60L,
                imageUrl = "res://img_food_burger",
                originCity = "Multan",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            MultanItem(
                id = "mlt_kfc_bucket",
                brandName = "KFC",
                categoryName = "Fast Food",
                name = "KFC Multan 9-Pc Hot & Crispy Bucket",
                description = "Signature KFC 9-piece crispy fried chicken family bucket packed fresh from KFC Gulgasht Multan.",
                regularPricePkr = 3250.0,
                discountPricePkr = 2990.0,
                unit = "1 bucket",
                stockQuantity = 30L,
                imageUrl = "res://img_food_fastfood",
                originCity = "Multan",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            MultanItem(
                id = "mlt_hafiz_halwa",
                brandName = "Hafiz Sohan Halwa",
                categoryName = "Sohan Halwa & Sweets",
                name = "Hafiz Multani Akhrot Sohan Halwa Tin",
                description = "Original desi-ghee Multani Sohan Halwa topped with roasted walnuts, pistachios and almonds.",
                regularPricePkr = 1950.0,
                discountPricePkr = 1790.0,
                unit = "1 kg tin",
                stockQuantity = 50L,
                imageUrl = "res://img_food_desserts",
                originCity = "Multan",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            MultanItem(
                id = "mlt_rewari_gift",
                brandName = "Rewari Sweets",
                categoryName = "Sohan Halwa & Sweets",
                name = "Rewari Pistachio & Saffron Sohan Halwa Box",
                description = "Rich handcrafted Multani mithai gift pack with saffron, cardamom & green pistachios.",
                regularPricePkr = 1650.0,
                discountPricePkr = 1490.0,
                unit = "1 kg box",
                stockQuantity = 45L,
                imageUrl = "res://img_food_desserts",
                originCity = "Multan",
                isAvailable = true,
                isFeatured = false,
                city = GharTakConstants.ALL_CITIES
            )
        )
        multanItems.forEach { item ->
            batch.set(
                db.collection("multanItems").document(item.id),
                mapOf(
                    "brandName" to item.brandName,
                    "categoryName" to item.categoryName,
                    "name" to item.name,
                    "description" to item.description,
                    "regularPricePkr" to item.regularPricePkr,
                    "discountPricePkr" to item.discountPricePkr,
                    "unit" to item.unit,
                    "stockQuantity" to item.stockQuantity,
                    "imageUrl" to item.imageUrl,
                    "additionalImages" to item.additionalImages,
                    "originCity" to item.originCity,
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "city" to item.city,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // 11. Celebration Catalog (Cakes, Flowers, Balloons, Gifts & Party Combos)
        val celebrationItems = listOf(
            CelebrationItem(
                id = "clb_bday_cake",
                categoryName = "Birthday Cakes",
                name = "Belgian Chocolate Fudge Birthday Cake (2 lbs)",
                description = "Rich triple-layer chocolate sponge coated in Belgian ganache with custom Happy Birthday message.",
                regularPricePkr = 2600.0,
                discountPricePkr = 2290.0,
                unit = "2 lbs cake",
                stockQuantity = 25L,
                imageUrl = "res://img_food_desserts",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            CelebrationItem(
                id = "clb_anniv_cake",
                categoryName = "Anniversary Specials",
                name = "Red Velvet Cream Cheese Heart Cake",
                description = "Velvety crimson sponge layered with silky cream cheese frosting & edible gold pearls.",
                regularPricePkr = 2900.0,
                discountPricePkr = 2550.0,
                unit = "2 lbs cake",
                stockQuantity = 20L,
                imageUrl = "res://img_food_desserts",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            CelebrationItem(
                id = "clb_roses_bouquet",
                categoryName = "Flowers & Bouquets",
                name = "24 Fresh Red Roses & Baby's Breath Bouquet",
                description = "Hand-tied fresh long-stem red roses wrapped in premium Korean matte paper with satin ribbon & card.",
                regularPricePkr = 2200.0,
                discountPricePkr = 1950.0,
                unit = "1 bouquet",
                stockQuantity = 30L,
                imageUrl = "res://img_banner_deals",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            CelebrationItem(
                id = "clb_balloons_decor",
                categoryName = "Balloons & Decor",
                name = "Gold & Black Birthday Balloon + LED Backdrop Kit",
                description = "30 metallic helium-ready balloons, Happy Birthday foil banner, fairy LED string lights & table confetti.",
                regularPricePkr = 1650.0,
                discountPricePkr = 1390.0,
                unit = "1 kit",
                stockQuantity = 40L,
                imageUrl = "res://img_banner_deals",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            ),
            CelebrationItem(
                id = "clb_party_combo",
                categoryName = "Party Combos",
                name = "Grand Birthday Combo (Cake + Roses + Balloons)",
                description = "Complete surprise box: 2lb Chocolate Fudge Cake, 12 Red Roses Bouquet, Birthday Balloons & Custom Card.",
                regularPricePkr = 4500.0,
                discountPricePkr = 3990.0,
                unit = "1 combo",
                stockQuantity = 20L,
                imageUrl = "res://img_hero_food",
                isAvailable = true,
                isFeatured = true,
                city = GharTakConstants.ALL_CITIES
            )
        )
        celebrationItems.forEach { item ->
            batch.set(
                db.collection("celebrationItems").document(item.id),
                mapOf(
                    "categoryName" to item.categoryName,
                    "name" to item.name,
                    "description" to item.description,
                    "regularPricePkr" to item.regularPricePkr,
                    "discountPricePkr" to item.discountPricePkr,
                    "unit" to item.unit,
                    "stockQuantity" to item.stockQuantity,
                    "imageUrl" to item.imageUrl,
                    "additionalImages" to item.additionalImages,
                    "isAvailable" to item.isAvailable,
                    "isFeatured" to item.isFeatured,
                    "city" to item.city,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        batch.commit().await()
    }
}
