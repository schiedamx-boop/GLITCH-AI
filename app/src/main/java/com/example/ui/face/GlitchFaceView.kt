package com.example.ui.face

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiEmotion
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRedGlitch
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Pure Floating Neon Smiley Face.
 * No box, no phone bezel, no lines around it.
 * The face itself radiates intense multi-layered neon glow directly in cyberspace.
 */
@Composable
fun GlitchFaceView(
    emotion: AiEmotion,
    isSpeaking: Boolean,
    isListening: Boolean,
    mouthAmplitude: Float,
    micVolume: Float,
    modifier: Modifier = Modifier,
    faceSize: Dp = 260.dp,
    showStatusBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    // Dynamic Neon Glow Color palette based on emotion
    val neonPrimaryColor by animateColorAsState(
        targetValue = when (emotion) {
            AiEmotion.DISAPPROVING -> CyberAmber
            AiEmotion.ANGRY -> CyberRedGlitch
            AiEmotion.HAPPY -> CyberEmerald
            AiEmotion.SHOCKED -> CyberCyan
            AiEmotion.SAD -> Color(0xFF2979FF)
            AiEmotion.SMIRK -> CyberPurple
            AiEmotion.GLITCH -> CyberMagenta
            AiEmotion.THINKING -> CyberCyan
            AiEmotion.NEUTRAL -> CyberCyan
        },
        animationSpec = tween(380),
        label = "neonPrimary"
    )

    // Inner bright filament core color (pure white or hot neon tint)
    val neonCoreColor by animateColorAsState(
        targetValue = when (emotion) {
            AiEmotion.ANGRY -> Color(0xFFFFD1D1)
            AiEmotion.DISAPPROVING -> Color(0xFFFFF0D0)
            else -> Color(0xFFF0FFFF)
        },
        animationSpec = tween(350),
        label = "neonCore"
    )

    // Morphable facial parameters
    val targetEyeCurvature by animateFloatAsState(
        targetValue = emotion.eyeCurvature,
        animationSpec = tween(350),
        label = "eyeCurvature"
    )

    val targetEyeNarrow by animateFloatAsState(
        targetValue = emotion.eyeNarrowFactor,
        animationSpec = tween(300),
        label = "eyeNarrow"
    )

    val targetBrowTilt by animateFloatAsState(
        targetValue = emotion.browTilt,
        animationSpec = tween(350),
        label = "browTilt"
    )

    val targetMouthCurve by animateFloatAsState(
        targetValue = emotion.mouthCurve,
        animationSpec = tween(350),
        label = "mouthCurve"
    )

    val targetShockFactor by animateFloatAsState(
        targetValue = emotion.mouthShockFactor,
        animationSpec = tween(300),
        label = "shockFactor"
    )

    // Anti-gravity floating bob & gentle holographic drift
    val infiniteTransition = rememberInfiniteTransition(label = "floatingAnimation")

    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverY"
    )

    val hoverTilt by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverTilt"
    )

    // Neon breathing & pulsing energy aura
    val neonPulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonPulse"
    )

    // Dynamic Blinking Loop
    var blinkProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2800, 5200))
            for (step in 1..3) {
                blinkProgress = step / 3f
                delay(20)
            }
            delay(40)
            for (step in 2 downTo 0) {
                blinkProgress = step / 3f
                delay(20)
            }
            blinkProgress = 0f
        }
    }

    // Subtle glitch jitter on emotional shift
    var glitchX by remember { mutableFloatStateOf(0f) }
    var glitchY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(emotion) {
        for (i in 0..3) {
            glitchX = (Random.nextFloat() - 0.5f) * 10f
            glitchY = (Random.nextFloat() - 0.5f) * 6f
            delay(30)
        }
        glitchX = 0f
        glitchY = 0f
    }

    Column(
        modifier = modifier.testTag("floating_neon_face_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(faceSize)
                .rotate(hoverTilt)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                // Face center coordinates in free space
                val centerX = canvasW / 2f + glitchX
                val centerY = canvasH / 2f + hoverOffset + glitchY

                // Voice/Speech dynamic modulation for the neon energy
                val speechEnergy = if (isSpeaking) (mouthAmplitude * 0.4f) else (micVolume * 0.25f)
                val totalNeonPulse = neonPulse * (1f + speechEnergy)

                // 1. Soft Ambient Luminous Halo directly behind the face
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonPrimaryColor.copy(alpha = 0.28f * totalNeonPulse),
                            neonPrimaryColor.copy(alpha = 0.10f * totalNeonPulse),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = canvasW * 0.44f
                    ),
                    center = Offset(centerX, centerY),
                    radius = canvasW * 0.44f
                )

                // 2. Pure Floating Neon Smiley Elements (Eyebrows, Eyes, Mouth)
                drawPureNeonFace(
                    center = Offset(centerX, centerY),
                    faceW = canvasW * 0.70f,
                    faceH = canvasH * 0.65f,
                    neonColor = neonPrimaryColor,
                    coreColor = neonCoreColor,
                    pulse = totalNeonPulse,
                    emotion = emotion,
                    eyeCurvature = targetEyeCurvature,
                    eyeNarrow = targetEyeNarrow,
                    browTilt = targetBrowTilt,
                    mouthCurve = targetMouthCurve,
                    shockFactor = targetShockFactor,
                    mouthAmplitude = mouthAmplitude,
                    isSpeaking = isSpeaking,
                    blinkProgress = blinkProgress
                )
            }
        }

        if (showStatusBadge) {
            Spacer(modifier = Modifier.height(10.dp))
            PalStatusBadge(emotion = emotion, isSpeaking = isSpeaking, isListening = isListening)
        }
    }
}

/**
 * Draws the pure glowing neon face: multi-layered bloom, vivid corona, and bright filament core.
 */
private fun DrawScope.drawPureNeonFace(
    center: Offset,
    faceW: Float,
    faceH: Float,
    neonColor: Color,
    coreColor: Color,
    pulse: Float,
    emotion: AiEmotion,
    eyeCurvature: Float,
    eyeNarrow: Float,
    browTilt: Float,
    mouthCurve: Float,
    shockFactor: Float,
    mouthAmplitude: Float,
    isSpeaking: Boolean,
    blinkProgress: Float
) {
    val eyeSpacing = faceW * 0.29f
    val eyeY = center.y - faceH * 0.14f
    val eyeWidth = faceW * 0.16f
    val eyeNormalHeight = faceH * 0.30f

    // 1. NEON EYEBROWS (Very expressive for disapproving / stern / angry / happy)
    val browWidth = faceW * 0.22f
    val browY = eyeY - eyeNormalHeight * 0.62f

    for (side in listOf(-1f, 1f)) {
        val browX = center.x + side * eyeSpacing
        val innerTilt = -browTilt * 14f
        val outerTilt = browTilt * 7f

        val start = Offset(
            browX - browWidth / 2f,
            browY + if (side < 0) outerTilt else innerTilt
        )
        val end = Offset(
            browX + browWidth / 2f,
            browY + if (side < 0) innerTilt else outerTilt
        )

        // Multi-layer neon glow for eyebrow
        drawNeonLine(
            start = start,
            end = end,
            neonColor = neonColor,
            coreColor = coreColor,
            pulse = pulse,
            strokeWidth = 6.5f
        )
    }

    // 2. NEON EYES
    for (side in listOf(-1f, 1f)) {
        val eyeX = center.x + side * eyeSpacing

        if (blinkProgress > 0.85f) {
            // Blinking: horizontal glowing neon slit
            drawNeonLine(
                start = Offset(eyeX - eyeWidth * 0.85f, eyeY),
                end = Offset(eyeX + eyeWidth * 0.85f, eyeY),
                neonColor = neonColor,
                coreColor = coreColor,
                pulse = pulse,
                strokeWidth = 5.5f
            )
        } else if (emotion == AiEmotion.SAD) {
            // Sad / Sorrowful Neon Eyes: Drooped downcast arcs `v v`
            val arcW = eyeWidth * 1.35f
            val arcH = eyeNormalHeight * 0.65f
            val path = Path().apply {
                moveTo(eyeX - arcW / 2f, eyeY - arcH * 0.15f)
                quadraticTo(eyeX, eyeY + arcH * 0.65f, eyeX + arcW / 2f, eyeY - arcH * 0.15f)
            }
            drawNeonPath(path = path, neonColor = neonColor, coreColor = coreColor, pulse = pulse, strokeWidth = 7f)

            // Neon tear droplet rolling below left eye
            if (side < 0) {
                val tearY = eyeY + arcH * 0.85f
                val tearPath = Path().apply {
                    moveTo(eyeX, tearY - 9f)
                    quadraticTo(eyeX + 6f, tearY + 2f, eyeX, tearY + 10f)
                    quadraticTo(eyeX - 6f, tearY + 2f, eyeX, tearY - 9f)
                }
                drawNeonPath(path = tearPath, neonColor = Color(0xFF00E5FF), coreColor = Color.White, pulse = pulse, strokeWidth = 4f)
            }
        } else if (eyeCurvature > 0.4f) {
            // Happy Neon Eyes: Cute upward curved arcs `^ ^`
            val arcW = eyeWidth * 1.35f
            val arcH = eyeNormalHeight * 0.65f
            val path = Path().apply {
                moveTo(eyeX - arcW / 2f, eyeY + arcH * 0.25f)
                quadraticTo(eyeX, eyeY - arcH * 0.85f, eyeX + arcW / 2f, eyeY + arcH * 0.25f)
            }
            drawNeonPath(path = path, neonColor = neonColor, coreColor = coreColor, pulse = pulse, strokeWidth = 7f)
        } else if (emotion == AiEmotion.DISAPPROVING || emotion == AiEmotion.ANGRY) {
            // Disapproving / Stern Eyes: Narrowed glowing capsule angled strictly inwards
            val narrowH = (eyeNormalHeight * 0.50f) * (1f - blinkProgress)
            val angleTilt = if (side < 0) 9f else -9f
            val eyeCenter = Offset(eyeX, eyeY + angleTilt)

            drawNeonPill(
                center = eyeCenter,
                width = eyeWidth * 1.15f,
                height = narrowH,
                neonColor = neonColor,
                coreColor = coreColor,
                pulse = pulse
            )

            // Inner dark pupil cut
            drawCircle(
                color = Color.Black,
                radius = narrowH * 0.26f,
                center = Offset(eyeX + side * 2.5f, eyeY + angleTilt)
            )
        } else if (shockFactor > 0.5f) {
            // Shocked Neon Eyes: Big glowing hollow rings `O O`
            val shockRadius = eyeNormalHeight * 0.46f
            drawNeonCircleOutline(
                center = Offset(eyeX, eyeY),
                radius = shockRadius,
                neonColor = neonColor,
                coreColor = coreColor,
                pulse = pulse,
                strokeWidth = 6.5f
            )
            // Tiny glowing center pupil
            drawCircle(color = coreColor, radius = shockRadius * 0.25f, center = Offset(eyeX, eyeY))
        } else {
            // Neutral / Standard Neon Eyes: Sleek glowing vertical pills
            val currentH = (eyeNormalHeight / eyeNarrow) * (1f - blinkProgress)
            drawNeonPill(
                center = Offset(eyeX, eyeY),
                width = eyeWidth,
                height = currentH,
                neonColor = neonColor,
                coreColor = coreColor,
                pulse = pulse
            )
        }
    }

    // 3. NEON MOUTH
    val mouthY = center.y + faceH * 0.26f
    val mouthWidth = faceW * 0.50f

    if (isSpeaking) {
        // Dynamic speaking mouth: glowing neon lip ring that moves in real-time
        val openH = (faceH * 0.32f) * (0.28f + mouthAmplitude * 0.72f)
        val mW = mouthWidth * (0.86f + mouthAmplitude * 0.26f)

        drawNeonPillOutline(
            center = Offset(center.x, mouthY),
            width = mW,
            height = openH,
            neonColor = neonColor,
            coreColor = coreColor,
            pulse = pulse,
            strokeWidth = 6.5f
        )

        // Inner electric neon audio wave
        val waveH = openH * 0.45f
        drawArc(
            color = neonColor.copy(alpha = 0.85f),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(center.x - mW * 0.28f, mouthY - waveH * 0.2f),
            size = Size(mW * 0.56f, waveH)
        )
    } else if (shockFactor > 0.5f) {
        // Shocked open `O` mouth
        val shockW = mouthWidth * 0.40f
        val shockH = shockW * 1.4f
        drawNeonPillOutline(
            center = Offset(center.x, mouthY),
            width = shockW,
            height = shockH,
            neonColor = neonColor,
            coreColor = coreColor,
            pulse = pulse,
            strokeWidth = 6.5f
        )
    } else if (emotion == AiEmotion.SAD) {
        // Melancholic curved sad frown
        val frownDepth = -16f
        val path = Path().apply {
            moveTo(center.x - mouthWidth * 0.44f, mouthY + 12f)
            quadraticTo(
                center.x, mouthY + frownDepth,
                center.x + mouthWidth * 0.44f, mouthY + 12f
            )
        }
        drawNeonPath(path = path, neonColor = neonColor, coreColor = coreColor, pulse = pulse, strokeWidth = 6.5f)
    } else if (emotion == AiEmotion.DISAPPROVING || mouthCurve < -0.3f) {
        // DISAPPROVING NEON FROWN:
        // Clear downturned stern neon frown
        val frownDepth = -18f
        val path = Path().apply {
            moveTo(center.x - mouthWidth / 2f, mouthY + 10f)
            quadraticTo(
                center.x, mouthY + frownDepth,
                center.x + mouthWidth / 2f, mouthY + 10f
            )
        }
        drawNeonPath(path = path, neonColor = neonColor, coreColor = coreColor, pulse = pulse, strokeWidth = 7f)

        // Stern downturned ticks at mouth edges
        drawNeonLine(
            start = Offset(center.x - mouthWidth / 2f, mouthY + 10f),
            end = Offset(center.x - mouthWidth / 2f - 4f, mouthY + 18f),
            neonColor = neonColor,
            coreColor = coreColor,
            pulse = pulse,
            strokeWidth = 5f
        )
        drawNeonLine(
            start = Offset(center.x + mouthWidth / 2f, mouthY + 10f),
            end = Offset(center.x + mouthWidth / 2f + 4f, mouthY + 18f),
            neonColor = neonColor,
            coreColor = coreColor,
            pulse = pulse,
            strokeWidth = 5f
        )
    } else if (emotion == AiEmotion.HAPPY || mouthCurve > 0.3f) {
        // Wide luminous happy smile!
        val smileDepth = 22f
        val path = Path().apply {
            moveTo(center.x - mouthWidth / 2f, mouthY - 4f)
            quadraticTo(
                center.x, mouthY + smileDepth,
                center.x + mouthWidth / 2f, mouthY - 4f
            )
        }
        drawNeonPath(path = path, neonColor = neonColor, coreColor = coreColor, pulse = pulse, strokeWidth = 7f)
    } else if (emotion == AiEmotion.SMIRK) {
        // Asymmetric sly smirk
        val path = Path().apply {
            moveTo(center.x - mouthWidth * 0.42f, mouthY)
            quadraticTo(
                center.x + mouthWidth * 0.18f, mouthY - 4f,
                center.x + mouthWidth * 0.52f, mouthY - 14f
            )
        }
        drawNeonPath(path = path, neonColor = neonColor, coreColor = coreColor, pulse = pulse, strokeWidth = 6.5f)
    } else {
        // Neutral: clean horizontal glowing neon bar
        drawNeonLine(
            start = Offset(center.x - mouthWidth / 2f, mouthY),
            end = Offset(center.x + mouthWidth / 2f, mouthY),
            neonColor = neonColor,
            coreColor = coreColor,
            pulse = pulse,
            strokeWidth = 6.5f
        )
    }
}

/**
 * Multi-layer Neon Line (Outer diffuse aura -> Mid corona -> Bright inner filament core).
 */
private fun DrawScope.drawNeonLine(
    start: Offset,
    end: Offset,
    neonColor: Color,
    coreColor: Color,
    pulse: Float,
    strokeWidth: Float
) {
    // Layer 1: Wide diffuse ambient bloom
    drawLine(
        color = neonColor.copy(alpha = 0.22f * pulse),
        start = start,
        end = end,
        strokeWidth = strokeWidth * 3.8f,
        cap = StrokeCap.Round
    )
    // Layer 2: Vivid neon corona
    drawLine(
        color = neonColor.copy(alpha = 0.65f * pulse),
        start = start,
        end = end,
        strokeWidth = strokeWidth * 1.9f,
        cap = StrokeCap.Round
    )
    // Layer 3: Ultra-bright core filament
    drawLine(
        color = coreColor,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

/**
 * Multi-layer Neon Path (for smiles, frowns, curved happy eyes).
 */
private fun DrawScope.drawNeonPath(
    path: Path,
    neonColor: Color,
    coreColor: Color,
    pulse: Float,
    strokeWidth: Float
) {
    // Layer 1: Wide diffuse bloom
    drawPath(
        path = path,
        color = neonColor.copy(alpha = 0.20f * pulse),
        style = Stroke(width = strokeWidth * 3.8f, cap = StrokeCap.Round)
    )
    // Layer 2: Vivid neon corona
    drawPath(
        path = path,
        color = neonColor.copy(alpha = 0.65f * pulse),
        style = Stroke(width = strokeWidth * 1.9f, cap = StrokeCap.Round)
    )
    // Layer 3: Bright core
    drawPath(
        path = path,
        color = coreColor,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

/**
 * Multi-layer Neon Solid Pill (for glowing filled eyes).
 */
private fun DrawScope.drawNeonPill(
    center: Offset,
    width: Float,
    height: Float,
    neonColor: Color,
    coreColor: Color,
    pulse: Float
) {
    val corner = width / 2f
    // Layer 1: Diffuse glow
    drawRoundRect(
        color = neonColor.copy(alpha = 0.25f * pulse),
        topLeft = Offset(center.x - width / 2f - 9f, center.y - height / 2f - 9f),
        size = Size(width + 18f, height + 18f),
        cornerRadius = CornerRadius(corner + 9f, corner + 9f)
    )
    // Layer 2: Intense corona
    drawRoundRect(
        color = neonColor.copy(alpha = 0.70f * pulse),
        topLeft = Offset(center.x - width / 2f - 3f, center.y - height / 2f - 3f),
        size = Size(width + 6f, height + 6f),
        cornerRadius = CornerRadius(corner + 3f, corner + 3f)
    )
    // Layer 3: Core fill
    drawRoundRect(
        color = coreColor,
        topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner)
    )
}

/**
 * Multi-layer Neon Outline Pill (for speaking mouth cavity outline).
 */
private fun DrawScope.drawNeonPillOutline(
    center: Offset,
    width: Float,
    height: Float,
    neonColor: Color,
    coreColor: Color,
    pulse: Float,
    strokeWidth: Float
) {
    val corner = height / 2f
    val topLeft = Offset(center.x - width / 2f, center.y - height / 2f)
    val rectSize = Size(width, height)

    // Dark interior mouth cavity
    drawRoundRect(
        color = Color(0xFF030508).copy(alpha = 0.85f),
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = CornerRadius(corner, corner)
    )

    // Layer 1: Diffuse outer bloom
    drawRoundRect(
        color = neonColor.copy(alpha = 0.22f * pulse),
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = strokeWidth * 3.6f)
    )
    // Layer 2: Mid neon corona
    drawRoundRect(
        color = neonColor.copy(alpha = 0.65f * pulse),
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = strokeWidth * 1.8f)
    )
    // Layer 3: Inner bright filament core
    drawRoundRect(
        color = coreColor,
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = strokeWidth)
    )
}

/**
 * Multi-layer Neon Circle Outline (for shocked eyes).
 */
private fun DrawScope.drawNeonCircleOutline(
    center: Offset,
    radius: Float,
    neonColor: Color,
    coreColor: Color,
    pulse: Float,
    strokeWidth: Float
) {
    drawCircle(
        color = neonColor.copy(alpha = 0.20f * pulse),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth * 3.6f)
    )
    drawCircle(
        color = neonColor.copy(alpha = 0.65f * pulse),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth * 1.8f)
    )
    drawCircle(
        color = coreColor,
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )
}

@Composable
fun PalStatusBadge(
    emotion: AiEmotion,
    isSpeaking: Boolean,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        isSpeaking -> "GLITCH PRAAT // NEON MOND ACTIEF"
        isListening -> "GLITCH LUISTERT... // SPREEK"
        emotion == AiEmotion.DISAPPROVING -> "GLITCH: NIET BLIJ! [AFKEUREND]"
        emotion == AiEmotion.HAPPY -> "GLITCH: BLIJ & TEVREDEN"
        emotion == AiEmotion.ANGRY -> "GLITCH: WOEDEND!"
        emotion == AiEmotion.SAD -> "GLITCH: VERDRIETIG... [MEELEVENDE MODUS 💧]"
        else -> "GLITCH // ${emotion.dutchLabel.uppercase()}"
    }

    Row(
        modifier = modifier
            .background(
                color = emotion.primaryColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emotion.iconSymbol, fontSize = 14.sp)
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = statusText,
            color = emotion.primaryColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}
