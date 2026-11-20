package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GharTakConstants
import com.example.data.model.SupportChatMessage
import com.example.data.model.SupportChatThread
import com.example.ui.components.ChatVoicePlayer
import com.example.ui.components.ChatVoiceRecorder
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GharTakImage
import com.example.ui.components.VoiceCommandAction
import com.example.ui.components.encodeBitmapToCompressedDataUri
import com.example.ui.components.encodeUriToCompressedDataUri
import com.example.ui.components.openSupportDialer
import com.example.ui.components.openWhatsAppSupport
import com.example.ui.components.parseChatVoiceCommand
import com.example.ui.theme.GharTakCharcoal
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.GharTakPinkDark
import com.example.ui.theme.GtMintSubtext
import com.example.ui.theme.GtNavyDeep
import com.example.ui.theme.GtNavyHeader
import com.example.ui.theme.gtNavyTopAppBarColors
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.GharTakViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportHelplineCard(
    onOpenLiveChat: (() -> Unit)? = null,
    unreadCount: Long = 0L,
    subtitle: String = "24/7 Order, Wallet & Rider Support",
    supportPhoneLocal: String = GharTakConstants.SUPPORT_PHONE_LOCAL,
    supportWhatsappNumber: String = GharTakConstants.SUPPORT_WHATSAPP_NUMBER,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activePhone = supportPhoneLocal.trim().ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
    val activeWhatsapp = supportWhatsappNumber.trim()
        .ifBlank { GharTakConstants.toWhatsappDigits(activePhone) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("support_helpline_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GtNavyHeader)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(GtNavyDeep, GtNavyHeader)
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.SupportAgent,
                            contentDescription = "GT Service Support Helpline",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            "GT Service Support & Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = GtMintSubtext
                        )
                    }
                }
                Surface(
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        activePhone,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onOpenLiveChat != null) {
                    Button(
                        onClick = onOpenLiveChat,
                        colors = ButtonDefaults.buttonColors(containerColor = GharTakPink, contentColor = Color.White),
                        modifier = Modifier.testTag("support_open_live_chat_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (unreadCount > 0) "Live Chat ($unreadCount)" else "Live Chat")
                    }
                }
                OutlinedButton(
                    onClick = { openSupportDialer(context, activePhone) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.testTag("support_call_button")
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call $activePhone")
                }
                FilledTonalButton(
                    onClick = {
                        openWhatsAppSupport(
                            context = context,
                            phoneDigits = activeWhatsapp,
                            prefillMessage = "Assalam-o-Alaikum GT Service Support ($activePhone), I need assistance."
                        )
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GharTakEmerald,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("support_whatsapp_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SupportChatScreen(
    viewModel: GharTakViewModel,
    initialOrderId: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.supportChatMessages.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val activeSupportPhone = settings.supportPhoneLocal.trim().ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
    val activeSupportWhatsapp = settings.supportWhatsappNumber.trim()
        .ifBlank { GharTakConstants.toWhatsappDigits(activeSupportPhone) }

    var draft by remember(initialOrderId) {
        mutableStateOf(
            if (initialOrderId.isNotBlank()) {
                "Assalam-o-Alaikum, I need help with order #${initialOrderId.takeLast(6).uppercase()}."
            } else {
                ""
            }
        )
    }
    val listState = rememberLazyListState()
    val voicePlayer = remember(context) { ChatVoicePlayer(context) }
    var playingMessageId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { voicePlayer.stop() }
    }

    LaunchedEffect(Unit) {
        viewModel.markSupportChatRead()
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
            viewModel.markSupportChatRead()
        }
    }

    val quickPrompts = remember(initialOrderId) {
        listOf(
            "Where is my order?",
            "Need help with payment / wallet",
            "Rider location update please",
            "Call me on my number"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GT Service Live Chat", fontWeight = FontWeight.Bold)
                        Text(
                            "Helpline & WhatsApp: $activeSupportPhone",
                            style = MaterialTheme.typography.labelSmall,
                            color = GtMintSubtext
                        )
                    }
                },
                colors = gtNavyTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { openSupportDialer(context, activeSupportPhone) },
                        modifier = Modifier.testTag("chat_header_call_button")
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "Call $activeSupportPhone")
                    }
                    TextButton(
                        onClick = {
                            val msg = if (initialOrderId.isNotBlank()) {
                                "Assalam-o-Alaikum GT Service Support ($activeSupportPhone), help needed for order #${initialOrderId.takeLast(6).uppercase()}."
                            } else {
                                "Assalam-o-Alaikum GT Service Support ($activeSupportPhone), I need help."
                            }
                            openWhatsAppSupport(context, phoneDigits = activeSupportWhatsapp, prefillMessage = msg)
                        },
                        modifier = Modifier.testTag("chat_header_whatsapp_button")
                    ) {
                        Text("WhatsApp", color = GtMintSubtext, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            ChatComposerBar(
                draft = draft,
                onDraftChange = { draft = it },
                placeholder = "Message GT Service ($activeSupportPhone)...",
                inputTestTag = "chat_message_input",
                sendTestTag = "chat_send_button",
                onCallSupport = { openSupportDialer(context, activeSupportPhone) },
                onOpenWhatsApp = {
                    openWhatsAppSupport(
                        context = context,
                        phoneDigits = activeSupportWhatsapp,
                        prefillMessage = "Assalam-o-Alaikum GT Service Support ($activeSupportPhone), I need help."
                    )
                },
                onSend = { msgText, photoDataUri, voiceDataUri, voiceSec ->
                    viewModel.sendSupportMessage(
                        text = msgText,
                        orderId = initialOrderId,
                        imageUrl = photoDataUri,
                        audioBase64 = voiceDataUri,
                        audioDurationSec = voiceSec
                    )
                    if (msgText.isNotBlank()) draft = ""
                },
                modifier = Modifier.imePadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Helpline strip
            Surface(
                color = GharTakCharcoal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = GharTakPink, modifier = Modifier.size(18.dp))
                        Text(
                            "Support Number: $activeSupportPhone",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        color = GharTakEmerald.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            "Online",
                            color = Color(0xFF7CF4B4),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (initialOrderId.isNotBlank()) {
                Surface(
                    color = GharTakPink.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Linked to Order #${initialOrderId.takeLast(6).uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = GharTakPink,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Assalam-o-Alaikum! Welcome to GT Service Support",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Send a message below to chat live with our admin desk, or tap Call / WhatsApp to connect directly on $activeSupportPhone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    quickPrompts.forEach { prompt ->
                                        SuggestionChip(
                                            onClick = {
                                                viewModel.sendSupportMessage(prompt, orderId = initialOrderId)
                                            },
                                            label = { Text(prompt) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        SupportChatBubble(
                            message = msg,
                            isOutgoing = msg.senderRole == "customer",
                            isPlayingAudio = playingMessageId == msg.id,
                            onToggleAudio = {
                                val audio = msg.audioBase64 ?: return@SupportChatBubble
                                voicePlayer.playOrPause(msg.id, audio, playingMessageId) {
                                    playingMessageId = it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportChatBubble(
    message: SupportChatMessage,
    isOutgoing: Boolean,
    isPlayingAudio: Boolean = false,
    onToggleAudio: () -> Unit = {}
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeText = message.createdAt?.toDate()?.let { timeFormatter.format(it) } ?: "Sending..."
    val bubbleBg = if (isOutgoing) GharTakPink else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleBg,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOutgoing) 16.dp else 4.dp,
                bottomEnd = if (isOutgoing) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isOutgoing) {
                    Text(
                        text = message.senderName.ifBlank { "GT Service Support (${message.supportPhone})" },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GharTakPink
                    )
                }
                if (message.orderId.isNotBlank()) {
                    Text(
                        text = "Order #${message.orderId.takeLast(6).uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOutgoing) GtMintSubtext else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!message.imageUrl.isNullOrBlank()) {
                    GharTakImage(
                        imageUrl = message.imageUrl,
                        contentDescription = "Chat photo attachment",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                if (!message.audioBase64.isNullOrBlank()) {
                    Surface(
                        color = if (isOutgoing) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggleAudio)
                            .testTag("chat_audio_bubble_${message.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlayingAudio) "Stop voice note" else "Play voice note",
                                tint = if (isOutgoing) Color.White else GharTakPink
                            )
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = if (isOutgoing) Color.White else GharTakPink,
                                modifier = Modifier.size(18.dp)
                            )
                            val dur = message.audioDurationSec ?: 0L
                            Text(
                                text = if (isPlayingAudio) "Playing voice note..." else "Voice Note (${dur}s)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }
                    }
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOutgoing) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminSupportChatsSection(adminVm: AdminViewModel) {
    val context = LocalContext.current
    val threads by adminVm.supportChatThreads.collectAsStateWithLifecycle()
    val selectedUserId by adminVm.selectedSupportUserId.collectAsStateWithLifecycle()
    val messages by adminVm.selectedSupportMessages.collectAsStateWithLifecycle()
    val settings by adminVm.settings.collectAsStateWithLifecycle()
    val activeSupportPhone = settings.supportPhoneLocal.trim().ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
    val activeSupportWhatsapp = settings.supportWhatsappNumber.trim()
        .ifBlank { GharTakConstants.toWhatsappDigits(activeSupportPhone) }

    val activeThread = remember(threads, selectedUserId) {
        threads.firstOrNull { it.userId == selectedUserId }
    }

    if (activeThread == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SupportHelplineCard(
                    subtitle = "Official Admin Support Desk • Call or WhatsApp Customers",
                    supportPhoneLocal = activeSupportPhone,
                    supportWhatsappNumber = activeSupportWhatsapp
                )
            }
            item {
                Text(
                    "Customer Support Threads (${threads.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (threads.isEmpty()) {
                item {
                    EmptyStateView(
                        "No live chat threads yet",
                        "Customer chats and helpline inquiries to $activeSupportPhone will appear here in real time."
                    )
                }
            } else {
                items(threads, key = { it.userId }) { thread ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { adminVm.selectSupportThread(thread.userId) }
                            .testTag("admin_chat_thread_${thread.userId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    thread.customerName.ifBlank { "Customer" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Phone: ${thread.customerPhone} • Helpline: ${thread.supportPhone}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GharTakPink
                                )
                                Text(
                                    thread.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (thread.unreadByAdmin > 0) {
                                    Badge(containerColor = GharTakPink) {
                                        Text("${thread.unreadByAdmin} new", color = Color.White)
                                    }
                                }
                                Text(
                                    "Reply ▶",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GharTakPink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        var replyDraft by remember(activeThread.userId) { mutableStateOf("") }
        val listState = rememberLazyListState()
        val voicePlayer = remember(context) { ChatVoicePlayer(context) }
        var playingMessageId by remember { mutableStateOf<String?>(null) }

        DisposableEffect(activeThread.userId) {
            onDispose { voicePlayer.stop() }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
                adminVm.selectSupportThread(activeThread.userId)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { adminVm.selectSupportThread(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to threads")
                        }
                        Column {
                            Text(activeThread.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${activeThread.customerPhone} • Helpline $activeSupportPhone",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { openSupportDialer(context, activeThread.customerPhone.ifBlank { activeSupportPhone }) }) {
                        Icon(Icons.Filled.Call, contentDescription = "Call customer", tint = GharTakPink)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyStateView("No messages", "Send the first reply to ${activeThread.customerName}.")
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        SupportChatBubble(
                            message = msg,
                            isOutgoing = msg.senderRole == "admin",
                            isPlayingAudio = playingMessageId == msg.id,
                            onToggleAudio = {
                                val audio = msg.audioBase64 ?: return@SupportChatBubble
                                voicePlayer.playOrPause(msg.id, audio, playingMessageId) {
                                    playingMessageId = it
                                }
                            }
                        )
                    }
                }
            }

            ChatComposerBar(
                draft = replyDraft,
                onDraftChange = { replyDraft = it },
                placeholder = "Reply as GT Service ($activeSupportPhone)...",
                inputTestTag = "admin_chat_reply_input",
                sendTestTag = "admin_chat_send_button",
                onCallSupport = {
                    openSupportDialer(
                        context,
                        activeThread.customerPhone.ifBlank { activeSupportPhone }
                    )
                },
                onOpenWhatsApp = {
                    openWhatsAppSupport(
                        context = context,
                        phoneDigits = activeSupportWhatsapp,
                        prefillMessage = "Assalam-o-Alaikum, GT Service Support ($activeSupportPhone) desk."
                    )
                },
                onSend = { msgText, photoDataUri, voiceDataUri, voiceSec ->
                    adminVm.sendAdminSupportReply(
                        thread = activeThread,
                        text = msgText,
                        imageUrl = photoDataUri,
                        audioBase64 = voiceDataUri,
                        audioDurationSec = voiceSec
                    )
                    if (msgText.isNotBlank()) replyDraft = ""
                }
            )
        }
    }
}

@Composable
private fun ChatComposerBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    placeholder: String,
    inputTestTag: String,
    sendTestTag: String,
    onCallSupport: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    onSend: (text: String, photoDataUri: String?, voiceDataUri: String?, voiceSec: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recorder = remember(context) { ChatVoiceRecorder(context) }

    var pendingPhoto by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var statusError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { recorder.stopSilently() }
    }

    fun processImageUri(uri: Uri?) {
        if (uri == null) return
        scope.launch {
            statusError = null
            val res = encodeUriToCompressedDataUri(context, uri, maxBytes = 120_000, maxDim = 460)
            res.onSuccess { encoded ->
                pendingPhoto = encoded
            }.onFailure { err ->
                statusError = err.message ?: "Unsupported image file."
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> processImageUri(uri) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> processImageUri(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            scope.launch {
                statusError = null
                val encoded = encodeBitmapToCompressedDataUri(bitmap, maxBytes = 120_000, maxDim = 460)
                if (encoded != null) {
                    pendingPhoto = encoded
                } else {
                    statusError = "Could not compress captured photo."
                }
            }
        }
    }

    fun toggleRecordingNow() {
        if (isRecording) {
            isRecording = false
            recorder.stopAndEncode().onSuccess { (audioUri, sec) ->
                statusError = null
                onSend(draft.trim(), pendingPhoto, audioUri, sec)
                pendingPhoto = null
            }.onFailure { err ->
                statusError = err.message ?: "Could not save voice note."
            }
        } else {
            recorder.startRecording().onSuccess {
                statusError = null
                isRecording = true
            }.onFailure { err ->
                statusError = err.message ?: "Microphone recording unavailable."
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            toggleRecordingNow()
        } else {
            statusError = "Microphone permission is required for voice notes."
        }
    }

    val voiceCommandLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val spoken = res.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()
        if (spoken.isNotBlank()) {
            when (val cmd = parseChatVoiceCommand(spoken)) {
                is VoiceCommandAction.SendNow -> {
                    onSend(cmd.text, pendingPhoto, null, null)
                    pendingPhoto = null
                }
                is VoiceCommandAction.ClearDraft -> onDraftChange("")
                is VoiceCommandAction.CallSupport -> onCallSupport()
                is VoiceCommandAction.OpenWhatsApp -> onOpenWhatsApp()
                is VoiceCommandAction.InsertDraft -> {
                    val merged = if (draft.isBlank()) cmd.text else "${draft.trim()} ${cmd.text}"
                    onDraftChange(merged)
                }
            }
        }
    }

    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!statusError.isNullOrBlank()) {
                Text(
                    text = statusError.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GharTakCoralError,
                    maxLines = 2
                )
            }

            if (isRecording) {
                Surface(
                    color = GharTakCoralError.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Recording voice note... Tap Stop to send",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GharTakCoralError
                        )
                        FilledTonalButton(
                            onClick = { toggleRecordingNow() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = GharTakCoralError,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("chat_stop_voice_note_button")
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Stop & Send")
                        }
                    }
                }
            }

            if (!pendingPhoto.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(64.dp)) {
                        GharTakImage(
                            imageUrl = pendingPhoto,
                            contentDescription = "Pending chat photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, GharTakPink, RoundedCornerShape(10.dp))
                        )
                    }
                    Text(
                        "Photo ready to send",
                        style = MaterialTheme.typography.labelMedium,
                        color = GharTakEmerald,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { pendingPhoto = null }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove photo")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("chat_gallery_photo_button")
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Pick Photo", tint = GharTakPink)
                }
                IconButton(
                    onClick = {
                        documentPickerLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
                    },
                    modifier = Modifier.testTag("chat_file_photo_button")
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = "Choose File Photo", tint = GharTakPink)
                }
                IconButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.testTag("chat_camera_photo_button")
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo", tint = GharTakPink)
                }
                IconButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            toggleRecordingNow()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.testTag("chat_voice_note_button")
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Stop Voice Note" else "Record Voice Note",
                        tint = if (isRecording) GharTakCoralError else GharTakPink
                    )
                }
                IconButton(
                    onClick = {
                        statusError = null
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak message or command (send / clear / call / whatsapp)")
                        }
                        runCatching { voiceCommandLauncher.launch(intent) }.onFailure {
                            statusError = "Voice command recognizer is not available on this device."
                        }
                    },
                    modifier = Modifier.testTag("chat_voice_command_button")
                ) {
                    Icon(Icons.Filled.RecordVoiceOver, contentDescription = "Voice Command", tint = GharTakEmerald)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    placeholder = { Text(placeholder) },
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(inputTestTag)
                )
                FilledIconButton(
                    enabled = draft.isNotBlank() || !pendingPhoto.isNullOrBlank(),
                    onClick = {
                        onSend(draft.trim(), pendingPhoto, null, null)
                        pendingPhoto = null
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = GharTakPink,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(sendTestTag)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message")
                }
            }
        }
    }
}

