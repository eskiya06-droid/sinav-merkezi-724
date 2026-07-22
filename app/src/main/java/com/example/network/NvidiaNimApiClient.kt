package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------
// NVIDIA NIM API Data Models (OpenAI Compatible)
// ---------------------------------------------------------

data class NvidiaChatMessage(
    val role: String,
    val content: String
)

data class ResponseFormat(
    val type: String
)

data class ChatCompletionRequest(
    val model: String,
    val messages: List<NvidiaChatMessage>,
    val temperature: Float = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int = 1024,
    val top_p: Float = 1.0f,
    val stream: Boolean = false,
    val response_format: ResponseFormat? = null
)

data class Choice(
    val index: Int,
    val message: NvidiaChatMessage,
    val finish_reason: String?
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage?
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// ---------------------------------------------------------
// Retrofit Service Interface
// ---------------------------------------------------------

interface NvidiaApiService {
    @POST("chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

// ---------------------------------------------------------
// API Client
// ---------------------------------------------------------

object NvidiaNimApiClient {
    private const val BASE_URL = "https://integrate.api.nvidia.com/v1/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: NvidiaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NvidiaApiService::class.java)
    }
}

// --- Custom Parsed JSON Classes for our Exam App ---

@JsonClass(generateAdapter = true)
data class GeneratedQuestionList(
    @Json(name = "questions") val questions: List<GeneratedQuestion>
)

@JsonClass(generateAdapter = true)
data class GeneratedQuestion(
    @Json(name = "lesson") val lesson: String,
    @Json(name = "topic") val topic: String,
    @Json(name = "questionText") val questionText: String,
    @Json(name = "optionA") val optionA: String,
    @Json(name = "optionB") val optionB: String,
    @Json(name = "optionC") val optionC: String,
    @Json(name = "optionD") val optionD: String,
    @Json(name = "correctAnswer") val correctAnswer: String, // "A", "B", "C", "D"
    @Json(name = "explanation") val explanation: String,      // Step-by-step logic
    @Json(name = "drawingCommands") val drawingCommands: String? = null // Coordinates/instructions for visual drawing if applicable
)
