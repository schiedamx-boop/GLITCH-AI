package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserProfile
import com.example.ui.AppMode
import com.example.ui.GlitchAiViewModel
import com.example.ui.chat.ChatPane
import com.example.ui.dialogs.MemoryBankDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgSurface
import com.example.ui.theme.CyberBgSurfaceVariant
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRedGlitch
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.voice.VoicePane

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GlitchAiApp()
            }
        }
    }
}

@Composable
fun GlitchAiApp(viewModel: GlitchAiViewModel = viewModel()) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val activeMode by viewModel.activeMode.collectAsState()
    val emotion by viewModel.currentEmotion.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val memories by viewModel.userMemories.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val hasApiKey by viewModel.hasApiKey.collectAsState()

    val selectedVoiceType by viewModel.selectedVoiceType.collectAsState()
    val speechPitch by viewModel.speechPitch.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val autoSpeakEnabled by viewModel.autoSpeakEnabled.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val micVolume by viewModel.micVolume.collectAsState()
    val mouthAmplitude by viewModel.mouthAmplitude.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()

    // Back handling for voice mode
    BackHandler(enabled = activeMode == AppMode.VOICE) {
        viewModel.setMode(AppMode.CHAT)
    }

    // Permission launcher for RECORD_AUDIO
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening()
        } else {
            Toast.makeText(context, "Microfoontoegang vereist voor spraak", Toast.LENGTH_SHORT).show()
        }
    }

    val requestMic = {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.toggleVoiceListening()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberBgDark,
        topBar = {
            TopCyberHeader(
                activeMode = activeMode,
                hasApiKey = hasApiKey,
                userProfile = userProfile,
                memoryCount = memories.size,
                onModeChange = { viewModel.setMode(it) },
                onOpenSettings = { showSettingsDialog = true },
                onOpenMemory = { showMemoryDialog = true },
                onClearHistory = { showClearConfirmDialog = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // API Key status banner if not configured yet
            if (!hasApiKey) {
                ApiKeyNoticeBanner(onClick = { showSettingsDialog = true })
            }

            // Main View based on mode: CHAT vs VOICE
            when (activeMode) {
                AppMode.CHAT -> {
                    ChatPane(
                        emotion = emotion,
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        mouthAmplitude = mouthAmplitude,
                        micVolume = micVolume,
                        messages = messages,
                        isLoading = isLoading,
                        onSendMessage = { text, attachment -> viewModel.sendMessage(text, attachment) },
                        onSpeakMessage = { viewModel.speakMessage(it) },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onToggleMic = requestMic,
                        onSelectEmotion = { viewModel.setEmotion(it) }
                    )
                }

                AppMode.VOICE -> {
                    VoicePane(
                        emotion = emotion,
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        mouthAmplitude = mouthAmplitude,
                        micVolume = micVolume,
                        partialTranscript = partialTranscript,
                        lastBotMessage = messages.lastOrNull { !it.isUser },
                        isLoading = isLoading,
                        onToggleMic = requestMic,
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onQuickPrompt = { viewModel.sendMessage(it) }
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            initialKey = viewModel.getApiKey(),
            currentVoiceType = selectedVoiceType,
            speechPitch = speechPitch,
            speechRate = speechRate,
            autoSpeakEnabled = autoSpeakEnabled,
            currentLanguage = selectedLanguage,
            userProfile = userProfile,
            messageCount = messages.size,
            memoryCount = memories.size,
            onSaveKey = { newKey -> viewModel.saveApiKey(newKey) },
            onSelectVoiceType = { viewModel.setVoiceType(it) },
            onSetPitch = { viewModel.setSpeechPitch(it) },
            onSetRate = { viewModel.setSpeechRate(it) },
            onSetAutoSpeak = { viewModel.setAutoSpeak(it) },
            onSelectLanguage = { viewModel.setLanguage(it) },
            onTestVoice = { viewModel.testVoice() },
            onLogin = { name, email -> viewModel.login(name, email) },
            onLogout = { viewModel.logout() },
            onClearHistory = {
                viewModel.clearHistory()
                Toast.makeText(context, "Gespreksgeschiedenis gewist & geheugen gereset", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showMemoryDialog) {
        MemoryBankDialog(
            memories = memories,
            onClearMemory = {
                viewModel.clearHistory()
                Toast.makeText(context, "Geheugen gewist & gereset", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showMemoryDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = CyberRedGlitch,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Geheugen Resetten",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Weet je zeker dat je alle opgeslagen gespreksberichten uit de Room-database wilt wissen en het geheugen van GLITCH wilt resetten?",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        Toast.makeText(context, "Gespreksgeschiedenis gewist & geheugen gereset", Toast.LENGTH_SHORT).show()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberRedGlitch,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("Wissen & Resetten", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Annuleren")
                }
            },
            containerColor = CyberBgDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun TopCyberHeader(
    activeMode: AppMode,
    hasApiKey: Boolean,
    userProfile: UserProfile,
    memoryCount: Int,
    onModeChange: (AppMode) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMemory: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = CyberBgSurface,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (userProfile.isLoggedIn) CyberEmerald else CyberCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GLITCH.AI",
                        color = CyberCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }

                // Action buttons: Profile chip, Memory Brain, Settings & Clear History
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile / Account Quick Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberBgSurfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (userProfile.isLoggedIn) CyberEmerald else CyberBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onOpenSettings() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("profile_quick_chip")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profiel",
                                tint = if (userProfile.isLoggedIn) CyberEmerald else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userProfile.isLoggedIn) userProfile.username.take(9) else "Inloggen",
                                color = if (userProfile.isLoggedIn) CyberEmerald else TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (userProfile.isLoggedIn) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Memory Bank button with live memory count badge
                    IconButton(
                        onClick = onOpenMemory,
                        modifier = Modifier.size(36.dp).testTag("open_memory_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Digitaal Geheugen ($memoryCount feiten)",
                                tint = if (memoryCount > 0) CyberCyan else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (memoryCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(CyberEmerald, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Settings button (Voice & API Key)
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(36.dp).testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Instellingen en Stem",
                            tint = if (hasApiKey) CyberCyan else CyberAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Reset / Clear chat button on toolbar
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.size(36.dp).testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Gespreksgeschiedenis wissen en AI-geheugen resetten",
                            tint = CyberRedGlitch.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Mode Selector: [ CHAT ] vs [ VOICE ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(CyberBgSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeTabButton(
                    title = "CHAT MODUS",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    isSelected = activeMode == AppMode.CHAT,
                    onClick = { onModeChange(AppMode.CHAT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_chat_mode")
                )

                Spacer(modifier = Modifier.width(4.dp))

                ModeTabButton(
                    title = "VOICE MODUS",
                    icon = Icons.Default.RecordVoiceOver,
                    isSelected = activeMode == AppMode.VOICE,
                    onClick = { onModeChange(AppMode.VOICE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_voice_mode")
                )
            }
        }
    }
}

@Composable
fun ModeTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCyan else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.Black else CyberCyan.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.Black else CyberCyan.copy(alpha = 0.7f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ApiKeyNoticeBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberAmber.copy(alpha = 0.12f))
            .border(width = 0.8.dp, color = CyberAmber.copy(alpha = 0.4f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("api_key_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "⚡", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Voer je Gemini API Key in om live AI te activeren. (Tik hier)",
                    color = CyberAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "INSTELLEN >",
                color = CyberAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
