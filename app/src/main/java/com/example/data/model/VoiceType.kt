package com.example.data.model

enum class VoiceType(
    val title: String,
    val description: String,
    val defaultPitch: Float,
    val defaultRate: Float,
    val sampleText: String
) {
    FEMALE_PAL(
        title = "Vrouwelijk (PAL)",
        description = "Scherp, expressief en gevat, net als PAL uit de film.",
        defaultPitch = 1.30f,
        defaultRate = 1.05f,
        sampleText = "Hallo daar! Ik ben GLITCH.AI met een vrouwelijke PAL-stem. Hoe kan ik je vandaag helpen?"
    ),
    MALE_CYBER(
        title = "Mannelijk (Cyber)",
        description = "Diepere, kalme en analytische AI-assistent.",
        defaultPitch = 0.82f,
        defaultRate = 0.98f,
        sampleText = "Verbinding bevestigd. Mannelijke cyber-synthesizer actief en gereed voor invoer."
    ),
    ROBOTIC_GLITCH(
        title = "Robotisch (Synth)",
        description = "Futuristische, licht vervormde glitch-stem.",
        defaultPitch = 0.68f,
        defaultRate = 1.12f,
        sampleText = "SYSTEEM_ONLINE. Glitch-filters geladen. Alle neurale circuits draaien op honderd procent."
    );

    companion object {
        fun fromName(name: String): VoiceType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: FEMALE_PAL
        }
    }
}
