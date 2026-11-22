package com.example.data.ai

import android.util.Log
import com.example.core.config.VeyroxConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "CustomBackendAiProvider"

class CustomBackendAiProvider : AiProvider {
    override val providerId: String = "custom_backend"
    override val isDemo: Boolean = false

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun createService(baseUrl: String): BackendApiService {
        val normalized = normalizeBaseUrl(baseUrl)
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendApiService::class.java)
    }

    suspend fun pingHealth(baseUrl: String): Result<BackendHealthResponseDto> {
        return runCatching {
            validateUrlOrThrow(baseUrl)
            withTimeout(15_000L) {
                createService(baseUrl).checkHealth()
            }
        }
    }

    override fun streamChat(request: AiRequest): Flow<AiStreamEvent> = flow {
        val baseUrl = request.customBackendUrl.trim()
        try {
            validateUrlOrThrow(baseUrl)
            val service = createService(baseUrl)
            val dto = BackendChatRequestDto(
                message = request.prompt,
                mode = request.mode.name,
                history = request.history.takeLast(20).map {
                    BackendHistoryMessageDto(
                        role = if (it.isUser) "user" else "assistant",
                        content = it.text
                    )
                }
            )
            val response = withTimeout(VeyroxConfig.AI_TIMEOUT_MS) {
                service.sendChat(dto)
            }
            if (response.reply.isNotBlank()) {
                emit(AiStreamEvent.Chunk(response.reply))
                emit(AiStreamEvent.Completed(fullText = response.reply, isDemo = response.demo))
            } else {
                emit(
                    AiStreamEvent.Error(
                        AiProviderError.ServiceUnavailable("Backend returned an empty response.")
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Backend request timed out", e)
            emit(AiStreamEvent.Error(AiProviderError.Timeout))
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalArgumentException) {
            emit(
                AiStreamEvent.Error(
                    AiProviderError.InvalidConfiguration(
                        e.message ?: "Configure a valid HTTPS backend URL in Settings."
                    )
                )
            )
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error from backend: ${e.code()}", e)
            emit(
                AiStreamEvent.Error(
                    AiProviderError.ServiceUnavailable(
                        "Backend returned HTTP ${e.code()}. Check server logs or switch to Live Gemini / Demo Mode."
                    )
                )
            )
        } catch (e: IOException) {
            Log.e(TAG, "Network IO error reaching backend", e)
            emit(
                AiStreamEvent.Error(
                    AiProviderError.ServiceUnavailable(
                        "Cannot reach VEYROX backend at $baseUrl. Verify the server is running or use Live Gemini / Demo Mode."
                    )
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected backend failure", e)
            emit(
                AiStreamEvent.Error(
                    AiProviderError.ServiceUnavailable(
                        e.localizedMessage ?: "Backend request failed."
                    )
                )
            )
        }
    }

    private fun validateUrlOrThrow(rawUrl: String) {
        require(rawUrl.isNotBlank()) {
            "No backend URL configured. Enter your VEYROX backend URL in Settings or select Live Gemini."
        }
        val lower = rawUrl.lowercase()
        val isHttps = lower.startsWith("https://")
        val isLocalDebug = lower.startsWith("http://10.0.2.2") ||
            lower.startsWith("http://localhost") ||
            lower.startsWith("http://127.0.0.1")
        require(isHttps || isLocalDebug) {
            "Backend URL must use HTTPS in production (or localhost / 10.0.2.2 for local development)."
        }
    }

    private fun normalizeBaseUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
