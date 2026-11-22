package com.example.domain.model

enum class ThemePreference(val label: String) {
    DARK("Dark (VEYROX Signature)"),
    LIGHT("Light"),
    SYSTEM("System Default");

    companion object {
        fun fromName(value: String?): ThemePreference {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DARK
        }
    }
}

enum class AiEngineType(val label: String, val badge: String) {
    FIREBASE_AI_LOGIC("Live Gemini (Firebase AI Logic)", "GEMINI AI"),
    DEMO_MODE("Offline Demo Mode", "DEMO MODE"),
    CUSTOM_BACKEND("Self-Hosted VEYROX Backend", "CUSTOM API");

    companion object {
        fun fromName(value: String?): AiEngineType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: FIREBASE_AI_LOGIC
        }
    }
}

data class UserPreferences(
    val themePreference: ThemePreference = ThemePreference.DARK,
    val aiEngineType: AiEngineType = AiEngineType.FIREBASE_AI_LOGIC,
    val streamingEnabled: Boolean = true,
    val defaultAiMode: AiMode = AiMode.GENERAL,
    val customBackendUrl: String = ""
)
