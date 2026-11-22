package com.example.data.ai

import android.util.Log
import com.example.core.config.VeyroxConfig
import com.example.domain.model.AiMode
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "FirebaseAiLogicProvider"

class FirebaseAiLogicProvider : AiProvider {
    override val providerId: String = "firebase_ai_logic"
    override val isDemo: Boolean = false

    private val modelCache = ConcurrentHashMap<AiMode, GenerativeModel>()

    private fun getOrCreateModel(mode: AiMode): GenerativeModel {
        return modelCache.getOrPut(mode) {
            Firebase.ai.generativeModel(
                modelName = VeyroxConfig.DEFAULT_GEMINI_MODEL,
                systemInstruction = content {
                    text(mode.systemInstruction)
                }
            )
        }
    }

    override fun streamChat(request: AiRequest): Flow<AiStreamEvent> = flow {
        val model = getOrCreateModel(request.mode)
        val historyContents = request.history
            .filter { it.text.isNotBlank() }
            .takeLast(20)
            .map { turn ->
                content(role = if (turn.isUser) "user" else "model") {
                    text(turn.text)
                }
            }

        val chat = model.startChat(history = historyContents)
        val accumulated = StringBuilder()

        try {
            withTimeout(VeyroxConfig.AI_TIMEOUT_MS) {
                if (request.streaming) {
                    chat.sendMessageStream(request.prompt).collect { chunk ->
                        val delta = chunk.text.orEmpty()
                        if (delta.isNotEmpty()) {
                            accumulated.append(delta)
                            emit(AiStreamEvent.Chunk(delta))
                        }
                    }
                } else {
                    val response = chat.sendMessage(request.prompt)
                    val text = response.text.orEmpty()
                    if (text.isNotEmpty()) {
                        accumulated.append(text)
                        emit(AiStreamEvent.Chunk(text))
                    }
                }
            }

            if (accumulated.isNotBlank()) {
                emit(AiStreamEvent.Completed(fullText = accumulated.toString(), isDemo = false))
            } else {
                emit(
                    AiStreamEvent.Error(
                        AiProviderError.ServiceUnavailable(
                            "Gemini returned an empty response. Please try rephrasing your prompt."
                        )
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Firebase AI generation timed out", e)
            emit(AiStreamEvent.Error(AiProviderError.Timeout))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            val msg = e.localizedMessage?.takeIf { it.isNotBlank() }
                ?: "Unable to reach Gemini via Firebase AI Logic. Check connectivity or switch to Demo Mode."
            emit(AiStreamEvent.Error(AiProviderError.ServiceUnavailable(msg)))
        }
    }
}
