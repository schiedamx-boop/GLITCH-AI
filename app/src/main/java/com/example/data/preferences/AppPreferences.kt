package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.UserProfile
import com.example.data.model.VoiceType

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("glitch_ai_prefs", Context.MODE_PRIVATE)

    var customApiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var speechRate: Float
        get() = prefs.getFloat(KEY_SPEECH_RATE, 1.05f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    var speechPitch: Float
        get() = prefs.getFloat(KEY_SPEECH_PITCH, 1.30f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_PITCH, value).apply()

    var autoSpeakEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SPEAK, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SPEAK, value).apply()

    var voiceType: VoiceType
        get() {
            val savedName = prefs.getString(KEY_VOICE_TYPE, VoiceType.FEMALE_PAL.name) ?: VoiceType.FEMALE_PAL.name
            return VoiceType.fromName(savedName)
        }
        set(value) {
            prefs.edit()
                .putString(KEY_VOICE_TYPE, value.name)
                .putFloat(KEY_SPEECH_PITCH, value.defaultPitch)
                .putFloat(KEY_SPEECH_RATE, value.defaultRate)
                .apply()
        }

    var language: AppLanguage
        get() {
            val code = prefs.getString(KEY_LANGUAGE, AppLanguage.DUTCH.code) ?: AppLanguage.DUTCH.code
            return AppLanguage.fromCode(code)
        }
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value.code).apply()
        }

    // User Account
    var username: String
        get() = prefs.getString(KEY_USER_NAME, "Gebruiker") ?: "Gebruiker"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value.trim()).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value.trim()).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var interactionCount: Int
        get() = prefs.getInt(KEY_INTERACTIONS, 0)
        set(value) = prefs.edit().putInt(KEY_INTERACTIONS, value).apply()

    fun getUserProfile(): UserProfile {
        return UserProfile(
            username = username,
            email = userEmail,
            isLoggedIn = isLoggedIn,
            totalInteractions = interactionCount
        )
    }

    fun login(name: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name.trim())
            .putString(KEY_USER_EMAIL, email.trim())
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_USER_NAME, "Gebruiker")
            .putString(KEY_USER_EMAIL, "")
            .apply()
    }

    companion object {
        private const val KEY_API_KEY = "custom_gemini_api_key"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_AUTO_SPEAK = "auto_speak_enabled"
        private const val KEY_VOICE_TYPE = "selected_voice_type"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_INTERACTIONS = "interaction_count"
    }
}
