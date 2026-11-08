package com.example.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.AiEmotion
import com.example.data.model.AttachmentType
import com.example.data.model.ChatAttachment
import com.example.data.model.ChatMessage
import com.example.data.util.AttachmentUtils
import com.example.ui.face.GlitchFaceView
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgSurface
import com.example.ui.theme.CyberBgSurfaceVariant
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberRedGlitch
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPane(
    emotion: AiEmotion,
    isSpeaking: Boolean,
    isListening: Boolean,
    mouthAmplitude: Float,
    micVolume: Float,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String, ChatAttachment?) -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleMic: () -> Unit,
    onSelectEmotion: (AiEmotion) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var currentAttachment by remember { mutableStateOf<ChatAttachment?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val base64 = AttachmentUtils.bitmapToBase64(bitmap)
            currentAttachment = ChatAttachment(
                type = AttachmentType.IMAGE,
                bitmap = bitmap,
                mimeType = "image/jpeg",
                displayName = "CameraFoto.jpg",
                base64Data = base64
            )
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Cameratoegang vereist om een foto te maken", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo picker launcher (zero permission!)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = AttachmentUtils.uriToBase64(context, uri)
            val name = AttachmentUtils.getDisplayName(context, uri)
            currentAttachment = ChatAttachment(
                type = AttachmentType.IMAGE,
                uri = uri,
                mimeType = "image/jpeg",
                displayName = name,
                base64Data = base64
            )
        }
    }

    // Audio file picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = AttachmentUtils.getDisplayName(context, uri)
            currentAttachment = ChatAttachment(
                type = AttachmentType.AUDIO,
                uri = uri,
                mimeType = "audio/*",
                displayName = name
            )
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBgDark)
    ) {
        // Top section: Pure floating animated neon face + emotion status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBgDark)
                .padding(top = 4.dp, bottom = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlitchFaceView(
                    emotion = emotion,
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    mouthAmplitude = mouthAmplitude,
                    micVolume = micVolume,
                    faceSize = 135.dp,
                    showStatusBadge = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick emotion selector chips for testing the facial morphs
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(AiEmotion.entries) { itemEmotion ->
                        val isSelected = itemEmotion == emotion
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) itemEmotion.primaryColor.copy(alpha = 0.25f)
                                    else CyberBgSurfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) itemEmotion.primaryColor else CyberBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectEmotion(itemEmotion) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("chip_emotion_${itemEmotion.name.lowercase()}")
                        ) {
                            Text(
                                text = "${itemEmotion.iconSymbol} ${itemEmotion.dutchLabel}",
                                color = if (isSelected) itemEmotion.primaryColor else TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    isSpeakingThis = isSpeaking && !message.isUser && messages.lastOrNull { !it.isUser }?.id == message.id,
                    onSpeak = { onSpeakMessage(message) },
                    onStop = { onStopSpeaking() }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GLITCH denkt na...",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Chat input bar with Plus Button & Attachment Preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CyberBgSurface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Pending Attachment preview chip (if user selected image/audio)
                currentAttachment?.let { att ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberBgSurfaceVariant)
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (att.bitmap != null) {
                            Image(
                                bitmap = att.bitmap.asImageBitmap(),
                                contentDescription = "Voorbeeld",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else if (att.uri != null && att.type == AttachmentType.IMAGE) {
                            AsyncImage(
                                model = att.uri,
                                contentDescription = "Voorbeeld",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = "Audio",
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = att.displayName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (att.type == AttachmentType.IMAGE) "Beeld gereed voor GLITCH" else "Audiobestand toegevoegd",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { currentAttachment = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Verwijderen",
                                tint = CyberRedGlitch,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PLUS (+) BUTTON for adding images/audio/camera
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberBgSurfaceVariant)
                            .border(1.dp, CyberCyan.copy(alpha = 0.6f), CircleShape)
                            .testTag("chat_plus_attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Beeld of audio toevoegen",
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Microphone dictate toggle
                    IconButton(
                        onClick = onToggleMic,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isListening) CyberRedGlitch.copy(alpha = 0.2f) else CyberBgSurfaceVariant)
                            .border(
                                1.dp,
                                if (isListening) CyberRedGlitch else CyberBorder,
                                CircleShape
                            )
                            .testTag("mic_dictate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Spraakopname",
                            tint = if (isListening) CyberRedGlitch else CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        placeholder = {
                            Text(
                                text = if (isListening) "Luisteren..." else if (currentAttachment != null) "Voeg een vraag toe..." else "Typ een bericht...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send button
                    val canSend = (inputText.isNotBlank() || currentAttachment != null) && !isLoading
                    IconButton(
                        onClick = {
                            if (canSend) {
                                val textToSend = inputText
                                val attToSend = currentAttachment
                                inputText = ""
                                currentAttachment = null
                                onSendMessage(textToSend, attToSend)
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (canSend) CyberCyan else CyberBgSurfaceVariant,
                                CircleShape
                            )
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Verzenden",
                            tint = if (canSend) Color.Black else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Attachment Options Bottom Sheet (Camera, Gallery, Audio)
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = sheetState,
            containerColor = CyberBgDark,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "BESTAND OF BEELD TOEVOEGEN",
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. FOTO MAKEN (Camera)
                AttachmentOptionRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Foto Maken",
                    subtitle = "Open je camera en laat GLITCH meekijken",
                    tint = CyberCyan,
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showAttachmentSheet = false
                            val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (hasCam == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. AFBEELDING KIEZEN (Photo Picker)
                AttachmentOptionRow(
                    icon = Icons.Default.Image,
                    title = "Afbeelding Kiezen",
                    subtitle = "Selecteer een afbeelding uit je galerij",
                    tint = CyberEmerald,
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showAttachmentSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. AUDIOBESTAND KIEZEN
                AttachmentOptionRow(
                    icon = Icons.Default.Audiotrack,
                    title = "Geluid / Audiobestand",
                    subtitle = "Voeg een geluidsopname of audiobestand toe",
                    tint = CyberAmber,
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showAttachmentSheet = false
                            audioPickerLauncher.launch("audio/*")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AttachmentOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberBgSurfaceVariant)
            .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isSpeakingThis: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) CyberBgSurfaceVariant else CyberBgSurface
    val borderColor = if (isUser) CyberCyan.copy(alpha = 0.5f) else message.emotion.primaryColor.copy(alpha = 0.5f)
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        // Sender header + Emotion tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp)
        ) {
            Text(
                text = if (isUser) "JIJ" else "GLITCH.AI",
                color = if (isUser) CyberCyan else message.emotion.primaryColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            if (!isUser) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[${message.emotion.iconSymbol} ${message.emotion.dutchLabel.uppercase()}]",
                    color = message.emotion.primaryColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                color = TextMuted,
                fontSize = 9.sp
            )
        }

        // Message bubble
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                // If message has an attachment, display it!
                message.attachment?.let { att ->
                    if (att.bitmap != null) {
                        Image(
                            bitmap = att.bitmap.asImageBitmap(),
                            contentDescription = "Bijlage",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else if (att.uri != null && att.type == AttachmentType.IMAGE) {
                        AsyncImage(
                            model = att.uri,
                            contentDescription = "Bijlage",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else if (att.type == AttachmentType.AUDIO) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberBgDark, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = att.displayName,
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                }

                // Voice playback replay button for GLITCH.AI responses
                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(message.emotion.primaryColor.copy(alpha = 0.15f))
                            .clickable { if (isSpeakingThis) onStop() else onSpeak() }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThis) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSpeakingThis) "Stop spraak" else "Luister naar GLITCH",
                            tint = message.emotion.primaryColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSpeakingThis) "Mond beweegt / Sprekend..." else "Uitspreken",
                            color = message.emotion.primaryColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
