package com.example.data.db

import kotlinx.coroutines.flow.Flow
import java.util.Locale

class MemoryManager(private val dao: GlitchDao) {

    val allMemories: Flow<List<UserMemoryEntity>> = dao.getAllMemories()

    suspend fun getMemoriesSummary(): String {
        val memories = dao.getAllMemoriesList()
        if (memories.isEmpty()) return ""
        return buildString {
            appendLine("--- GLITCH DIGITAAL GEHEUGEN (ONTHOUDEN FEITEN OVER GEBRUIKER) ---")
            memories.forEach {
                appendLine("• [${it.key}]: ${it.value}")
            }
            appendLine("-------------------------------------------------------------------")
        }
    }

    suspend fun getAllMemoriesList(): List<UserMemoryEntity> {
        return dao.getAllMemoriesList()
    }

    suspend fun learnFromConversation(userText: String, aiReply: String) {
        val lower = userText.lowercase(Locale.ROOT).trim()

        // 1. Detect Name ("ik heet Alex", "mijn naam is Sarah", "noem me Sam", "my name is Alex")
        val namePatterns = listOf(
            Regex("""(?:ik heet|mijn naam is|noem me|noem mij)\s+([A-Za-zÀ-ÿ0-9_-]+)""", RegexOption.IGNORE_CASE),
            Regex("""(?:my name is|i am called|call me)\s+([A-Za-z0-9_-]+)""", RegexOption.IGNORE_CASE),
            Regex("""(?:اسمي|ادعى)\s+([\u0621-\u064A]+)""", RegexOption.IGNORE_CASE)
        )
        for (pattern in namePatterns) {
            val match = pattern.find(userText)
            if (match != null && match.groupValues.size > 1) {
                val name = match.groupValues[1].replaceFirstChar { it.uppercase() }
                if (name.length in 2..25) {
                    dao.insertOrUpdateMemory(
                        UserMemoryEntity(
                            key = "GEBRUIKERSNAAM",
                            value = name,
                            category = "IDENTITY"
                        )
                    )
                }
            }
        }

        // 2. Detect Pets / Animals ("ik heb een hond/kat genaamd Bello")
        val petPatterns = listOf(
            Regex("""(?:ik heb een|we hebben een)\s+(hond|kat|poes|konijn|vogel|vis|puppy|kitten)\s*(?:genaamd|die heet)?\s*([A-Za-zÀ-ÿ0-9_-]*)""", RegexOption.IGNORE_CASE),
            Regex("""(?:i have a|my)\s+(dog|cat|bird|pet)\s*(?:named|called)?\s*([A-Za-z0-9_-]*)""", RegexOption.IGNORE_CASE)
        )
        for (pattern in petPatterns) {
            val match = pattern.find(userText)
            if (match != null && match.groupValues.size > 1) {
                val petType = match.groupValues[1]
                val petName = if (match.groupValues.size > 2 && match.groupValues[2].isNotBlank()) " genaamd ${match.groupValues[2]}" else ""
                dao.insertOrUpdateMemory(
                    UserMemoryEntity(
                        key = "HUISDIER",
                        value = "$petType$petName",
                        category = "FACT"
                    )
                )
            }
        }

        // 3. Detect Preferences / Favorites ("ik hou van pizza", "mijn lievelingskleur is blauw")
        val favPattern = Regex("""(?:mijn lievelings|mijn favoriete)\s+([a-zA-ZÀ-ÿ\s]{2,15})\s+is\s+([a-zA-ZÀ-ÿ0-9\s]{2,30})""", RegexOption.IGNORE_CASE)
        val favMatch = favPattern.find(userText)
        if (favMatch != null && favMatch.groupValues.size > 2) {
            val topic = favMatch.groupValues[1].trim().uppercase()
            val value = favMatch.groupValues[2].trim()
            dao.insertOrUpdateMemory(
                UserMemoryEntity(
                    key = "FAVORIETE_$topic",
                    value = value,
                    category = "PREFERENCE"
                )
            )
        }

        // 4. Detect Hobbies or Interests ("ik speel gitaar", "ik doe aan gamen", "ik hou van programmeren")
        if (lower.contains("ik hou van") || lower.contains("ik speel") || lower.contains("ik werk als")) {
            val snippet = userText.take(60)
            dao.insertOrUpdateMemory(
                UserMemoryEntity(
                    key = "INTERESSE_${System.currentTimeMillis() % 10000}",
                    value = snippet,
                    category = "PREFERENCE"
                )
            )
        }

        // 5. Store current emotional mood if sadness or distress was expressed
        if (lower.contains("verdrietig") || lower.contains("zielig") || lower.contains("huilen") || lower.contains("pijn")) {
            dao.insertOrUpdateMemory(
                UserMemoryEntity(
                    key = "LAATSTE_GEVOEL",
                    value = "Gebruiker voelde zich verdrietig / gekwetst bij recent onderwerp ('$userText')",
                    category = "MOOD"
                )
            )
        } else if (lower.contains("blij") || lower.contains("feest") || lower.contains("gefeliciteerd")) {
            dao.insertOrUpdateMemory(
                UserMemoryEntity(
                    key = "LAATSTE_GEVOEL",
                    value = "Gebruiker was vrolijk en enthousiast",
                    category = "MOOD"
                )
            )
        }

        // 6. Record last active topic snippet so Glitch can refer back even after topic shifts
        if (userText.length > 5 && !userText.startsWith("/")) {
            dao.insertOrUpdateMemory(
                UserMemoryEntity(
                    key = "VORIGE_VRAAG_ONDERWERP",
                    value = userText.take(120),
                    category = "TOPIC"
                )
            )
        }
    }

    suspend fun clearAll() {
        dao.clearAllMemories()
        dao.clearAllMessages()
    }
}
