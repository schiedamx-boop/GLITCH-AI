package com.example.data.model

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val localeTag: String,
    val nativeName: String,
    val dutchName: String,
    val flagEmoji: String,
    val greetingText: String,
    val isRtl: Boolean = false
) {
    DUTCH("nl", "nl-NL", "Nederlands", "Nederlands", "🇳🇱", "Hallo! Ik ben GLITCH.AI, je zwevende AI. Wat wil je bespreken?"),
    ARABIC("ar", "ar-SA", "العربية", "Arabisch", "🇸🇦", "مرحباً! أنا GLITCH.AI، ذكاؤك الاصطناعي العائم. كيف يمكنني مساعدتك اليوم؟", isRtl = true),
    ENGLISH("en", "en-US", "English", "Engels", "🇺🇸", "Hello! I am GLITCH.AI, your floating AI assistant. What's on your mind?"),
    JAPANESE("ja", "ja-JP", "日本語", "Japans", "🇯🇵", "こんにちは！浮遊するAI、GLITCH.AIです。何について話しましょうか？"),
    GERMAN("de", "de-DE", "Deutsch", "Duits", "🇩🇪", "Hallo! Ich bin GLITCH.AI, deine schwebende KI. Wie kann ich dir helfen?"),
    FRENCH("fr", "fr-FR", "Français", "Frans", "🇫🇷", "Bonjour ! Je suis GLITCH.AI, votre IA flottante. Que souhaitez-vous savoir ?"),
    SPANISH("es", "es-ES", "Español", "Spaans", "🇪🇸", "¡Hola! Soy GLITCH.AI, tu inteligencia artificial flotante. ¿De qué quieres hablar?"),
    PORTUGUESE("pt", "pt-BR", "Português", "Portugees", "🇧🇷", "Olá! Eu sou o GLITCH.AI, sua IA flutuante. Como posso ajudar você hoje?"),
    ITALIAN("it", "it-IT", "Italiano", "Italiaans", "🇮🇹", "Ciao! Sono GLITCH.AI, la tua intelligenza artificiale fluttuante. Di cosa vuoi parlare?"),
    RUSSIAN("ru", "ru-RU", "Русский", "Russisch", "🇷🇺", "Привет! Я GLITCH.AI, твой парящий ИИ. О чем поговорим?"),
    CHINESE("zh", "zh-CN", "中文 (简体)", "Chinees", "🇨🇳", "你好！我是 GLITCH.AI，你的悬浮人工智能助手。有什么想聊的？"),
    KOREAN("ko", "ko-KR", "한국어", "Koreaans", "🇰🇷", "안녕하세요! 공중부양 AI GLITCH.AI입니다. 어떤 이야기를 나눌까요?"),
    TURKISH("tr", "tr-TR", "Türkçe", "Turks", "🇹🇷", "Merhaba! Ben GLITCH.AI, senin yüzen yapay zekan. Bugün ne hakkında konuşalım?"),
    HINDI("hi", "hi-IN", "हिन्दी", "Hindi", "🇮🇳", "नमस्ते! मैं GLITCH.AI हूँ, आपका तैरता हुआ AI। आज हम किस बारे में बात करें?"),
    POLISH("pl", "pl-PL", "Polski", "Pools", "🇵🇱", "Cześć! Jestem GLITCH.AI, twoją lewitującą sztuczną inteligencją. O czym porozmawiamy?"),
    SWEDISH("sv", "sv-SE", "Svenska", "Zweeds", "🇸🇪", "Hej! Jag är GLITCH.AI, din svävande AI-assistent. Vad vill du prata om?"),
    NORWEGIAN("no", "no-NO", "Norsk", "Noors", "🇳🇴", "Hei! Jeg er GLITCH.AI, din svevende AI. Hva vil du snakke om i dag?"),
    DANISH("da", "da-DK", "Dansk", "Deens", "🇩🇰", "Hej! Jeg er GLITCH.AI, din svævende AI. Hvad vil du tale om?"),
    FINNISH("fi", "fi-FI", "Suomi", "Fins", "🇫🇮", "Hei! Olen GLITCH.AI, leijuva tekoälysi. Mistä haluaisit puhua tänään?"),
    GREEK("el", "el-GR", "Ελληνικά", "Grieks", "🇬🇷", "Γεια σας! Είμαι το GLITCH.AI, το αιωρούμενο AI σας. Τι θα θέλατε να συζητήσουμε;"),
    CZECH("cs", "cs-CZ", "Čeština", "Tsjechisch", "🇨🇿", "Ahoj! Jsem GLITCH.AI, tvůj vznášející se AI asistent. O čem si promluvíme?"),
    HUNGARIAN("hu", "hu-HU", "Magyar", "Hongaars", "🇭🇺", "Szia! GLITCH.AI vagyok, a lebegő mesterséges intelligenciád. Miről beszélgessünk?"),
    ROMANIAN("ro", "ro-RO", "Română", "Roemeens", "🇷🇴", "Salut! Sunt GLITCH.AI, inteligența ta artificială plutitoare. Despre ce vorbim?"),
    THAI("th", "th-TH", "ไทย", "Thais", "🇹🇭", "สวัสดี! ฉันคือ GLITCH.AI ปัญญาประดิษฐ์ลอยตัวของคุณ วันนี้อยากคุยเรื่องอะไร?"),
    VIETNAMESE("vi", "vi-VN", "Tiếng Việt", "Vietnamees", "🇻🇳", "Xin chào! Tôi là GLITCH.AI, trí tuệ nhân tạo bay lơ lửng của bạn."),
    INDONESIAN("id", "id-ID", "Bahasa Indonesia", "Indonesisch", "🇮🇩", "Halo! Saya GLITCH.AI, AI melayang Anda. Apa yang ingin Anda bicarakan?"),
    UKRAINIAN("uk", "uk-UA", "Українська", "Oekraïens", "🇺🇦", "Привіт! Я GLITCH.AI, твій літаючий ШІ. Про що поговоримо?"),
    HEBREW("he", "he-IL", "עברית", "Hebreeuws", "🇮🇱", "שלום! אני GLITCH.AI, הבינה המלאכותית המרחפת שלך. על מה נדבר?", isRtl = true);

    val locale: Locale get() = Locale.forLanguageTag(localeTag)

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DUTCH
        }
    }
}
