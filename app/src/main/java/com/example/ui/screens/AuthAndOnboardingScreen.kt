package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakEmeraldBright
import com.example.ui.theme.GtMintSubtext
import com.example.ui.theme.GtNavyDeep
import com.example.ui.theme.GtNavyHeader
import com.example.ui.theme.GharTakPinkDark

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onOpenAdminLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GtNavyDeep, GtNavyHeader, MaterialTheme.colorScheme.background)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = GharTakEmerald.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GtMintSubtext, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Now Delivering in Bahawalpur & 20 Cities",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_hero_food),
                    contentDescription = "GT Service Pakistani Food and Grocery Delivery",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(26.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFF7F8FD),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gt_service_logo),
                            contentDescription = "GT Service Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(42.dp)
                                .padding(3.dp)
                        )
                    }
                    Text(
                        text = "GT Service",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Your Favourite Food, Delivered to Your Door!",
                    style = MaterialTheme.typography.titleMedium,
                    color = GtMintSubtext,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeaturePill(Icons.Filled.Restaurant, "Hot Meals", Modifier.weight(1f))
                    FeaturePill(Icons.Filled.ShoppingBasket, "Fresh Groceries", Modifier.weight(1f))
                    FeaturePill(Icons.Filled.DeliveryDining, "PKR & COD", Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = GharTakEmerald, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign In to GT Service", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        "One secure account for Customer Orders, Restaurant & Grocery Store Dashboards, Rider Deliveries, and Super Admin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (errorMsg != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            isLoading = true
                            errorMsg = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMsg = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GharTakEmerald),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign in with Google", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenAdminLogin,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_admin_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "Admin Login",
                            tint = GtNavyHeader,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Admin Login (Add Products)", color = GtNavyHeader)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = GharTakEmeraldBright, modifier = Modifier.size(22.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    }
}
