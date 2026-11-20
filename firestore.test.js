const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const crypto = require("node:crypto");

function sha256Hex(raw) {
  return crypto.createHash("sha256").update(raw, "utf8").digest("hex");
}

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const VENDOR_UID = "vendor_789";
const RIDER_UID = "rider_321";
const ADMIN_UID = "admin_001";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowTs() {
  return new Date(Date.now() - 1000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or list protected collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("restaurants").where("approvalStatus", "==", "approved").get());
  await assertFails(unauthDb.collection("foodItems").where("isAvailable", "==", true).get());
  await assertFails(unauthDb.collection("groceryProducts").where("isAvailable", "==", true).get());
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("User profile & addresses enforce strict owner isolation and block admin self-escalation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@test.com",
      phone: "+923001234567",
      city: "Bahawalpur",
      activePortal: "customer",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Bob cannot read Alice's profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Alice cannot self-escalate to admin portal
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activePortal: "admin",
      updatedAt: nowTs(),
    })
  );

  // Alice can add an address, Bob cannot read it
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("addresses").doc("addr_1").set({
      userId: ALICE_UID,
      label: "Home",
      houseNumber: "12-B",
      street: "Model Town A",
      area: "Satellite Town",
      city: "Bahawalpur",
      latitude: 29.3956,
      longitude: 71.6836,
      isDefault: true,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("addresses").get());
});

test("Bootstrapped Admin verification rejects unverified spoofed email and allows verified admin", async () => {
  const spoofedDb = testEnv.authenticatedContext("spoof_uid", {
    email: "hanifkhan23846@gmail.com",
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofedDb.collection("foodCategories").doc("cat_pizza").set({
      name: "Pizza",
      iconKey: "pizza",
      isActive: true,
      sortOrder: 1,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("foodCategories").doc("cat_pizza").set({
      name: "Pizza",
      iconKey: "pizza",
      isActive: true,
      sortOrder: 1,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Admin activity log is append-only (no update/delete)
  await assertSucceeds(
    adminDb.collection("adminActivityLogs").doc("log_1").set({
      adminId: ADMIN_UID,
      adminEmail: "hanifkhan23846@gmail.com",
      action: "CREATE_CATEGORY",
      targetType: "foodCategory",
      targetId: "cat_pizza",
      details: "Created Pizza category",
      createdAt: nowTs(),
    })
  );
  await assertFails(adminDb.collection("adminActivityLogs").doc("log_1").update({ details: "tampered" }));
  await assertFails(adminDb.collection("adminActivityLogs").doc("log_1").delete());
});

test("Restaurant ownership, approval, and order lifecycle enforce role boundaries and terminal locking", async () => {
  const vendorDb = testEnv.authenticatedContext(VENDOR_UID, { email: "vendor@test.com", email_verified: true }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();
  const riderDb = testEnv.authenticatedContext(RIDER_UID, { email: "rider@test.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();

  // Vendor cannot self-approve restaurant on creation
  await assertFails(
    vendorDb.collection("restaurants").doc("rest_1").set({
      ownerId: VENDOR_UID,
      name: "Royal Biryani House",
      ownerName: "Usman",
      phone: "+923001112233",
      email: "vendor@test.com",
      address: "Circular Road",
      city: "Bahawalpur",
      deliveryTimeMinutes: 30,
      deliveryFeePkr: 90,
      minOrderPkr: 300,
      rating: 5,
      reviewCount: 0,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 15,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Vendor registers pending restaurant
  await assertSucceeds(
    vendorDb.collection("restaurants").doc("rest_1").set({
      ownerId: VENDOR_UID,
      name: "Royal Biryani House",
      ownerName: "Usman",
      phone: "+923001112233",
      email: "vendor@test.com",
      address: "Circular Road",
      city: "Bahawalpur",
      deliveryTimeMinutes: 30,
      deliveryFeePkr: 90,
      minOrderPkr: 300,
      rating: 5,
      reviewCount: 0,
      approvalStatus: "pending",
      isOpen: true,
      commissionPercent: 15,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Admin approves restaurant
  await assertSucceeds(
    adminDb.collection("restaurants").doc("rest_1").update({
      approvalStatus: "approved",
      updatedAt: nowTs(),
    })
  );

  // Unapproved rider registers and admin approves
  await assertSucceeds(
    riderDb.collection("riders").doc(RIDER_UID).set({
      userId: RIDER_UID,
      fullName: "Bilal Ahmed",
      phone: "+923214445566",
      email: "rider@test.com",
      cnicNumber: "31202-1234567-1",
      licenseNumber: "BWP-9921",
      vehicleType: "Motorbike",
      vehicleNumber: "BWP-4421",
      city: "Bahawalpur",
      approvalStatus: "pending",
      isOnline: true,
      latitude: 29.3956,
      longitude: 71.6836,
      totalEarningsPkr: 0,
      completedDeliveries: 0,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Customer Alice places order
  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_1").set({
      orderType: "food",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "+923001234567",
      vendorId: "rest_1",
      vendorName: "Royal Biryani House",
      vendorOwnerId: VENDOR_UID,
      riderId: "",
      riderName: "",
      deliveryAddress: "12-B Model Town A, Bahawalpur",
      city: "Bahawalpur",
      deliveryLat: 29.3956,
      deliveryLng: 71.6836,
      items: [{ productId: "item_1", name: "Chicken Biryani", quantity: 2, unitPricePkr: 450, unit: "plate" }],
      subtotalPkr: 900,
      deliveryFeePkr: 90,
      discountPkr: 0,
      couponCode: "",
      totalPkr: 990,
      commissionPkr: 135,
      vendorEarningsPkr: 765,
      riderEarningsPkr: 70,
      paymentMethod: "COD",
      paymentStatus: "pending",
      status: "Pending",
      cancellationReason: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Uninvolved user Bob cannot read Alice's order
  await assertFails(bobDb.collection("orders").doc("ord_1").get());

  // Vendor marks Ready for Pickup
  await assertSucceeds(
    vendorDb.collection("orders").doc("ord_1").update({
      status: "Ready for Pickup",
      updatedAt: nowTs(),
    })
  );

  // Unapproved rider cannot claim order
  await assertFails(
    riderDb.collection("orders").doc("ord_1").update({
      riderId: RIDER_UID,
      riderName: "Bilal Ahmed",
      status: "Rider Assigned",
      updatedAt: nowTs(),
    })
  );

  // Admin approves rider
  await assertSucceeds(
    adminDb.collection("riders").doc(RIDER_UID).update({
      approvalStatus: "approved",
      updatedAt: nowTs(),
    })
  );

  // Approved rider claims and delivers
  await assertSucceeds(
    riderDb.collection("orders").doc("ord_1").update({
      riderId: RIDER_UID,
      riderName: "Bilal Ahmed",
      status: "Rider Assigned",
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    riderDb.collection("orders").doc("ord_1").update({
      status: "Delivered",
      paymentStatus: "paid",
      updatedAt: nowTs(),
    })
  );

  // Terminal state lock: customer/vendor/rider cannot mutate Delivered order
  await assertFails(
    aliceDb.collection("orders").doc("ord_1").update({
      status: "Cancelled",
      cancellationReason: "late",
      updatedAt: nowTs(),
    })
  );
});

test("FoodItem & GroceryProduct enforce images, prep time, units, price validation, and admin/owner boundaries", async () => {
  const vendorDb = testEnv.authenticatedContext(VENDOR_UID, { email: "vendor@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();

  // Seed restaurant and grocery store owned by VENDOR_UID
  await assertSucceeds(
    adminDb.collection("restaurants").doc("rest_food_1").set({
      ownerId: VENDOR_UID,
      name: "BWP Karahi Spot",
      ownerName: "Usman",
      phone: "+923001112233",
      email: "vendor@test.com",
      address: "Model Town A",
      city: "Bahawalpur",
      deliveryTimeMinutes: 30,
      deliveryFeePkr: 90,
      minOrderPkr: 300,
      rating: 4.8,
      reviewCount: 12,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 15,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("groceryStores").doc("store_groc_1").set({
      ownerId: VENDOR_UID,
      name: "Al-Fatah Bahawalpur",
      ownerName: "Usman",
      phone: "+923001112233",
      email: "vendor@test.com",
      address: "Circular Road",
      city: "Bahawalpur",
      deliveryTimeMinutes: 25,
      deliveryFeePkr: 70,
      minOrderPkr: 250,
      rating: 4.9,
      reviewCount: 20,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 12,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Restaurant owner CANNOT add new FoodItem products (restricted to Super Admin only)
  await assertFails(
    vendorDb.collection("foodItems").doc("food_1").set({
      restaurantId: "rest_food_1",
      restaurantName: "BWP Karahi Spot",
      ownerId: VENDOR_UID,
      categoryId: "cat_pakistani",
      categoryName: "Pakistani Food",
      name: "Mutton Karahi Half",
      description: "Freshly cooked mutton karahi",
      regularPricePkr: 1850,
      discountPricePkr: 1650,
      imageUrl: "data:image/jpeg;base64,/9j/4AAQ",
      additionalImages: ["data:image/png;base64,iVBORw0KGgo="],
      preparationTimeMinutes: 25,
      stockQuantity: 20,
      isAvailable: true,
      isFeatured: true,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Admin creates FoodItem for restaurant with additionalImages & preparationTimeMinutes
  await assertSucceeds(
    adminDb.collection("foodItems").doc("food_1").set({
      restaurantId: "rest_food_1",
      restaurantName: "BWP Karahi Spot",
      ownerId: VENDOR_UID,
      categoryId: "cat_pakistani",
      categoryName: "Pakistani Food",
      name: "Mutton Karahi Half",
      description: "Freshly cooked mutton karahi",
      regularPricePkr: 1850,
      discountPricePkr: 1650,
      imageUrl: "data:image/jpeg;base64,/9j/4AAQ",
      additionalImages: ["data:image/png;base64,iVBORw0KGgo="],
      preparationTimeMinutes: 25,
      stockQuantity: 20,
      isAvailable: true,
      isFeatured: true,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Restaurant owner can still update stock/availability on an existing item assigned to them
  await assertSucceeds(
    vendorDb.collection("foodItems").doc("food_1").update({
      stockQuantity: 18,
      updatedAt: nowTs(),
    })
  );

  // Invalid discountPricePkr > regularPricePkr fails
  await assertFails(
    adminDb.collection("foodItems").doc("food_bad_price").set({
      restaurantId: "rest_food_1",
      restaurantName: "BWP Karahi Spot",
      ownerId: VENDOR_UID,
      categoryId: "cat_pakistani",
      categoryName: "Pakistani Food",
      name: "Bad Discount Dish",
      description: "Invalid discount",
      regularPricePkr: 500,
      discountPricePkr: 700,
      preparationTimeMinutes: 15,
      stockQuantity: 10,
      isAvailable: true,
      isFeatured: false,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Invalid preparationTimeMinutes > 300 fails
  await assertFails(
    adminDb.collection("foodItems").doc("food_bad_prep").set({
      restaurantId: "rest_food_1",
      restaurantName: "BWP Karahi Spot",
      ownerId: VENDOR_UID,
      categoryId: "cat_pakistani",
      categoryName: "Pakistani Food",
      name: "Slow Dish",
      description: "Invalid prep time",
      regularPricePkr: 500,
      discountPricePkr: 450,
      preparationTimeMinutes: 450,
      stockQuantity: 10,
      isAvailable: true,
      isFeatured: false,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Vendor creates GroceryProduct with standard unit "500 gram" & custom unit "2.5 kg box"
  await assertSucceeds(
    vendorDb.collection("groceryProducts").doc("groc_1").set({
      storeId: "store_groc_1",
      storeName: "Al-Fatah Bahawalpur",
      ownerId: VENDOR_UID,
      categoryId: "cat_dairy",
      categoryName: "Dairy Products",
      name: "Desi Butter",
      description: "Pure farm butter",
      regularPricePkr: 950,
      discountPricePkr: 880,
      unit: "500 gram",
      stockQuantity: 35,
      imageUrl: "data:image/jpeg;base64,/9j/4AAQ",
      additionalImages: ["data:image/webp;base64,UklGR"],
      isAvailable: true,
      isFeatured: true,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("groceryProducts").doc("groc_custom").set({
      storeId: "store_groc_1",
      storeName: "Al-Fatah Bahawalpur",
      ownerId: ADMIN_UID,
      categoryId: "cat_fruits",
      categoryName: "Fruits",
      name: "Multan Chaunsa Box",
      description: "Sweet seasonal mangoes",
      regularPricePkr: 2400,
      discountPricePkr: 2100,
      unit: "2.5 kg box",
      stockQuantity: 15,
      additionalImages: [],
      isAvailable: true,
      isFeatured: true,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Uninvolved user Bob cannot update or delete vendor's products
  await assertFails(
    bobDb.collection("foodItems").doc("food_1").update({
      name: "Hacked Dish",
      updatedAt: nowTs(),
    })
  );
  await assertFails(bobDb.collection("foodItems").doc("food_1").delete());
  await assertFails(bobDb.collection("groceryProducts").doc("groc_1").delete());

  // Admin can update imageUrl to null / edit & delete any product
  await assertSucceeds(
    adminDb.collection("foodItems").doc("food_1").update({
      imageUrl: null,
      preparationTimeMinutes: 20,
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(adminDb.collection("foodItems").doc("food_1").delete());
  await assertSucceeds(adminDb.collection("groceryProducts").doc("groc_1").delete());
});

test("Admin login credential key activates /admins/{uid} safely and grants admin catalog access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();

  // Wrong adminKey is rejected
  await assertFails(
    aliceDb.collection("admins").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@test.com",
      adminKey: "admin:wrongpass",
      createdAt: nowTs(),
    })
  );

  // Valid adminKey ("admin:admin123") activates /admins/{uid}
  await assertSucceeds(
    aliceDb.collection("admins").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@test.com",
      adminKey: "admin:admin123",
      createdAt: nowTs(),
    })
  );

  // Activated admin can list foodItems & create categories
  await assertSucceeds(aliceDb.collection("foodItems").get());
  await assertSucceeds(
    aliceDb.collection("foodCategories").doc("cat_admin_test").set({
      name: "Specials",
      iconKey: "pizza",
      isActive: true,
      sortOrder: 99,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("GharTak Wallet enforces non-zero start prohibition, single-step top-up caps, tx immutability, and Wallet orders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();

  // Owner cannot initialize profile with forged positive wallet balance
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@test.com",
      phone: "+923001234567",
      city: "Bahawalpur",
      activePortal: "customer",
      walletBalancePkr: 5000,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Owner initializes profile with 0 PKR wallet
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@test.com",
      phone: "+923001234567",
      city: "Bahawalpur",
      activePortal: "customer",
      walletBalancePkr: 0,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Single-step top-up > 50,000 PKR or negative balance fails for customer
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalancePkr: 80000,
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalancePkr: -100,
      updatedAt: nowTs(),
    })
  );

  // Valid top-up <= 50,000 PKR succeeds and ledger entry is immutable
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalancePkr: 2500,
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("walletTransactions").doc("wtx_1").set({
      userId: ALICE_UID,
      type: "topup",
      amountPkr: 2500,
      balanceAfterPkr: 2500,
      paymentChannel: "JazzCash",
      referenceId: "wtx_1",
      note: "Added via JazzCash",
      createdAt: nowTs(),
    })
  );

  // Non-admin cannot forge admin_credit or read/mutate someone else's wallet ledger
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("walletTransactions").doc("wtx_forge").set({
      userId: ALICE_UID,
      type: "admin_credit",
      amountPkr: 10000,
      balanceAfterPkr: 12500,
      paymentChannel: "Admin Credit",
      referenceId: "wtx_forge",
      note: "Forged promo",
      createdAt: nowTs(),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("walletTransactions").get());
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("walletTransactions").doc("wtx_1").update({
      amountPkr: 99999,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("walletTransactions").doc("wtx_1").delete()
  );

  // Admin can credit any customer's wallet beyond customer cap and write admin_credit log
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      walletBalancePkr: 102500,
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).collection("walletTransactions").doc("wtx_admin").set({
      userId: ALICE_UID,
      type: "admin_credit",
      amountPkr: 100000,
      balanceAfterPkr: 102500,
      paymentChannel: "Admin Credit",
      referenceId: "wtx_admin",
      note: "Promotional wallet bonus",
      createdAt: nowTs(),
    })
  );

  // Placing order with paymentMethod "Wallet" is permitted
  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_wallet_1").set({
      orderType: "food",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "+923001234567",
      vendorId: "rest_1",
      vendorName: "Royal Biryani House",
      vendorOwnerId: VENDOR_UID,
      riderId: "",
      riderName: "",
      deliveryAddress: "12-B Model Town A, Bahawalpur",
      city: "Bahawalpur",
      deliveryLat: 29.3956,
      deliveryLng: 71.6836,
      items: [{ productId: "item_1", name: "Chicken Biryani", quantity: 1, unitPricePkr: 450, unit: "plate" }],
      subtotalPkr: 450,
      deliveryFeePkr: 90,
      discountPkr: 0,
      couponCode: "",
      totalPkr: 540,
      commissionPkr: 67.5,
      vendorEarningsPkr: 382.5,
      riderEarningsPkr: 70,
      paymentMethod: "Wallet",
      paymentStatus: "paid",
      status: "Pending",
      cancellationReason: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("Cross-device product sync: foodItems and groceryProducts added by Admin are immediately readable by another customer", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();

  await assertSucceeds(
    adminDb.collection("restaurants").doc("rest_sync_1").set({
      ownerId: ADMIN_UID,
      name: "Admin Lahore Karahi",
      ownerName: "Admin",
      phone: "+923001234567",
      email: "admin@ghartak.pk",
      address: "Circular Road",
      city: "Bahawalpur",
      deliveryTimeMinutes: 30,
      deliveryFeePkr: 80,
      minOrderPkr: 200,
      rating: 4.9,
      reviewCount: 1,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 15,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    adminDb.collection("groceryStores").doc("store_sync_1").set({
      ownerId: ADMIN_UID,
      name: "BWP Super Mart",
      ownerName: "Admin",
      phone: "+923001234567",
      email: "admin@ghartak.pk",
      address: "Model Town A",
      city: "Bahawalpur",
      deliveryTimeMinutes: 25,
      deliveryFeePkr: 60,
      minOrderPkr: 200,
      rating: 4.9,
      reviewCount: 1,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 12,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("foodItems").doc("food_sync_1").set({
      restaurantId: "rest_sync_1",
      restaurantName: "Admin Lahore Karahi",
      ownerId: ADMIN_UID,
      categoryId: "cat_biryani",
      categoryName: "Biryani",
      name: "Special Sindhi Biryani",
      description: "Live synced across devices",
      regularPricePkr: 520,
      discountPricePkr: 460,
      imageUrl: "res://img_food_biryani",
      additionalImages: [],
      preparationTimeMinutes: 20,
      stockQuantity: 25,
      isAvailable: true,
      isFeatured: false,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("groceryProducts").doc("groc_sync_1").set({
      storeId: "store_sync_1",
      storeName: "BWP Super Mart",
      ownerId: ADMIN_UID,
      categoryId: "cat_fruits",
      categoryName: "Fruits",
      name: "Fresh Kinnow",
      description: "Live synced grocery product",
      regularPricePkr: 350,
      discountPricePkr: 299,
      unit: "1 kg",
      stockQuantity: 40,
      imageUrl: "res://img_grocery_fruits",
      additionalImages: [],
      isAvailable: true,
      isFeatured: false,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Customer Alice on another device queries available products and sees both items
  const foodSnap = await assertSucceeds(
    aliceDb.collection("foodItems").where("isAvailable", "==", true).get()
  );
  const grocSnap = await assertSucceeds(
    aliceDb.collection("groceryProducts").where("isAvailable", "==", true).get()
  );
  if (!foodSnap.docs.some((d) => d.id === "food_sync_1")) {
    throw new Error("Expected food_sync_1 to be readable by second device");
  }
  if (!grocSnap.docs.some((d) => d.id === "groc_sync_1")) {
    throw new Error("Expected groc_sync_1 to be readable by second device");
  }
});

test("Support Chat enforces per-customer isolation, Helpline 03036767152, and admin-only replies", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();

  // Customer Alice starts a support thread linked to helpline 03036767152
  await assertSucceeds(
    aliceDb.collection("supportChats").doc(ALICE_UID).set({
      userId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "+923001234567",
      supportPhone: "03036767152",
      lastMessage: "Where is my order?",
      lastSenderRole: "customer",
      unreadByAdmin: 1,
      unreadByCustomer: 0,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("supportChats").doc(ALICE_UID).collection("messages").doc("msg_1").set({
      userId: ALICE_UID,
      senderId: ALICE_UID,
      senderName: "Alice Khan",
      senderRole: "customer",
      text: "Where is my order?",
      orderId: "ord_123",
      supportPhone: "03036767152",
      createdAt: nowTs(),
    })
  );

  // Customer cannot spoof an admin reply message
  await assertFails(
    aliceDb.collection("supportChats").doc(ALICE_UID).collection("messages").doc("msg_spoof").set({
      userId: ALICE_UID,
      senderId: ALICE_UID,
      senderName: "Fake Admin",
      senderRole: "admin",
      text: "Free refund approved",
      orderId: "",
      supportPhone: "03036767152",
      createdAt: nowTs(),
    })
  );

  // Bob cannot read or write Alice's support thread or messages
  await assertFails(bobDb.collection("supportChats").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("supportChats").doc(ALICE_UID).collection("messages").get());
  await assertFails(
    bobDb.collection("supportChats").doc(ALICE_UID).collection("messages").doc("msg_bob").set({
      userId: ALICE_UID,
      senderId: BOB_UID,
      senderName: "Bob",
      senderRole: "customer",
      text: "Intruder message",
      orderId: "",
      supportPhone: "03036767152",
      createdAt: nowTs(),
    })
  );

  // Verified admin can list all support threads and reply to Alice
  await assertSucceeds(adminDb.collection("supportChats").get());
  await assertSucceeds(
    adminDb.collection("supportChats").doc(ALICE_UID).collection("messages").doc("msg_admin_1").set({
      userId: ALICE_UID,
      senderId: ADMIN_UID,
      senderName: "GharTak Support (03036767152)",
      senderRole: "admin",
      text: "Your rider is 5 minutes away! Call 03036767152 if urgent.",
      orderId: "ord_123",
      supportPhone: "03036767152",
      createdAt: nowTs(),
    })
  );

  // Customer Alice sends photo & voice note message, Admin replies with photo
  await assertSucceeds(
    aliceDb.collection("supportChats").doc(ALICE_UID).collection("messages").doc("msg_media_1").set({
      userId: ALICE_UID,
      senderId: ALICE_UID,
      senderName: "Alice Khan",
      senderRole: "customer",
      text: "Photo & Voice Note",
      orderId: "ord_123",
      supportPhone: "03036767152",
      imageUrl: "data:image/jpeg;base64,/9j/4AAQ",
      audioBase64: "UklGRgAAAABXQVZFZm10",
      audioDurationSec: 4,
      createdAt: nowTs(),
    })
  );
  await assertSucceeds(
    adminDb.collection("supportChats").doc(ALICE_UID).collection("messages").doc("msg_admin_photo").set({
      userId: ALICE_UID,
      senderId: ADMIN_UID,
      senderName: "GharTak Support (03036767152)",
      senderRole: "admin",
      text: "Photo",
      orderId: "ord_123",
      supportPhone: "03036767152",
      imageUrl: "data:image/jpeg;base64,/9j/4AAQ",
      createdAt: nowTs(),
    })
  );
});

test("Medical vertical supports medicalCategories, medicalStores, medicalProducts, favorites, orders, and reviews", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();

  await assertFails(unauthDb.collection("medicalProducts").where("isAvailable", "==", true).get());

  await assertSucceeds(
    adminDb.collection("medicalCategories").doc("mcat_meds").set({
      name: "Medicines",
      iconKey: "medical",
      isActive: true,
      sortOrder: 1,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("medicalStores").doc("med_store_1").set({
      ownerId: ADMIN_UID,
      name: "Al-Shifa Pharmacy BWP",
      ownerName: "Dr. Tariq",
      phone: "+923009876543",
      email: "admin@ghartak.pk",
      address: "Victoria Hospital Road",
      city: "Bahawalpur",
      deliveryTimeMinutes: 20,
      deliveryFeePkr: 60,
      minOrderPkr: 150,
      rating: 4.9,
      reviewCount: 15,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 10,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("medicalProducts").doc("med_prod_1").set({
      storeId: "med_store_1",
      storeName: "Al-Shifa Pharmacy BWP",
      ownerId: ADMIN_UID,
      categoryId: "mcat_meds",
      categoryName: "Medicines",
      name: "Panadol Extra Strip",
      description: "Fast fever and headache relief",
      regularPricePkr: 75,
      discountPricePkr: 68,
      unit: "1 strip (10 tabs)",
      stockQuantity: 100,
      imageUrl: "res://img_med_medicines",
      additionalImages: [],
      isAvailable: true,
      isFeatured: true,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Customer Alice can read, favorite, order ("medical"), and review ("medicalStore")
  const medSnap = await assertSucceeds(
    aliceDb.collection("medicalProducts").where("isAvailable", "==", true).get()
  );
  if (!medSnap.docs.some((d) => d.id === "med_prod_1")) {
    throw new Error("Expected med_prod_1 to be readable by Alice");
  }

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("medicalProduct_med_prod_1").set({
      userId: ALICE_UID,
      targetId: "med_prod_1",
      targetType: "medicalProduct",
      title: "Panadol Extra Strip",
      subtitle: "Rs. 68",
      createdAt: nowTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_med_1").set({
      orderType: "medical",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "+923001234567",
      vendorId: "med_store_1",
      vendorName: "Al-Shifa Pharmacy BWP",
      vendorOwnerId: ADMIN_UID,
      riderId: "",
      riderName: "",
      deliveryAddress: "12-B Model Town A, Bahawalpur",
      city: "Bahawalpur",
      deliveryLat: 29.3956,
      deliveryLng: 71.6836,
      items: [{ productId: "med_prod_1", name: "Panadol Extra Strip", quantity: 2, unitPricePkr: 68, unit: "1 strip" }],
      subtotalPkr: 136,
      deliveryFeePkr: 60,
      discountPkr: 0,
      couponCode: "",
      totalPkr: 196,
      commissionPkr: 13.6,
      vendorEarningsPkr: 122.4,
      riderEarningsPkr: 50,
      paymentMethod: "COD",
      paymentStatus: "pending",
      status: "Pending",
      cancellationReason: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_med_1").set({
      userId: ALICE_UID,
      userName: "Alice Khan",
      orderId: "ord_med_1",
      targetId: "med_store_1",
      targetType: "medicalStore",
      targetName: "Al-Shifa Pharmacy BWP",
      rating: 5,
      comment: "Genuine medicines delivered fast.",
      isHidden: false,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("Restaurant Send Parcel & Hire a Rider requests require Super Admin approval before Rider claim", async () => {
  const vendorDb = testEnv.authenticatedContext(VENDOR_UID, { email: "vendor@test.com", email_verified: true }).firestore();
  const riderDb = testEnv.authenticatedContext(RIDER_UID, { email: "rider@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("restaurants").doc("rest_prq_1").set({
      ownerId: VENDOR_UID,
      name: "Bahawalpur Grill",
      ownerName: "Usman",
      phone: "+923001112233",
      email: "vendor@test.com",
      address: "Circular Road",
      city: "Bahawalpur",
      deliveryTimeMinutes: 30,
      deliveryFeePkr: 90,
      minOrderPkr: 300,
      rating: 4.8,
      reviewCount: 10,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 15,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    riderDb.collection("riders").doc(RIDER_UID).set({
      userId: RIDER_UID,
      fullName: "Bilal Ahmed",
      phone: "+923214445566",
      email: "rider@test.com",
      cnicNumber: "31202-1234567-1",
      licenseNumber: "BWP-9921",
      vehicleType: "Motorbike",
      vehicleNumber: "BWP-4421",
      city: "Bahawalpur",
      approvalStatus: "pending",
      isOnline: true,
      latitude: 29.3956,
      longitude: 71.6836,
      totalEarningsPkr: 0,
      completedDeliveries: 0,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Restaurant cannot self-approve a parcel request on creation
  await assertFails(
    vendorDb.collection("parcelRiderRequests").doc("prq_bad_1").set({
      requestType: "send_parcel",
      requesterId: VENDOR_UID,
      restaurantId: "rest_prq_1",
      restaurantName: "Bahawalpur Grill",
      pickupAddress: "Circular Road, BWP",
      dropoffAddress: "Model Town A, BWP",
      city: "Bahawalpur",
      recipientName: "Kamran",
      recipientPhone: "+923001119999",
      details: "Food parcel",
      offeredFeePkr: 200,
      approvalStatus: "approved",
      riderId: "",
      riderName: "",
      adminNote: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Restaurant creates pending Send Parcel & Hire a Rider requests
  await assertSucceeds(
    vendorDb.collection("parcelRiderRequests").doc("prq_1").set({
      requestType: "send_parcel",
      requesterId: VENDOR_UID,
      restaurantId: "rest_prq_1",
      restaurantName: "Bahawalpur Grill",
      pickupAddress: "Circular Road, BWP",
      dropoffAddress: "Model Town A, BWP",
      city: "Bahawalpur",
      recipientName: "Kamran",
      recipientPhone: "+923001119999",
      details: "Urgent catering box",
      offeredFeePkr: 250,
      approvalStatus: "pending",
      riderId: "",
      riderName: "",
      adminNote: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    vendorDb.collection("parcelRiderRequests").doc("prq_hire_1").set({
      requestType: "hire_rider",
      requesterId: VENDOR_UID,
      restaurantId: "rest_prq_1",
      restaurantName: "Bahawalpur Grill",
      pickupAddress: "Circular Road, BWP",
      dropoffAddress: "",
      city: "Bahawalpur",
      recipientName: "",
      recipientPhone: "+92",
      details: "Need 1 rider for 4 hours dinner shift",
      offeredFeePkr: 600,
      approvalStatus: "pending",
      riderId: "",
      riderName: "",
      adminNote: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Restaurant owner cannot approve their own request; uninvolved Bob cannot approve
  await assertFails(
    vendorDb.collection("parcelRiderRequests").doc("prq_1").update({
      approvalStatus: "approved",
      adminNote: "Self approved",
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    bobDb.collection("parcelRiderRequests").doc("prq_1").update({
      approvalStatus: "approved",
      updatedAt: nowTs(),
    })
  );

  // Admin approves prq_1
  await assertSucceeds(
    adminDb.collection("parcelRiderRequests").doc("prq_1").update({
      approvalStatus: "approved",
      adminNote: "Verified dispatch",
      updatedAt: nowTs(),
    })
  );

  // Unapproved rider cannot claim approved job
  await assertFails(
    riderDb.collection("parcelRiderRequests").doc("prq_1").update({
      riderId: RIDER_UID,
      riderName: "Bilal Ahmed",
      approvalStatus: "rider_assigned",
      updatedAt: nowTs(),
    })
  );

  // Admin approves rider, then rider claims & completes job
  await assertSucceeds(
    adminDb.collection("riders").doc(RIDER_UID).update({
      approvalStatus: "approved",
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    riderDb.collection("parcelRiderRequests").doc("prq_1").update({
      riderId: RIDER_UID,
      riderName: "Bilal Ahmed",
      approvalStatus: "rider_assigned",
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    riderDb.collection("parcelRiderRequests").doc("prq_1").update({
      approvalStatus: "picked_up",
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    riderDb.collection("parcelRiderRequests").doc("prq_1").update({
      approvalStatus: "completed",
      updatedAt: nowTs(),
    })
  );

  // Terminal state lock: rider cannot alter completed job
  await assertFails(
    riderDb.collection("parcelRiderRequests").doc("prq_1").update({
      approvalStatus: "picked_up",
      updatedAt: nowTs(),
    })
  );
});

test("Super Admin can change login ID & password via platformConfig/adminAuth and non-admin cannot read or spoof it", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();

  const newKeyHash = sha256Hex("gtadmin:SuperSecret99");

  // Non-admin cannot create or read platformConfig/adminAuth
  await assertFails(
    aliceDb.collection("platformConfig").doc("adminAuth").set({
      loginEmail: "alice",
      adminKeyHash: sha256Hex("alice:hacked"),
      updatedAt: nowTs(),
    })
  );

  // Admin sets new admin login & password hash
  await assertSucceeds(
    adminDb.collection("platformConfig").doc("adminAuth").set({
      loginEmail: "gtadmin",
      adminKeyHash: newKeyHash,
      updatedAt: nowTs(),
    })
  );

  // Non-admin still cannot read platformConfig/adminAuth
  await assertFails(aliceDb.collection("platformConfig").doc("adminAuth").get());

  // Old default adminKey "admin:admin123" is now rejected
  await assertFails(
    aliceDb.collection("admins").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@test.com",
      adminKey: "admin:admin123",
      createdAt: nowTs(),
    })
  );

  // New adminKey "gtadmin:SuperSecret99" is accepted for second admin account
  await assertSucceeds(
    bobDb.collection("admins").doc(BOB_UID).set({
      userId: BOB_UID,
      email: "bob@test.com",
      adminKey: "gtadmin:SuperSecret99",
      createdAt: nowTs(),
    })
  );
});

test("Per-restaurant portal credentials isolate Admin directory and grant multi-device scoped restaurant access", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();
  const staffDb = testEnv.authenticatedContext(ALICE_UID, { email: "staff@test.com", email_verified: true }).firestore();
  const outsiderDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();

  const loginId = "royal_bwp";
  const pass = "Royal@2026";
  const passHash = sha256Hex(`${loginId}:${pass}`);

  // Admin creates two restaurants, one with credentials
  await assertSucceeds(
    adminDb.collection("restaurants").doc("rest_1").set({
      ownerId: ADMIN_UID,
      name: "Royal Biryani",
      ownerName: "GT Service Partner",
      phone: "+923001112201",
      email: "admin@gtservice.pk",
      address: "Circular Road, Bahawalpur",
      city: "Bahawalpur",
      deliveryTimeMinutes: 25,
      deliveryFeePkr: 70,
      minOrderPkr: 300,
      rating: 4.8,
      reviewCount: 10,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 15,
      portalLoginId: loginId,
      portalPassHash: passHash,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("restaurants").doc("rest_2").set({
      ownerId: ADMIN_UID,
      name: "Baghdad Grill",
      ownerName: "GT Service Partner",
      phone: "+923001112202",
      email: "admin@gtservice.pk",
      address: "Model Town A, Bahawalpur",
      city: "Bahawalpur",
      deliveryTimeMinutes: 30,
      deliveryFeePkr: 80,
      minOrderPkr: 300,
      rating: 4.7,
      reviewCount: 8,
      approvalStatus: "approved",
      isOpen: true,
      commissionPercent: 15,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Admin stores plaintext directory in /adminRestaurantCredentials (Admin-only!)
  await assertSucceeds(
    adminDb.collection("adminRestaurantCredentials").doc("rest_1").set({
      restaurantId: "rest_1",
      restaurantName: "Royal Biryani",
      loginId,
      password: pass,
      updatedAt: nowTs(),
    })
  );
  await assertFails(staffDb.collection("adminRestaurantCredentials").doc("rest_1").get());
  await assertFails(outsiderDb.collection("adminRestaurantCredentials").get());

  // Seed order for rest_1 and rest_2
  await assertSucceeds(
    outsiderDb.collection("orders").doc("ord_rest_1").set({
      orderType: "food",
      customerId: BOB_UID,
      customerName: "Bob",
      customerPhone: "+923001234567",
      vendorId: "rest_1",
      vendorName: "Royal Biryani",
      vendorOwnerId: ADMIN_UID,
      riderId: "",
      riderName: "",
      deliveryAddress: "Model Town A",
      city: "Bahawalpur",
      deliveryLat: 29.39,
      deliveryLng: 71.68,
      items: [{ productId: "f1", name: "Chicken Biryani", quantity: 1, unitPricePkr: 450, unit: "plate" }],
      subtotalPkr: 450,
      deliveryFeePkr: 70,
      discountPkr: 0,
      couponCode: "",
      totalPkr: 520,
      commissionPkr: 67.5,
      vendorEarningsPkr: 382.5,
      riderEarningsPkr: 70,
      paymentMethod: "COD",
      paymentStatus: "pending",
      status: "Pending",
      cancellationReason: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Wrong restaurant password is rejected
  await assertFails(
    staffDb.collection("restaurantAccess").doc(`rest_1_${ALICE_UID}`).set({
      userId: ALICE_UID,
      restaurantId: "rest_1",
      loginId,
      loginKey: `${loginId}:WrongPass`,
      createdAt: nowTs(),
    })
  );

  // Cannot spoof staffSessions without verified restaurantAccess
  await assertFails(
    staffDb.collection("staffSessions").doc(ALICE_UID).set({
      userId: ALICE_UID,
      lastRestaurantId: "rest_1",
      restaurantIds: ["rest_1"],
      updatedAt: nowTs(),
    })
  );

  // Valid password creates restaurantAccess and then staffSessions
  await assertSucceeds(
    staffDb.collection("restaurantAccess").doc(`rest_1_${ALICE_UID}`).set({
      userId: ALICE_UID,
      restaurantId: "rest_1",
      loginId,
      loginKey: `${loginId}:${pass}`,
      createdAt: nowTs(),
    })
  );

  await assertSucceeds(
    staffDb.collection("staffSessions").doc(ALICE_UID).set({
      userId: ALICE_UID,
      lastRestaurantId: "rest_1",
      restaurantIds: ["rest_1"],
      updatedAt: nowTs(),
    })
  );

  // Staff member cannot inject unverified rest_2 into staffSessions
  await assertFails(
    staffDb.collection("staffSessions").doc(ALICE_UID).update({
      lastRestaurantId: "rest_2",
      restaurantIds: ["rest_1", "rest_2"],
      updatedAt: nowTs(),
    })
  );

  // Staff member can list and accept orders for rest_1!
  const listRes = await assertSucceeds(
    staffDb.collection("orders").where("vendorId", "==", "rest_1").get()
  );
  if (listRes.docs.length !== 1) {
    throw new Error("Expected 1 order for rest_1");
  }
  await assertSucceeds(
    staffDb.collection("orders").doc("ord_rest_1").update({
      status: "Accepted",
      cancellationReason: "",
      updatedAt: nowTs(),
    })
  );

  // Staff member logged into rest_1 CANNOT create new food items and CANNOT overwrite portalPassHash
  await assertFails(
    staffDb.collection("restaurants").doc("rest_1").update({
      portalPassHash: sha256Hex("hacked:1234"),
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    staffDb.collection("foodItems").doc("food_staff_forbidden").set({
      restaurantId: "rest_1",
      restaurantName: "Royal Biryani",
      ownerId: ALICE_UID,
      categoryId: "fcat_3",
      categoryName: "Biryani",
      name: "Unauthorized Dish",
      description: "Test",
      regularPricePkr: 300,
      discountPricePkr: 0,
      stockQuantity: 10,
      isAvailable: true,
      isFeatured: false,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Staff member logged into rest_1 CAN submit a pending Send Parcel request
  await assertSucceeds(
    staffDb.collection("parcelRiderRequests").doc("prq_staff_1").set({
      requestType: "send_parcel",
      requesterId: ALICE_UID,
      restaurantId: "rest_1",
      restaurantName: "Royal Biryani",
      pickupAddress: "Circular Road, Bahawalpur",
      dropoffAddress: "Satellite Town, Bahawalpur",
      city: "Bahawalpur",
      recipientName: "Bilal",
      recipientPhone: "+923009998877",
      details: "Urgent parcel",
      offeredFeePkr: 150,
      approvalStatus: "pending",
      riderId: "",
      riderName: "",
      adminNote: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("PlatformSettings supports JazzCash & Easypaisa numbers (03298580188) and custom beneficiaryAccounts", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();

  await assertSucceeds(
    adminDb.collection("platformSettings").doc("global").set({
      cities: ["Bahawalpur", "Lahore"],
      restaurantCommissionPercent: 15,
      groceryCommissionPercent: 10,
      baseDeliveryFeePkr: 80,
      riderPayoutPkr: 70,
      jazzCashConfigured: true,
      easypaisaConfigured: true,
      jazzCashNumber: "03298580188",
      jazzCashAccountTitle: "GT Service",
      easypaisaNumber: "03298580188",
      easypaisaAccountTitle: "GT Service",
      beneficiaryAccounts: [
        {
          id: "ben_1",
          channel: "Meezan Bank",
          accountTitle: "GT Service Official",
          accountNumber: "01020304050607",
        },
      ],
      updatedAt: nowTs(),
    })
  );

  const snap = await assertSucceeds(aliceDb.collection("platformSettings").doc("global").get());
  if (snap.data().jazzCashNumber !== "03298580188" || snap.data().beneficiaryAccounts.length !== 1) {
    throw new Error("Expected saved payment numbers and beneficiaryAccounts");
  }
});

test("Multan Express (/multanItems) & Celebration (/celebrationItems) enforce admin-only writes, customer reads, favorites, and orders", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();

  // Regular customer cannot create Multan or Celebration items
  await assertFails(
    aliceDb.collection("multanItems").doc("multan_unauth").set({
      brandName: "Crumble",
      categoryName: "Cookies & Bakery",
      name: "Unauthorized Cookie Box",
      description: "Test",
      regularPricePkr: 1500,
      discountPricePkr: 0,
      unit: "1 box (6 pcs)",
      stockQuantity: 10,
      originCity: "Multan",
      isAvailable: true,
      isFeatured: false,
      city: "All Cities",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    aliceDb.collection("celebrationItems").doc("cel_unauth").set({
      categoryName: "Birthday Cakes",
      name: "Unauthorized Cake",
      description: "Test",
      regularPricePkr: 2200,
      discountPricePkr: 0,
      unit: "2 lbs",
      stockQuantity: 10,
      isAvailable: true,
      isFeatured: false,
      city: "All Cities",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Admin creates Multan & Celebration catalog items
  await assertSucceeds(
    adminDb.collection("multanItems").doc("multan_1").set({
      brandName: "Crumble",
      categoryName: "Cookies & Bakery",
      name: "Crumble Assorted Box",
      description: "Fresh baked cookies from Multan",
      regularPricePkr: 2400,
      discountPricePkr: 2150,
      unit: "1 box (6 pcs)",
      stockQuantity: 25,
      imageUrl: "res://img_food_desserts",
      additionalImages: [],
      originCity: "Multan",
      isAvailable: true,
      isFeatured: true,
      city: "All Cities",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("celebrationItems").doc("cel_1").set({
      categoryName: "Birthday Cakes",
      name: "Belgian Fudge Celebration Cake",
      description: "2 lbs custom message cake",
      regularPricePkr: 3200,
      discountPricePkr: 2890,
      unit: "2 lbs",
      stockQuantity: 15,
      imageUrl: "res://img_food_desserts",
      additionalImages: [],
      isAvailable: true,
      isFeatured: true,
      city: "All Cities",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Customer Alice can list available Multan & Celebration items
  const multanSnap = await assertSucceeds(
    aliceDb.collection("multanItems").where("isAvailable", "==", true).get()
  );
  const celSnap = await assertSucceeds(
    aliceDb.collection("celebrationItems").where("isAvailable", "==", true).get()
  );
  if (!multanSnap.docs.some((d) => d.id === "multan_1") || !celSnap.docs.some((d) => d.id === "cel_1")) {
    throw new Error("Expected Multan & Celebration items to be readable by customers");
  }

  // Customer can favorite both special item types and place "multan" & "celebration" orders
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("multanItem_multan_1").set({
      userId: ALICE_UID,
      targetId: "multan_1",
      targetType: "multanItem",
      title: "Crumble Assorted Box",
      subtitle: "PKR 2,150",
      createdAt: nowTs(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("celebrationItem_cel_1").set({
      userId: ALICE_UID,
      targetId: "cel_1",
      targetType: "celebrationItem",
      title: "Belgian Fudge Celebration Cake",
      subtitle: "PKR 2,890",
      createdAt: nowTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_multan_1").set({
      orderType: "multan",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "+923001234567",
      vendorId: "multan_express",
      vendorName: "Anything You Want from Multan",
      vendorOwnerId: ADMIN_UID,
      riderId: "",
      riderName: "",
      deliveryAddress: "Satellite Town, Bahawalpur",
      city: "Bahawalpur",
      deliveryLat: 29.3956,
      deliveryLng: 71.6836,
      items: [{ productId: "multan_1", name: "Crumble Assorted Box", quantity: 1, unitPricePkr: 2150, unit: "1 box (6 pcs)" }],
      subtotalPkr: 2150,
      deliveryFeePkr: 350,
      discountPkr: 0,
      couponCode: "",
      totalPkr: 2500,
      commissionPkr: 0,
      vendorEarningsPkr: 2150,
      riderEarningsPkr: 200,
      paymentMethod: "COD",
      paymentStatus: "pending",
      status: "Pending",
      cancellationReason: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_cel_1").set({
      orderType: "celebration",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "+923001234567",
      vendorId: "gt_celebration",
      vendorName: "GT Celebration Store",
      vendorOwnerId: ADMIN_UID,
      riderId: "",
      riderName: "",
      deliveryAddress: "Model Town A, Bahawalpur",
      city: "Bahawalpur",
      deliveryLat: 29.3956,
      deliveryLng: 71.6836,
      items: [{ productId: "cel_1", name: "Belgian Fudge Celebration Cake [Msg: Happy Birthday]", quantity: 1, unitPricePkr: 2890, unit: "2 lbs" }],
      subtotalPkr: 2890,
      deliveryFeePkr: 120,
      discountPkr: 0,
      couponCode: "",
      totalPkr: 3010,
      commissionPkr: 0,
      vendorEarningsPkr: 2890,
      riderEarningsPkr: 100,
      paymentMethod: "COD",
      paymentStatus: "pending",
      status: "Pending",
      cancellationReason: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("Vendor Operating Hours, Day-wise Food Menu, and Area & Multan Delivery Fees enforce validation and RBAC", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hanifkhan23846@gmail.com",
    email_verified: true,
  }).firestore();
  const vendorDb = testEnv.authenticatedContext(VENDOR_UID, { email: "vendor@test.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@test.com", email_verified: true }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();

  // Seed Restaurant, Grocery Store & Medical Store owned by VENDOR_UID
  await assertSucceeds(
    adminDb.collection("restaurants").doc("rest_hrs_1").set({
      ownerId: VENDOR_UID,
      name: "Timings Kitchen",
      ownerName: "Usman",
      phone: "+923001112233",
      email: "vendor@test.com",
      address: "Model Town A",
      city: "Bahawalpur",
      deliveryTimeMinutes: 30,
      deliveryFeePkr: 90,
      minOrderPkr: 300,
      rating: 4.8,
      reviewCount: 5,
      approvalStatus: "approved",
      isOpen: true,
      openingTime: "09:00",
      closingTime: "23:00",
      commissionPercent: 15,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    adminDb.collection("groceryStores").doc("store_hrs_1").set({
      ownerId: VENDOR_UID,
      name: "Timings Mart",
      ownerName: "Usman",
      phone: "+923001112233",
      email: "vendor@test.com",
      address: "Satellite Town",
      city: "Bahawalpur",
      deliveryTimeMinutes: 25,
      deliveryFeePkr: 70,
      minOrderPkr: 250,
      rating: 4.8,
      reviewCount: 4,
      approvalStatus: "approved",
      isOpen: true,
      openingTime: "08:00",
      closingTime: "22:00",
      commissionPercent: 10,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    adminDb.collection("medicalStores").doc("med_hrs_1").set({
      ownerId: VENDOR_UID,
      name: "24/7 Care Pharmacy",
      ownerName: "Dr. Tariq",
      phone: "+923009998811",
      email: "vendor@test.com",
      address: "Circular Road",
      city: "Bahawalpur",
      deliveryTimeMinutes: 20,
      deliveryFeePkr: 60,
      minOrderPkr: 150,
      rating: 4.9,
      reviewCount: 12,
      approvalStatus: "approved",
      isOpen: true,
      openingTime: "00:00",
      closingTime: "23:59",
      commissionPercent: 10,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Owners can toggle open/close & update openingTime/closingTime; Bob cannot
  await assertSucceeds(
    vendorDb.collection("restaurants").doc("rest_hrs_1").update({
      isOpen: false,
      openingTime: "12:00",
      closingTime: "02:00",
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    vendorDb.collection("groceryStores").doc("store_hrs_1").update({
      isOpen: false,
      openingTime: "09:00",
      closingTime: "21:00",
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    vendorDb.collection("medicalStores").doc("med_hrs_1").update({
      isOpen: true,
      openingTime: "07:00",
      closingTime: "23:30",
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    bobDb.collection("restaurants").doc("rest_hrs_1").update({
      isOpen: true,
      updatedAt: nowTs(),
    })
  );
  // OpeningTime > 5 chars fails schema check
  await assertFails(
    vendorDb.collection("restaurants").doc("rest_hrs_1").update({
      openingTime: "12:00 PM",
      updatedAt: nowTs(),
    })
  );

  // Day-wise menu fields (availableDays & isDailySpecial)
  await assertSucceeds(
    adminDb.collection("foodItems").doc("food_day_1").set({
      restaurantId: "rest_hrs_1",
      restaurantName: "Timings Kitchen",
      ownerId: VENDOR_UID,
      categoryId: "fcat_3",
      categoryName: "Biryani",
      name: "Friday Special Dum Biryani",
      description: "Served Mon, Wed, Fri",
      regularPricePkr: 550,
      discountPricePkr: 480,
      stockQuantity: 25,
      isAvailable: true,
      isFeatured: true,
      availableDays: ["Mon", "Wed", "Fri"],
      isDailySpecial: true,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    vendorDb.collection("foodItems").doc("food_day_1").update({
      availableDays: ["Fri", "Sat"],
      isDailySpecial: false,
      updatedAt: nowTs(),
    })
  );
  // > 7 entries in availableDays fails validation
  await assertFails(
    adminDb.collection("foodItems").doc("food_day_bad").set({
      restaurantId: "rest_hrs_1",
      restaurantName: "Timings Kitchen",
      ownerId: VENDOR_UID,
      categoryId: "fcat_3",
      categoryName: "Biryani",
      name: "Invalid 8-day dish",
      description: "Test",
      regularPricePkr: 400,
      discountPricePkr: 0,
      stockQuantity: 10,
      isAvailable: true,
      isFeatured: false,
      availableDays: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "Extra"],
      isDailySpecial: false,
      city: "Bahawalpur",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Area delivery rules, 5 km city radius settings & Multan per-city delivery fees in PlatformSettings
  await assertSucceeds(
    adminDb.collection("platformSettings").doc("global").set({
      cities: ["Bahawalpur", "Lahore", "Multan"],
      restaurantCommissionPercent: 15,
      groceryCommissionPercent: 10,
      baseDeliveryFeePkr: 80,
      riderPayoutPkr: 70,
      jazzCashConfigured: true,
      easypaisaConfigured: true,
      deliveryAreaFeeRules: [
        { id: "area_1", city: "Bahawalpur", areaName: "Model Town A", feePkr: 100, maxDistanceKm: 3 },
        { id: "area_2", city: "Bahawalpur", areaName: "Ibrahim City", feePkr: 200, maxDistanceKm: 10 },
      ],
      multanCityDeliveryFees: [
        { city: "Bahawalpur", feePkr: 350 },
        { city: "Lahore", feePkr: 600 },
      ],
      multanDefaultDeliveryFeePkr: 400,
      radiusPricingEnabled: true,
      deliveryBaseRadiusKm: 5,
      withinRadiusFeePkr: 100,
      extraPerKmFeePkr: 25,
      updatedAt: nowTs(),
    })
  );

  const settingsSnap = await assertSucceeds(aliceDb.collection("platformSettings").doc("global").get());
  if (
    settingsSnap.data().deliveryAreaFeeRules.length !== 2 ||
    settingsSnap.data().multanCityDeliveryFees.length !== 2 ||
    settingsSnap.data().multanDefaultDeliveryFeePkr !== 400 ||
    settingsSnap.data().deliveryBaseRadiusKm !== 5 ||
    settingsSnap.data().withinRadiusFeePkr !== 100 ||
    settingsSnap.data().extraPerKmFeePkr !== 25
  ) {
    throw new Error("Expected area, 5 km radius, and Multan delivery fee rules to persist");
  }
  await assertFails(
    aliceDb.collection("platformSettings").doc("global").update({
      multanDefaultDeliveryFeePkr: 0,
      updatedAt: nowTs(),
    })
  );
  // Negative or invalid radius fee rejected by schema rules
  await assertFails(
    adminDb.collection("platformSettings").doc("global").update({
      deliveryBaseRadiusKm: -2,
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    adminDb.collection("platformSettings").doc("global").update({
      withinRadiusFeePkr: -50,
      updatedAt: nowTs(),
    })
  );

  // Customer places a custom "Anything from Multan" order
  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_multan_custom_1").set({
      orderType: "multan",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "+923001234567",
      vendorId: "gt_multan_hub",
      vendorName: "GT Service • Multan Express",
      vendorOwnerId: "platform_admin",
      riderId: "",
      riderName: "",
      deliveryAddress: "Satellite Town, Bahawalpur",
      city: "Bahawalpur",
      deliveryLat: 29.3956,
      deliveryLng: 71.6836,
      items: [{ productId: "custom_multan_1", name: "Lotus Cookie Box (Crumble) (Fresh batch)", quantity: 1, unitPricePkr: 1850, unit: "1 box" }],
      subtotalPkr: 1850,
      deliveryFeePkr: 350,
      discountPkr: 0,
      couponCode: "",
      totalPkr: 2200,
      commissionPkr: 0,
      vendorEarningsPkr: 1850,
      riderEarningsPkr: 70,
      paymentMethod: "COD",
      paymentStatus: "pending",
      status: "Pending",
      cancellationReason: "",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});


