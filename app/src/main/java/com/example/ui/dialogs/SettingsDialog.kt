package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.GeminiClient
import com.example.data.model.AppLanguage
import com.example.data.model.UserProfile
import com.example.data.model.VoiceType
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgSurface
import com.example.ui.theme.CyberBgSurfaceVariant
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRedGlitch
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsDialog(
    initialKey: String,
    currentVoiceType: VoiceType,
    speechPitch: Float,
    speechRate: Float,
    autoSpeakEnabled: Boolean,
    currentLanguage: AppLanguage,
    userProfile: UserProfile,
    messageCount: Int = 0,
    memoryCount: Int = 0,
    onSaveKey: (String) -> Unit,
    onSelectVoiceType: (VoiceType) -> Unit,
    onSetPitch: (Float) -> Unit,
    onSetRate: (Float) -> Unit,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onTestVoice: () -> Unit,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Stem", "Talen", "Account", "Geheugen", "API")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CyberBgDark)
                .border(1.5.dp, CyberCyan, RoundedCornerShape(18.dp))
                .padding(18.dp)
                .testTag("settings_dialog")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Instellingen",
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INSTELLINGEN",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sluiten",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CyberBgSurfaceVariant,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CyberCyan
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> VoiceSettingsTab(
                        selectedVoiceType = currentVoiceType,
                        pitch = speechPitch,
                        rate = speechRate,
                        autoSpeak = autoSpeakEnabled,
                        onSelectVoiceType = onSelectVoiceType,
                        onSetPitch = onSetPitch,
                        onSetRate = onSetRate,
                        onSetAutoSpeak = onSetAutoSpeak,
                        onTestVoice = onTestVoice
                    )

                    1 -> LanguagesTab(
                        currentLanguage = currentLanguage,
                        onSelectLanguage = onSelectLanguage
                    )

                    2 -> AccountTab(
                        userProfile = userProfile,
                        onLogin = onLogin,
                        onLogout = onLogout
                    )

                    3 -> MemorySettingsTab(
                        messageCount = messageCount,
                        memoryCount = memoryCount,
                        onClearHistory = onClearHistory
                    )

                    4 -> ApiKeyTab(
                        initialKey = initialKey,
                        onSaveKey = onSaveKey
                    )
                }
            }
        }
    }
}

@Composable
fun LanguagesTab(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            AppLanguage.entries
        } else {
            val q = searchQuery.trim().lowercase()
            AppLanguage.entries.filter {
                it.nativeName.lowercase().contains(q) ||
                        it.dutchName.lowercase().contains(q) ||
                        it.code.lowercase().contains(q)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "KIES APP- EN STEMTAAL (28 TALEN)",
            color = CyberCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().testTag("language_search_field"),
            placeholder = { Text("Zoek taal (bijv. Arabisch, Japans...)", color = TextMuted, fontSize = 11.5.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredLanguages) { lang ->
                val isSelected = lang == currentLanguage
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectLanguage(lang) }
                        .testTag("language_item_${lang.code}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CyberCyan.copy(alpha = 0.15f) else CyberBgSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.8.dp,
                        color = if (isSelected) CyberCyan else CyberBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = lang.flagEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lang.nativeName,
                                color = if (isSelected) CyberCyan else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${lang.dutchName} (${lang.localeTag})",
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Actief",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceSettingsTab(
    selectedVoiceType: VoiceType,
    pitch: Float,
    rate: Float,
    autoSpeak: Boolean,
    onSelectVoiceType: (VoiceType) -> Unit,
    onSetPitch: (Float) -> Unit,
    onSetRate: (Float) -> Unit,
    onSetAutoSpeak: (Boolean) -> Unit,
    onTestVoice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "KIES TYPE STEM",
            color = CyberCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Voice Type Cards
        VoiceType.entries.forEach { voice ->
            val isSelected = voice == selectedVoiceType
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectVoiceType(voice) }
                    .testTag("voice_option_${voice.name.lowercase()}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CyberCyan.copy(alpha = 0.15f) else CyberBgSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 0.8.dp,
                    color = if (isSelected) CyberCyan else CyberBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (voice) {
                            VoiceType.FEMALE_PAL -> Icons.Default.RecordVoiceOver
                            VoiceType.MALE_CYBER -> Icons.Default.Person
                            VoiceType.ROBOTIC_GLITCH -> Icons.Default.Tune
                        },
                        contentDescription = null,
                        tint = if (isSelected) CyberCyan else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = voice.title,
                            color = if (isSelected) CyberCyan else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = voice.description,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Geselecteerd",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sliders for Pitch and Speed
        Text(
            text = "TOONHOOGTE (PITCH): ${String.format(Locale.US, "%.2f", pitch)}x",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Slider(
            value = pitch,
            onValueChange = onSetPitch,
            valueRange = 0.6f..1.8f,
            colors = SliderDefaults.colors(
                thumbColor = CyberCyan,
                activeTrackColor = CyberCyan,
                inactiveTrackColor = CyberBorder
            ),
            modifier = Modifier.testTag("pitch_slider")
        )

        Text(
            text = "SPREEKSNELHEID (SPEED): ${String.format(Locale.US, "%.2f", rate)}x",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Slider(
            value = rate,
            onValueChange = onSetRate,
            valueRange = 0.7f..1.5f,
            colors = SliderDefaults.colors(
                thumbColor = CyberCyan,
                activeTrackColor = CyberCyan,
                inactiveTrackColor = CyberBorder
            ),
            modifier = Modifier.testTag("rate_slider")
        )

        // Auto speak toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Automatisch voorlezen", color = TextPrimary, fontSize = 12.sp)
                Text(text = "GLITCH spreekt reacties hardop uit", color = TextSecondary, fontSize = 10.sp)
            }
            Switch(
                checked = autoSpeak,
                onCheckedChange = onSetAutoSpeak,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = CyberCyan,
                    uncheckedTrackColor = CyberBgSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Test Voice Button
        Button(
            onClick = onTestVoice,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("test_voice_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberBgSurfaceVariant,
                contentColor = CyberCyan
            ),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TEST STEM VOORBEELD",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AccountTab(
    userProfile: UserProfile,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var usernameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPassVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (userProfile.isLoggedIn) {
            // Logged in User Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberBgSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(CyberEmerald.copy(alpha = 0.2f), CircleShape)
                            .border(1.5.dp, CyberEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.username.take(1).uppercase(),
                            color = CyberEmerald,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = userProfile.username,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (userProfile.email.isNotBlank()) {
                        Text(
                            text = userProfile.email,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .background(CyberEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STATUS // NEURAAL VERBONDEN",
                            color = CyberEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "INTERACTIES", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "${userProfile.totalInteractions}", color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "VEILIGHEID", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(text = userProfile.securityLevel, color = CyberEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().testTag("logout_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRedGlitch),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberRedGlitch)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "UITLOGGEN", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        } else {
            // Login Form
            Text(
                text = "INLOGGEN OF REGISTREREN",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Koppel je account om je steminstellingen, profiel en chatgeschiedenis op te slaan.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                modifier = Modifier.fillMaxWidth().testTag("account_username_input"),
                label = { Text("Gebruikersnaam", color = TextSecondary, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                modifier = Modifier.fillMaxWidth().testTag("account_email_input"),
                label = { Text("E-mailadres (optioneel)", color = TextSecondary, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                modifier = Modifier.fillMaxWidth().testTag("account_password_input"),
                label = { Text("Wachtwoord", color = TextSecondary, fontSize = 12.sp) },
                visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPassVisible = !isPassVisible }) {
                        Icon(
                            imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CyberCyan
                        )
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val name = if (usernameInput.isNotBlank()) usernameInput.trim() else "Piloot_${(100..999).random()}"
                    onLogin(name, emailInput.trim())
                },
                modifier = Modifier.fillMaxWidth().testTag("account_login_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "INLOGGEN & KOPPELEN",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    onLogin("Gast_${(100..999).random()}", "")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text(text = "Snel inloggen als Gast", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ApiKeyTab(
    initialKey: String,
    onSaveKey: (String) -> Unit
) {
    var keyInput by remember { mutableStateOf(initialKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "GEMINI API SLEUTEL",
            color = CyberCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Koppel je eigen Gemini API Key van Google AI Studio (aistudio.google.com).",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = keyInput,
            onValueChange = {
                keyInput = it
                testStatus = null
                isSuccess = false
            },
            modifier = Modifier.fillMaxWidth().testTag("api_key_input_settings"),
            placeholder = { Text("AIzaSy...", color = TextMuted) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = CyberCyan
                    )
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    val clip = clipboardManager.getText()?.text
                    if (!clip.isNullOrBlank()) {
                        keyInput = clip.trim()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
            ) {
                Text("Plakken", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    if (keyInput.isBlank()) {
                        testStatus = "Voer eerst een sleutel in"
                        isSuccess = false
                        return@OutlinedButton
                    }
                    isTesting = true
                    testStatus = "Testen..."
                    scope.launch {
                        val client = GeminiClient { keyInput }
                        val result = client.sendMessage(emptyList(), "Hallo GLITCH.AI!")
                        isTesting = false
                        if (result.isLiveApi) {
                            isSuccess = true
                            testStatus = "Verbinding succesvol! (${result.emotion.name})"
                        } else {
                            isSuccess = false
                            testStatus = result.errorMessage ?: "Kon niet verbinden met Gemini API"
                        }
                    }
                },
                enabled = !isTesting,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberAmber)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CyberAmber)
                } else {
                    Text("Test Sleutel", fontSize = 11.sp)
                }
            }
        }

        testStatus?.let { status ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status,
                color = if (isSuccess) CyberEmerald else CyberRedGlitch,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { onSaveKey(keyInput.trim()) },
            modifier = Modifier.fillMaxWidth().testTag("save_key_settings_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "OPSLAAN",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun MemorySettingsTab(
    messageCount: Int,
    memoryCount: Int,
    onClearHistory: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "ROOM DATABASE & GEHEUGEN",
            color = CyberCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "GLITCH slaat alle gesprekken lokaal op in een Room SQLite-database, zodat eerdere vragen en context bewaard blijven.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Database stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CyberBgSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "BERICHTEN",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$messageCount",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "opgeslagen in Room",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CyberBgSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "GEHEUGENFEITEN",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$memoryCount",
                        color = CyberEmerald,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "actieve herinneringen",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Reset Section Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberBgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberRedGlitch.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Geheugen & Geschiedenis Resetten",
                    color = CyberRedGlitch,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hiermee wis je alle eerdere chatberichten uit de Room database en reset je het geheugen van GLITCH.AI.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberRedGlitch,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GESPREKSGESCHIEDENIS WISSEN",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Geschiedenis Wissen?",
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Weet je zeker dat je alle opgeslagen chatberichten en het AI-geheugen uit de Room-database wilt verwijderen?",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberRedGlitch,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("settings_confirm_clear_button")
                ) {
                    Text("Ja, Wissen")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Annuleren", color = TextSecondary)
                }
            },
            containerColor = CyberBgDark,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
