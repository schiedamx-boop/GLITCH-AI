package com.example.ui.voice

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiEmotion
import com.example.data.model.ChatMessage
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

@Composable
fun VoicePane(
    emotion: AiEmotion,
    isSpeaking: Boolean,
    isListening: Boolean,
    mouthAmplitude: Float,
    micVolume: Float,
    partialTranscript: String,
    lastBotMessage: ChatMessage?,
    isLoading: Boolean,
    onToggleMic: () -> Unit,
    onStopSpeaking: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CyberBgDark, Color(0xFF070F1A), CyberBgDark)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top status readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            val statusColor = when {
                isSpeaking -> emotion.primaryColor
                isListening -> CyberRedGlitch
                isLoading -> CyberAmber
                else -> CyberCyan
            }

            val statusText = when {
                isSpeaking -> "GLITCH.AI SPREEKT // MOND BEWEEGT"
                isListening -> "MICROFOON ACTIEF // SPREEK NU..."
                isLoading -> "NEURALE PROCESSOR DENKT NA..."
                else -> "VOICE MODE KLAAR // TIK OP MIC OM TE SPREKEN"
            }

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "EMOTIE: ${emotion.dutchLabel.uppercase()} ${emotion.iconSymbol}",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Center: Full-sized animated Glitch Face
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            GlitchFaceView(
                emotion = emotion,
                isSpeaking = isSpeaking,
                isListening = isListening,
                mouthAmplitude = mouthAmplitude,
                micVolume = micVolume,
                faceSize = 280.dp,
                showStatusBadge = false
            )
        }

        // Bottom section: Transcription / Live subtitle & Mic controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Live subtitle card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberBgSurface.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isListening && partialTranscript.isNotBlank()) {
                        Text(
                            text = "\"$partialTranscript\"",
                            color = CyberCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    } else if (lastBotMessage != null && !lastBotMessage.isUser) {
                        Text(
                            text = lastBotMessage.text,
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text = "Druk op de microfoonknop hieronder en zeg bijvoorbeeld:\n\"Ik sloeg gisteren iemand\"",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Quick Voice Test Scenarios (Demonstrating facial emotion and mouth sync)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val voiceTests = listOf(
                    "Gister sloeg ik iemand..." to CyberAmber,
                    "Ik heb de loterij gewonnen!" to CyberEmerald,
                    "Wie ben jij en wat doe je?" to CyberCyan,
                    "Vertel een geheim..." to CyberMagenta
                )

                items(voiceTests) { (prompt, tint) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberBgSurfaceVariant)
                            .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable { onQuickPrompt(prompt) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            color = tint,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Mic control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop speech playback button if currently talking
                if (isSpeaking) {
                    IconButton(
                        onClick = onStopSpeaking,
                        modifier = Modifier
                            .size(50.dp)
                            .background(CyberBgSurfaceVariant, CircleShape)
                            .border(1.dp, CyberRedGlitch, CircleShape)
                            .testTag("voice_stop_speaking")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop spraak",
                            tint = CyberRedGlitch,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                }

                // Main Big Glowing Mic Button
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening) {
                        // Expanding pulse ring
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .scale(pulseScale)
                                .background(CyberRedGlitch.copy(alpha = 0.25f), CircleShape)
                        )
                    }

                    IconButton(
                        onClick = onToggleMic,
                        modifier = Modifier
                            .size(68.dp)
                            .background(
                                if (isListening) CyberRedGlitch else CyberCyan,
                                CircleShape
                            )
                            .border(
                                2.dp,
                                if (isListening) Color.White else CyberCyan,
                                CircleShape
                            )
                            .testTag("main_voice_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop luisteren" else "Begin met praten",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }
    }
}
