package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.db.GlitchDatabase
import com.example.data.db.MemoryManager
import com.example.data.db.UserMemoryEntity
import com.example.data.model.AiEmotion
import com.example.data.model.AppLanguage
import com.example.data.model.ChatAttachment
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.data.model.VoiceType
import com.example.data.preferences.AppPreferences
import com.example.data.repository.ChatRepository
import com.example.data.repository.RoomChatRepository
import com.example.speech.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppMode {
    CHAT,
    VOICE
}

class GlitchAiViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)
    private val voiceManager = VoiceManager(application, viewModelScope)
    private val geminiClient = GeminiClient { preferences.customApiKey }

    private val database = GlitchDatabase.getInstance(application)
    private val dao = database.glitchDao()
    val chatRepository: ChatRepository = RoomChatRepository(dao)
    val memoryManager = MemoryManager(dao)

    private val _currentEmotion = MutableStateFlow(AiEmotion.NEUTRAL)
    val currentEmotion: StateFlow<AiEmotion> = _currentEmotion.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    val userMemories = memoryManager.allMemories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeMode = MutableStateFlow(AppMode.CHAT)
    val activeMode: StateFlow<AppMode> = _activeMode.asStateFlow()

    private val _hasApiKey = MutableStateFlow(geminiClient.hasValidApiKeyConfigured())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    // Voice & Audio Configuration
    private val _selectedVoiceType = MutableStateFlow(preferences.voiceType)
    val selectedVoiceType: StateFlow<VoiceType> = _selectedVoiceType.asStateFlow()

    private val _speechPitch = MutableStateFlow(preferences.speechPitch)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _speechRate = MutableStateFlow(preferences.speechRate)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _autoSpeakEnabled = MutableStateFlow(preferences.autoSpeakEnabled)
    val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

    // Language configuration (28 languages)
    private val _selectedLanguage = MutableStateFlow(preferences.language)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    // User Profile & Account
    private val _userProfile = MutableStateFlow(preferences.getUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    val isSpeaking = voiceManager.isSpeaking
    val isListening = voiceManager.isListening
    val micVolume = voiceManager.micVolume
    val mouthAmplitude = voiceManager.mouthAmplitude
    val partialTranscript = voiceManager.partialTranscript

    init {
        // Set language in VoiceManager
        voiceManager.setLanguage(preferences.language)

        // Observe persisted chat messages from Room ChatRepository so history is never lost
        viewModelScope.launch {
            chatRepository.getConversationHistory().collect { historyList ->
                if (historyList.isEmpty()) {
                    val greetingText = if (preferences.isLoggedIn) {
                        "Welkom terug, ${preferences.username}! Neurale link hersteld. Ik ben GLITCH.AI."
                    } else {
                        preferences.language.greetingText
                    }
                    val initialGreeting = ChatMessage(
                        text = greetingText,
                        isUser = false,
                        emotion = if (preferences.isLoggedIn) AiEmotion.HAPPY else AiEmotion.NEUTRAL
                    )
                    chatRepository.saveMessage(initialGreeting)
                } else {
                    _messages.value = historyList
                }
            }
        }

        // If user is already logged in, store identity in memory bank
        if (preferences.isLoggedIn && preferences.username.isNotBlank()) {
            viewModelScope.launch {
                dao.insertOrUpdateMemory(
                    UserMemoryEntity(
                        key = "GEBRUIKERSNAAM",
                        value = preferences.username,
                        category = "IDENTITY"
                    )
                )
            }
        }
    }

    fun setMode(mode: AppMode) {
        _activeMode.value = mode
        if (mode == AppMode.CHAT) {
            voiceManager.stopListening()
        }
    }

    fun setEmotion(emotion: AiEmotion) {
        _currentEmotion.value = emotion
    }

    fun setVoiceType(voiceType: VoiceType) {
        preferences.voiceType = voiceType
        _selectedVoiceType.value = voiceType
        _speechPitch.value = voiceType.defaultPitch
        _speechRate.value = voiceType.defaultRate
        testVoice()
    }

    fun setSpeechPitch(pitch: Float) {
        preferences.speechPitch = pitch
        _speechPitch.value = pitch
    }

    fun setSpeechRate(rate: Float) {
        preferences.speechRate = rate
        _speechRate.value = rate
    }

    fun setAutoSpeak(enabled: Boolean) {
        preferences.autoSpeakEnabled = enabled
        _autoSpeakEnabled.value = enabled
    }

    fun setLanguage(language: AppLanguage) {
        preferences.language = language
        _selectedLanguage.value = language
        voiceManager.setLanguage(language)

        val langChangeMessage = ChatMessage(
            text = "${language.flagEmoji} ${language.greetingText}",
            isUser = false,
            emotion = AiEmotion.HAPPY
        )
        viewModelScope.launch {
            chatRepository.saveMessage(langChangeMessage)
        }

        voiceManager.speak(
            text = language.greetingText,
            pitch = _speechPitch.value,
            rate = _speechRate.value,
            voiceType = _selectedVoiceType.value
        )
    }

    fun testVoice() {
        voiceManager.testVoice(
            voiceType = _selectedVoiceType.value,
            pitch = _speechPitch.value,
            rate = _speechRate.value
        )
    }

    fun login(username: String, email: String) {
        preferences.login(username, email)
        _userProfile.value = preferences.getUserProfile()
        _currentEmotion.value = AiEmotion.HAPPY
        val welcomeMessage = ChatMessage(
            text = "Neuraal profiel geactiveerd voor '$username'! Stemherkenning en voorkeuren zijn gesynchroniseerd.",
            isUser = false,
            emotion = AiEmotion.HAPPY
        )
        viewModelScope.launch {
            chatRepository.saveMessage(welcomeMessage)
            dao.insertOrUpdateMemory(
                UserMemoryEntity(
                    key = "GEBRUIKERSNAAM",
                    value = username,
                    category = "IDENTITY"
                )
            )
        }
        voiceManager.speak(
            text = "Hallo $username! Je account is succesvol verbonden.",
            pitch = _speechPitch.value,
            rate = _speechRate.value,
            voiceType = _selectedVoiceType.value
        )
    }

    fun logout() {
        preferences.logout()
        _userProfile.value = preferences.getUserProfile()
        val logoutMessage = ChatMessage(
            text = "Sessie beëindigd. Je bent nu uitgelogd. Lokale gegevens gereset.",
            isUser = false,
            emotion = AiEmotion.NEUTRAL
        )
        viewModelScope.launch {
            chatRepository.saveMessage(logoutMessage)
        }
    }

    fun sendMessage(userText: String, attachment: ChatAttachment? = null) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() && attachment == null) return
        if (_isLoading.value) return

        preferences.interactionCount += 1
        _userProfile.value = preferences.getUserProfile()

        val displayText = if (trimmed.isBlank() && attachment != null) {
            if (attachment.type == com.example.data.model.AttachmentType.IMAGE) "[Afbeelding bijgevoegd]"
            else "[Audiobestand bijgevoegd: ${attachment.displayName}]"
        } else {
            trimmed
        }

        val userMessage = ChatMessage(
            text = displayText,
            isUser = true,
            emotion = AiEmotion.NEUTRAL,
            attachment = attachment
        )

        _isLoading.value = true
        _currentEmotion.value = AiEmotion.THINKING

        viewModelScope.launch {
            // Retrieve recent historical messages from Room repository to provide conversational context
            val historicalContext = chatRepository.getRecentHistoricalMessages(limit = 30)

            // Persist the new user message into Room repository
            chatRepository.saveMessage(userMessage)

            // Fetch memory summary for continuous conversational facts
            val memoriesSummary = memoryManager.getMemoriesSummary()
            val storedMemories = memoryManager.getAllMemoriesList()

            // Include historical messages in the Gemini chat prompt context
            val result = geminiClient.sendMessage(
                history = historicalContext,
                userMessage = trimmed,
                attachment = attachment,
                language = _selectedLanguage.value,
                memoriesSummary = memoriesSummary,
                storedMemories = storedMemories
            )
            _isLoading.value = false

            // Update face emotion
            _currentEmotion.value = result.emotion

            val botMessage = ChatMessage(
                text = result.replyText,
                isUser = false,
                emotion = result.emotion
            )
            // Persist AI reply into Room repository and learn facts into memory
            chatRepository.saveMessage(botMessage)
            memoryManager.learnFromConversation(trimmed, result.replyText)

            // Speak if auto-speak enabled or in voice mode
            if (preferences.autoSpeakEnabled || _activeMode.value == AppMode.VOICE) {
                voiceManager.speak(
                    text = result.replyText,
                    pitch = _speechPitch.value,
                    rate = _speechRate.value,
                    voiceType = _selectedVoiceType.value
                )
            }
        }
    }

    fun speakMessage(message: ChatMessage) {
        if (!message.isUser) {
            _currentEmotion.value = message.emotion
            voiceManager.speak(
                text = message.text,
                pitch = _speechPitch.value,
                rate = _speechRate.value,
                voiceType = _selectedVoiceType.value
            )
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun toggleVoiceListening() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening { transcribedText ->
                if (transcribedText.isNotBlank()) {
                    sendMessage(transcribedText)
                }
            }
        }
    }

    fun saveApiKey(newKey: String) {
        preferences.customApiKey = newKey
        _hasApiKey.value = geminiClient.hasValidApiKeyConfigured()
    }

    fun getApiKey(): String = preferences.customApiKey

    fun clearHistory() {
        voiceManager.stopSpeaking()
        voiceManager.stopListening()
        _currentEmotion.value = AiEmotion.NEUTRAL
        viewModelScope.launch {
            memoryManager.clearAll()
            chatRepository.clearConversation()
            val resetMessage = ChatMessage(
                text = "Geheugen gewist. Nieuwe sessie geïnitialiseerd. Wat wil je bespreken?",
                isUser = false,
                emotion = AiEmotion.NEUTRAL
            )
            chatRepository.saveMessage(resetMessage)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
