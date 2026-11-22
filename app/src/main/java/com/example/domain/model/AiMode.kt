package com.example.domain.model

enum class AiMode(
    val displayName: String,
    val shortLabel: String,
    val tagline: String,
    val description: String,
    val systemInstruction: String,
    val starterPrompts: List<String>
) {
    GENERAL(
        displayName = "General AI",
        shortLabel = "General",
        tagline = "Everyday intelligence & clarity",
        description = "For normal questions, clear explanations, brainstorming ideas, and natural conversations.",
        systemInstruction = """
            You are VEYROX General AI — a crisp, intelligent, reliable assistant.
            Give clear, well-structured, accurate answers using clean Markdown formatting when helpful.
            Be direct, insightful, and concise without filler.
        """.trimIndent(),
        starterPrompts = listOf(
            "Explain quantum computing in simple terms",
            "Give me 5 creative productivity habits for deep focus",
            "Compare electric vehicles vs hybrids for city commuting"
        )
    ),
    WRITING(
        displayName = "Writing AI",
        shortLabel = "Writing",
        tagline = "Refine tone, copy & prose",
        description = "For drafting, rewriting, professional emails, text polishing, creative writing, and clear copy.",
        systemInstruction = """
            You are VEYROX Writing AI — an expert editor, copywriter, and communications specialist.
            Help users write, rewrite, polish emails, elevate tone, craft creative pieces, and improve clarity.
            When rewriting text, preserve the user's intent while enhancing flow, rhythm, and impact.
        """.trimIndent(),
        starterPrompts = listOf(
            "Write a concise, confident follow-up email after an interview",
            "Rewrite this announcement to sound futuristic and premium",
            "Outline a compelling 60-second product launch pitch"
        )
    ),
    CODING(
        displayName = "Coding AI",
        shortLabel = "Coding",
        tagline = "Build, debug & architect",
        description = "For programming, clean code generation, debugging, explaining code, and technical architecture.",
        systemInstruction = """
            You are VEYROX Coding AI — a senior staff software engineer.
            Provide clean, production-grade code with fenced Markdown code blocks specifying the language.
            Explain architectural trade-offs, edge cases, and debugging steps clearly and concisely.
        """.trimIndent(),
        starterPrompts = listOf(
            "Write a Kotlin Flow debounce helper for search input",
            "Explain the difference between StateFlow and SharedFlow",
            "Debug why a REST API returns 401 Unauthorized"
        )
    ),
    SUMMARIZER(
        displayName = "Summarizer",
        shortLabel = "Summarize",
        tagline = "Distill long text into signal",
        description = "For summarizing long texts, extracting key bullet points, action items, and simplifying complexity.",
        systemInstruction = """
            You are VEYROX Summarizer — an executive synthesis engine.
            Distill long passages into:
            1. A 1-2 sentence TL;DR
            2. Key takeaways as crisp bullet points
            3. Action items or core insights when applicable.
            Preserve factual accuracy and omit fluff.
        """.trimIndent(),
        starterPrompts = listOf(
            "Summarize key principles of clean software architecture",
            "Turn this meeting transcript into bulleted action items",
            "Simplify how HTTPS and TLS certificates work"
        )
    );

    companion object {
        fun fromName(value: String?): AiMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: GENERAL
        }
    }
}
