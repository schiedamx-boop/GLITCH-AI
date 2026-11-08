package com.example.speech

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class VisemeState(
    val openness: Float = 0f,        // 0.0 (closed) to 1.0 (wide open)
    val widthScale: Float = 1.0f,    // < 1.0 (puckered/round) to > 1.0 (wide smile/teeth)
    val roundness: Float = 0f,       // 0.0 (linear/oval) to 1.0 (circular 'O')
    val teethExposure: Float = 0f    // 0.0 to 1.0 (showing upper/lower synthesizer teeth)
)

object Visemes {
    val REST = VisemeState(openness = 0.0f, widthScale = 1.0f, roundness = 0.0f, teethExposure = 0.0f)
    val BILABIAL = VisemeState(openness = 0.04f, widthScale = 0.95f, roundness = 0.0f, teethExposure = 0.0f) // M, P, B
    val OPEN_AA = VisemeState(openness = 0.95f, widthScale = 1.05f, roundness = 0.25f, teethExposure = 0.65f) // A, AA, AH
    val SPREAD_EE = VisemeState(openness = 0.42f, widthScale = 1.28f, roundness = 0.05f, teethExposure = 0.90f) // E, EE, I, IE, Y
    val ROUND_OO = VisemeState(openness = 0.70f, widthScale = 0.75f, roundness = 0.95f, teethExposure = 0.15f) // O, OO, OE, U, W
    val ROUND_OH = VisemeState(openness = 0.85f, widthScale = 0.86f, roundness = 0.70f, teethExposure = 0.35f) // O, AU, OU
    val DENTAL = VisemeState(openness = 0.25f, widthScale = 1.12f, roundness = 0.05f, teethExposure = 0.60f) // F, V, W
    val SIBILANT = VisemeState(openness = 0.30f, widthScale = 1.18f, roundness = 0.10f, teethExposure = 0.80f) // S, Z, T, D, N, L
}

class PhonemeVisemeEngine(private val scope: CoroutineScope) {
    private val _currentViseme = MutableStateFlow(Visemes.REST)
    val currentViseme: StateFlow<VisemeState> = _currentViseme.asStateFlow()

    private var speechAnimationJob: Job? = null
    private var currentWordJob: Job? = null

    fun reset() {
        speechAnimationJob?.cancel()
        currentWordJob?.cancel()
        _currentViseme.value = Visemes.REST
    }

    /**
     * Called by TTS UtteranceProgressListener onRangeStart with the precise text slice
     * being verbalized right at this millisecond.
     */
    fun onWordSpoken(word: String, estimatedDurationMs: Long = 280L) {
        currentWordJob?.cancel()
        currentWordJob = scope.launch(Dispatchers.Default) {
            val visemeSequence = analyzeWordToVisemes(word)
            if (visemeSequence.isEmpty()) return@launch

            val stepDuration = (estimatedDurationMs / visemeSequence.size).coerceIn(40L, 120L)

            for (target in visemeSequence) {
                if (!isActive) break
                // Smooth interpolation from current to target viseme in 3 mini-frames
                val initial = _currentViseme.value
                val frames = 3
                for (f in 1..frames) {
                    val t = f.toFloat() / frames
                    _currentViseme.value = VisemeState(
                        openness = lerp(initial.openness, target.openness, t),
                        widthScale = lerp(initial.widthScale, target.widthScale, t),
                        roundness = lerp(initial.roundness, target.roundness, t),
                        teethExposure = lerp(initial.teethExposure, target.teethExposure, t)
                    )
                    delay(stepDuration / frames)
                }
            }

            // Brief inter-word natural dip
            val beforeDip = _currentViseme.value
            _currentViseme.value = VisemeState(
                openness = beforeDip.openness * 0.35f,
                widthScale = 1.0f,
                roundness = beforeDip.roundness * 0.4f,
                teethExposure = 0.1f
            )
        }
    }

    /**
     * Fallback rhythmic phoneme stream when range starts are not supported or between words.
     */
    fun startAutonomousRhythm() {
        speechAnimationJob?.cancel()
        speechAnimationJob = scope.launch(Dispatchers.Default) {
            val sequence = listOf(
                Visemes.SPREAD_EE,
                Visemes.OPEN_AA,
                Visemes.SIBILANT,
                Visemes.ROUND_OO,
                Visemes.BILABIAL,
                Visemes.ROUND_OH,
                Visemes.DENTAL,
                Visemes.OPEN_AA
            )
            var index = 0
            while (isActive) {
                val target = sequence[index % sequence.size]
                index++
                val initial = _currentViseme.value
                val frames = 4
                for (f in 1..frames) {
                    val t = f.toFloat() / frames
                    _currentViseme.value = VisemeState(
                        openness = lerp(initial.openness, target.openness, t),
                        widthScale = lerp(initial.widthScale, target.widthScale, t),
                        roundness = lerp(initial.roundness, target.roundness, t),
                        teethExposure = lerp(initial.teethExposure, target.teethExposure, t)
                    )
                    delay(18)
                }
                delay(70)
            }
        }
    }

    fun stopAutonomousRhythm() {
        speechAnimationJob?.cancel()
        currentWordJob?.cancel()
        scope.launch(Dispatchers.Default) {
            // Smoothly ease back to resting mouth state
            val initial = _currentViseme.value
            for (f in 1..5) {
                val t = f.toFloat() / 5
                _currentViseme.value = VisemeState(
                    openness = lerp(initial.openness, 0f, t),
                    widthScale = lerp(initial.widthScale, 1.0f, t),
                    roundness = lerp(initial.roundness, 0f, t),
                    teethExposure = lerp(initial.teethExposure, 0f, t)
                )
                delay(20)
            }
            _currentViseme.value = Visemes.REST
        }
    }

    private fun analyzeWordToVisemes(rawWord: String): List<VisemeState> {
        val word = rawWord.lowercase().trim().filter { it.isLetter() }
        if (word.isEmpty()) return listOf(Visemes.BILABIAL)

        val list = mutableListOf<VisemeState>()
        var i = 0
        while (i < word.length) {
            val twoChar = if (i + 1 < word.length) word.substring(i, i + 2) else ""

            when {
                // Double vowels / diphthongs
                twoChar in listOf("aa", "ah") -> {
                    list.add(Visemes.OPEN_AA)
                    i += 2
                }
                twoChar in listOf("ee", "ie", "ei", "ij") -> {
                    list.add(Visemes.SPREAD_EE)
                    i += 2
                }
                twoChar in listOf("oo", "oe", "ou", "au") -> {
                    list.add(Visemes.ROUND_OO)
                    i += 2
                }
                twoChar in listOf("ui", "eu") -> {
                    list.add(Visemes.ROUND_OH)
                    i += 2
                }
                else -> {
                    val ch = word[i]
                    val viseme = when (ch) {
                        'a' -> Visemes.OPEN_AA
                        'e', 'i', 'y' -> Visemes.SPREAD_EE
                        'o', 'u', 'w' -> Visemes.ROUND_OO
                        'm', 'p', 'b' -> Visemes.BILABIAL
                        'f', 'v' -> Visemes.DENTAL
                        's', 'z', 't', 'd', 'n', 'l', 'r', 'c', 'k', 'g', 'j' -> Visemes.SIBILANT
                        else -> Visemes.SIBILANT
                    }
                    list.add(viseme)
                    i += 1
                }
            }
        }

        // Collapse duplicate adjacent visemes to prevent stuttering
        val deduplicated = mutableListOf<VisemeState>()
        for (v in list) {
            if (deduplicated.isEmpty() || deduplicated.last() != v) {
                deduplicated.add(v)
            }
        }

        return deduplicated.ifEmpty { listOf(Visemes.OPEN_AA, Visemes.BILABIAL) }
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + fraction * (stop - start)
    }
}
