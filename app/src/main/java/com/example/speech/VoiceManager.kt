package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _micVolume = MutableStateFlow(0f)
    val micVolume: StateFlow<Float> = _micVolume.asStateFlow()

    private val _mouthAmplitude = MutableStateFlow(0f)
    val mouthAmplitude: StateFlow<Float> = _mouthAmplitude.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private var mouthJob: Job? = null
    private var onSpeechResultCallback: ((String) -> Unit)? = null
    private var currentLocale: Locale = Locale.forLanguageTag("nl-NL")

    init {
        initTts()
    }

    fun setLanguage(language: com.example.data.model.AppLanguage) {
        currentLocale = language.locale
        if (isTtsInitialized && tts != null) {
            val res = tts?.setLanguage(currentLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                val langResult = tts?.setLanguage(currentLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(0.92f) // Slightly robotic / deeper cyber tone

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        startMouthAnimation()
                    }

                    override fun onDone(utteranceId: String?) {
                        mainHandler.post {
                            _isSpeaking.value = false
                            stopMouthAnimation()
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        mainHandler.post {
                            _isSpeaking.value = false
                            stopMouthAnimation()
                        }
                    }
                })
            } else {
                Log.e("VoiceManager", "Failed to initialize TextToSpeech")
            }
        }
    }

    fun speak(text: String, pitch: Float = 1.25f, rate: Float = 1.05f, voiceType: com.example.data.model.VoiceType? = null) {
        if (!isTtsInitialized || tts == null) return
        stopSpeaking()
        stopListening()

        configureVoice(voiceType)
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "glitch_utterance_${System.currentTimeMillis()}")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "glitch_utterance")
    }

    private fun configureVoice(voiceType: com.example.data.model.VoiceType?) {
        if (voiceType == null) return
        try {
            val voices = tts?.voices ?: return
            val targetLocale = tts?.voice?.locale ?: Locale.getDefault()
            val matchingVoices = voices.filter { it.locale.language == targetLocale.language }

            if (matchingVoices.isNotEmpty()) {
                val selected = when (voiceType) {
                    com.example.data.model.VoiceType.FEMALE_PAL -> {
                        matchingVoices.firstOrNull { it.name.lowercase().contains("female") || it.name.lowercase().contains("vrouw") }
                            ?: matchingVoices.firstOrNull()
                    }
                    com.example.data.model.VoiceType.MALE_CYBER -> {
                        matchingVoices.firstOrNull { it.name.lowercase().contains("male") || it.name.lowercase().contains("man") }
                            ?: matchingVoices.lastOrNull()
                    }
                    com.example.data.model.VoiceType.ROBOTIC_GLITCH -> {
                        matchingVoices.firstOrNull()
                    }
                }
                selected?.let { tts?.voice = it }
            }
        } catch (e: Exception) {
            Log.w("VoiceManager", "Could not set specific voice from engine: ${e.message}")
        }
    }

    fun testVoice(voiceType: com.example.data.model.VoiceType, pitch: Float, rate: Float) {
        speak(voiceType.sampleText, pitch = pitch, rate = rate, voiceType = voiceType)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        stopMouthAnimation()
    }

    private fun startMouthAnimation() {
        mouthJob?.cancel()
        mouthJob = scope.launch(Dispatchers.Default) {
            var counter = 0f
            while (isActive && _isSpeaking.value) {
                counter += 0.45f
                // Simulates organic speech syllables and pauses
                val base = kotlin.math.sin(counter.toDouble()).toFloat()
                val flutter = (kotlin.random.Random.nextFloat() * 0.4f)
                val amp = ((base * 0.5f + 0.5f) * 0.7f + flutter).coerceIn(0.1f, 1.0f)
                _mouthAmplitude.value = amp
                delay(70)
            }
            _mouthAmplitude.value = 0f
        }
    }

    private fun stopMouthAnimation() {
        mouthJob?.cancel()
        _mouthAmplitude.value = 0f
    }

    fun startListening(onResult: (String) -> Unit) {
        stopSpeaking()
        onSpeechResultCallback = onResult
        _partialTranscript.value = ""

        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                _isListening.value = true
                            }

                            override fun onBeginningOfSpeech() {}

                            override fun onRmsChanged(rmsdB: Float) {
                                // rmsdB is typically between -2 and 10 dB
                                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                                _micVolume.value = normalized
                            }

                            override fun onBufferReceived(buffer: ByteArray?) {}

                            override fun onEndOfSpeech() {
                                _isListening.value = false
                                _micVolume.value = 0f
                            }

                            override fun onError(error: Int) {
                                _isListening.value = false
                                _micVolume.value = 0f
                                Log.w("VoiceManager", "Speech recognition error: $error")
                            }

                            override fun onResults(results: Bundle?) {
                                _isListening.value = false
                                _micVolume.value = 0f
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull() ?: ""
                                if (text.isNotBlank()) {
                                    _partialTranscript.value = text
                                    onSpeechResultCallback?.invoke(text)
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {
                                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull() ?: ""
                                if (text.isNotBlank()) {
                                    _partialTranscript.value = text
                                }
                            }

                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }

                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocale.toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }

                    speechRecognizer?.startListening(intent)
                } else {
                    Log.w("VoiceManager", "SpeechRecognizer is not available on this device")
                }
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error starting speech recognition", e)
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _isListening.value = false
                _micVolume.value = 0f
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error stopping speech recognition", e)
            }
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        speechRecognizer?.destroy()
        speechRecognizer = null
        mouthJob?.cancel()
    }
}
