package com.example.data.ai

import com.example.domain.model.AiMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Offline Demo Mode provider.
 * Clearly marks every response as simulated demo output and never pretends to be a live AI model.
 */
class DemoAiProvider : AiProvider {
    override val providerId: String = "demo_mode"
    override val isDemo: Boolean = true

    override fun streamChat(request: AiRequest): Flow<AiStreamEvent> = flow {
        val responseText = buildDemoResponse(request.prompt.trim(), request.mode)

        if (request.streaming) {
            val tokens = responseText.split(Regex("(?<=\\s)"))
            val sb = StringBuilder()
            for (token in tokens) {
                sb.append(token)
                emit(AiStreamEvent.Chunk(token))
                delay(18L)
            }
        } else {
            delay(350L)
            emit(AiStreamEvent.Chunk(responseText))
        }

        emit(AiStreamEvent.Completed(fullText = responseText, isDemo = true))
    }

    private fun buildDemoResponse(prompt: String, mode: AiMode): String {
        val preview = if (prompt.length > 90) prompt.take(90) + "..." else prompt
        return when (mode) {
            AiMode.GENERAL -> """
                > **Demo Mode Active** — This is a simulated offline response for previewing VEYROX without a live AI connection.

                ### VEYROX General AI Overview
                You asked about: *"$preview"*

                - **Direct Breakdown**: In live mode powered by **Gemini 3.8 Flash**, VEYROX analyzes your prompt context and multi-turn conversation history to provide a tailored answer.
                - **Structured Clarity**: Responses support headings, bullet lists, inline `code`, and full fenced code blocks.
                - **Next Step**: Switch to **Live Gemini (Firebase AI Logic)** in Settings whenever you are online.
            """.trimIndent()

            AiMode.WRITING -> """
                > **Demo Mode Active** — Simulated offline Writing AI preview.

                ### Polished Draft
                Here is a structured, clear revision inspired by *"$preview"*:

                > *"Precision turns ideas into momentum. With VEYROX, every draft becomes sharper, clearer, and ready to share—without losing your authentic voice."*

                **Editorial Notes:**
                - Tightened opening phrasing for immediate clarity.
                - Maintained a confident, modern cadence.
                - Enable **Live Gemini** in Settings to generate bespoke rewrites for your exact text.
            """.trimIndent()

            AiMode.CODING -> """
                > **Demo Mode Active** — Simulated offline Coding AI preview.

                ### Reference Implementation
                For your request (*"$preview"*), here is a clean Kotlin coroutine pattern demonstrating structured VEYROX engineering:

                ```kotlin
                suspend fun fetchInsight(query: String): Result<String> {
                    require(query.isNotBlank()) { "Query must not be empty" }
                    return runCatching {
                        "Processed: ${'$'}query"
                    }
                }
                ```

                - **Clean Separation**: Keeps validation explicit before I/O.
                - **Switch to Live Gemini** in Settings for real debugging, refactoring, and code generation.
            """.trimIndent()

            AiMode.SUMMARIZER -> """
                > **Demo Mode Active** — Simulated offline Summarizer preview.

                ### Executive Summary
                - **TL;DR**: Your input (*"$preview"*) was processed by VEYROX Summarizer in offline preview mode.
                - **Key Point 1**: Core concepts are condensed into high-signal bullet points.
                - **Key Point 2**: Actionable items are separated from background detail.
                - **Key Point 3**: Switch to **Live Gemini** in Settings to summarize real articles, notes, or transcripts.
            """.trimIndent()
        }
    }
}
