package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GharTakConstants
import com.example.data.model.WalletTransaction
import com.example.ui.components.EmptyStateView
import com.example.ui.components.openWhatsAppSupport
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakEmeraldDark
import com.example.ui.theme.GtMintSubtext
import com.example.ui.theme.GtNavyDeep
import com.example.ui.theme.GtNavyHeader
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.GharTakViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WalletScreen(
    viewModel: GharTakViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.walletTransactions.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val jcNumber = settings.jazzCashNumber.ifBlank { GharTakConstants.DEFAULT_JAZZCASH_NUMBER }
    val jcTitle = settings.jazzCashAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
    val epNumber = settings.easypaisaNumber.ifBlank { GharTakConstants.DEFAULT_EASYPAISA_NUMBER }
    val epTitle = settings.easypaisaAccountTitle.ifBlank { GharTakConstants.DEFAULT_BENEFICIARY_TITLE }
    val supportPhone = settings.supportPhoneLocal.trim().ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
    val supportWa = settings.supportWhatsappNumber.trim()
        .ifBlank { GharTakConstants.toWhatsappDigits(supportPhone) }

    val balance = profile?.walletBalancePkr ?: 0.0
    val channels = remember(settings.beneficiaryAccounts) {
        (listOf("JazzCash", "Easypaisa", "Bank Card") +
            settings.beneficiaryAccounts.map { it.channel.trim() }.filter { it.isNotBlank() }).distinct()
    }
    val quickAmounts = listOf(500, 1000, 2500, 5000)

    var selectedChannel by remember { mutableStateOf("JazzCash") }
    var amountInput by remember { mutableStateOf("1000") }
    var referenceNote by remember { mutableStateOf("") }
    var sentConfirmationBanner by remember { mutableStateOf<String?>(null) }

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val amountValid = parsedAmount in 1.0..50000.0

    fun copyAccountNumber(label: String, number: String) {
        clipboard.setText(AnnotatedString(number))
        scope.launch {
            snackbarHostState.showSnackbar("Copied $label number: $number")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GT Service Wallet") },
                colors = gtNavyTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Balance Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(GtNavyDeep, GtNavyHeader, GharTakEmeraldDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.AccountBalanceWallet,
                                    contentDescription = "GT Service Wallet",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    "Available Balance",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    "Instant Checkout",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = GharTakConstants.formatPkr(balance),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("wallet_balance_text")
                        )
                        Text(
                            text = "${profile?.fullName ?: viewModel.currentUserName} • Synced with your GT Service account",
                            color = GtMintSubtext,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Official Beneficiary Accounts Card (JazzCash / Easypaisa / Custom)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = GharTakEmerald, modifier = Modifier.size(20.dp))
                            Text(
                                "Payment & Beneficiary Accounts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (settings.jazzCashConfigured) {
                            BeneficiaryAccountRow(
                                channel = "JazzCash",
                                accountTitle = jcTitle,
                                accountNumber = jcNumber,
                                onCopy = { copyAccountNumber("JazzCash", jcNumber) },
                                copyTestTag = "wallet_copy_jazzcash_button"
                            )
                        }
                        if (settings.easypaisaConfigured) {
                            BeneficiaryAccountRow(
                                channel = "Easypaisa",
                                accountTitle = epTitle,
                                accountNumber = epNumber,
                                onCopy = { copyAccountNumber("Easypaisa", epNumber) },
                                copyTestTag = "wallet_copy_easypaisa_button"
                            )
                        }
                        settings.beneficiaryAccounts.forEach { acc ->
                            BeneficiaryAccountRow(
                                channel = acc.channel,
                                accountTitle = acc.accountTitle,
                                accountNumber = acc.accountNumber,
                                onCopy = { copyAccountNumber(acc.channel, acc.accountNumber) },
                                copyTestTag = "wallet_copy_beneficiary_${acc.id}"
                            )
                        }
                    }
                }
            }

            // Top-Up Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Add Money to Wallet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "JazzCash / Easypaisa: $jcNumber ($jcTitle) • Support: $supportPhone",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            channels.forEach { channel ->
                                FilterChip(
                                    selected = selectedChannel == channel,
                                    onClick = { selectedChannel = channel },
                                    leadingIcon = {
                                        Icon(
                                            if (channel == "Bank Card") Icons.Filled.CreditCard else Icons.Filled.PhoneAndroid,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(channel) }
                                )
                            }
                        }

                        Text(
                            "Quick Top-Up (PKR)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickAmounts.forEach { amt ->
                                OutlinedButton(
                                    onClick = { amountInput = amt.toString() },
                                    modifier = Modifier.testTag("wallet_quick_$amt")
                                ) {
                                    Text("+ PKR $amt")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Amount in PKR (1 - 50,000)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wallet_amount_input")
                        )

                        OutlinedTextField(
                            value = referenceNote,
                            onValueChange = { referenceNote = it },
                            label = { Text("Sender Mobile / Transaction ID (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val matchedBeneficiary = settings.beneficiaryAccounts.firstOrNull {
                            it.channel.equals(selectedChannel, ignoreCase = true)
                        }
                        val activeAccountNumber = when {
                            selectedChannel.equals("JazzCash", ignoreCase = true) -> jcNumber
                            selectedChannel.equals("Easypaisa", ignoreCase = true) -> epNumber
                            matchedBeneficiary != null -> matchedBeneficiary.accountNumber
                            else -> ""
                        }
                        val activeAccountTitle = when {
                            selectedChannel.equals("JazzCash", ignoreCase = true) -> jcTitle
                            selectedChannel.equals("Easypaisa", ignoreCase = true) -> epTitle
                            matchedBeneficiary != null -> matchedBeneficiary.accountTitle
                            else -> ""
                        }

                        if (activeAccountNumber.isNotBlank() && amountValid) {
                            Surface(
                                color = GharTakEmerald.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Send ${GharTakConstants.formatPkr(parsedAmount)} to $selectedChannel: $activeAccountNumber ($activeAccountTitle)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = GharTakEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "After sending money, notify GT Service Admin so your payment is confirmed immediately.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.sendPaymentSentConfirmation(
                                                    paymentChannel = selectedChannel,
                                                    amountPkr = parsedAmount,
                                                    targetNumber = activeAccountNumber,
                                                    targetTitle = activeAccountTitle,
                                                    referenceNote = referenceNote,
                                                    onSuccess = { msg ->
                                                        sentConfirmationBanner = msg
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Sent confirmation message to Admin!")
                                                        }
                                                    }
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("wallet_notify_payment_sent_button")
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("I Have Sent Payment")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val waMsg =
                                                    "Assalam-o-Alaikum GT Service, maine aap ke $selectedChannel number $activeAccountNumber ($activeAccountTitle) per ${GharTakConstants.formatPkr(parsedAmount)} send kar diye hain." +
                                                        if (referenceNote.isNotBlank()) " TID: ${referenceNote.trim()}" else ""
                                                openWhatsAppSupport(context, phoneDigits = supportWa, prefillMessage = waMsg)
                                            },
                                            modifier = Modifier.testTag("wallet_whatsapp_payment_button")
                                        ) {
                                            Text("WhatsApp")
                                        }
                                    }
                                    sentConfirmationBanner?.let { banner ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GharTakEmerald, modifier = Modifier.size(16.dp))
                                            Text(
                                                "Sent: \"$banner\"",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GharTakEmerald,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            enabled = amountValid,
                            onClick = {
                                viewModel.topUpWallet(
                                    amountPkr = parsedAmount,
                                    channel = selectedChannel,
                                    note = referenceNote
                                ) {
                                    if (activeAccountNumber.isNotBlank()) {
                                        sentConfirmationBanner =
                                            "I have sent ${GharTakConstants.formatPkr(parsedAmount)} via $selectedChannel to your number $activeAccountNumber."
                                    }
                                    referenceNote = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("wallet_confirm_topup_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (amountValid) "Top Up ${GharTakConstants.formatPkr(parsedAmount)}"
                                else "Enter valid PKR amount"
                            )
                        }
                    }
                }
            }

            // Transaction History Header
            item {
                Text(
                    "Transaction History (${transactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No wallet transactions yet",
                        subtitle = "Top up via JazzCash, Easypaisa, or Bank Card to pay faster on Food & Grocery orders."
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    WalletTransactionCard(tx)
                }
            }
        }
    }
}

@Composable
private fun WalletTransactionCard(tx: WalletTransaction) {
    val isCredit = tx.type != "payment"
    val accent = if (isCredit) GharTakEmerald else GharTakCoralError
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val dateLabel = tx.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = if (isCredit) "Credit" else "Debit",
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(tx.paymentChannel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (tx.note.isNotBlank()) {
                    Text(tx.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "$dateLabel • Bal: ${GharTakConstants.formatPkr(tx.balanceAfterPkr)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = (if (isCredit) "+ " else "- ") + GharTakConstants.formatPkr(tx.amountPkr),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun BeneficiaryAccountRow(
    channel: String,
    accountTitle: String,
    accountNumber: String,
    onCopy: () -> Unit,
    copyTestTag: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "$channel • $accountTitle",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    accountNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GharTakEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = onCopy,
                modifier = Modifier.testTag(copyTestTag)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy $channel number", tint = GharTakEmerald)
            }
        }
    }
}
