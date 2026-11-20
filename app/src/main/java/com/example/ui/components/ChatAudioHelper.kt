package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File
import java.util.Locale

sealed interface VoiceCommandAction {
    data class SendNow(val text: String) : VoiceCommandAction
    data object ClearDraft : VoiceCommandAction
    data object CallSupport : VoiceCommandAction
    data object OpenWhatsApp : VoiceCommandAction
    data class InsertDraft(val text: String) : VoiceCommandAction
}

fun parseChatVoiceCommand(spokenRaw: String): VoiceCommandAction {
    val clean = spokenRaw.trim()
    val lower = clean.lowercase(Locale.getDefault())
    return when {
        lower == "clear" || lower == "clear message" || lower == "saaf karo" -> VoiceCommandAction.ClearDraft
        lower == "call" || lower == "call support" || lower == "call helpline" -> VoiceCommandAction.CallSupport
        lower == "whatsapp" || lower == "open whatsapp" -> VoiceCommandAction.OpenWhatsApp
        lower.startsWith("send ") && clean.length > 5 -> {
            VoiceCommandAction.SendNow(clean.substring(5).trim())
        }
        else -> VoiceCommandAction.InsertDraft(clean)
    }
}

class ChatVoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAtMs: Long = 0L

    fun startRecording(): Result<Unit> = runCatching {
        stopSilently()
        val file = File(context.cacheDir, "chat_voice_${System.currentTimeMillis()}.m4a")
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setAudioEncodingBitRate(24_000)
        rec.setAudioSamplingRate(16_000)
        rec.setAudioChannels(1)
        rec.setMaxDuration(20_000)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()
        recorder = rec
        outputFile = file
        startedAtMs = System.currentTimeMillis()
    }

    fun stopAndEncode(): Result<Pair<String, Long>> = runCatching {
        val rec = recorder ?: error("Recorder not active")
        val file = outputFile ?: error("Voice file missing")
        val elapsedSec = (((System.currentTimeMillis() - startedAtMs) / 1000L).coerceIn(1L, 20L))
        runCatching { rec.stop() }
        runCatching { rec.reset() }
        runCatching { rec.release() }
        recorder = null
        outputFile = null

        val bytes = file.takeIf { it.exists() }?.readBytes() ?: error("Could not read voice recording")
        file.delete()
        if (bytes.isEmpty()) error("Voice recording was empty")

        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val dataUri = "data:audio/mp4;base64,$b64"
        if (dataUri.length > 185_000) {
            error("Voice note is too long. Please keep voice notes under 18 seconds.")
        }
        dataUri to elapsedSec
    }

    fun stopSilently() {
        recorder?.let { rec ->
            runCatching { rec.stop() }
            runCatching { rec.reset() }
            runCatching { rec.release() }
        }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}

class ChatVoicePlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var tempFile: File? = null

    fun playOrPause(
        messageId: String,
        audioData: String,
        currentlyPlayingId: String?,
        onStateChanged: (String?) -> Unit
    ) {
        if (currentlyPlayingId == messageId) {
            stop()
            onStateChanged(null)
            return
        }
        stop()
        runCatching {
            val base64Part = if (audioData.contains("base64,")) {
                audioData.substringAfter("base64,")
            } else {
                audioData
            }
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            val file = File(context.cacheDir, "chat_play_${System.currentTimeMillis()}.m4a")
            file.writeBytes(bytes)
            tempFile = file

            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    stop()
                    onStateChanged(null)
                }
                setOnErrorListener { _, _, _ ->
                    stop()
                    onStateChanged(null)
                    true
                }
                prepare()
                start()
            }
            player = mp
            onStateChanged(messageId)
        }.onFailure {
            stop()
            onStateChanged(null)
        }
    }

    fun stop() {
        player?.let { mp ->
            runCatching { if (mp.isPlaying) mp.stop() }
            runCatching { mp.release() }
        }
        player = null
        tempFile?.delete()
        tempFile = null
    }
}
