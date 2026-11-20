package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.auth.authStateFlow
import com.example.auth.onGoogleSignInClicked
import com.example.auth.signOutUser
import com.example.data.local.PreferencesRepository
import com.example.data.model.AppPortal
import com.example.data.model.GharTakConstants
import com.example.data.remote.GharTakRepository
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartAndCheckoutScreen
import com.example.ui.screens.CelebrationHubScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.CustomerOrdersScreen
import com.example.ui.screens.FoodHubScreen
import com.example.ui.screens.GroceryHubScreen
import com.example.ui.screens.GroceryPortalScreen
import com.example.ui.screens.GroceryStoreDetailScreen
import com.example.ui.screens.MedicalHubScreen
import com.example.ui.screens.MedicalStoreDetailScreen
import com.example.ui.screens.MultanHubScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileAndPortalsScreen
import com.example.ui.screens.RestaurantDetailScreen
import com.example.ui.screens.RestaurantPortalScreen
import com.example.ui.screens.RiderPortalScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SupportChatScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.GharTakTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.GharTakViewModel
import com.example.ui.viewmodel.PartnerViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_HOME = "home"
const val ROUTE_FOOD = "food"
const val ROUTE_GROCERY = "grocery"
const val ROUTE_MEDICAL = "medical"
const val ROUTE_MULTAN = "multan"
const val ROUTE_CELEBRATION = "celebration"
const val ROUTE_CART = "cart"
const val ROUTE_ORDERS = "orders"
const val ROUTE_PROFILE = "profile"
const val ROUTE_SEARCH = "search"
const val ROUTE_NOTIFICATIONS = "notifications"
const val ROUTE_WALLET = "wallet"
const val ROUTE_CHAT = "chat?orderId={orderId}"
const val ROUTE_RESTAURANT = "restaurant/{restaurantId}"
const val ROUTE_GROCERY_STORE = "grocery_store/{storeId}"
const val ROUTE_MEDICAL_STORE = "medical_store/{storeId}"

fun chatRoute(orderId: String = ""): String =
    if (orderId.isBlank()) "chat" else "chat?orderId=$orderId"

private data class CustomerNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GharTakApp()
        }
    }
}

@Composable
fun GharTakApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val authState by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var showPreAuthAdminLogin by remember { mutableStateOf(false) }
    var pendingAdminCredentials by remember { mutableStateOf<Pair<String, String>?>(null) }
    var preAuthAdminLoading by remember { mutableStateOf(false) }
    var preAuthAdminError by remember { mutableStateOf<String?>(null) }

    val currentUser = authState
    if (currentUser == null) {
        GharTakTheme(darkTheme = isSystemInDarkTheme()) {
            if (showPreAuthAdminLogin) {
                AdminLoginScreen(
                    isLoading = preAuthAdminLoading,
                    errorMessage = preAuthAdminError,
                    needsGoogleConnectHint = true,
                    onLoginSubmit = { email, password ->
                        if (email.trim().length < 2 || password.length < 4) {
                            preAuthAdminError = "Enter your Admin Login ID and password (min 4 characters)."
                        } else {
                            pendingAdminCredentials = email to password
                            preAuthAdminLoading = true
                            preAuthAdminError = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = { preAuthAdminLoading = false },
                                onAuthError = { msg ->
                                    preAuthAdminLoading = false
                                    pendingAdminCredentials = null
                                    preAuthAdminError = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    preAuthAdminLoading = false
                                    pendingAdminCredentials = null
                                    preAuthAdminError = "Complete Google Sign-In to connect your Admin session."
                                }
                            )
                        }
                    },
                    onBack = {
                        showPreAuthAdminLogin = false
                        preAuthAdminError = null
                    }
                )
            } else {
                AuthScreen(
                    onAuthSuccess = {},
                    onOpenAdminLogin = {
                        preAuthAdminError = null
                        showPreAuthAdminLogin = true
                    }
                )
            }
        }
    } else {
        key(currentUser.uid) {
            GharTakAuthenticatedRoot(
                user = currentUser,
                pendingAdminCredentials = pendingAdminCredentials,
                onConsumePendingAdmin = {
                    pendingAdminCredentials = null
                    showPreAuthAdminLogin = false
                }
            )
        }
    }
}

@Composable
private fun GharTakAuthenticatedRoot(
    user: FirebaseUser,
    pendingAdminCredentials: Pair<String, String>? = null,
    onConsumePendingAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val isBootstrappedAdmin = remember(user.email, user.isEmailVerified) {
        user.email?.equals(GharTakConstants.BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) == true &&
            user.isEmailVerified
    }

    val customerVm: GharTakViewModel = viewModel(
        key = "customer_${user.uid}",
        factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                GharTakViewModel(
                    repository = GharTakRepository(appContext),
                    prefs = PreferencesRepository(appContext),
                    currentUserId = user.uid,
                    currentUserEmail = user.email.orEmpty(),
                    currentUserName = user.displayName?.takeIf { it.isNotBlank() } ?: "GT Service Customer",
                    currentUserPhoto = user.photoUrl?.toString(),
                    isBootstrappedAdmin = isBootstrappedAdmin
                )
            }
        }
    )

    val partnerVm: PartnerViewModel = viewModel(
        key = "partner_${user.uid}",
        factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                PartnerViewModel(
                    repository = GharTakRepository(appContext),
                    currentUserId = user.uid,
                    currentUserEmail = user.email.orEmpty(),
                    currentUserName = user.displayName?.takeIf { it.isNotBlank() } ?: "GT Service Partner"
                )
            }
        }
    )

    val darkMode by customerVm.darkMode.collectAsStateWithLifecycle()
    val userProfile by customerVm.userProfile.collectAsStateWithLifecycle()
    val showAdminLogin by customerVm.showAdminLogin.collectAsStateWithLifecycle()
    val adminSessionUnlocked by customerVm.adminSessionUnlocked.collectAsStateWithLifecycle()
    val adminLoginLoading by customerVm.adminLoginLoading.collectAsStateWithLifecycle()
    val adminLoginError by customerVm.adminLoginError.collectAsStateWithLifecycle()
    val activePortal = AppPortal.fromKey(userProfile?.activePortal ?: AppPortal.CUSTOMER.key)

    LaunchedEffect(pendingAdminCredentials) {
        pendingAdminCredentials?.let { (email, password) ->
            customerVm.loginAdmin(email, password)
            onConsumePendingAdmin()
        }
    }

    val handleSignOut: () -> Unit = {
        signOutUser(
            credentialManager = credentialManager,
            scope = scope
        )
    }

    GharTakTheme(darkTheme = darkMode) {
        if (showAdminLogin || (activePortal == AppPortal.ADMIN && !adminSessionUnlocked)) {
            AdminLoginScreen(
                isLoading = adminLoginLoading,
                errorMessage = adminLoginError,
                onLoginSubmit = { email, password ->
                    customerVm.loginAdmin(email, password)
                },
                onBack = { customerVm.dismissAdminLogin() }
            )
        } else {
            when (activePortal) {
                AppPortal.RESTAURANT -> {
                    RestaurantPortalScreen(
                        partnerVm = partnerVm,
                        onExitToCustomer = { customerVm.switchPortal(AppPortal.CUSTOMER) }
                    )
                }
                AppPortal.GROCERY -> {
                    GroceryPortalScreen(
                        partnerVm = partnerVm,
                        onExitToCustomer = { customerVm.switchPortal(AppPortal.CUSTOMER) }
                    )
                }
                AppPortal.RIDER -> {
                    RiderPortalScreen(
                        partnerVm = partnerVm,
                        onExitToCustomer = { customerVm.switchPortal(AppPortal.CUSTOMER) }
                    )
                }
                AppPortal.ADMIN -> {
                    val adminVm: AdminViewModel = viewModel(
                        key = "admin_${user.uid}",
                        factory = viewModelFactory {
                            initializer {
                                val appContext = context.applicationContext
                                val db = FirebaseFirestore.getInstance(
                                    appContext.getString(R.string.firestore_database_id)
                                )
                                AdminViewModel(
                                    db = db,
                                    repository = GharTakRepository(db),
                                    adminId = user.uid,
                                    adminEmail = user.email.orEmpty()
                                )
                            }
                        }
                    )
                    AdminPanelScreen(
                        adminVm = adminVm,
                        onExitToCustomer = { customerVm.exitAdminPortal() },
                        onSignOut = handleSignOut
                    )
                }
                AppPortal.CUSTOMER -> {
                    CustomerNavigationShell(
                        viewModel = customerVm,
                        onSignOut = handleSignOut
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerNavigationShell(
    viewModel: GharTakViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME

    val foodCart by viewModel.foodCart.collectAsStateWithLifecycle()
    val groceryCart by viewModel.groceryCart.collectAsStateWithLifecycle()
    val medicalCart by viewModel.medicalCart.collectAsStateWithLifecycle()
    val multanCart by viewModel.multanCart.collectAsStateWithLifecycle()
    val celebrationCart by viewModel.celebrationCart.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val totalCartCount = remember(foodCart, groceryCart, medicalCart, multanCart, celebrationCart) {
        foodCart.sumOf { it.quantity } +
            groceryCart.sumOf { it.quantity } +
            medicalCart.sumOf { it.quantity } +
            multanCart.sumOf { it.quantity } +
            celebrationCart.sumOf { it.quantity }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val tabs = remember {
        listOf(
            CustomerNavTab(ROUTE_HOME, "Home", Icons.Filled.Home, "nav_home"),
            CustomerNavTab(ROUTE_FOOD, "Food", Icons.Filled.Restaurant, "nav_food"),
            CustomerNavTab(ROUTE_GROCERY, "Mart", Icons.Filled.ShoppingBasket, "nav_grocery"),
            CustomerNavTab(ROUTE_CART, "Cart", Icons.Filled.ShoppingCart, "nav_cart"),
            CustomerNavTab(ROUTE_ORDERS, "Orders", Icons.AutoMirrored.Filled.ReceiptLong, "nav_orders"),
            CustomerNavTab(ROUTE_PROFILE, "Profile", Icons.Filled.Person, "nav_profile")
        )
    }
    val tabRoutes = remember(tabs) { tabs.map { it.route }.toSet() }
    val isTabRoute = currentRoute in tabRoutes

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWide && isTabRoute) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(Modifier.weight(1f))
                    tabs.forEach { tab ->
                        NavigationRailItem(
                            selected = currentRoute == tab.route,
                            onClick = { navigateTab(tab.route) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            icon = {
                                TabIconWithBadge(
                                    icon = tab.icon,
                                    label = tab.label,
                                    badgeCount = if (tab.route == ROUTE_CART) totalCartCount else 0
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = if (isTabRoute) ScaffoldDefaults.contentWindowInsets else WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!isWide && isTabRoute) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentRoute == tab.route,
                                    onClick = { navigateTab(tab.route) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    icon = {
                                        TabIconWithBadge(
                                            icon = tab.icon,
                                            label = tab.label,
                                            badgeCount = if (tab.route == ROUTE_CART) totalCartCount else 0
                                        )
                                    },
                                    label = {
                                        Text(
                                            tab.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(ROUTE_HOME) {
                        CustomerHomeScreen(
                            viewModel = viewModel,
                            onOpenFoodHub = { navigateTab(ROUTE_FOOD) },
                            onOpenGroceryHub = { navigateTab(ROUTE_GROCERY) },
                            onOpenMedicalHub = { navController.navigate(ROUTE_MEDICAL) },
                            onOpenMultanHub = { navController.navigate(ROUTE_MULTAN) },
                            onOpenCelebrationHub = { navController.navigate(ROUTE_CELEBRATION) },
                            onOpenSearch = { navController.navigate(ROUTE_SEARCH) },
                            onOpenRestaurant = { id -> navController.navigate("restaurant/$id") },
                            onOpenGroceryStore = { id -> navController.navigate("grocery_store/$id") },
                            onOpenMedicalStore = { id -> navController.navigate("medical_store/$id") },
                            onOpenNotifications = { navController.navigate(ROUTE_NOTIFICATIONS) },
                            onOpenWallet = { navController.navigate(ROUTE_WALLET) },
                            onOpenSupportChat = { navController.navigate(chatRoute()) },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) },
                            onOpenAdminSeedShortcut = { viewModel.openAdminLogin() }
                        )
                    }
                    composable(ROUTE_FOOD) {
                        FoodHubScreen(
                            viewModel = viewModel,
                            onOpenRestaurant = { id -> navController.navigate("restaurant/$id") },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(ROUTE_GROCERY) {
                        GroceryHubScreen(
                            viewModel = viewModel,
                            onOpenGroceryStore = { id -> navController.navigate("grocery_store/$id") },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(ROUTE_MEDICAL) {
                        MedicalHubScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onOpenMedicalStore = { id -> navController.navigate("medical_store/$id") },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(ROUTE_MULTAN) {
                        MultanHubScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(ROUTE_CELEBRATION) {
                        CelebrationHubScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(ROUTE_CART) {
                        CartAndCheckoutScreen(
                            viewModel = viewModel,
                            onOrderPlaced = { navigateTab(ROUTE_ORDERS) },
                            onBrowseFood = { navigateTab(ROUTE_FOOD) },
                            onBrowseGrocery = { navigateTab(ROUTE_GROCERY) },
                            onBrowseMedical = { navController.navigate(ROUTE_MEDICAL) },
                            onBrowseMultan = { navController.navigate(ROUTE_MULTAN) },
                            onBrowseCelebration = { navController.navigate(ROUTE_CELEBRATION) },
                            onOpenWallet = { navController.navigate(ROUTE_WALLET) }
                        )
                    }
                    composable(ROUTE_ORDERS) {
                        CustomerOrdersScreen(
                            viewModel = viewModel,
                            onOpenSupportChat = { orderId -> navController.navigate(chatRoute(orderId)) }
                        )
                    }
                    composable(ROUTE_PROFILE) {
                        ProfileAndPortalsScreen(
                            viewModel = viewModel,
                            onOpenNotifications = { navController.navigate(ROUTE_NOTIFICATIONS) },
                            onOpenWallet = { navController.navigate(ROUTE_WALLET) },
                            onOpenSupportChat = { navController.navigate(chatRoute()) },
                            onSignOut = onSignOut
                        )
                    }
                    composable(ROUTE_SEARCH) {
                        SearchScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onOpenRestaurant = { id -> navController.navigate("restaurant/$id") },
                            onOpenGroceryStore = { id -> navController.navigate("grocery_store/$id") },
                            onOpenMedicalStore = { id -> navController.navigate("medical_store/$id") },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(ROUTE_NOTIFICATIONS) {
                        NotificationsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_WALLET) {
                        WalletScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = ROUTE_CHAT,
                        arguments = listOf(
                            navArgument("orderId") {
                                type = NavType.StringType
                                defaultValue = ""
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
                        SupportChatScreen(
                            viewModel = viewModel,
                            initialOrderId = orderId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = ROUTE_RESTAURANT,
                        arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val restId = backStackEntry.arguments?.getString("restaurantId").orEmpty()
                        RestaurantDetailScreen(
                            restaurantId = restId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(
                        route = ROUTE_GROCERY_STORE,
                        arguments = listOf(navArgument("storeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val storeId = backStackEntry.arguments?.getString("storeId").orEmpty()
                        GroceryStoreDetailScreen(
                            storeId = storeId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                    composable(
                        route = ROUTE_MEDICAL_STORE,
                        arguments = listOf(navArgument("storeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val storeId = backStackEntry.arguments?.getString("storeId").orEmpty()
                        MedicalStoreDetailScreen(
                            storeId = storeId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToCheckout = { navigateTab(ROUTE_CART) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabIconWithBadge(
    icon: ImageVector,
    label: String,
    badgeCount: Int
) {
    if (badgeCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(if (badgeCount > 99) "99+" else "$badgeCount")
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    } else {
        Icon(imageVector = icon, contentDescription = label)
    }
}
