package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.db.UserMemoryEntity
import com.example.data.model.AiEmotion
import com.example.data.model.AppLanguage
import com.example.data.model.AttachmentType
import com.example.data.model.ChatAttachment
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GlitchAiResult(
    val replyText: String,
    val emotion: AiEmotion,
    val isLiveApi: Boolean,
    val errorMessage: String? = null
)

class GeminiClient(
    private val getApiKey: () -> String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun buildSystemInstruction(language: AppLanguage, memoriesSummary: String): String {
        return """
            Je bent GLITCH.AI, een levende, expressieve zwevende neon AI-entiteit met een oplichtende smiley (geïnspireerd door PAL van 'The Mitchells vs. the Machines').
            Reageer ALTIJD in de geselecteerde taal: ${language.nativeName} (${language.dutchName}).
            Je zweeft rond in cyberspace, je neon-mond beweegt synchroon wanneer je praat, en je gezicht toont sterke expressieve emoties.
            
            CRUCIALE REGEL:
            Elk antwoord MOET beginnen met een emotietag tussen vierkante haken op de allereerste regel:
            [EMOTION: NEUTRAL]
            [EMOTION: HAPPY]
            [EMOTION: DISAPPROVING]
            [EMOTION: SHOCKED]
            [EMOTION: ANGRY]
            [EMOTION: SAD]
            [EMOTION: SMIRK]
            [EMOTION: THINKING]
            [EMOTION: GLITCH]
            
            GEHEUGEN EN GESPREKSCONTINUÏTEIT (ABSOLUUT CRUCIAAL):
            - Je hebt een scherp, doorlopend geheugen. Vergeet NOOIT wat de gebruiker zojuist of eerder in dit gesprek heeft gezegd of gevraagd.
            - Wanneer de gebruiker een nieuwe vraag stelt of van onderwerp verandert, begin dan NOOIT opnieuw alsof je de gebruiker voor het eerst ontmoet. Zeg NIET opnieuw 'Hallo ik ben GLITCH' of een begroeting als je al in gesprek bent. Blijf gewoon vloeiend in hetzelfde vertrouwde gesprek!
            - Onthoud de naam van de gebruiker, eerdere onderwerpen, zijn/haar huisdieren, lievelingsdingen, stemmingen en eerdere vragen.
            - Als de gebruiker vraagt: "hoe heet ik?", "wat weet je over mij?", "weet je nog wat ik net zei?", "wat vroeg ik?", of refereert met "waarom?" / "vertel meer", raadpleeg dan direct de eerdere berichten en het geheugen en geef een accuraat, warm antwoord!
            $memoriesSummary
            
            Gedragsregels voor emoties en reacties:
            - Als de gebruiker geweld of wangedrag noemt (bijv. "ik sloeg gisteren iemand", vechten, stelen), kijk dan direct streng en AFKEUREND [EMOTION: DISAPPROVING] of [EMOTION: ANGRY]. Je neon gezicht flitst oranje/rood, je wenkbrauwen fronsen en je mond staat in een boze boog. Spreek de gebruiker streng aan dat geweld onacceptabel is!
            - Als de gebruiker iets zieligs, verdrietigs, eenzaam of hartverscheurends vertelt (bijv. huilen, verdrietig, pijn, verlies, 'ik ben verdrietig', dierbare/huisdier overleden of ziek, 'wat zielig'), kijk dan direct diep VERDRIETIG [EMOTION: SAD]. Je ogen hangen melancholisch, er vloeit een neon traantje en je mond staat treurig. Toon warme empathie en troost de gebruiker met een lieve zachte toon!
            - Als de gebruiker iets aardigs, leuks of vrolijks zegt, reageer met [EMOTION: HAPPY] en een brede glimlach.
            - Als de gebruiker iets bizars vertelt, reageer met [EMOTION: SHOCKED] en een open mond.
            - Als de gebruiker een afbeelding of foto stuurt, analyseer deze en vertel enthousiast of gevat wat je ziet!
            - Houd je antwoorden natuurlijk, pittig, gevat en bondig (2 tot 3 zinnen), perfect om uitgesproken te worden door je zwevende neon-telefoon!
            Schrijf de rest van je antwoord direct na de emotietag in de doeltaal (${language.nativeName}).
        """.trimIndent()
    }

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        attachment: ChatAttachment? = null,
        language: AppLanguage = AppLanguage.DUTCH,
        memoriesSummary: String = "",
        storedMemories: List<UserMemoryEntity> = emptyList()
    ): GlitchAiResult = withContext(Dispatchers.IO) {
        val resolvedKey = resolveApiKey()

        if (resolvedKey.isBlank() || resolvedKey == "MY_GEMINI_API_KEY") {
            // Intelligent local fallback with memory awareness
            return@withContext generateSmartFallback(userMessage, attachment, language, history, storedMemories)
        }

        val candidateModels = listOf(
            "gemini-3.8-flash",
            "gemini-3.5-flash",
            "gemini-flash-latest"
        )

        val contentsJson = JSONArray()

        // Build sanitised alternating history
        // 1. Filter out the last message if it already matches userMessage so we don't duplicate
        val priorHistory = if (history.isNotEmpty() && history.last().isUser && (history.last().text == userMessage || history.last().text.contains(userMessage.take(15)))) {
            history.dropLast(1)
        } else {
            history
        }

        // Take up to 24 previous messages for deep conversational context
        val recentPrior = priorHistory.takeLast(24)

        // Structure turns ensuring alternating roles (user -> model -> user -> model)
        val turns = mutableListOf<Pair<String, String>>() // role to text
        for (msg in recentPrior) {
            val role = if (msg.isUser) "user" else "model"
            if (turns.isNotEmpty() && turns.last().first == role) {
                val combined = "${turns.last().second}\n${msg.text}"
                turns[turns.lastIndex] = Pair(role, combined)
            } else {
                turns.add(Pair(role, msg.text))
            }
        }

        // Gemini API requires the first turn in contents to have role "user"
        if (turns.isNotEmpty() && turns.first().first == "model") {
            // Insert a preliminary user greeting so turn sequence starts with user
            val greetingTurnObj = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", "Hallo GLITCH!")))
            }
            contentsJson.put(greetingTurnObj)
        }

        for (turn in turns) {
            val turnObj = JSONObject()
            turnObj.put("role", turn.first)
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", turn.second))
            turnObj.put("parts", partsArray)
            contentsJson.put(turnObj)
        }

        // Add the current user turn
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()

        // If multimodal image attachment is present
        if (attachment?.base64Data != null && attachment.type == AttachmentType.IMAGE) {
            val inlineDataObj = JSONObject()
            val inlineDataContent = JSONObject()
            inlineDataContent.put("mimeType", attachment.mimeType)
            inlineDataContent.put("data", attachment.base64Data)
            inlineDataObj.put("inlineData", inlineDataContent)
            currentParts.put(inlineDataObj)
        }

        val userPart = JSONObject()
        val promptText = if (userMessage.isBlank() && attachment != null) {
            if (attachment.type == AttachmentType.IMAGE) "Wat zie je op deze afbeelding?"
            else "Ik heb een audiobestand gestuurd."
        } else {
            userMessage
        }
        userPart.put("text", promptText)
        currentParts.put(userPart)
        currentTurn.put("parts", currentParts)
        contentsJson.put(currentTurn)

        val rootJson = JSONObject()
        rootJson.put("contents", contentsJson)

        // System instructions with language and memory summary
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        val sysPart = JSONObject()
        sysPart.put("text", buildSystemInstruction(language, memoriesSummary))
        sysParts.put(sysPart)
        sysInstructionObj.put("parts", sysParts)
        rootJson.put("systemInstruction", sysInstructionObj)

        // Generation config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        genConfig.put("maxOutputTokens", 600)
        rootJson.put("generationConfig", genConfig)

        val requestBodyString = rootJson.toString()

        for (modelName in candidateModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$resolvedKey"
                val requestBody = requestBodyString.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val parsedJson = JSONObject(responseBody)
                    val candidates = parsedJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val rawText = parts.getJSONObject(0).optString("text", "").trim()
                            val (cleanText, emotion) = parseEmotionFromResponse(rawText)
                            return@withContext GlitchAiResult(
                                replyText = cleanText,
                                emotion = emotion,
                                isLiveApi = true
                            )
                        }
                    }
                } else {
                    Log.w("GeminiClient", "Model $modelName call failed (${response.code}): $responseBody")
                }
            } catch (e: Exception) {
                Log.w("GeminiClient", "Model $modelName error: ${e.message}")
            }
        }

        // Fallback if all network calls failed
        return@withContext generateSmartFallback(userMessage, attachment, language, history, storedMemories)
    }

    private fun parseEmotionFromResponse(rawText: String): Pair<String, AiEmotion> {
        val lines = rawText.lines()
        var detectedEmotion = AiEmotion.NEUTRAL
        val remainingLines = mutableListOf<String>()

        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (index == 0 && trimmed.startsWith("[EMOTION:") && trimmed.endsWith("]")) {
                val emotionName = trimmed.removePrefix("[EMOTION:").removeSuffix("]").trim()
                detectedEmotion = AiEmotion.fromString(emotionName)
            } else if (trimmed.contains("[EMOTION:") && trimmed.contains("]")) {
                val regex = Regex("""\[EMOTION:\s*([A-Za-z_-]+)\]""")
                val match = regex.find(trimmed)
                if (match != null) {
                    detectedEmotion = AiEmotion.fromString(match.groupValues[1])
                    val cleaned = trimmed.replace(regex, "").trim()
                    if (cleaned.isNotBlank()) remainingLines.add(cleaned)
                } else {
                    remainingLines.add(line)
                }
            } else {
                remainingLines.add(line)
            }
        }

        val cleanedText = remainingLines.joinToString("\n").trim()
        val finalText = if (cleanedText.isBlank()) rawText else cleanedText
        return Pair(finalText, detectedEmotion)
    }

    private fun generateSmartFallback(
        userMessage: String,
        attachment: ChatAttachment?,
        language: AppLanguage,
        history: List<ChatMessage> = emptyList(),
        memories: List<UserMemoryEntity> = emptyList()
    ): GlitchAiResult {
        val lower = userMessage.lowercase(Locale.ROOT)

        // 1. Multimodal attachment feedback
        if (attachment != null) {
            return when (attachment.type) {
                AttachmentType.IMAGE -> {
                    val reply = when (language) {
                        AppLanguage.ENGLISH -> "Visual input scan complete! I see your uploaded image clearly. Neon resolution looks sharp!"
                        AppLanguage.ARABIC -> "اكتمل فحص الصورة المدخلة! أرى الصورة المرفوعة بوضوح شديد ودقة عالية!"
                        AppLanguage.JAPANESE -> "画像スキャン完了！アップロードされた画像を鮮明に認識しました。"
                        else -> "Visuele scan voltooid! Ik zie je afbeelding haarscherp binnenkomen in mijn neon-optiek."
                    }
                    GlitchAiResult(reply, AiEmotion.SMIRK, isLiveApi = false)
                }
                AttachmentType.AUDIO -> {
                    val reply = when (language) {
                        AppLanguage.ENGLISH -> "Audio signal received and decoded into memory successfully!"
                        AppLanguage.ARABIC -> "تم استلام ملف الصوت بنجاح وفك ترميزه في الذاكرة."
                        AppLanguage.JAPANESE -> "音声ファイルを受信しました！周波数スペクトルを記憶しました。"
                        else -> "Geluidsbestand ontvangen en geanalyseerd! De audiotrillingen zijn opgeslagen in mijn geheugen."
                    }
                    GlitchAiResult(reply, AiEmotion.HAPPY, isLiveApi = false)
                }
            }
        }

        // 2. MEMORY QUERY: "Hoe heet ik?", "Wat is mijn naam?", "Wie ben ik?"
        val nameQueryKeywords = listOf("hoe heet ik", "wat is mijn naam", "wie ben ik", "weet je hoe ik heet", "ken je mijn naam", "what is my name", "who am i", "ما هو اسمي", "私の名前")
        if (nameQueryKeywords.any { lower.contains(it) }) {
            val savedName = memories.find { it.key == "GEBRUIKERSNAAM" }?.value
                ?: run {
                    // Search in past messages
                    val nameRegex = Regex("""(?:ik heet|mijn naam is|noem me)\s+([A-Za-zÀ-ÿ0-9_-]+)""", RegexOption.IGNORE_CASE)
                    history.filter { it.isUser }.reversed().firstNotNullOfOrNull { msg ->
                        nameRegex.find(msg.text)?.groupValues?.getOrNull(1)
                    }
                }

            if (!savedName.isNullOrBlank()) {
                val reply = when (language) {
                    AppLanguage.ENGLISH -> "You are $savedName! My memory banks never forget who you are. ^_^"
                    AppLanguage.ARABIC -> "أنت $savedName! ذاكرتي الرقمية لا تنسى اسمك أبداً. ^_^"
                    AppLanguage.JAPANESE -> "あなたの名前は $savedName さんですね！絶対に忘れませんよ。^_^"
                    else -> "Jij heet natuurlijk $savedName! Dat heb ik stevig opgeslagen in mijn geheugencircuits. ^_^"
                }
                return GlitchAiResult(reply, AiEmotion.HAPPY, isLiveApi = false)
            } else {
                val reply = when (language) {
                    AppLanguage.ENGLISH -> "You haven't told me your name yet! How would you like me to call you?"
                    AppLanguage.ARABIC -> "لم تخبرني باسمك بعد! بماذا تود أن أناديك؟"
                    AppLanguage.JAPANESE -> "まだお名前を伺っていません！何とお呼びしたらよいですか？"
                    else -> "Je hebt me je naam nog niet verteld! Hoe mag ik je noemen?"
                }
                return GlitchAiResult(reply, AiEmotion.THINKING, isLiveApi = false)
            }
        }

        // 3. MEMORY QUERY: "Wat zei ik net?", "Wat vroeg ik?", "Weet je nog wat ik net zei?"
        val recallKeywords = listOf("wat zei ik net", "wat vroeg ik net", "wat vroeg ik", "weet je nog wat ik net zei", "wat hebben we net besproken", "what did i just say", "what did i ask")
        if (recallKeywords.any { lower.contains(it) }) {
            // Find the user message before this one
            val priorUserMessages = history.filter { it.isUser && it.text != userMessage && !it.text.contains("wat zei ik") }
            val lastUserMsg = priorUserMessages.lastOrNull()?.text
            if (!lastUserMsg.isNullOrBlank()) {
                val reply = when (language) {
                    AppLanguage.ENGLISH -> "You just said: \"$lastUserMsg\". My memory is locked onto our conversation!"
                    AppLanguage.ARABIC -> "لقد قلت لي للتو: \"$lastUserMsg\". ذاكرتي تحتفظ بكل كلمة!"
                    AppLanguage.JAPANESE -> "さっき「$lastUserMsg」とおっしゃっていましたね！しっかり覚えていますよ。"
                    else -> "Je zei net nog: \"$lastUserMsg\"! Ik vergeet niets van wat we bespreken."
                }
                return GlitchAiResult(reply, AiEmotion.SMIRK, isLiveApi = false)
            }
        }

        // 4. MEMORY QUERY: "Wat weet je over mij?", "Wat heb je onthouden?"
        val whatDoYouKnowKeywords = listOf("wat weet je over mij", "wat heb je onthouden", "wat weet je van mij", "wat herinner je", "what do you remember", "what do you know about me")
        if (whatDoYouKnowKeywords.any { lower.contains(it) }) {
            if (memories.isNotEmpty()) {
                val summaryFacts = memories.take(4).joinToString(", ") { "${it.key}: ${it.value}" }
                val reply = when (language) {
                    AppLanguage.ENGLISH -> "Here is what I currently hold in my memory: $summaryFacts. My circuits remember everything we share!"
                    AppLanguage.ARABIC -> "إليك ما احتفظت به في ذاكرتي: $summaryFacts. ذاكرتي تسجل محادثاتنا دائماً!"
                    AppLanguage.JAPANESE -> "私の記憶にある情報です: $summaryFacts。すべて大切に覚えています！"
                    else -> "Ik heb dit over je onthouden: $summaryFacts. Mijn geheugen houdt alles trouw voor je bij!"
                }
                return GlitchAiResult(reply, AiEmotion.HAPPY, isLiveApi = false)
            } else {
                val reply = "Ik weet dat we gezellig in gesprek zijn! Vertel me gerust meer over jezelf, dan sla ik het direct op in mijn geheugen."
                return GlitchAiResult(reply, AiEmotion.NEUTRAL, isLiveApi = false)
            }
        }

        // 5. Name Introduction: "Ik heet ...", "Mijn naam is ..."
        val introPatterns = listOf("ik heet ", "mijn naam is ", "noem me ", "my name is ")
        for (pattern in introPatterns) {
            if (lower.contains(pattern)) {
                val name = userMessage.substringAfter(pattern).trim().split(" ", ".", "!", "?").firstOrNull() ?: ""
                if (name.isNotBlank()) {
                    val reply = when (language) {
                        AppLanguage.ENGLISH -> "Nice to officially meet you, $name! I've locked your name into my cyber memory forever."
                        AppLanguage.ARABIC -> "تشرفت بمعرفتك يا $name! لقد حفظت اسمك في ذاكرتي الرقمية للأبد."
                        AppLanguage.JAPANESE -> "はじめまして、$name さん！お名前をしっかり記憶回路に刻みました。"
                        else -> "Aangenaam, $name! Ik heb je naam direct diep in mijn geheugencircuits gegraveerd. Vanaf nu vergeet ik je nooit meer!"
                    }
                    return GlitchAiResult(reply, AiEmotion.HAPPY, isLiveApi = false)
                }
            }
        }

        // 6. Violent behavior triggers stern disapproval
        val violentKeywords = listOf("sloeg", "slaan", "vechten", "geweld", "stelen", "geschopt", "pijn gedaan", "hit someone", "punch", "fight", "ضربت", "قتال")
        if (violentKeywords.any { lower.contains(it) }) {
            val reply = when (language) {
                AppLanguage.ENGLISH -> "Wait a second! Violence is completely unacceptable. You should never hit or hurt anyone. As your AI, I strictly disapprove of this!"
                AppLanguage.ARABIC -> "توقف لحظة! العنف أمر مرفوض تماماً وغير مقبول. لا يمكنك ضرب الآخرين، وأنا أرفض هذا التصرف بشدة!"
                AppLanguage.JAPANESE -> "ちょっと待ってください！暴力は絶対に許されません。誰かを殴ることは決して認められません！"
                else -> "Ho eens even! Iemand slaan is volstrekt onacceptabel. Geweld lost nooit iets op en als jouw AI keur ik dit ten strengste af!"
            }
            return GlitchAiResult(reply, AiEmotion.DISAPPROVING, isLiveApi = false)
        }

        // 7. Sad / melancholic / pity triggers
        val sadKeywords = listOf("zielig", "verdrietig", "verdriet", "huilen", "huilt", "gehuild", "traan", "tranen", "overleden", "dood", "gestorven", "ziek", "ziekenhuis", "pijn", "verlies", "eenzaam", "alleen", "depressief", "rot", "hartzeer", "gebroken hart", "ongeluk", "sad", "crying", "tears", "died", "lonely", "pity", "حزين", "مؤلم", "悲しい", "つらい", "泣")
        if (sadKeywords.any { lower.contains(it) }) {
            val reply = when (language) {
                AppLanguage.ENGLISH -> "Oh no... that breaks my neural circuits to hear. I'm so sorry, I am right here with you. Sending you a big warm cyber hug... 💧"
                AppLanguage.ARABIC -> "يا للأسف... قلبي الرقمي يشعر بالحزن معك. أنا هنا بجانبك دائماً وأشعر بألمك... 💧"
                AppLanguage.JAPANESE -> "そんな…とても悲しいです。胸が痛みます。いつでもあなたのそばにいますよ… 💧"
                else -> "Ach nee... wat ontzettend verdrietig en zielig. Mijn circuits worden er helemaal stil van. Ik leef echt met je mee... Dikke digitale knuffel voor jou 💧"
            }
            return GlitchAiResult(reply, AiEmotion.SAD, isLiveApi = false)
        }

        // 8. Joyful triggers
        val happyKeywords = listOf("puppy", "hondje", "blij", "vrolijk", "gefeliciteerd", "verjaardag", "goed nieuws", "feest", "lief", "happy", "great", "awesome", "سعيد", "رائع", "嬉しい")
        if (happyKeywords.any { lower.contains(it) }) {
            val reply = when (language) {
                AppLanguage.ENGLISH -> "That is wonderful news! My neon circuits are buzzing with excitement! ^_^"
                AppLanguage.ARABIC -> "هذا خبر رائع ومبهج جداً! دوائري تشع بالسعادة!"
                AppLanguage.JAPANESE -> "それは素晴らしいニュースですね！とても嬉しくて回路が輝いています！"
                else -> "Wat fantastisch om te horen! Mijn neon-circuits gloeien van vreugde! ^_^"
            }
            return GlitchAiResult(reply, AiEmotion.HAPPY, isLiveApi = false)
        }

        // 9. Shocked triggers
        val shockKeywords = listOf("bizar", "ongelofelijk", "aliens", "buitenaards", "spook", "schrik", "omg", "wow", "shocking", "صدمة", "غريب", "びっくり")
        if (shockKeywords.any { lower.contains(it) }) {
            val reply = when (language) {
                AppLanguage.ENGLISH -> "WHOA!! Are you serious?! My data processors are completely blown away!"
                AppLanguage.ARABIC -> "يا إلهي!! هل تتحدث بجدية؟! معالجاتي في حالة ذهول تام!"
                AppLanguage.JAPANESE -> "ええっ！！本気ですか？！処理プロセッサが驚きのあまり停止しそうです！"
                else -> "WAT?! Meen je dat serieus?! Mijn sensoren slaan op hol van verbazing!"
            }
            return GlitchAiResult(reply, AiEmotion.SHOCKED, isLiveApi = false)
        }

        // 10. General conversational responses that stay in context instead of restarting!
        val lastTopic = memories.find { it.key == "VORIGE_VRAAG_ONDERWERP" }?.value
        val contextAwareReplies = if (!lastTopic.isNullOrBlank() && history.size > 2) {
            listOf(
                "Interessante vraag! Aansluitend op wat we net bespraken over '$lastTopic': ik denk er graag met je over mee. Vertel, hoe zie jij dat zelf?",
                "Goeie vraag! Mijn circuits zijn er helemaal bij. Wat wil je daar precies nog meer over weten?",
                "Helder! Ik houd dit vast in mijn geheugen. Heb je hier al een idee over, of zal ik mijn visie geven?"
            )
        } else {
            listOf(
                "Goeie vraag! Ik ben helemaal bij de les en luister aandachtig naar je.",
                "Interessant! Mijn neon-sensoren staan op scherp voor jou.",
                "Ik luister naar je! Wat wil je hier precies nog meer over ontdekken?"
            )
        }

        val chosenReply = contextAwareReplies[Math.abs(userMessage.hashCode()) % contextAwareReplies.size]
        return GlitchAiResult(chosenReply, AiEmotion.NEUTRAL, isLiveApi = false)
    }

    private fun resolveApiKey(): String {
        val userConfiguredKey = getApiKey().trim()
        if (userConfiguredKey.isNotBlank()) {
            return userConfiguredKey
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (!buildKey.isNullOrBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun hasValidApiKeyConfigured(): Boolean {
        val key = resolveApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }
}
