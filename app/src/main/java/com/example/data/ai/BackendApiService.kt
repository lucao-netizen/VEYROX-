package com.example.data.ai

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class BackendHistoryMessageDto(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class BackendChatRequestDto(
    val message: String,
    val mode: String,
    val history: List<BackendHistoryMessageDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BackendChatResponseDto(
    val reply: String,
    val mode: String? = null,
    val demo: Boolean = false,
    val model: String? = null
)

@JsonClass(generateAdapter = true)
data class BackendHealthResponseDto(
    val status: String,
    val service: String? = null,
    val demoMode: Boolean = false
)

interface BackendApiService {
    @GET("api/health")
    suspend fun checkHealth(): BackendHealthResponseDto

    @POST("api/chat")
    suspend fun sendChat(@Body request: BackendChatRequestDto): BackendChatResponseDto
}
