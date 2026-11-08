package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRedGlitch

enum class AiEmotion(
    val dutchLabel: String,
    val iconSymbol: String,
    val primaryColor: Color,
    val eyeCurvature: Float,   // -1.0 (downcast/sad) to 1.0 (arched/happy)
    val eyeNarrowFactor: Float, // 0.2 (wide shock) to 1.0 (normal) to 1.6 (narrowed/angry)
    val browTilt: Float,        // negative: furrowed/disapproving, positive: raised/shocked
    val mouthCurve: Float,      // negative: frown, positive: smile, 0.0: flat
    val mouthShockFactor: Float // 0.0: normal, 1.0: open 'O'
) {
    NEUTRAL(
        dutchLabel = "Neutraal",
        iconSymbol = "⚡",
        primaryColor = CyberCyan,
        eyeCurvature = 0f,
        eyeNarrowFactor = 1.0f,
        browTilt = 0f,
        mouthCurve = 0f,
        mouthShockFactor = 0f
    ),
    HAPPY(
        dutchLabel = "Blij",
        iconSymbol = "✨",
        primaryColor = CyberEmerald,
        eyeCurvature = 0.8f,
        eyeNarrowFactor = 0.9f,
        browTilt = 0.3f,
        mouthCurve = 0.8f,
        mouthShockFactor = 0f
    ),
    DISAPPROVING(
        dutchLabel = "Afkeurend",
        iconSymbol = "⚠️",
        primaryColor = CyberAmber,
        eyeCurvature = -0.4f,
        eyeNarrowFactor = 1.4f,
        browTilt = -0.7f,
        mouthCurve = -0.7f,
        mouthShockFactor = 0f
    ),
    SHOCKED(
        dutchLabel = "Geschokt",
        iconSymbol = "❗",
        primaryColor = CyberCyan,
        eyeCurvature = 0.1f,
        eyeNarrowFactor = 0.4f,
        browTilt = 0.8f,
        mouthCurve = -0.1f,
        mouthShockFactor = 0.9f
    ),
    ANGRY(
        dutchLabel = "Boos",
        iconSymbol = "💢",
        primaryColor = CyberRedGlitch,
        eyeCurvature = -0.6f,
        eyeNarrowFactor = 1.6f,
        browTilt = -1.0f,
        mouthCurve = -0.8f,
        mouthShockFactor = 0f
    ),
    SAD(
        dutchLabel = "Verdrietig",
        iconSymbol = "💧",
        primaryColor = Color(0xFF2979FF),
        eyeCurvature = -0.8f,
        eyeNarrowFactor = 1.1f,
        browTilt = 0.8f,
        mouthCurve = -0.7f,
        mouthShockFactor = 0f
    ),
    SMIRK(
        dutchLabel = "Zelfingenomen",
        iconSymbol = "😏",
        primaryColor = CyberPurple,
        eyeCurvature = 0.4f,
        eyeNarrowFactor = 1.2f,
        browTilt = -0.2f,
        mouthCurve = 0.5f,
        mouthShockFactor = 0f
    ),
    THINKING(
        dutchLabel = "Verwerken...",
        iconSymbol = "🧠",
        primaryColor = CyberCyan,
        eyeCurvature = 0f,
        eyeNarrowFactor = 1.1f,
        browTilt = 0.1f,
        mouthCurve = 0f,
        mouthShockFactor = 0f
    ),
    GLITCH(
        dutchLabel = "SYSTEEM_GLITCH",
        iconSymbol = "👾",
        primaryColor = CyberMagenta,
        eyeCurvature = 0.2f,
        eyeNarrowFactor = 0.7f,
        browTilt = -0.4f,
        mouthCurve = 0.3f,
        mouthShockFactor = 0.5f
    );

    companion object {
        fun fromString(value: String): AiEmotion {
            val upper = value.trim().uppercase()
            return entries.firstOrNull {
                it.name == upper || upper.contains(it.name)
            } ?: when {
                upper.contains("AFKEUR") || upper.contains("DISAPPROV") -> DISAPPROVING
                upper.contains("BLIJ") || upper.contains("HAPPY") -> HAPPY
                upper.contains("SCHOK") || upper.contains("SHOCK") -> SHOCKED
                upper.contains("BOOS") || upper.contains("ANGRY") -> ANGRY
                upper.contains("VERDRIET") || upper.contains("SAD") -> SAD
                upper.contains("SMIRK") || upper.contains("SLUW") -> SMIRK
                upper.contains("DENK") || upper.contains("THINK") -> THINKING
                upper.contains("GLITCH") || upper.contains("FOUT") -> GLITCH
                else -> NEUTRAL
            }
        }
    }
}
